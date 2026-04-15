<template>
  <div class="dashboard-page">
    <div class="hero-card">
      <div>
        <div class="hero-title">高速公路机电施工项目管理系统</div>
        <div class="hero-subtitle">围绕项目周报、任务分配、请假审批和统计分析的统一协同平台</div>
      </div>
      <div class="hero-tags">
        <el-tag type="primary" effect="light">{{ authStore.realName || "未登录" }}</el-tag>
        <el-tag type="success" effect="light">{{ roleText }}</el-tag>
      </div>
    </div>

    <div class="stats-grid">
      <el-card v-for="item in summaryCards" :key="item.label" shadow="hover" class="stat-card">
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">{{ item.value }}</div>
        <div class="stat-desc">{{ item.desc }}</div>
      </el-card>
    </div>

    <el-row :gutter="16">
      <el-col :lg="14" :xs="24">
        <el-card class="panel-card">
          <template #header>
            <div class="card-header-row">
              <span>待办事项</span>
              <el-button text type="primary" @click="loadDashboard">刷新</el-button>
            </div>
          </template>
          <el-table :data="todoList" stripe>
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="title" label="标题" min-width="220" />
            <el-table-column prop="owner" label="责任人" width="120" />
            <el-table-column prop="dueDate" label="截止时间" width="140" />
            <el-table-column prop="status" label="状态" width="120" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :lg="10" :xs="24">
        <el-card class="panel-card">
          <template #header>
            <div class="card-header-row">
              <span>本周重点项目</span>
            </div>
          </template>
          <div v-if="projects.length" class="project-list">
            <div v-for="item in projects" :key="item.projectCode" class="project-item">
              <div>
                <div class="project-name">{{ item.projectName }}</div>
                <div class="project-meta">{{ item.projectType }} · {{ item.location }}</div>
              </div>
              <div class="project-rate">{{ item.reportSubmitRate }}%</div>
            </div>
          </div>
          <el-empty v-else description="暂无项目数据" :image-size="72" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :lg="12" :xs="24">
        <el-card class="panel-card">
          <template #header>
            <div class="card-header-row">
              <span>请假待审批</span>
            </div>
          </template>
          <el-table :data="pendingLeaves" stripe>
            <el-table-column prop="applicantName" label="申请人" width="120" />
            <el-table-column prop="leaveType" label="请假类型" width="120" />
            <el-table-column prop="dateRange" label="请假时间" min-width="180" />
            <el-table-column prop="approvalStatus" label="状态" width="120" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :lg="12" :xs="24">
        <el-card class="panel-card">
          <template #header>
            <div class="card-header-row">
              <span>未完成事项原因统计</span>
            </div>
          </template>
          <div class="reason-list">
            <div v-for="item in reasonStats" :key="item.reason" class="reason-item">
              <span>{{ item.reason }}</span>
              <strong>{{ item.count }}</strong>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { projectApi } from "@/api/project";
import { useAuthStore } from "@/store/auth";

const authStore = useAuthStore();
const dashboard = ref<any>({});

const summaryCards = computed(() => [
  { label: "本周周报提交", value: dashboard.value.reportSubmittedCount || 0, desc: "已提交人数" },
  { label: "未提交周报", value: dashboard.value.reportPendingCount || 0, desc: "待提醒人员" },
  { label: "任务完成数", value: dashboard.value.taskCompletedCount || 0, desc: "本周已完成" },
  { label: "待审批请假", value: dashboard.value.leavePendingCount || 0, desc: "需要及时处理" },
]);

const todoList = computed(() => dashboard.value.todoList || []);
const projects = computed(() => dashboard.value.projectFocusList || []);
const pendingLeaves = computed(() => dashboard.value.pendingLeaves || []);
const reasonStats = computed(() => dashboard.value.unfinishedReasonStats || []);
const roleText = computed(() => (authStore.roleCodes.length ? authStore.roleCodes.join(" / ") : "统一门户用户"));

async function loadDashboard() {
  const res = await projectApi.dashboard();
  dashboard.value = res.data || {};
}

onMounted(loadDashboard);
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 28px;
  border-radius: 18px;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 48%, #ffffff 100%);
  border: 1px solid #dbeafe;
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.hero-subtitle {
  margin-top: 8px;
  color: #64748b;
}

.hero-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  border-radius: 16px;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
}

.stat-value {
  margin-top: 8px;
  font-size: 30px;
  font-weight: 700;
  color: #0f172a;
}

.stat-desc {
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
}

.panel-card {
  border-radius: 16px;
}

.project-list,
.reason-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.project-item,
.reason-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.project-name {
  font-weight: 600;
  color: #0f172a;
}

.project-meta {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.project-rate,
.reason-item strong {
  font-size: 24px;
  font-weight: 700;
  color: #2563eb;
}

@media (max-width: 960px) {
  .hero-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
