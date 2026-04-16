<template>
  <div class="saas-dashboard">
    <!-- Hero -->
    <section class="saas-hero">
      <div class="hero-left">
        <div class="hero-greeting">工作台 · {{ authStore.realName || "未登录" }}</div>
        <p class="hero-subtitle">高速公路机电施工项目管理 · 周报、任务、请假、统计一体化协同</p>
        <div class="hero-meta">
          <span class="meta-tag"><el-icon><UserFilled /></el-icon>{{ roleText }}</span>
          <span class="meta-tag"><el-icon><Calendar /></el-icon>{{ currentWeek }}</span>
        </div>
      </div>
      <div class="hero-user">
        <div class="hero-avatar">{{ (authStore.realName || "U").charAt(0) }}</div>
        <div>
          <div class="hero-user-name">{{ authStore.realName || "未登录" }}</div>
          <div class="hero-user-account">项目协同工作台</div>
        </div>
      </div>
    </section>

    <!-- KPI -->
    <div class="saas-kpi-grid">
      <div class="saas-kpi" v-for="item in summaryCards" :key="item.label">
        <div class="saas-kpi-icon" :class="`tone-${item.tone}`">
          <el-icon :size="20"><component :is="item.icon" /></el-icon>
        </div>
        <div class="saas-kpi-body">
          <div class="saas-kpi-label">{{ item.label }}</div>
          <div class="saas-kpi-value">{{ item.value }}</div>
          <div class="saas-kpi-sub">{{ item.desc }}</div>
        </div>
      </div>
    </div>

    <!-- 待办 + 项目 -->
    <div class="dashboard-grid">
      <section class="saas-card is-flush">
        <div class="card-head">
          <h3 class="saas-card-title"><span>待办事项</span></h3>
          <el-button text type="primary" @click="loadDashboard">
            <el-icon><Refresh /></el-icon>
            <span>刷新</span>
          </el-button>
        </div>
        <el-table :data="todoList" stripe>
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column prop="title" label="标题" min-width="220" />
          <el-table-column prop="owner" label="责任人" width="120" />
          <el-table-column prop="dueDate" label="截止时间" width="140" />
          <el-table-column prop="status" label="状态" width="120" />
        </el-table>
      </section>

      <section class="saas-card">
        <h3 class="saas-card-title"><span>本周重点项目</span></h3>
        <div v-if="projects.length" class="project-list">
          <div v-for="item in projects" :key="item.projectCode" class="project-item">
            <div class="project-main">
              <div class="project-name">{{ item.projectName }}</div>
              <div class="project-meta">{{ item.projectType }} · {{ item.location }}</div>
            </div>
            <div class="project-rate">
              <span class="rate-value">{{ item.reportSubmitRate }}%</span>
              <span class="rate-label">周报</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无项目数据" :image-size="56" />
      </section>
    </div>

    <!-- 请假 + 原因 -->
    <div class="dashboard-grid">
      <section class="saas-card is-flush">
        <div class="card-head">
          <h3 class="saas-card-title"><span>请假待审批</span></h3>
        </div>
        <el-table :data="pendingLeaves" stripe>
          <el-table-column prop="applicantName" label="申请人" width="120" />
          <el-table-column prop="leaveType" label="请假类型" width="120" />
          <el-table-column prop="dateRange" label="请假时间" min-width="180" />
          <el-table-column prop="approvalStatus" label="状态" width="120" />
        </el-table>
      </section>

      <section class="saas-card">
        <h3 class="saas-card-title"><span>未完成事项原因</span></h3>
        <div v-if="reasonStats.length" class="reason-list">
          <div v-for="item in reasonStats" :key="item.reason" class="reason-item">
            <span class="reason-name">{{ item.reason }}</span>
            <strong class="reason-count">{{ item.count }}</strong>
          </div>
        </div>
        <el-empty v-else description="暂无原因统计" :image-size="56" />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { projectApi } from "@/api/project";
import { useAuthStore } from "@/store/auth";
import {
  Document, Warning, Checked, Clock, UserFilled, Calendar, Refresh,
} from "@element-plus/icons-vue";

const authStore = useAuthStore();
const dashboard = ref<any>({});

const summaryCards = computed(() => [
  { label: "本周周报提交", value: dashboard.value.reportSubmittedCount || 0, desc: "已提交人数", icon: Document, tone: "brand" },
  { label: "未提交周报", value: dashboard.value.reportPendingCount || 0, desc: "待提醒人员", icon: Warning, tone: "warning" },
  { label: "任务完成数", value: dashboard.value.taskCompletedCount || 0, desc: "本周已完成", icon: Checked, tone: "success" },
  { label: "待审批请假", value: dashboard.value.leavePendingCount || 0, desc: "需要及时处理", icon: Clock, tone: "info" },
]);

const todoList = computed(() => dashboard.value.todoList || []);
const projects = computed(() => dashboard.value.projectFocusList || []);
const pendingLeaves = computed(() => dashboard.value.pendingLeaves || []);
const reasonStats = computed(() => dashboard.value.unfinishedReasonStats || []);
const roleText = computed(() => (authStore.roleCodes.length ? authStore.roleCodes.join(" / ") : "统一门户用户"));

const currentWeek = computed(() => {
  const now = new Date();
  const firstDay = new Date(now.getFullYear(), 0, 1);
  const pastDays = Math.floor((now.getTime() - firstDay.getTime()) / 86400000);
  const week = Math.ceil((pastDays + firstDay.getDay() + 1) / 7);
  return `${now.getFullYear()} 年第 ${week} 周`;
});

async function loadDashboard() {
  try {
    const res = await projectApi.dashboard();
    dashboard.value = res.data || {};
  } catch { /* ignore */ }
}

onMounted(loadDashboard);
</script>

<style scoped>
.saas-dashboard { display: flex; flex-direction: column; gap: var(--saas-space-5); }

.saas-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  border-radius: var(--saas-radius-lg);
  background: linear-gradient(135deg, var(--saas-brand-700) 0%, var(--saas-brand-500) 100%);
  color: #ffffff;
  box-shadow: var(--saas-shadow-brand);
  position: relative;
  overflow: hidden;
}
.saas-hero::before {
  content: "";
  position: absolute;
  right: -80px; top: -80px;
  width: 280px; height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.14), transparent 70%);
}
.hero-greeting { font-size: 24px; font-weight: 700; letter-spacing: -0.01em; }
.hero-subtitle { margin: 8px 0 18px; font-size: 14px; color: rgba(219, 234, 254, 0.92); }
.hero-meta { display: flex; flex-wrap: wrap; gap: 10px; }
.meta-tag {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 5px 12px; border-radius: 999px;
  font-size: 12px; color: #fff;
  background: rgba(255,255,255,0.14);
  border: 1px solid rgba(255,255,255,0.18);
}

.hero-user {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 18px;
  border-radius: var(--saas-radius-md);
  background: rgba(255,255,255,0.14);
  border: 1px solid rgba(255,255,255,0.22);
  min-width: 220px;
  position: relative; z-index: 1;
}
.hero-avatar {
  width: 48px; height: 48px;
  border-radius: 50%;
  background: #fff; color: var(--saas-brand-700);
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 20px;
  border: 2px solid rgba(255,255,255,0.5);
}
.hero-user-name { font-size: 16px; font-weight: 600; color: #fff; }
.hero-user-account { margin-top: 2px; font-size: 12px; color: rgba(219,234,254,0.85); }

.saas-kpi-sub { margin-top: 4px; font-size: var(--saas-fs-xs); color: var(--saas-text-4); }

.dashboard-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: var(--saas-space-5);
}
.card-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid var(--saas-border-3);
}
.card-head .saas-card-title { margin: 0; }

.project-list, .reason-list { display: flex; flex-direction: column; gap: 10px; }
.project-item, .reason-item {
  display: flex; align-items: center; justify-content: space-between;
  gap: 12px; padding: 12px 14px;
  border-radius: var(--saas-radius-md);
  background: var(--saas-bg-subtle);
  border: 1px solid var(--saas-border-3);
}
.project-main { flex: 1; min-width: 0; }
.project-name { font-weight: 600; color: var(--saas-text-1); }
.project-meta { margin-top: 4px; font-size: var(--saas-fs-xs); color: var(--saas-text-3); }
.project-rate { display: flex; flex-direction: column; align-items: flex-end; }
.rate-value { font-size: 22px; font-weight: 700; color: var(--saas-brand-600); line-height: 1; }
.rate-label { font-size: 11px; color: var(--saas-text-4); margin-top: 2px; }

.reason-name { color: var(--saas-text-2); font-size: var(--saas-fs-sm); }
.reason-count { font-size: 22px; font-weight: 700; color: var(--saas-brand-600); }

@media (max-width: 1100px) {
  .dashboard-grid { grid-template-columns: 1fr; }
  .saas-hero { flex-direction: column; align-items: flex-start; }
  .hero-user { width: 100%; }
}
</style>
