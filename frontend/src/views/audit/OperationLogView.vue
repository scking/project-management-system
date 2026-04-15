<template>
  <el-card>
    <template #header>
      <div class="card-header-row">
        <span>操作日志</span>
        <el-tag type="info" effect="plain">记录新增、编辑、审批、状态变更等关键动作</el-tag>
      </div>
    </template>

    <div class="toolbar-row">
      <el-input v-model="query.moduleName" placeholder="模块名称" clearable class="toolbar-input" @keyup.enter="loadData" />
      <el-input v-model="query.operationType" placeholder="操作类型" clearable class="toolbar-select" @keyup.enter="loadData" />
      <el-select v-model="query.resultStatus" placeholder="结果" clearable class="toolbar-select">
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAILED" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="moduleName" label="模块" width="140" />
      <el-table-column prop="operationType" label="操作" width="140" />
      <el-table-column prop="bizType" label="业务类型" width="140" />
      <el-table-column prop="operatorName" label="操作人" width="120" />
      <el-table-column prop="requestMethod" label="请求方式" width="100" />
      <el-table-column prop="requestUri" label="请求路径" min-width="200" />
      <el-table-column prop="resultStatus" label="结果" width="100">
        <template #default="{ row }">
          <el-tag :type="row.resultStatus === 'SUCCESS' ? 'success' : 'danger'" effect="plain">
            {{ row.resultStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operationDesc" label="说明" min-width="200" />
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="errorMessage" label="错误信息" min-width="180" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { operationLogApi } from "@/api/operationLog";

const loading = ref(false);
const rows = ref<any[]>([]);
const query = reactive({
  moduleName: "",
  operationType: "",
  resultStatus: "",
});

async function loadData() {
  loading.value = true;
  try {
    const res = await operationLogApi.list(query);
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>
