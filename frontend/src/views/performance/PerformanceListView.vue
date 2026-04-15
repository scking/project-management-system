<template>
  <el-card>
    <template #header>
      <div class="card-header-row">
        <span>履约管理</span>
        <el-button type="primary" @click="openCreate">新增履约节点</el-button>
      </div>
    </template>

    <div class="toolbar-row">
      <el-input v-model="query.contractId" placeholder="合同ID" class="toolbar-select" clearable />
      <el-select v-model="query.status" placeholder="状态" clearable class="toolbar-select">
        <el-option label="待执行" value="PENDING" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="contractId" label="合同ID" width="100" />
      <el-table-column prop="supplierId" label="供应商ID" width="110" />
      <el-table-column prop="performanceType" label="类型" width="140" />
      <el-table-column prop="nodeName" label="节点名称" min-width="180" />
      <el-table-column prop="plannedDate" label="计划日期" width="140" />
      <el-table-column prop="actualDate" label="实际日期" width="140" />
      <el-table-column prop="status" label="状态" width="120" />
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增履约节点" width="720px">
    <el-form :model="form" label-width="100px">
      <div class="form-grid-2">
        <el-form-item label="合同ID"><el-input v-model="form.contractId" /></el-form-item>
        <el-form-item label="项目ID"><el-input v-model="form.projectId" /></el-form-item>
        <el-form-item label="供应商ID"><el-input v-model="form.supplierId" /></el-form-item>
        <el-form-item label="履约类型"><el-input v-model="form.performanceType" /></el-form-item>
        <el-form-item label="节点名称"><el-input v-model="form.nodeName" /></el-form-item>
        <el-form-item label="责任人ID"><el-input v-model="form.responsibleUserId" /></el-form-item>
        <el-form-item label="计划日期"><el-input v-model="form.plannedDate" placeholder="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="实际日期"><el-input v-model="form.actualDate" placeholder="YYYY-MM-DD" /></el-form-item>
      </div>
      <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
    </el-form>
    <template #footer>
      <div class="drawer-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { performanceApi } from "@/api/performance";

const loading = ref(false);
const dialogVisible = ref(false);
const rows = ref<any[]>([]);
const query = reactive({ contractId: "", status: "" });
const form = reactive({
  contractId: "",
  projectId: "",
  supplierId: "",
  performanceType: "",
  nodeName: "",
  plannedDate: "",
  actualDate: "",
  responsibleUserId: "",
  description: "",
  remark: "",
});

async function loadData() {
  loading.value = true;
  try {
    const res = await performanceApi.list({
      contractId: query.contractId || undefined,
      status: query.status || undefined,
    });
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  Object.assign(form, {
    contractId: "", projectId: "", supplierId: "", performanceType: "", nodeName: "",
    plannedDate: "", actualDate: "", responsibleUserId: "", description: "", remark: "",
  });
  dialogVisible.value = true;
}

async function submitForm() {
  await performanceApi.create({
    contractId: Number(form.contractId),
    projectId: Number(form.projectId || 0) || null,
    supplierId: Number(form.supplierId),
    performanceType: form.performanceType,
    nodeName: form.nodeName,
    plannedDate: form.plannedDate,
    actualDate: form.actualDate,
    responsibleUserId: Number(form.responsibleUserId || 0) || null,
    description: form.description,
    remark: form.remark,
  });
  ElMessage.success("履约节点已创建");
  dialogVisible.value = false;
  await loadData();
}

onMounted(loadData);
</script>

