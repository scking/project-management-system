<template>
  <el-card>
    <template #header>
      <div class="card-header-row">
        <span>采购需求管理</span>
        <el-button type="primary" @click="openCreate">新增采购需求</el-button>
      </div>
    </template>

    <div class="toolbar-row">
      <el-input v-model="query.keyword" placeholder="搜索需求编号 / 标题 / 项目" clearable class="toolbar-input" @keyup.enter="loadData" />
      <el-select v-model="query.reqStatus" placeholder="状态" clearable class="toolbar-select">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已提交" value="SUBMITTED" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="reqNo" label="需求编号" width="160" />
      <el-table-column prop="reqTitle" label="需求标题" min-width="220" />
      <el-table-column prop="projectName" label="项目名称" min-width="200" />
      <el-table-column prop="applicantName" label="申请人" width="120" />
      <el-table-column prop="deptName" label="部门" width="120" />
      <el-table-column prop="requiredDate" label="需求日期" width="140" />
      <el-table-column prop="totalAmount" label="预算金额" width="140" />
      <el-table-column prop="reqStatus" label="状态" width="120" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="loadItems(row.id)">查看明细</el-button>
          <el-button v-if="row.reqStatus === 'DRAFT'" link type="primary" @click="submitReq(row.id)">提交</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-divider>采购明细</el-divider>
    <div class="toolbar-row">
      <el-input v-model="itemQuery.reqId" placeholder="采购需求ID" clearable class="toolbar-select" />
      <el-button @click="loadItems()">查询明细</el-button>
      <el-button type="primary" @click="openItemCreate">新增明细</el-button>
    </div>

    <el-table :data="items" stripe>
      <el-table-column prop="reqId" label="需求ID" width="100" />
      <el-table-column prop="productName" label="产品名称" min-width="180" />
      <el-table-column prop="specification" label="规格型号" min-width="180" />
      <el-table-column prop="unit" label="单位" width="90" />
      <el-table-column prop="qty" label="数量" width="120" />
      <el-table-column prop="budgetPrice" label="预算单价" width="120" />
      <el-table-column prop="budgetAmount" label="预算金额" width="120" />
      <el-table-column prop="technicalRequirements" label="技术要求" min-width="220" />
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增采购需求" width="720px">
    <el-form :model="form" label-width="100px">
      <div class="form-grid-2">
        <el-form-item label="需求编号"><el-input v-model="form.reqNo" /></el-form-item>
        <el-form-item label="需求标题"><el-input v-model="form.reqTitle" /></el-form-item>
        <el-form-item label="项目ID"><el-input v-model="form.projectId" /></el-form-item>
        <el-form-item label="项目名称"><el-input v-model="form.projectName" /></el-form-item>
        <el-form-item label="申请人ID"><el-input v-model="form.applicantId" /></el-form-item>
        <el-form-item label="申请人"><el-input v-model="form.applicantName" /></el-form-item>
        <el-form-item label="部门ID"><el-input v-model="form.deptId" /></el-form-item>
        <el-form-item label="部门名称"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="需求日期"><el-input v-model="form.requiredDate" placeholder="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="预算金额"><el-input v-model="form.totalAmount" /></el-form-item>
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

  <el-dialog v-model="itemDialogVisible" title="新增采购需求明细" width="760px">
    <el-form :model="itemForm" label-width="100px">
      <div class="form-grid-2">
        <el-form-item label="需求ID"><el-input v-model="itemForm.reqId" /></el-form-item>
        <el-form-item label="产品ID"><el-input v-model="itemForm.productId" /></el-form-item>
        <el-form-item label="产品名称"><el-input v-model="itemForm.productName" /></el-form-item>
        <el-form-item label="规格型号"><el-input v-model="itemForm.specification" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="itemForm.unit" /></el-form-item>
        <el-form-item label="数量"><el-input v-model="itemForm.qty" /></el-form-item>
        <el-form-item label="预算单价"><el-input v-model="itemForm.budgetPrice" /></el-form-item>
        <el-form-item label="预算金额"><el-input v-model="itemForm.budgetAmount" placeholder="可留空自动计算" /></el-form-item>
      </div>
      <el-form-item label="技术要求"><el-input v-model="itemForm.technicalRequirements" type="textarea" :rows="3" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="itemForm.remark" type="textarea" :rows="2" /></el-form-item>
    </el-form>
    <template #footer>
      <div class="drawer-footer">
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitItem">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { purchaseApi } from "@/api/purchase";

const loading = ref(false);
const dialogVisible = ref(false);
const itemDialogVisible = ref(false);
const rows = ref<any[]>([]);
const items = ref<any[]>([]);
const query = reactive({ keyword: "", reqStatus: "" });
const itemQuery = reactive({ reqId: "" });
const form = reactive({
  reqNo: "",
  projectId: "",
  projectName: "",
  reqTitle: "",
  applicantId: "",
  applicantName: "",
  deptId: "",
  deptName: "",
  requiredDate: "",
  totalAmount: "",
  remark: "",
});
const itemForm = reactive({
  reqId: "",
  productId: "",
  productName: "",
  specification: "",
  unit: "",
  qty: "",
  budgetPrice: "",
  budgetAmount: "",
  technicalRequirements: "",
  remark: "",
});

async function loadData() {
  loading.value = true;
  try {
    const res = await purchaseApi.list(query);
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  Object.assign(form, {
    reqNo: "",
    projectId: "",
    projectName: "",
    reqTitle: "",
    applicantId: "",
    applicantName: "",
    deptId: "",
    deptName: "",
    requiredDate: "",
    totalAmount: "",
    remark: "",
  });
  dialogVisible.value = true;
}

function openItemCreate() {
  Object.assign(itemForm, {
    reqId: itemQuery.reqId,
    productId: "",
    productName: "",
    specification: "",
    unit: "",
    qty: "",
    budgetPrice: "",
    budgetAmount: "",
    technicalRequirements: "",
    remark: "",
  });
  itemDialogVisible.value = true;
}

async function submitForm() {
  await purchaseApi.create({
    ...form,
    projectId: Number(form.projectId || 0) || null,
    applicantId: Number(form.applicantId || 0) || null,
    deptId: Number(form.deptId || 0) || null,
    totalAmount: Number(form.totalAmount || 0),
  });
  ElMessage.success("采购需求已创建");
  dialogVisible.value = false;
  await loadData();
}

async function submitReq(id: number) {
  await purchaseApi.submit(id);
  ElMessage.success("采购需求已提交");
  await loadData();
}

async function loadItems(reqId?: number) {
  if (reqId) {
    itemQuery.reqId = String(reqId);
  }
  const res = await purchaseApi.itemList({
    reqId: itemQuery.reqId ? Number(itemQuery.reqId) : undefined,
  });
  items.value = res.data || [];
}

async function submitItem() {
  await purchaseApi.itemCreate({
    reqId: Number(itemForm.reqId),
    productId: Number(itemForm.productId || 0) || null,
    productName: itemForm.productName,
    specification: itemForm.specification,
    unit: itemForm.unit,
    qty: Number(itemForm.qty || 0),
    budgetPrice: Number(itemForm.budgetPrice || 0),
    budgetAmount: itemForm.budgetAmount ? Number(itemForm.budgetAmount) : null,
    technicalRequirements: itemForm.technicalRequirements,
    remark: itemForm.remark,
  });
  ElMessage.success("采购需求明细已创建");
  itemDialogVisible.value = false;
  await loadItems();
}

onMounted(async () => {
  await loadData();
  await loadItems();
});
</script>
