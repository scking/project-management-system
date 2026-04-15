package com.songchao.project.modules.projectmgmt.controller;

import com.songchao.project.audit.operationlog.annotation.OperationLogRecord;
import com.songchao.project.common.api.ApiResponse;
import com.songchao.project.modules.projectmgmt.dto.LeaveApproveRequest;
import com.songchao.project.modules.projectmgmt.dto.LeaveSaveRequest;
import com.songchao.project.modules.projectmgmt.dto.TaskSaveRequest;
import com.songchao.project.modules.projectmgmt.dto.TaskStatusUpdateRequest;
import com.songchao.project.modules.projectmgmt.dto.WeeklyReportSaveRequest;
import com.songchao.project.modules.projectmgmt.service.ProjectManagementService;
import com.songchao.project.security.permission.RequirePermission;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pm")
public class ProjectManagementController {
    private final ProjectManagementService service;

    public ProjectManagementController(ProjectManagementService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    @RequirePermission("project:dashboard:view")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.ok(service.dashboard());
    }

    @GetMapping("/projects")
    @RequirePermission("project:project:view")
    public ApiResponse<List<Map<String, Object>>> projects(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(service.listProjects(keyword));
    }

    @GetMapping("/project-members")
    @RequirePermission("project:member:view")
    public ApiResponse<List<Map<String, Object>>> projectMembers() {
        return ApiResponse.ok(service.listProjectMembers());
    }

    @GetMapping("/tasks")
    @RequirePermission("project:task:view")
    public ApiResponse<List<Map<String, Object>>> tasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(service.listTasks(keyword, status));
    }

    @PostMapping("/tasks")
    @RequirePermission("project:task:create")
    @OperationLogRecord(module = "项目管理", operation = "CREATE", bizType = "TASK", description = "新增任务")
    public ApiResponse<Map<String, Object>> createTask(@Valid @RequestBody TaskSaveRequest request) {
        return ApiResponse.ok(service.createTask(request));
    }

    @PostMapping("/tasks/{id}/status")
    @RequirePermission("project:task:update")
    @OperationLogRecord(module = "项目管理", operation = "UPDATE", bizType = "TASK", description = "更新任务状态")
    public ApiResponse<Map<String, Object>> updateTaskStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusUpdateRequest request) {
        return ApiResponse.ok(service.updateTaskStatus(id, request.getTaskStatus()));
    }

    @GetMapping("/weekly-reports")
    @RequirePermission("project:weekly-report:view")
    public ApiResponse<List<Map<String, Object>>> weeklyReports(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(service.listWeeklyReports(keyword));
    }

    @PostMapping("/weekly-reports")
    @RequirePermission("project:weekly-report:create")
    @OperationLogRecord(module = "项目管理", operation = "CREATE", bizType = "WEEKLY_REPORT", description = "提交周报")
    public ApiResponse<Map<String, Object>> createWeeklyReport(@Valid @RequestBody WeeklyReportSaveRequest request) {
        return ApiResponse.ok(service.createWeeklyReport(request));
    }

    @GetMapping("/leaves")
    @RequirePermission("project:leave:view")
    public ApiResponse<List<Map<String, Object>>> leaves(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(service.listLeaves(keyword, status));
    }

    @PostMapping("/leaves")
    @RequirePermission("project:leave:create")
    @OperationLogRecord(module = "项目管理", operation = "CREATE", bizType = "LEAVE", description = "提交请假申请")
    public ApiResponse<Map<String, Object>> createLeave(@Valid @RequestBody LeaveSaveRequest request) {
        return ApiResponse.ok(service.createLeave(request));
    }

    @PostMapping("/leaves/{id}/approve")
    @RequirePermission("project:leave:approve")
    @OperationLogRecord(module = "项目管理", operation = "APPROVE", bizType = "LEAVE", description = "审批请假申请")
    public ApiResponse<Map<String, Object>> approveLeave(@PathVariable Long id, @Valid @RequestBody LeaveApproveRequest request) {
        return ApiResponse.ok(service.approveLeave(id, request));
    }

    @GetMapping("/notifications")
    @RequirePermission("project:notification:view")
    public ApiResponse<List<Map<String, Object>>> notifications() {
        return ApiResponse.ok(service.listNotifications());
    }

    @GetMapping("/stats/overview")
    @RequirePermission("project:stats:view")
    public ApiResponse<Map<String, Object>> statsOverview() {
        return ApiResponse.ok(service.statsOverview());
    }
}
