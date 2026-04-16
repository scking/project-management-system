CREATE TABLE IF NOT EXISTS pm_project (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_code VARCHAR(64) NOT NULL,
  project_name VARCHAR(255) NOT NULL,
  project_type VARCHAR(64),
  location VARCHAR(255),
  owner_org VARCHAR(255),
  contract_amount DECIMAL(18,2),
  start_date DATE,
  planned_finish_date DATE,
  project_status VARCHAR(64),
  project_manager_name VARCHAR(64),
  project_desc TEXT
);

CREATE TABLE IF NOT EXISTS pm_project_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  project_dept_name VARCHAR(128),
  employee_name VARCHAR(64) NOT NULL,
  position_name VARCHAR(64),
  arrival_date DATE,
  leave_date DATE,
  on_duty TINYINT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS pm_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_code VARCHAR(64) NOT NULL,
  project_id BIGINT,
  project_name VARCHAR(255),
  project_dept_name VARCHAR(128),
  task_title VARCHAR(255) NOT NULL,
  task_content TEXT,
  assigner_name VARCHAR(64),
  assignee_name VARCHAR(64),
  priority VARCHAR(32),
  task_status VARCHAR(32),
  required_finish_date DATE,
  remark TEXT,
  created_at DATETIME
);

CREATE TABLE IF NOT EXISTS pm_weekly_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_code VARCHAR(64) NOT NULL,
  project_id BIGINT,
  project_name VARCHAR(255),
  project_dept_name VARCHAR(128),
  report_user_name VARCHAR(64),
  week_label VARCHAR(64),
  report_date DATE,
  completed_work_text TEXT,
  unfinished_work_text TEXT,
  unfinished_reason_text TEXT,
  next_week_plan_text TEXT,
  support_needs TEXT,
  remark TEXT
);

CREATE TABLE IF NOT EXISTS pm_leave (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  leave_code VARCHAR(64) NOT NULL,
  applicant_name VARCHAR(64),
  project_id BIGINT,
  project_name VARCHAR(255),
  project_dept_name VARCHAR(128),
  leave_type VARCHAR(64),
  start_time DATETIME,
  end_time DATETIME,
  leave_days DECIMAL(10,1),
  reason TEXT,
  approval_status VARCHAR(64),
  approval_comment TEXT,
  approved_by VARCHAR(64),
  approved_at DATETIME,
  submitted_at DATETIME
);
