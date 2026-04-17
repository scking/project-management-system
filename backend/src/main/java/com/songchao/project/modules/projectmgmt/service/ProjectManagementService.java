package com.songchao.project.modules.projectmgmt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.songchao.project.common.exception.BizException;
import com.songchao.project.modules.projectmgmt.dto.LeaveApproveRequest;
import com.songchao.project.modules.projectmgmt.dto.LeaveSaveRequest;
import com.songchao.project.modules.projectmgmt.dto.ProjectMemberSaveRequest;
import com.songchao.project.modules.projectmgmt.dto.ProjectSaveRequest;
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

    public Map<String, Object> createProject(ProjectSaveRequest request) {
        PmProject project = new PmProject();
        applyProjectSave(project, request);
        projectMapper.insert(project);
        return mapProject(project);
    }

    public Map<String, Object> updateProject(Long id, ProjectSaveRequest request) {
        PmProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException("项目不存在");
        }
        applyProjectSave(project, request);
        projectMapper.updateById(project);
        return mapProject(project);
    }

    public void deleteProject(Long id) {
        PmProject project = projectMapper.selectById(id);
        if (project == null) {
            return;
        }
        LambdaQueryWrapper<PmProjectMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(PmProjectMember::getProjectId, id);
        projectMemberMapper.delete(memberWrapper);

        LambdaQueryWrapper<PmTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(PmTask::getProjectId, id);
        taskMapper.delete(taskWrapper);

        LambdaQueryWrapper<PmWeeklyReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.eq(PmWeeklyReport::getProjectId, id);
        weeklyReportMapper.delete(reportWrapper);

        LambdaQueryWrapper<PmLeave> leaveWrapper = new LambdaQueryWrapper<>();
        leaveWrapper.eq(PmLeave::getProjectId, id);
        leaveMapper.delete(leaveWrapper);

        projectMapper.deleteById(id);
    }

    public List<Map<String, Object>> listProjectMembers() {
        List<PmProject> projects = allProjects();
        return allMembers().stream()
                .sorted(Comparator.comparing(PmProjectMember::getId).reversed())
                .map(item -> mapMember(item, projects))
                .collect(Collectors.toList());
    }

    public Map<String, Object> createProjectMember(ProjectMemberSaveRequest request) {
        PmProject project = resolveProject(request.getProjectId(), null);
        PmProjectMember member = new PmProjectMember();
        applyMemberSave(member, request, project);
        projectMemberMapper.insert(member);
        return mapMember(member, allProjects());
    }

    public Map<String, Object> updateProjectMember(Long id, ProjectMemberSaveRequest request) {
        PmProjectMember member = projectMemberMapper.selectById(id);
        if (member == null) {
            throw new BizException("项目成员不存在");
        }
        PmProject project = resolveProject(request.getProjectId(), null);
        applyMemberSave(member, request, project);
        projectMemberMapper.updateById(member);
        return mapMember(member, allProjects());
    }

    public void deleteProjectMember(Long id) {
        projectMemberMapper.deleteById(id);
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

    public Map<String, Object> getWeeklyReportById(Long id) {
        PmWeeklyReport report = weeklyReportMapper.selectById(id);
        if (report == null) throw new IllegalArgumentException("周报不存在");
        return mapWeeklyReport(report);
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

    private void applyProjectSave(PmProject project, ProjectSaveRequest request) {
        project.setProjectCode(requiredText(request.getProjectCode(), "项目编号不能为空"));
        project.setProjectName(requiredText(request.getProjectName(), "项目名称不能为空"));
        project.setProjectType(hasText(request.getProjectType()) ? request.getProjectType().trim() : null);
        project.setLocation(request.getLocation());
        project.setOwnerOrg(request.getOwnerOrg());
        project.setContractAmount(parseAmount(request.getContractAmount()));
        project.setStartDate(parseDateValue(request.getStartDate(), "开始日期格式不正确"));
        project.setPlannedFinishDate(parseDateValue(request.getPlannedFinishDate(), "计划完工日期格式不正确"));
        project.setProjectStatus(defaultText(request.getProjectStatus(), "在建"));
        project.setProjectManagerName(request.getProjectManagerName());
        project.setProjectDesc(request.getProjectDesc());
    }

    private void applyMemberSave(PmProjectMember member, ProjectMemberSaveRequest request, PmProject project) {
        member.setProjectId(project.getId());
        member.setProjectDeptName(firstNonBlank(request.getProjectDeptName(), project.getProjectName() + "项目部"));
        member.setEmployeeName(requiredText(request.getEmployeeName(), "员工姓名不能为空"));
        member.setPositionName(requiredText(request.getPositionName(), "岗位不能为空"));
        member.setArrivalDate(parseDateValue(request.getArrivalDate(), "到岗日期格式不正确"));
        member.setLeaveDate(parseDateValue(request.getLeaveDate(), "离岗日期格式不正确"));
        member.setOnDuty(request.getOnDuty() == null ? 1 : request.getOnDuty());
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
        ensureProjectSeed("XM-2026-001", "G3018 精河至阿拉山口机电施工项目", "高速机电", "博州阿拉山口", "新疆交投建设管理中心", "12800000.00", "2026-01-10", "2026-12-31", "在建", "刘建国", "收费、监控、通信综合实施项目");
        ensureProjectSeed("XM-2026-002", "乌尉高速乌拉泊互通监控通信项目", "监控通信", "乌鲁木齐", "乌尉高速项目公司", "8600000.00", "2026-02-01", "2026-11-30", "在建", "王志强", "监控通信分项工程");
        ensureProjectSeed("XM-2026-003", "G580 阿拉尔至和田机电设备安装项目", "机电安装", "阿拉尔-和田", "新疆生产建设兵团交建公司", "15600000.00", "2026-03-05", "2026-12-20", "在建", "魏小军", "沿线收费、通信、供配电系统安装");
        ensureProjectSeed("XM-2026-004", "S12 线监控中心升级改造项目", "信息化改造", "昌吉", "昌吉州交投公司", "4200000.00", "2026-04-01", "2026-09-30", "筹备中", "张晓峰", "监控中心软硬件升级");
    }

    private void ensureSeedMembers() {
        PmProject project1 = getProjectByCode("XM-2026-001");
        PmProject project2 = getProjectByCode("XM-2026-002");
        PmProject project3 = getProjectByCode("XM-2026-003");
        PmProject project4 = getProjectByCode("XM-2026-004");
        ensureMemberSeed(project1, "精河项目部", "张凯", "项目经理", "2026-01-10", null, 1);
        ensureMemberSeed(project1, "精河项目部", "李雪", "资料员", "2026-02-01", null, 1);
        ensureMemberSeed(project1, "精河项目部", "周鹏", "安全员", "2026-02-15", null, 1);
        ensureMemberSeed(project2, "乌拉泊项目部", "陈涛", "施工员", "2026-01-18", null, 1);
        ensureMemberSeed(project2, "乌拉泊项目部", "马燕", "试验员", "2026-02-08", null, 1);
        ensureMemberSeed(project3, "和田项目部", "魏小军", "项目经理", "2026-03-05", null, 1);
        ensureMemberSeed(project3, "和田项目部", "赵敏", "预算员", "2026-03-10", null, 1);
        ensureMemberSeed(project4, "昌吉项目部", "张晓峰", "项目经理", "2026-04-01", null, 1);
    }

    private void ensureSeedTasks() {
        PmProject project1 = getProjectByCode("XM-2026-001");
        PmProject project2 = getProjectByCode("XM-2026-002");
        PmProject project3 = getProjectByCode("XM-2026-003");
        ensureTaskSeed("TASK-20260414-001", project1, "精河项目部", "收费站光纤测试", "刘建国", "李雪", "高", "2026-04-18", "进行中");
        ensureTaskSeed("TASK-20260414-002", project2, "乌拉泊项目部", "监控立杆基础复测", "王志强", "陈涛", "中", "2026-04-20", "待接收");
        ensureTaskSeed("TASK-20260414-003", project1, "精河项目部", "收费岛设备联调", "刘建国", "张凯", "高", "2026-04-22", "待处理");
        ensureTaskSeed("TASK-20260414-004", project3, "和田项目部", "和田段供电设备清点", "魏小军", "赵敏", "中", "2026-04-23", "进行中");
        ensureTaskSeed("TASK-20260414-005", project3, "和田项目部", "机房桥架安装验收", "魏小军", "魏小军", "高", "2026-04-25", "待接收");
    }

    private void ensureSeedReports() {
        PmProject project1 = getProjectByCode("XM-2026-001");
        PmProject project2 = getProjectByCode("XM-2026-002");
        PmProject project3 = getProjectByCode("XM-2026-003");
        ensureWeeklyReportSeed("WR-20260414-001", project1, "精河项目部", "李雪", "2026年第16周",
                "2026-04-14", "完成收费站监控点位核对\n完成机房设备到货清点",
                "通信管道整改未完成", "材料未到场\n外部协调未完成",
                "推进通信管道整改\n完成监控设备安装", "协调土建单位提供作业面", "需同步设备到货计划");
        ensureWeeklyReportSeed("WR-20260414-002", project2, "乌拉泊项目部", "陈涛", "2026年第16周",
                "2026-04-14", "完成立杆基础复核\n完成监控箱体位置确认",
                "监控杆件安装未启动", "吊装资源尚未进场",
                "落实吊装班组\n开始首批杆件安装", "协调总包开放吊装作业面", "");
        ensureWeeklyReportSeed("WR-20260414-003", project3, "和田项目部", "赵敏", "2026年第16周",
                "2026-04-15", "完成设备台账梳理\n完成仓储区布置",
                "预算清单复核未完成", "业主清单版本仍在调整",
                "完成预算清单锁定\n推进主材订货", "请商务部支持确认最终清单版本", "预计下周进入设备订货");
    }

    private void ensureSeedLeaves() {
        PmProject project2 = getProjectByCode("XM-2026-002");
        PmProject project3 = getProjectByCode("XM-2026-003");
        ensureLeaveSeed("LEAVE-20260415-001", project2, "乌拉泊项目部", "陈涛", "事假",
                "2026-04-16 09:00:00", "2026-04-17 18:00:00", "2.0", "家中有事", "待项目经理审批");
        ensureLeaveSeed("LEAVE-20260415-002", project3, "和田项目部", "赵敏", "调休",
                "2026-04-18 10:00:00", "2026-04-18 19:00:00", "1.0", "个人事务办理", "待部门经理审批");
        ensureLeaveSeed("LEAVE-20260415-003", project3, "和田项目部", "魏小军", "年假",
                "2026-04-20 09:00:00", "2026-04-21 18:00:00", "2.0", "年度休假", "已通过");
    }

    private void ensureProjectSeed(String code, String name, String type, String location, String ownerOrg, String contractAmount,
                                   String startDate, String finishDate, String status, String manager, String desc) {
        if (getProjectByCode(code) != null) {
            return;
        }
        PmProject project = new PmProject();
        project.setProjectCode(code);
        project.setProjectName(name);
        project.setProjectType(type);
        project.setLocation(location);
        project.setOwnerOrg(ownerOrg);
        project.setContractAmount(new BigDecimal(contractAmount));
        project.setStartDate(LocalDate.parse(startDate));
        project.setPlannedFinishDate(LocalDate.parse(finishDate));
        project.setProjectStatus(status);
        project.setProjectManagerName(manager);
        project.setProjectDesc(desc);
        projectMapper.insert(project);
    }

    private void ensureMemberSeed(PmProject project, String deptName, String employeeName, String positionName, String arrivalDate, String leaveDate, Integer onDuty) {
        if (project == null || hasMember(project.getId(), employeeName)) {
            return;
        }
        projectMemberMapper.insert(seedMember(project.getId(), deptName, employeeName, positionName, arrivalDate, leaveDate, onDuty));
    }

    private void ensureTaskSeed(String taskCode, PmProject project, String projectDeptName, String taskTitle, String assignerName,
                                String assigneeName, String priority, String requiredFinishDate, String taskStatus) {
        if (project == null || getTaskByCode(taskCode) != null) {
            return;
        }
        taskMapper.insert(seedTask(taskCode, project.getId(), project.getProjectName(), projectDeptName, taskTitle, assignerName, assigneeName, priority, requiredFinishDate, taskStatus));
    }

    private void ensureWeeklyReportSeed(String reportCode, PmProject project, String deptName, String reportUserName, String weekLabel,
                                        String reportDate, String completedText, String unfinishedText, String unfinishedReason,
                                        String nextWeekPlan, String supportNeeds, String remark) {
        if (project == null || getWeeklyReportByCode(reportCode) != null) {
            return;
        }
        PmWeeklyReport report = new PmWeeklyReport();
        report.setReportCode(reportCode);
        report.setProjectId(project.getId());
        report.setProjectName(project.getProjectName());
        report.setProjectDeptName(deptName);
        report.setReportUserName(reportUserName);
        report.setWeekLabel(weekLabel);
        report.setReportDate(LocalDate.parse(reportDate));
        report.setCompletedWorkText(completedText);
        report.setUnfinishedWorkText(unfinishedText);
        report.setUnfinishedReasonText(unfinishedReason);
        report.setNextWeekPlanText(nextWeekPlan);
        report.setSupportNeeds(supportNeeds);
        report.setRemark(remark);
        weeklyReportMapper.insert(report);
    }

    private void ensureLeaveSeed(String leaveCode, PmProject project, String deptName, String applicantName, String leaveType,
                                 String startTime, String endTime, String leaveDays, String reason, String status) {
        if (project == null || getLeaveByCode(leaveCode) != null) {
            return;
        }
        PmLeave leave = new PmLeave();
        leave.setLeaveCode(leaveCode);
        leave.setApplicantName(applicantName);
        leave.setProjectId(project.getId());
        leave.setProjectName(project.getProjectName());
        leave.setProjectDeptName(deptName);
        leave.setLeaveType(leaveType);
        leave.setStartTime(parseDateTime(startTime, "开始时间格式不正确"));
        leave.setEndTime(parseDateTime(endTime, "结束时间格式不正确"));
        leave.setLeaveDays(new BigDecimal(leaveDays));
        leave.setReason(reason);
        leave.setApprovalStatus(status);
        leave.setSubmittedAt(LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).minusHours(4));
        if ("已通过".equals(status)) {
            leave.setApprovedBy("系统管理员");
            leave.setApprovedAt(LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).minusHours(2));
        }
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

    private boolean hasMember(Long projectId, String employeeName) {
        LambdaQueryWrapper<PmProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmProjectMember::getProjectId, projectId)
                .eq(PmProjectMember::getEmployeeName, employeeName)
                .last("limit 1");
        return projectMemberMapper.selectOne(wrapper) != null;
    }

    private PmTask getTaskByCode(String taskCode) {
        LambdaQueryWrapper<PmTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmTask::getTaskCode, taskCode).last("limit 1");
        return taskMapper.selectOne(wrapper);
    }

    private PmWeeklyReport getWeeklyReportByCode(String reportCode) {
        LambdaQueryWrapper<PmWeeklyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmWeeklyReport::getReportCode, reportCode).last("limit 1");
        return weeklyReportMapper.selectOne(wrapper);
    }

    private PmLeave getLeaveByCode(String leaveCode) {
        LambdaQueryWrapper<PmLeave> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmLeave::getLeaveCode, leaveCode).last("limit 1");
        return leaveMapper.selectOne(wrapper);
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

    private LocalDate parseDateValue(String value, String message) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (Exception exception) {
            throw new BizException(message);
        }
    }

    private BigDecimal parseAmount(String value) {
        if (!hasText(value)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim().replace(",", "").replace("，", ""));
        } catch (Exception exception) {
            throw new BizException("合同金额格式不正确");
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
