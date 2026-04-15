<template>
  <el-card>
    <template #header>
      <div class="card-header-row">
        <span>供应商台账</span>
        <div class="toolbar-row">
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="openCreate">新增供应商</el-button>
        </div>
      </div>
    </template>

    <div class="toolbar-row supplier-toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索供应商名称 / 编号 / 联系人"
        clearable
        class="toolbar-input"
        @keyup.enter="loadData"
      />
      <el-select v-model="query.status" placeholder="状态" clearable class="toolbar-select">
        <el-option label="正常" value="NORMAL" />
        <el-option label="冻结" value="FROZEN" />
        <el-option label="黑名单" value="BLACKLIST" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="supplierCode" label="供应商编号" width="160" />
      <el-table-column prop="supplierName" label="供应商名称" min-width="220" />
      <el-table-column prop="supplierType" label="类型" width="120" />
      <el-table-column prop="contactPerson" label="联系人" width="120" />
      <el-table-column prop="contactPhone" label="联系电话" width="150" />
      <el-table-column prop="city" label="城市" width="120" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="cooperationLevel" label="合作等级" width="100" />
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row.id)">编辑</el-button>
            <el-button v-if="row.status === 'NORMAL'" link type="warning" @click="changeStatus(row.id, 'freeze')">冻结</el-button>
            <el-button v-if="row.status === 'FROZEN'" link type="success" @click="changeStatus(row.id, 'unfreeze')">解冻</el-button>
            <el-button v-if="row.status !== 'BLACKLIST'" link type="danger" @click="changeStatus(row.id, 'blacklist')">拉黑</el-button>
            <el-button v-if="row.status === 'BLACKLIST'" link type="success" @click="changeStatus(row.id, 'removeBlacklist')">移出黑名单</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-drawer v-model="drawerVisible" :title="drawerTitle" size="720px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <div class="form-grid-2">
        <el-form-item label="供应商编号" prop="supplierCode">
          <el-input v-model="form.supplierCode" :disabled="mode === 'detail'" />
        </el-form-item>
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="form.supplierName" :disabled="mode === 'detail'" />
        </el-form-item>
        <el-form-item label="供应商类型" prop="supplierType">
          <el-select v-model="form.supplierType" :disabled="mode === 'detail'">
            <el-option label="生产商" value="生产商" />
            <el-option label="代理商" value="代理商" />
            <el-option label="服务商" value="服务商" />
            <el-option label="集成商" value="集成商" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作等级">
          <el-select v-model="form.cooperationLevel" :disabled="mode === 'detail'">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="统一信用代码">
          <el-input v-model="form.creditCode" :disabled="mode === 'detail'" />
        </el-form-item>
        <el-form-item label="法人代表">
          <el-input v-model="form.legalPerson" :disabled="mode === 'detail'" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" :disabled="mode === 'detail'" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" :disabled="mode === 'detail'" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.contactEmail" :disabled="mode === 'detail'" />
        </el-form-item>
        <el-form-item label="纳税人类型">
          <el-input v-model="form.taxpayerType" :disabled="mode === 'detail'" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" :disabled="mode === 'detail'" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" :disabled="mode === 'detail'" />
        </el-form-item>
        <el-form-item label="开户地址">
          <el-input v-model="form.bankName" :disabled="mode === 'detail'" />
        </el-form-item>
        <el-form-item label="银行账号">
          <el-input v-model="form.bankAccount" :disabled="mode === 'detail'" />
        </el-form-item>
      </div>
      <el-form-item label="联系地址">
        <el-input v-model="form.address" :disabled="mode === 'detail'" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="4" :disabled="mode === 'detail'" />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="drawer-footer">
        <el-button @click="drawerVisible = false">关闭</el-button>
        <el-button v-if="mode !== 'detail'" type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onActivated, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { supplierApi } from "@/api/supplier";

type Mode = "create" | "edit" | "detail";

const loading = ref(false);
const submitting = ref(false);
const drawerVisible = ref(false);
const mode = ref<Mode>("create");
const currentId = ref<number | null>(null);
const formRef = ref<FormInstance>();

const query = reactive({
  keyword: "",
  status: "",
});

const createEmptyForm = () => ({
  supplierCode: "",
  supplierName: "",
  supplierType: "生产商",
  creditCode: "",
  legalPerson: "",
  contactPerson: "",
  contactPhone: "",
  contactEmail: "",
  province: "",
  city: "",
  address: "",
  bankName: "",
  bankAccount: "",
  taxpayerType: "",
  cooperationLevel: "B",
  remark: "",
});

const form = reactive(createEmptyForm());
const rows = ref<any[]>([]);

const rules: FormRules = {
  supplierCode: [{ required: true, message: "请输入供应商编号", trigger: "blur" }],
  supplierName: [{ required: true, message: "请输入供应商名称", trigger: "blur" }],
  supplierType: [{ required: true, message: "请选择供应商类型", trigger: "change" }],
};

const drawerTitle = computed(() => {
  if (mode.value === "create") return "新增供应商";
  if (mode.value === "edit") return "编辑供应商";
  return "供应商详情";
});

function resetForm() {
  Object.assign(form, createEmptyForm());
  currentId.value = null;
}

function resetQuery() {
  query.keyword = "";
  query.status = "";
  loadData();
}

function statusText(status: string) {
  return {
    NORMAL: "正常",
    FROZEN: "冻结",
    BLACKLIST: "黑名单",
  }[status] || status || "-";
}

function statusTagType(status: string) {
  return {
    NORMAL: "success",
    FROZEN: "warning",
    BLACKLIST: "danger",
  }[status] || "info";
}

async function loadData() {
  loading.value = true;
  try {
    const res = await supplierApi.list(query);
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

async function loadDetail(id: number) {
  const res = await supplierApi.detail(id);
  Object.assign(form, createEmptyForm(), res.data || {});
}

function openCreate() {
  mode.value = "create";
  resetForm();
  drawerVisible.value = true;
}

async function openEdit(id: number) {
  mode.value = "edit";
  currentId.value = id;
  await loadDetail(id);
  drawerVisible.value = true;
}

async function openDetail(id: number) {
  mode.value = "detail";
  currentId.value = id;
  await loadDetail(id);
  drawerVisible.value = true;
}

async function submitForm() {
  if (!formRef.value) return;
  await formRef.value.validate();
  submitting.value = true;
  try {
    if (mode.value === "create") {
      await supplierApi.create(form);
      ElMessage.success("供应商已创建");
    } else if (currentId.value) {
      await supplierApi.update(currentId.value, form);
      ElMessage.success("供应商已更新");
    }
    drawerVisible.value = false;
    await loadData();
  } finally {
    submitting.value = false;
  }
}

async function changeStatus(id: number, action: "freeze" | "unfreeze" | "blacklist" | "removeBlacklist") {
  const actionText = {
    freeze: "冻结",
    unfreeze: "解冻",
    blacklist: "拉入黑名单",
    removeBlacklist: "移出黑名单",
  }[action];
  await ElMessageBox.confirm(`确认要${actionText}该供应商吗？`, "状态变更确认", { type: "warning" });
  await supplierApi[action](id);
  ElMessage.success(`${actionText}成功`);
  await loadData();
}

onMounted(loadData);
onActivated(() => void 0);
</script>
