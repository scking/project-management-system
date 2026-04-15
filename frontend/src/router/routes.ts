export const routes = [
  {
    path: "/login",
    name: "login",
    component: () => import("@/views/login/LoginView.vue"),
    meta: { title: "登录" },
  },
  {
    path: "/",
    component: () => import("@/layout/MainLayout.vue"),
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        name: "dashboard",
        component: () => import("@/views/dashboard/DashboardView.vue"),
        meta: { title: "工作台", cacheKey: "dashboard" },
      },
      {
        path: "projects/list",
        name: "project-list",
        component: () => import("@/views/project/ProjectListView.vue"),
        meta: { title: "项目基础管理", cacheKey: "project-list" },
      },
      {
        path: "projects/members",
        name: "project-members",
        component: () => import("@/views/project/ProjectMemberView.vue"),
        meta: { title: "项目人员管理", cacheKey: "project-members" },
      },
      {
        path: "tasks/list",
        name: "task-list",
        component: () => import("@/views/task/TaskListView.vue"),
        meta: { title: "任务分配管理", cacheKey: "task-list" },
      },
      {
        path: "weekly-reports/list",
        name: "weekly-report-list",
        component: () => import("@/views/report/WeeklyReportView.vue"),
        meta: { title: "项目周报管理", cacheKey: "weekly-report-list" },
      },
      {
        path: "leaves/list",
        name: "leave-list",
        component: () => import("@/views/leave/LeaveListView.vue"),
        meta: { title: "请假审批管理", cacheKey: "leave-list" },
      },
      {
        path: "notifications/list",
        name: "notification-list",
        component: () => import("@/views/notification/NotificationListView.vue"),
        meta: { title: "通知提醒", cacheKey: "notification-list" },
      },
      {
        path: "stats/index",
        name: "stats-index",
        component: () => import("@/views/stats/StatsView.vue"),
        meta: { title: "统计分析", cacheKey: "stats-index" },
      },
      {
        path: "audit/operation-log",
        name: "audit-operation-log",
        component: () => import("@/views/audit/OperationLogView.vue"),
        meta: { title: "操作日志", cacheKey: "audit-operation-log" },
      },
    ],
  },
];
