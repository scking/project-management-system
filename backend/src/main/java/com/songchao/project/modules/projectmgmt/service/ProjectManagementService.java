package com.songchao.project.modules.projectmgmt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.songchao.project.common.exception.BizException;
import com.songchao.project.modules.projectmgmt.dto.LeaveApproveRequest;
import com.songchao.project.modules.projectmgmt.dto.LeaveSaveRequest;
import com.songchao.project.modules.projectmgmt.dto.TaskSaveRequest;
import com.songchao.project.modules.projectmgmt.dto.WeeklyReportSaveRequest;
import com.songchao.project.modules.projectmgmt.entity.PmLeave;
import com.songchao.project.modules.projectmgmt.entity.PmProject;
import com.songchao.project.modules.projectmgmt.entity.PmProjectMember;
import com.songchao.project.modules.projectmgmt.entity.PmTask;
import com.songchao.project.modules.projectmgmt.entity.PmWeeklyReport;
import com.songchao.project.modules.projectmgmt.mapper.PmLeaveMapper;
import com.songchao.project.modules.projectmgmt.mapper.PmProjectMapper;
import com.songchao.project.modules.projectmgmt.mapper.PmProjectMemberMapper;
import com.songchao.project.modules.projectmgmt.mapper.PmTaskMapper;
import com.songchao.project.modules.projectmgmt.mapper.PmWeeklyReportMapper;
import com.songchao.project.security.auth.AuthContext;
import com.songchao.project.security.auth.CurrentUser;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ProjectManagementService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PmProjectMapper projectMapper;
    private final PmProjectMemberMapper projectMemberMapper;
    private final PmTaskMapper taskMapper;
    private final PmWeeklyReportMapper weeklyReportMapper;
    private final PmLeaveMapper leaveMapper;

    public ProjectManagementService(
            PmProjectMapper projectMapper,
            PmProjectMemberMapper projectMemberMapper,
            PmTaskMapper taskMapper,
            PmWeeklyReportMapper weeklyReportMapper,
            PmLeaveMapper leaveMapper
    ) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.taskMapper = taskMapper;
        this.weeklyReportMapper = weeklyReportMapper;
        this.leaveMapper = leaveMapper;
    }

    @PostConstruct
    public void initSeedData() {
        ensureSeedProjects();
        ensureSeedMembers();
        ensureSeedTasks();
        ensureSeedReports();
        ensureSeedLeaves();
    }

    public Map<String, Object> dashboard() {
        List<PmProject> projects = allProjects();
        List<PmProjectMember> members = allMembers();
        List<PmTask> tasks = allTasks();
        List<PmWeeklyReport> reports = allReports();
        List<PmLeave> leaves = allLeaves();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reportSubmittedCount", reports.size());
        result.put("reportPendingCount", Math.max(members.size() - reports.size(), 0));
        result.put("taskCompletedCount", tasks.stream().filter(item -> "已完成".equals(item.getTaskStatus())).count());
        result.put("leavePendingCount", leaves.stream().filter(item -> safeText(item.getApprovalStatus()).contains("待")).count());
        result.put("todoList", tasks.stream()
                .filter(item -> !"已完成".equals(item.getTaskStatus()) && !"已关闭".equals(item.getTaskStatus()))
                .sorted(Comparator.comparing(item -> defaultDate(item.getRequiredFinishDate())))
                .limit(6)
                .map(item -> {
                    Map<String, Object> todo = new LinkedHashMap<>();
                    todo.put("type", "任务");
                    todo.put("title", item.getTaskTitle());
                    todo.put("owner", item.getAssigneeName());
                    todo.put("dueDate", formatDate(item.getRequiredFinishDate()));
                    todo.put("status", item.getTaskStatus());
                    return todo;
                })
                .collect(Collectors.toList()));
        result.put("projectFocusList", projects.stream().map(item -> {
            Map<String, Object> focus = mapProject(item);
            long totalMember = members.stream().filter(member -> Objects.equals(item.getId(), member.getProjectId())).count();
            long submitted = reports.stream().filter(report -> Objects.equals(item.getId(), report.getProjectId())).count();
            focus.put("reportSubmitRate", totalMember == 0 ? 0 : (int) Math.round(submitted * 100.0 / totalMember));
            return focus;
        }).collect(Collectors.toList()));
        result.put("pendingLeaves", leaves.stream()
                .filter(item -> safeText(item.getApprovalStatus()).contains("待"))
                .limit(5)
                .map(this::mapLeave)
                .collect(Collectors.toList()));
        result.put("unfinishedReasonStats", unfinishedReasonStats(reports));
        return result;
    }

    public List<Map<String, Object>> listProjects(String keyword) {
        LambdaQueryWrapper<PmProject> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(PmProject::getProjectCode, keyword)
                    .or().like(PmProject::getProjectName, keyword)
                    .or().like(PmProject::getLocation, keyword)
                    .or().like(PmProject::getProjectManagerName, keyword));
        }
        wrapper.orderByDesc(PmProject::getId);
        return projectMapper.selectList(wrapper).stream().map(this::mapProject).collect(Collectors.toList());
    }

    public List<Map<String, Object>> listProjectMembers() {
        List<PmProject> projects = allProjects();
        return allMembers().stream()
                .sorted(Comparator.comparing(PmProjectMember::getId).reversed())
                .map(item -> mapMember(item, projects))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listTasks(String keyword, String status) {
        LambdaQueryWrapper<PmTask> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(PmTask::getTaskTitle, keyword)
                    .or().like(PmTask::getProjectName, keyword)
                    .or().like(PmTask::getAssigneeName, keyword)
                    .or().like(PmTask::getTaskCode, keyword));
        }
        if (hasText(status)) {
            wrapper.eq(PmTask::getTaskStatus, status);
        }
        wrapper.orderByAsc(PmTask::getRequiredFinishDate).orderByDesc(PmTask::getId);
        return taskMapper.selectList(wrapper).stream().map(this::mapTask).collect(Collectors.toList());
    }

    public Map<String, Object> createTask(TaskSaveRequest request) {
        PmProject project = resolveProject(request.getProjectId(), request.getProjectName());
        PmProjectMember assignee = resolveMember(request.getAssigneeId(), request.getAssigneeName(), project.getId());

        PmTask task = new PmTask();
        task.setTaskCode(generateCode("TASK"));
        task.setProjectId(project.getId());
        task.setProjectName(project.getProjectName());
        task.setProjectDeptName(firstNonBlank(
                request.getProjectDeptName(),
                assignee == null ? null : assignee.getProjectDeptName()
        ));
        task.setTaskTitle(requiredText(request.getTaskTitle(), "任务标题不能为空"));
        task.setTaskContent(request.getTaskContent());
        task.setAssignerName(currentUserName());
        task.setAssigneeName(resolveAssigneeName(request, assignee));
        task.setPriority(defaultText(request.getPriority(), "中"));
        task.setTaskStatus("待接收");
        task.setRequiredFinishDate(parseDate(request.getRequiredFinishDate()));
        task.setRemark(request.getRemark());
        task.setCreatedAt(LocalDateTime.now());
        taskMapper.insert(task);
        return mapTask(task);
    }

    public Map<String, Object> updateTaskStatus(Long id, String status) {
        PmTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException("任务不存在");
        }
        task.setTaskStatus(status);
        taskMapper.updateById(task);
        return mapTask(task);
    }

    public List<Map<String, Object>> listWeeklyReports(String keyword) {
        LambdaQueryWrapper<PmWeeklyReport> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(PmWeeklyReport::getReportCode, keyword)
                    .or().like(PmWeeklyReport::getReportUserName, keyword)
                    .or().like(PmWeeklyReport::getProjectName, keyword)
                    .or().like(PmWeeklyReport::getWeekLabel, keyword));
        }
        wrapper.orderByDesc(PmWeeklyReport::getReportDate).orderByDesc(PmWeeklyReport::getId);
        return weeklyReportMapper.selectList(wrapper).stream().map(this::mapWeeklyReport).collect(Collectors.toList());
    }

    public Map<String, Object> createWeeklyReport(WeeklyReportSaveRequest request) {
        PmProject project = resolveProject(request.getProjectId(), request.getProjectName());

        PmWeeklyReport report = new PmWeeklyReport();
        report.setReportCode(generateCode("WR"));
        report.setProjectId(project.getId());
        report.setProjectName(project.getProjectName());
        report.setProjectDeptName(request.getProjectDeptName());
        report.setReportUserName(requiredText(request.getReportUserName(), "填报人不能为空"));
        report.setWeekLabel(requiredText(request.getWeekLabel(), "周次不能为空"));
        report.setReportDate(LocalDate.now());
        report.setCompletedWorkText(request.getCompletedWorkText());
        report.setUnfinishedWorkText(request.getUnfinishedWorkText());
        report.setUnfinishedReasonText(request.getUnfinishedReasonText());
        report.setNextWeekPlanText(request.getNextWeekPlanText());
        report.setSupportNeeds(request.getSupportNeeds());
        report.setRemark(request.getRemark());
        weeklyReportMapper.insert(report);
        return mapWeeklyReport(report);
    }

    public List<Map<String, Object>> listLeaves(String keyword, String status) {
        LambdaQueryWrapper<PmLeave> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(PmLeave::getLeaveCode, keyword)
                    .or().like(PmLeave::getApplicantName, keyword)
                    .or().like(PmLeave::getProjectName, keyword));
        }
        if (hasText(status)) {
            wrapper.eq(PmLeave::getApprovalStatus, status);
        }
        wrapper.orderByDesc(PmLeave::getSubmittedAt).orderByDesc(PmLeave::getId);
        return leaveMapper.selectList(wrapper).stream().map(this::mapLeave).collect(Collectors.toList());
    }

    public Map<String, Object> createLeave(LeaveSaveRequest request) {
        PmProject project = resolveProject(request.getProjectId(), request.getProjectName());

        PmLeave leave = new PmLeave();
        leave.setLeaveCode(generateCode("LEAVE"));
        leave.setApplicantName(requiredText(request.getApplicantName(), "申请人不能为空"));
        leave.setProjectId(project.getId());
        leave.setProjectName(project.getProjectName());
        leave.setProjectDeptName(request.getProjectDeptName());
        leave.setLeaveType(requiredText(request.getLeaveType(), "请假类型不能为空"));
        leave.setStartTime(parseDateTime(request.getStartTime(), "开始时间格式不正确"));
        leave.setEndTime(parseDateTime(request.getEndTime(), "结束时间格式不正确"));
        leave.setLeaveDays(BigDecimal.valueOf(request.getLeaveDays()));
        leave.setReason(request.getReason());
        leave.setApprovalStatus("待项目经理审批");
        leave.setSubmittedAt(LocalDateTime.now());
        leaveMapper.insert(leave);
        return mapLeave(leave);
    }

    public Map<String, Object> approveLeave(Long id, LeaveApproveRequest request) {
        PmLeave leave = leaveMapper.selectById(id);
        if (leave == null) {
            throw new BizException("请假记录不存在");
        }
        String currentStatus = safeText(leave.getApprovalStatus());
        if ("同意".equals(request.getAction())) {
            if ("待项目经理审批".equals(currentStatus)) {
                leave.setApprovalStatus("待部门经理审批");
            } else {
                leave.setApprovalStatus("已通过");
            }
        } else if ("退回".equals(request.getAction())) {
            leave.setApprovalStatus("已退回");
        } else {
            leave.setApprovalStatus("已驳回");
        }
        leave.setApprovalComment(request.getComment());
        leave.setApprovedBy(currentUserName());
        leave.setApprovedAt(LocalDateTime.now());
        leaveMapper.updateById(leave);
        return mapLeave(leave);
    }

    public List<Map<String, Object>> listNotifications() {
        List<Map<String, Object>> notices = new ArrayList<>();

        allTasks().stream()
                .filter(item -> !"已完成".equals(item.getTaskStatus()) && !"已关闭".equals(item.getTaskStatus()))
                .sorted(Comparator.comparing(item -> defaultDate(item.getRequiredFinishDate())))
                .limit(6)
                .forEach(item -> notices.add(notice(
                        "任务待处理提醒",
                        safeText(item.getAssigneeName()) + " 有新的任务需要推进：" + safeText(item.getTaskTitle()),
                        isUrgent(item.getRequiredFinishDate()) ? "高" : "中",
                        item.getCreatedAt() == null ? LocalDateTime.now() : item.getCreatedAt()
                )));

        allLeaves().stream()
                .filter(item -> safeText(item.getApprovalStatus()).contains("待"))
                .limit(4)
                .forEach(item -> notices.add(notice(
                        "请假待审批提醒",
                        safeText(item.getApplicantName()) + " 提交了请假申请，请及时处理。",
                        "中",
                        item.getSubmittedAt() == null ? LocalDateTime.now() : item.getSubmittedAt()
                )));

        notices.sort(Comparator.comparing(item -> String.valueOf(item.get("createdAt")), Comparator.reverseOrder()));
        return notices;
    }

    public Map<String, Object> statsOverview() {
        List<PmProject> projects = allProjects();
        List<PmProjectMember> members = allMembers();
        List<PmTask> tasks = allTasks();
        List<PmWeeklyReport> reports = allReports();
        List<PmLeave> leaves = allLeaves();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reportSubmittedCount", reports.size());
        result.put("reportPendingCount", Math.max(members.size() - reports.size(), 0));
        result.put("taskCreatedCount", tasks.size());
        result.put("leavePendingCount", leaves.stream().filter(item -> safeText(item.getApprovalStatus()).contains("待")).count());
        result.put("projectReportRates", projects.stream().map(item -> {
            long totalMember = members.stream().filter(member -> Objects.equals(item.getId(), member.getProjectId())).count();
            long submitted = reports.stream().filter(report -> Objects.equals(item.getId(), report.getProjectId())).count();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("projectName", item.getProjectName());
            map.put("submitted", submitted);
            map.put("pending", Math.max(totalMember - submitted, 0));
            map.put("rate", totalMember == 0 ? 0 : (int) Math.round(submitted * 100.0 / totalMember));
            return map;
        }).collect(Collectors.toList()));
        result.put("leaveTypeStats", leaves.stream().collect(Collectors.groupingBy(
                item -> safeText(item.getLeaveType()),
                Collectors.counting()
        )).entrySet().stream().map(item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", item.getKey());
            map.put("count", item.getValue());
            return map;
        }).collect(Collectors.toList()));
        result.put("unfinishedReasonStats", unfinishedReasonStats(reports));
        return result;
    }

    private List<Map<String, Object>> unfinishedReasonStats(List<PmWeeklyReport> reports) {
        Map<String, List<PmWeeklyReport>> grouped = new LinkedHashMap<>();
        for (PmWeeklyReport report : reports) {
            for (String reason : splitLines(report.getUnfinishedReasonText())) {
                grouped.computeIfAbsent(reason, key -> new ArrayList<>()).add(report);
            }
        }
        return grouped.entrySet().stream().map(item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("reason", item.getKey());
            map.put("count", item.getValue().size());
            map.put("projectCount", item.getValue().stream().map(PmWeeklyReport::getProjectName).distinct().count());
            return map;
        }).collect(Collectors.toList());
    }

    private void ensureSeedProjects() {
        Long count = projectMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        PmProject project1 = new PmProject();
        project1.setProjectCode("XM-2026-001");
        project1.setProjectName("G3018 精河至阿拉山口机电施工项目");
        project1.setProjectType("高速机电");
        project1.setLocation("博州阿拉山口");
        project1.setOwnerOrg("新疆交投建设管理中心");
        project1.setContractAmount(new BigDecimal("12800000.00"));
        project1.setStartDate(LocalDate.parse("2026-01-10"));
        project1.setPlannedFinishDate(LocalDate.parse("2026-12-31"));
        project1.setProjectStatus("在建");
        project1.setProjectManagerName("刘建国");
        project1.setProjectDesc("首版种子项目");
        projectMapper.insert(project1);

        PmProject project2 = new PmProject();
        project2.setProjectCode("XM-2026-002");
        project2.setProjectName("乌尉高速乌拉泊互通监控通信项目");
        project2.setProjectType("监控通信");
        project2.setLocation("乌鲁木齐");
        project2.setOwnerOrg("乌尉高速项目公司");
        project2.setContractAmount(new BigDecimal("8600000.00"));
        project2.setStartDate(LocalDate.parse("2026-02-01"));
        project2.setPlannedFinishDate(LocalDate.parse("2026-11-30"));
        project2.setProjectStatus("在建");
        project2.setProjectManagerName("王志强");
        project2.setProjectDesc("首版种子项目");
        projectMapper.insert(project2);
    }

    private void ensureSeedMembers() {
        Long count = projectMemberMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        PmProject project1 = getProjectByCode("XM-2026-001");
        PmProject project2 = getProjectByCode("XM-2026-002");
        if (project1 == null || project2 == null) {
            return;
        }
        projectMemberMapper.insert(seedMember(project1.getId(), "精河项目部", "张凯", "项目经理", "2026-01-10", null, 1));
        projectMemberMapper.insert(seedMember(project1.getId(), "精河项目部", "李雪", "资料员", "2026-02-01", null, 1));
        projectMemberMapper.insert(seedMember(project2.getId(), "乌拉泊项目部", "陈涛", "施工员", "2026-01-18", null, 1));
    }

    private void ensureSeedTasks() {
        Long count = taskMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        PmProject project1 = getProjectByCode("XM-2026-001");
        PmProject project2 = getProjectByCode("XM-2026-002");
        if (project1 == null || project2 == null) {
            return;
        }
        taskMapper.insert(seedTask("TASK-20260414-001", project1.getId(), project1.getProjectName(), "精河项目部", "收费站光纤测试", "王志强", "李雪", "高", "2026-04-18", "进行中"));
        taskMapper.insert(seedTask("TASK-20260414-002", project2.getId(), project2.getProjectName(), "乌拉泊项目部", "监控立杆基础复测", "王志强", "陈涛", "中", "2026-04-20", "待接收"));
    }

    private void ensureSeedReports() {
        Long count = weeklyReportMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        PmProject project1 = getProjectByCode("XM-2026-001");
        if (project1 == null) {
            return;
        }
        PmWeeklyReport report = new PmWeeklyReport();
        report.setReportCode("WR-20260414-001");
        report.setProjectId(project1.getId());
        report.setProjectName(project1.getProjectName());
        report.setProjectDeptName("精河项目部");
        report.setReportUserName("李雪");
        report.setWeekLabel("2026年第16周");
        report.setReportDate(LocalDate.parse("2026-04-14"));
        report.setCompletedWorkText("完成收费站监控点位核对\n完成机房设备到货清点");
        report.setUnfinishedWorkText("通信管道整改未完成");
        report.setUnfinishedReasonText("材料未到场\n外部协调未完成");
        report.setNextWeekPlanText("推进通信管道整改\n完成监控设备安装");
        report.setSupportNeeds("协调土建单位提供作业面");
        weeklyReportMapper.insert(report);
    }

    private void ensureSeedLeaves() {
        Long count = leaveMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        PmProject project2 = getProjectByCode("XM-2026-002");
        if (project2 == null) {
            return;
        }
        PmLeave leave = new PmLeave();
        leave.setLeaveCode("LEAVE-20260415-001");
        leave.setApplicantName("陈涛");
        leave.setProjectId(project2.getId());
        leave.setProjectName(project2.getProjectName());
        leave.setProjectDeptName("乌拉泊项目部");
        leave.setLeaveType("事假");
        leave.setStartTime(parseDateTime("2026-04-16 09:00:00", "开始时间格式不正确"));
        leave.setEndTime(parseDateTime("2026-04-17 18:00:00", "结束时间格式不正确"));
        leave.setLeaveDays(BigDecimal.valueOf(2.0));
        leave.setReason("家中有事");
        leave.setApprovalStatus("待项目经理审批");
        leave.setSubmittedAt(LocalDateTime.parse("2026-04-15 09:30:00", DATE_TIME_FORMATTER));
        leaveMapper.insert(leave);
    }

    private PmProjectMember seedMember(Long projectId, String deptName, String employeeName, String positionName, String arrivalDate, String leaveDate, Integer onDuty) {
        PmProjectMember member = new PmProjectMember();
        member.setProjectId(projectId);
        member.setProjectDeptName(deptName);
        member.setEmployeeName(employeeName);
        member.setPositionName(positionName);
        member.setArrivalDate(arrivalDate == null ? null : LocalDate.parse(arrivalDate));
        member.setLeaveDate(leaveDate == null ? null : LocalDate.parse(leaveDate));
        member.setOnDuty(onDuty);
        return member;
    }

    private PmTask seedTask(String taskCode, Long projectId, String projectName, String projectDeptName, String taskTitle, String assignerName, String assigneeName, String priority, String requiredFinishDate, String taskStatus) {
        PmTask task = new PmTask();
        task.setTaskCode(taskCode);
        task.setProjectId(projectId);
        task.setProjectName(projectName);
        task.setProjectDeptName(projectDeptName);
        task.setTaskTitle(taskTitle);
        task.setTaskContent(taskTitle + "执行与反馈");
        task.setAssignerName(assignerName);
        task.setAssigneeName(assigneeName);
        task.setPriority(priority);
        task.setRequiredFinishDate(LocalDate.parse(requiredFinishDate));
        task.setTaskStatus(taskStatus);
        task.setCreatedAt(LocalDateTime.now().minusDays(1));
        return task;
    }

    private PmProject resolveProject(Long projectId, String projectName) {
        if (projectId != null) {
            PmProject project = projectMapper.selectById(projectId);
            if (project == null) {
                throw new BizException("项目不存在");
            }
            return project;
        }
        if (!hasText(projectName)) {
            throw new BizException("projectId 或 projectName 不能为空");
        }
        LambdaQueryWrapper<PmProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmProject::getProjectName, projectName).last("limit 1");
        PmProject project = projectMapper.selectOne(wrapper);
        if (project == null) {
            throw new BizException("项目不存在");
        }
        return project;
    }

    private PmProjectMember resolveMember(Long memberId, String memberName, Long projectId) {
        if (memberId != null) {
            PmProjectMember member = projectMemberMapper.selectById(memberId);
            if (member == null) {
                throw new BizException("指派对象不存在");
            }
            if (projectId != null && member.getProjectId() != null && !Objects.equals(projectId, member.getProjectId())) {
                throw new BizException("指派对象不属于当前项目");
            }
            return member;
        }
        if (!hasText(memberName)) {
            return null;
        }
        LambdaQueryWrapper<PmProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmProjectMember::getEmployeeName, memberName);
        if (projectId != null) {
            wrapper.eq(PmProjectMember::getProjectId, projectId);
        }
        wrapper.last("limit 1");
        return projectMemberMapper.selectOne(wrapper);
    }

    private String resolveAssigneeName(TaskSaveRequest request, PmProjectMember assignee) {
        String assigneeName = assignee == null ? request.getAssigneeName() : assignee.getEmployeeName();
        if (!hasText(assigneeName)) {
            throw new BizException("assigneeId 或 assigneeName 不能为空");
        }
        return assigneeName;
    }

    private String requiredText(String value, String message) {
        if (!hasText(value)) {
            throw new BizException(message);
        }
        return value.trim();
    }

    private List<PmProject> allProjects() {
        LambdaQueryWrapper<PmProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PmProject::getId);
        return projectMapper.selectList(wrapper);
    }

    private List<PmProjectMember> allMembers() {
        LambdaQueryWrapper<PmProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(PmProjectMember::getProjectId).orderByAsc(PmProjectMember::getId);
        return projectMemberMapper.selectList(wrapper);
    }

    private List<PmTask> allTasks() {
        LambdaQueryWrapper<PmTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PmTask::getId);
        return taskMapper.selectList(wrapper);
    }

    private List<PmWeeklyReport> allReports() {
        LambdaQueryWrapper<PmWeeklyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PmWeeklyReport::getReportDate).orderByDesc(PmWeeklyReport::getId);
        return weeklyReportMapper.selectList(wrapper);
    }

    private List<PmLeave> allLeaves() {
        LambdaQueryWrapper<PmLeave> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PmLeave::getSubmittedAt).orderByDesc(PmLeave::getId);
        return leaveMapper.selectList(wrapper);
    }

    private PmProject getProjectByCode(String code) {
        LambdaQueryWrapper<PmProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmProject::getProjectCode, code).last("limit 1");
        return projectMapper.selectOne(wrapper);
    }

    private Map<String, Object> mapProject(PmProject project) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", project.getId());
        map.put("projectCode", project.getProjectCode());
        map.put("projectName", project.getProjectName());
        map.put("projectType", project.getProjectType());
        map.put("location", project.getLocation());
        map.put("ownerOrg", project.getOwnerOrg());
        map.put("contractAmount", project.getContractAmount());
        map.put("startDate", formatDate(project.getStartDate()));
        map.put("plannedFinishDate", formatDate(project.getPlannedFinishDate()));
        map.put("projectStatus", project.getProjectStatus());
        map.put("projectManagerName", project.getProjectManagerName());
        map.put("projectDesc", project.getProjectDesc());
        return map;
    }

    private Map<String, Object> mapMember(PmProjectMember member, List<PmProject> projects) {
        String projectName = projects.stream()
                .filter(project -> Objects.equals(project.getId(), member.getProjectId()))
                .map(PmProject::getProjectName)
                .findFirst()
                .orElse("");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", member.getId());
        map.put("projectId", member.getProjectId());
        map.put("projectName", projectName);
        map.put("projectDeptName", member.getProjectDeptName());
        map.put("employeeName", member.getEmployeeName());
        map.put("positionName", member.getPositionName());
        map.put("arrivalDate", formatDate(member.getArrivalDate()));
        map.put("leaveDate", formatDate(member.getLeaveDate()));
        map.put("onDuty", member.getOnDuty());
        return map;
    }

    private Map<String, Object> mapTask(PmTask task) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", task.getId());
        map.put("taskCode", task.getTaskCode());
        map.put("projectId", task.getProjectId());
        map.put("projectName", task.getProjectName());
        map.put("projectDeptName", task.getProjectDeptName());
        map.put("taskTitle", task.getTaskTitle());
        map.put("taskContent", task.getTaskContent());
        map.put("assignerName", task.getAssignerName());
        map.put("assigneeName", task.getAssigneeName());
        map.put("priority", task.getPriority());
        map.put("requiredFinishDate", formatDate(task.getRequiredFinishDate()));
        map.put("taskStatus", task.getTaskStatus());
        map.put("remark", task.getRemark());
        map.put("createdAt", formatDateTime(task.getCreatedAt()));
        return map;
    }

    private Map<String, Object> mapWeeklyReport(PmWeeklyReport report) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", report.getId());
        map.put("reportCode", report.getReportCode());
        map.put("projectId", report.getProjectId());
        map.put("projectName", report.getProjectName());
        map.put("projectDeptName", report.getProjectDeptName());
        map.put("reportUserName", report.getReportUserName());
        map.put("weekLabel", report.getWeekLabel());
        map.put("reportDate", formatDate(report.getReportDate()));
        map.put("completedWorkText", report.getCompletedWorkText());
        map.put("unfinishedWorkText", report.getUnfinishedWorkText());
        map.put("unfinishedReasonText", report.getUnfinishedReasonText());
        map.put("nextWeekPlanText", report.getNextWeekPlanText());
        map.put("supportNeeds", report.getSupportNeeds());
        map.put("remark", report.getRemark());
        map.put("completedCount", splitLines(report.getCompletedWorkText()).size());
        map.put("unfinishedCount", splitLines(report.getUnfinishedWorkText()).size());
        map.put("nextWeekPlanCount", splitLines(report.getNextWeekPlanText()).size());
        return map;
    }

    private Map<String, Object> mapLeave(PmLeave leave) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", leave.getId());
        map.put("leaveCode", leave.getLeaveCode());
        map.put("applicantName", leave.getApplicantName());
        map.put("projectId", leave.getProjectId());
        map.put("projectName", leave.getProjectName());
        map.put("projectDeptName", leave.getProjectDeptName());
        map.put("leaveType", leave.getLeaveType());
        map.put("startTime", formatDateTime(leave.getStartTime()));
        map.put("endTime", formatDateTime(leave.getEndTime()));
        map.put("dateRange", formatDateTime(leave.getStartTime()) + " ~ " + formatDateTime(leave.getEndTime()));
        map.put("leaveDays", leave.getLeaveDays());
        map.put("reason", leave.getReason());
        map.put("approvalStatus", leave.getApprovalStatus());
        map.put("approvalComment", leave.getApprovalComment());
        map.put("approvedBy", leave.getApprovedBy());
        map.put("approvedAt", formatDateTime(leave.getApprovedAt()));
        map.put("submittedAt", formatDateTime(leave.getSubmittedAt()));
        return map;
    }

    private Map<String, Object> notice(String title, String content, String level, LocalDateTime createdAt) {
        Map<String, Object> notice = new LinkedHashMap<>();
        notice.put("id", generateNoticeId());
        notice.put("title", title);
        notice.put("content", content);
        notice.put("level", level);
        notice.put("createdAt", formatDateTime(createdAt));
        return notice;
    }

    private Long generateNoticeId() {
        return System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000);
    }

    private String generateCode(String prefix) {
        return prefix + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private boolean isUrgent(LocalDate date) {
        return date != null && !date.isAfter(LocalDate.now().plusDays(2));
    }

    private LocalDate defaultDate(LocalDate date) {
        return date == null ? LocalDate.of(2099, 12, 31) : date;
    }

    private List<String> splitLines(String text) {
        return java.util.Arrays.stream((text == null ? "" : text).split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
    }

    private LocalDate parseDate(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (Exception exception) {
            throw new BizException("要求完成时间格式不正确");
        }
    }

    private LocalDateTime parseDateTime(String value, String message) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (Exception exception) {
            throw new BizException(message);
        }
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private String currentUserName() {
        CurrentUser currentUser = AuthContext.get();
        return currentUser == null || !hasText(currentUser.realName()) ? "系统用户" : currentUser.realName().trim();
    }

    private String defaultText(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
