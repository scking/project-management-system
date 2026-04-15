<template>
  <el-card>
    <template #header>
      <div class="card-header-row">
        <span>产品管理</span>
        <el-button type="primary" @click="openCreate">新增产品</el-button>
      </div>
    </template>

    <div class="toolbar-row">
      <el-input v-model="query.keyword" placeholder="搜索产品名称 / 编号 / 品牌 / 型号" clearable class="toolbar-input" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="productCode" label="产品编号" width="160" />
      <el-table-column prop="productName" label="产品名称" min-width="220" />
      <el-table-column prop="productCategory" label="分类" width="140" />
      <el-table-column prop="brand" label="品牌" width="140" />
      <el-table-column prop="model" label="型号" width="160" />
      <el-table-column prop="unit" label="单位" width="100" />
      <el-table-column prop="status" label="状态" width="120" />
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增产品" width="680px">
    <el-form :model="form" label-width="100px">
      <div class="form-grid-2">
        <el-form-item label="产品编号"><el-input v-model="form.productCode" /></el-form-item>
        <el-form-item label="产品名称"><el-input v-model="form.productName" /></el-form-item>
        <el-form-item label="产品分类"><el-input v-model="form.productCategory" /></el-form-item>
        <el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item>
        <el-form-item label="型号"><el-input v-model="form.model" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.unit" /></el-form-item>
        <el-form-item label="状态"><el-input v-model="form.status" /></el-form-item>
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
import { productApi } from "@/api/product";

const loading = ref(false);
const dialogVisible = ref(false);
const rows = ref<any[]>([]);
const query = reactive({ keyword: "" });
const form = reactive({
  productCode: "",
  productName: "",
  productCategory: "",
  brand: "",
  model: "",
  unit: "",
  status: "ENABLED",
  remark: "",
});

async function loadData() {
  loading.value = true;
  try {
    const res = await productApi.list(query);
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  Object.assign(form, {
    productCode: "",
    productName: "",
    productCategory: "",
    brand: "",
    model: "",
    unit: "",
    status: "ENABLED",
    remark: "",
  });
  dialogVisible.value = true;
}

async function submitForm() {
  await productApi.create(form);
  ElMessage.success("产品已创建");
  dialogVisible.value = false;
  await loadData();
}

onMounted(loadData);
</script>
