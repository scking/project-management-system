<template>
  <div class="page-shell stats-page">
    <div class="stats-grid">
      <el-card v-for="item in cards" :key="item.label" class="stats-card" shadow="hover">
        <div class="stats-label">{{ item.label }}</div>
        <div class="stats-value">{{ item.value }}</div>
        <div class="stats-desc">{{ item.desc }}</div>
      </el-card>
    </div>

    <el-row :gutter="16">
      <el-col :lg="12" :xs="24">
        <el-card>
          <template #header>
            <div class="card-header-row">
              <span>各项目周报提交率</span>
            </div>
          </template>
          <el-table :data="stats.projectReportRates || []" stripe>
            <el-table-column prop="projectName" label="项目名称" min-width="180" />
            <el-table-column prop="submitted" label="已提交" width="100" />
            <el-table-column prop="pending" label="未提交" width="100" />
            <el-table-column prop="rate" label="提交率" width="120">
              <template #default="{ row }">{{ row.rate }}%</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :lg="12" :xs="24">
        <el-card>
          <template #header>
            <div class="card-header-row">
              <span>请假类型分布</span>
            </div>
          </template>
          <div class="reason-list">
            <div v-for="item in stats.leaveTypeStats || []" :key="item.type" class="reason-item">
              <span>{{ item.type }}</span>
              <strong>{{ item.count }}</strong>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <div class="card-header-row">
          <span>未完成原因统计</span>
        </div>
      </template>
      <el-table :data="stats.unfinishedReasonStats || []" stripe>
        <el-table-column prop="reason" label="原因分类" min-width="180" />
        <el-table-column prop="count" label="数量" width="120" />
        <el-table-column prop="projectCount" label="涉及项目数" width="140" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { projectStatsApi } from "@/api/projectStats";

const stats = ref<any>({});

const cards = computed(() => [
  { label: "本周已提交周报人数", value: stats.value.reportSubmittedCount || 0, desc: "项目员工周报提交情况" },
  { label: "本周未提交周报人数", value: stats.value.reportPendingCount || 0, desc: "待提醒人员数量" },
  { label: "本周新分配任务数", value: stats.value.taskCreatedCount || 0, desc: "部门经理分配任务" },
  { label: "待审批请假数", value: stats.value.leavePendingCount || 0, desc: "当前流程中的请假申请" },
]);

async function loadData() {
  const res = await projectStatsApi.overview();
  stats.value = res.data || {};
}

onMounted(loadData);
</script>

<style scoped>
.stats-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stats-card {
  border-radius: 16px;
}

.stats-label {
  font-size: 13px;
  color: #64748b;
}

.stats-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.stats-desc {
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.reason-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reason-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.reason-item strong {
  color: #2563eb;
}

@media (max-width: 960px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
