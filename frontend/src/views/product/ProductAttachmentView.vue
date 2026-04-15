<template>
  <el-card>
    <template #header>
      <div class="card-header-row">
        <span>产品附件管理</span>
        <el-button type="primary" @click="openCreate">新增附件</el-button>
      </div>
    </template>

    <div class="toolbar-row">
      <el-input v-model="query.productId" placeholder="按产品ID筛选" clearable class="toolbar-select" />
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="productId" label="产品ID" width="100" />
      <el-table-column prop="attachmentType" label="附件类型" width="140" />
      <el-table-column prop="fileName" label="文件名" min-width="220" />
      <el-table-column prop="fileType" label="文件类型" min-width="220" />
      <el-table-column prop="fileSize" label="文件大小" width="120" />
      <el-table-column prop="filePath" label="存储路径" min-width="240" />
      <el-table-column prop="remark" label="备注" min-width="180" />
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增产品附件" width="760px">
    <el-form :model="form" label-width="100px">
      <div class="form-grid-2">
        <el-form-item label="产品ID"><el-input v-model="form.productId" /></el-form-item>
        <el-form-item label="附件类型"><el-input v-model="form.attachmentType" placeholder="如技术资料 / 图纸 / 接口文档" /></el-form-item>
        <el-form-item label="文件名"><el-input v-model="form.fileName" /></el-form-item>
        <el-form-item label="文件类型"><el-input v-model="form.fileType" /></el-form-item>
        <el-form-item label="文件大小"><el-input v-model="form.fileSize" /></el-form-item>
        <el-form-item label="上传人ID"><el-input v-model="form.uploadedBy" /></el-form-item>
      </div>
      <el-form-item label="文件路径"><el-input v-model="form.filePath" /></el-form-item>
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
import { productAttachmentApi } from "@/api/productAttachment";

const loading = ref(false);
const dialogVisible = ref(false);
const rows = ref<any[]>([]);
const query = reactive({ productId: "" });
const form = reactive({
  productId: "",
  fileName: "",
  filePath: "",
  fileType: "",
  fileSize: "",
  attachmentType: "技术资料",
  remark: "",
  uploadedBy: "1",
});

async function loadData() {
  loading.value = true;
  try {
    const res = await productAttachmentApi.list({
      productId: query.productId ? Number(query.productId) : undefined,
    });
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  Object.assign(form, {
    productId: "",
    fileName: "",
    filePath: "",
    fileType: "",
    fileSize: "",
    attachmentType: "技术资料",
    remark: "",
    uploadedBy: "1",
  });
  dialogVisible.value = true;
}

async function submitForm() {
  await productAttachmentApi.create({
    productId: Number(form.productId),
    fileName: form.fileName,
    filePath: form.filePath,
    fileType: form.fileType,
    fileSize: Number(form.fileSize || 0),
    attachmentType: form.attachmentType,
    remark: form.remark,
    uploadedBy: Number(form.uploadedBy || 1),
  });
  ElMessage.success("产品附件已创建");
  dialogVisible.value = false;
  await loadData();
}

onMounted(loadData);
</script>
