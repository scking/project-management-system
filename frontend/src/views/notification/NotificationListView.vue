<template>
  <div class="page-shell">
    <el-card>
      <template #header>
        <div class="card-header-row">
          <span>通知提醒</span>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </div>
      </template>
      <div class="notice-list">
        <div v-for="item in rows" :key="item.id" class="notice-item">
          <div>
            <div class="notice-title">{{ item.title }}</div>
            <div class="notice-content">{{ item.content }}</div>
          </div>
          <div class="notice-meta">
            <el-tag :type="item.level === '高' ? 'danger' : item.level === '中' ? 'warning' : 'info'" effect="light">
              {{ item.level }}
            </el-tag>
            <span>{{ item.createdAt }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { notificationApi } from "@/api/notification";

const rows = ref<any[]>([]);

async function loadData() {
  const res = await notificationApi.list();
  rows.value = res.data || [];
}

onMounted(loadData);
</script>

<style scoped>
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.notice-title {
  font-weight: 600;
  color: #0f172a;
}

.notice-content {
  margin-top: 6px;
  color: #475569;
}

.notice-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  color: #64748b;
  font-size: 12px;
}
</style>
