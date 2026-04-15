<template>
  <el-card>
    <template #header>
      <div class="card-header-row">
        <span>供应商资质管理</span>
        <el-button type="primary" @click="openCreate">新增资质</el-button>
      </div>
    </template>

    <div class="toolbar-row">
      <el-input v-model="query.supplierId" placeholder="输入供应商ID筛选" class="toolbar-select" clearable />
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="supplierId" label="供应商ID" width="110" />
      <el-table-column prop="qualificationType" label="资质类型" width="160" />
      <el-table-column prop="qualificationName" label="资质名称" min-width="220" />
      <el-table-column prop="qualificationNo" label="证书编号" width="180" />
      <el-table-column prop="issuedBy" label="发证机构" width="180" />
      <el-table-column prop="expireDate" label="到期日期" width="140" />
      <el-table-column prop="status" label="状态" width="120" />
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增资质" width="680px">
    <el-form :model="form" label-width="100px">
      <div class="form-grid-2">
        <el-form-item label="供应商ID"><el-input v-model="form.supplierId" /></el-form-item>
        <el-form-item label="资质类型"><el-input v-model="form.qualificationType" /></el-form-item>
        <el-form-item label="资质名称"><el-input v-model="form.qualificationName" /></el-form-item>
        <el-form-item label="证书编号"><el-input v-model="form.qualificationNo" /></el-form-item>
        <el-form-item label="发证机构"><el-input v-model="form.issuedBy" /></el-form-item>
        <el-form-item label="状态"><el-input v-model="form.status" /></el-form-item>
        <el-form-item label="发证日期"><el-input v-model="form.issueDate" placeholder="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="到期日期"><el-input v-model="form.expireDate" placeholder="YYYY-MM-DD" /></el-form-item>
      </div>
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
import { supplierApi } from "@/api/supplier";

const loading = ref(false);
const dialogVisible = ref(false);
const rows = ref<any[]>([]);
const query = reactive({ supplierId: "" });
const form = reactive({
  supplierId: "",
  qualificationType: "",
  qualificationName: "",
  qualificationNo: "",
  issuedBy: "",
  issueDate: "",
  expireDate: "",
  status: "VALID",
  remark: "",
});

async function loadData() {
  loading.value = true;
  try {
    const res = await supplierApi.qualificationList({ supplierId: query.supplierId || undefined });
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  Object.assign(form, {
    supplierId: "",
    qualificationType: "",
    qualificationName: "",
    qualificationNo: "",
    issuedBy: "",
    issueDate: "",
    expireDate: "",
    status: "VALID",
    remark: "",
  });
  dialogVisible.value = true;
}

async function submitForm() {
  await supplierApi.qualificationCreate({
    ...form,
    supplierId: Number(form.supplierId),
  });
  ElMessage.success("资质已新增");
  dialogVisible.value = false;
  await loadData();
}

onMounted(loadData);
</script>

