<template>
  <el-card>
    <template #header>
      <div class="card-header-row">
        <span>供应商评价</span>
        <el-button type="primary" @click="openCreate">新增评价</el-button>
      </div>
    </template>

    <div class="toolbar-row">
      <el-input v-model="query.supplierId" placeholder="供应商ID" class="toolbar-select" clearable />
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="supplierId" label="供应商ID" width="110" />
      <el-table-column prop="projectId" label="项目ID" width="100" />
      <el-table-column prop="contractId" label="合同ID" width="100" />
      <el-table-column prop="totalScore" label="总分" width="100" />
      <el-table-column prop="rating" label="评级" width="100" />
      <el-table-column prop="remark" label="评价说明" min-width="240" />
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增供应商评价" width="760px">
    <el-form :model="form" label-width="110px">
      <div class="form-grid-2">
        <el-form-item label="供应商ID"><el-input v-model="form.supplierId" /></el-form-item>
        <el-form-item label="项目ID"><el-input v-model="form.projectId" /></el-form-item>
        <el-form-item label="合同ID"><el-input v-model="form.contractId" /></el-form-item>
        <el-form-item label="评价人ID"><el-input v-model="form.evaluationUserId" /></el-form-item>
        <el-form-item label="质量评分"><el-input v-model="form.qualityScore" /></el-form-item>
        <el-form-item label="交付评分"><el-input v-model="form.deliveryScore" /></el-form-item>
        <el-form-item label="服务评分"><el-input v-model="form.serviceScore" /></el-form-item>
        <el-form-item label="价格评分"><el-input v-model="form.priceScore" /></el-form-item>
        <el-form-item label="合规评分"><el-input v-model="form.complianceScore" /></el-form-item>
      </div>
      <el-form-item label="评价说明"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
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
import { evaluationApi } from "@/api/evaluation";

const loading = ref(false);
const dialogVisible = ref(false);
const rows = ref<any[]>([]);
const query = reactive({ supplierId: "" });
const form = reactive({
  supplierId: "",
  projectId: "",
  contractId: "",
  evaluationUserId: "1",
  qualityScore: "90",
  deliveryScore: "88",
  serviceScore: "92",
  priceScore: "85",
  complianceScore: "95",
  remark: "",
});

async function loadData() {
  loading.value = true;
  try {
    const res = await evaluationApi.list({ supplierId: query.supplierId || undefined });
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  Object.assign(form, {
    supplierId: "", projectId: "", contractId: "", evaluationUserId: "1",
    qualityScore: "90", deliveryScore: "88", serviceScore: "92", priceScore: "85", complianceScore: "95", remark: "",
  });
  dialogVisible.value = true;
}

async function submitForm() {
  await evaluationApi.create({
    supplierId: Number(form.supplierId),
    projectId: Number(form.projectId || 0) || null,
    contractId: Number(form.contractId || 0) || null,
    evaluationUserId: Number(form.evaluationUserId || 1),
    qualityScore: Number(form.qualityScore || 0),
    deliveryScore: Number(form.deliveryScore || 0),
    serviceScore: Number(form.serviceScore || 0),
    priceScore: Number(form.priceScore || 0),
    complianceScore: Number(form.complianceScore || 0),
    remark: form.remark,
  });
  ElMessage.success("供应商评价已创建");
  dialogVisible.value = false;
  await loadData();
}

onMounted(loadData);
</script>

