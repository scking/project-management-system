<template>
  <div class="page-shell">
    <el-card>
      <template #header>
        <div class="card-header-row">
          <span>项目基础管理</span>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </div>
      </template>
      <div class="toolbar-row">
        <el-input v-model="keyword" placeholder="搜索项目编号 / 名称 / 地点 / 项目经理" clearable class="toolbar-input" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
      <el-table :data="rows" stripe>
        <el-table-column prop="projectCode" label="项目编号" width="150" />
        <el-table-column prop="projectName" label="项目名称" min-width="220" />
        <el-table-column prop="projectType" label="项目类型" width="140" />
        <el-table-column prop="location" label="项目地点" min-width="180" />
        <el-table-column prop="ownerOrg" label="建设单位" min-width="180" />
        <el-table-column prop="projectManagerName" label="项目经理" width="120" />
        <el-table-column prop="projectStatus" label="状态" width="120" />
        <el-table-column label="合同金额" width="140">
          <template #default="{ row }">¥{{ row.contractAmount || 0 }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { projectApi } from "@/api/project";

const keyword = ref("");
const rows = ref<any[]>([]);

async function loadData() {
  const res = await projectApi.projectList({ keyword: keyword.value });
  rows.value = res.data || [];
}

onMounted(loadData);
</script>
