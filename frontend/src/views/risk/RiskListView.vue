<template>
  <el-card>
    <template #header>风险预警</template>
    <div class="toolbar-row">
      <el-select v-model="query.status" placeholder="状态" clearable class="toolbar-select">
        <el-option label="待处理" value="OPEN" />
        <el-option label="已处理" value="HANDLED" />
        <el-option label="已忽略" value="IGNORED" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="warningType" label="预警类型" width="140" />
      <el-table-column prop="warningLevel" label="等级" width="100" />
      <el-table-column prop="warningTitle" label="预警标题" min-width="220" />
      <el-table-column prop="warningContent" label="内容" min-width="260" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="triggerTime" label="触发时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'OPEN'" link type="primary" @click="handleRisk(row.id, 'handle')">处理</el-button>
          <el-button v-if="row.status === 'OPEN'" link type="info" @click="handleRisk(row.id, 'ignore')">忽略</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { riskApi } from "@/api/risk";

const loading = ref(false);
const rows = ref<any[]>([]);
const query = reactive({ status: "" });

async function loadData() {
  loading.value = true;
  try {
    const res = await riskApi.list({ status: query.status || undefined });
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

async function handleRisk(id: number, action: "handle" | "ignore") {
  await riskApi[action](id, { handlerId: 1, remark: action === "handle" ? "已人工处理" : "已人工忽略" });
  ElMessage.success(action === "handle" ? "风险已处理" : "风险已忽略");
  await loadData();
}

onMounted(loadData);
</script>
