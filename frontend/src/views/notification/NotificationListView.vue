<template>
  <div class="saas-list-page">
    <div class="saas-page-header">
      <div>
        <h2 class="saas-page-title">通知提醒</h2>
        <p class="saas-page-subtitle">查看系统推送的项目进度、审批结果与重要事项</p>
      </div>
      <div class="saas-row-actions">
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </el-button>
      </div>
    </div>

    <section class="saas-card">
      <div v-if="rows.length" class="notice-list">
        <div v-for="item in rows" :key="item.id" class="notice-item">
          <span class="notice-dot" :class="levelClass(item.level)"></span>
          <div class="notice-main">
            <div class="notice-title">{{ item.title }}</div>
            <div class="notice-content">{{ item.content }}</div>
          </div>
          <div class="notice-meta">
            <el-tag
              size="small"
              :type="item.level === '高' ? 'danger' : item.level === '中' ? 'warning' : 'info'"
              effect="light"
            >
              {{ item.level }}
            </el-tag>
            <span class="notice-time">{{ item.createdAt }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无通知" :image-size="72" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { Refresh } from "@element-plus/icons-vue";
import { notificationApi } from "@/api/notification";

const rows = ref<any[]>([]);

function levelClass(level: string) {
  if (level === "高") return "is-danger";
  if (level === "中") return "is-warning";
  return "is-info";
}

async function loadData() {
  try {
    const res = await notificationApi.list();
    rows.value = res.data || [];
  } catch { /* ignore */ }
}

onMounted(loadData);
</script>

<style scoped>
.notice-list { display: flex; flex-direction: column; }
.notice-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 4px;
  border-bottom: 1px solid var(--saas-border-3);
}
.notice-item:last-child { border-bottom: none; }

.notice-dot {
  width: 10px; height: 10px; border-radius: 50%;
  flex: none;
}
.notice-dot.is-danger { background: var(--saas-danger); box-shadow: 0 0 0 4px var(--saas-danger-bg); }
.notice-dot.is-warning { background: var(--saas-warning); box-shadow: 0 0 0 4px var(--saas-warning-bg); }
.notice-dot.is-info { background: var(--saas-brand-400); box-shadow: 0 0 0 4px var(--saas-brand-50); }

.notice-main { flex: 1; min-width: 0; }
.notice-title {
  font-size: var(--saas-fs-base);
  font-weight: 600;
  color: var(--saas-text-1);
}
.notice-content {
  margin-top: 4px;
  font-size: var(--saas-fs-sm);
  color: var(--saas-text-3);
  line-height: 1.6;
}
.notice-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex: none;
}
.notice-time { font-size: var(--saas-fs-xs); color: var(--saas-text-4); }
</style>
