<template>
  <div class="page-shell">
    <el-card>
      <template #header>
        <div class="card-header-row">
          <span>项目人员管理</span>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </div>
      </template>
      <el-table :data="rows" stripe>
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="projectDeptName" label="所属项目部" width="160" />
        <el-table-column prop="employeeName" label="员工姓名" width="120" />
        <el-table-column prop="positionName" label="岗位" width="140" />
        <el-table-column prop="arrivalDate" label="到岗时间" width="130" />
        <el-table-column prop="leaveDate" label="离岗时间" width="130" />
        <el-table-column prop="onDuty" label="是否在岗" width="110">
          <template #default="{ row }">
            <el-tag :type="row.onDuty ? 'success' : 'info'" effect="light">{{ row.onDuty ? "在岗" : "离岗" }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { projectApi } from "@/api/project";

const rows = ref<any[]>([]);

async function loadData() {
  const res = await projectApi.memberList();
  rows.value = res.data || [];
}

onMounted(loadData);
</script>
