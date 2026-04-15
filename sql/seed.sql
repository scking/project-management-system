INSERT INTO pm_project (project_code, project_name, project_type, location, owner_org, contract_amount, start_date, planned_finish_date, project_status, project_manager_name, project_desc)
VALUES
('XM-2026-001', 'G3018 精河至阿拉山口机电施工项目', '高速机电', '博州阿拉山口', '新疆交投建设管理中心', 12800000.00, '2026-01-10', '2026-12-31', '在建', '刘建国', '首版种子项目'),
('XM-2026-002', '乌尉高速乌拉泊互通监控通信项目', '监控通信', '乌鲁木齐', '乌尉高速项目公司', 8600000.00, '2026-02-01', '2026-11-30', '在建', '王志强', '首版种子项目');

INSERT INTO pm_project_member (project_id, project_dept_name, employee_name, position_name, arrival_date, leave_date, on_duty)
SELECT id, '精河项目部', '张凯', '项目经理', '2026-01-10', NULL, 1 FROM pm_project WHERE project_code = 'XM-2026-001'
UNION ALL
SELECT id, '精河项目部', '李雪', '资料员', '2026-02-01', NULL, 1 FROM pm_project WHERE project_code = 'XM-2026-001'
UNION ALL
SELECT id, '乌拉泊项目部', '陈涛', '施工员', '2026-01-18', NULL, 1 FROM pm_project WHERE project_code = 'XM-2026-002';

INSERT INTO pm_task (task_code, project_id, project_name, project_dept_name, task_title, task_content, assigner_name, assignee_name, priority, task_status, required_finish_date, remark, created_at)
SELECT 'TASK-20260414-001', id, project_name, '精河项目部', '收费站光纤测试', '收费站光纤测试执行与反馈', '王志强', '李雪', '高', '进行中', '2026-04-18', NULL, NOW()
FROM pm_project WHERE project_code = 'XM-2026-001'
UNION ALL
SELECT 'TASK-20260414-002', id, project_name, '乌拉泊项目部', '监控立杆基础复测', '监控立杆基础复测执行与反馈', '王志强', '陈涛', '中', '待接收', '2026-04-20', NULL, NOW()
FROM pm_project WHERE project_code = 'XM-2026-002';

INSERT INTO pm_weekly_report (report_code, project_id, project_name, project_dept_name, report_user_name, week_label, report_date, completed_work_text, unfinished_work_text, unfinished_reason_text, next_week_plan_text, support_needs, remark)
SELECT 'WR-20260414-001', id, project_name, '精河项目部', '李雪', '2026年第16周', '2026-04-14',
       '完成收费站监控点位核对\n完成机房设备到货清点',
       '通信管道整改未完成',
       '材料未到场\n外部协调未完成',
       '推进通信管道整改\n完成监控设备安装',
       '协调土建单位提供作业面',
       NULL
FROM pm_project WHERE project_code = 'XM-2026-001';

INSERT INTO pm_leave (leave_code, applicant_name, project_id, project_name, project_dept_name, leave_type, start_time, end_time, leave_days, reason, approval_status, approval_comment, approved_by, approved_at, submitted_at)
SELECT 'LEAVE-20260415-001', '陈涛', id, project_name, '乌拉泊项目部', '事假', '2026-04-16 09:00:00', '2026-04-17 18:00:00', 2.0, '家中有事', '待项目经理审批', NULL, NULL, NULL, '2026-04-15 09:30:00'
FROM pm_project WHERE project_code = 'XM-2026-002';
