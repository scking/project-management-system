<template>
  <el-card>
    <template #header>
      <div class="card-header-row">
        <span>询价比价管理</span>
        <el-button type="primary" @click="openCreate">新增询价单</el-button>
      </div>
    </template>

    <div class="toolbar-row">
      <el-input v-model="query.keyword" placeholder="搜索询价编号 / 标题" clearable class="toolbar-input" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable class="toolbar-select">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已比价" value="COMPARED" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="inquiryNo" label="询价编号" width="160" />
      <el-table-column prop="inquiryTitle" label="询价标题" min-width="220" />
      <el-table-column prop="projectId" label="项目ID" width="100" />
      <el-table-column prop="reqId" label="需求ID" width="100" />
      <el-table-column prop="deadlineTime" label="截止时间" width="180" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="recommendedReason" label="推荐说明" min-width="260" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'DRAFT'" link type="primary" @click="compare(row.id)">执行比价</el-button>
          <el-button link type="success" @click="recommend(row.id)">推荐中选</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-divider>报价明细</el-divider>
    <div class="toolbar-row">
      <el-input v-model="quoteQuery.inquiryId" placeholder="询价单ID" class="toolbar-select" clearable />
      <el-button @click="loadQuotes">查询报价</el-button>
      <el-button type="primary" @click="openQuoteCreate">录入报价</el-button>
    </div>
    <el-table :data="quotes" stripe>
      <el-table-column prop="compareRank" label="排名" width="80" />
      <el-table-column prop="inquiryId" label="询价单ID" width="110" />
      <el-table-column prop="supplierId" label="供应商ID" width="110" />
      <el-table-column prop="supplierName" label="供应商名称" min-width="180" />
      <el-table-column prop="totalAmount" label="报价金额" width="140" />
      <el-table-column prop="deliveryCycleDays" label="供货周期" width="120" />
      <el-table-column prop="warrantyMonths" label="质保(月)" width="100" />
      <el-table-column prop="priceScore" label="价格分" width="100" />
      <el-table-column prop="deliveryScore" label="交期分" width="100" />
      <el-table-column prop="warrantyScore" label="质保分" width="100" />
      <el-table-column prop="serviceScore" label="评价分" width="100" />
      <el-table-column prop="totalScore" label="综合分" width="100" />
      <el-table-column prop="quoteStatus" label="状态" width="120" />
    </el-table>

    <el-divider>报价行项目</el-divider>
    <div class="toolbar-row">
      <el-input v-model="quoteItemQuery.inquiryId" placeholder="询价单ID" class="toolbar-select" clearable />
      <el-input v-model="quoteItemQuery.quoteId" placeholder="报价单ID" class="toolbar-select" clearable />
      <el-button @click="loadQuoteItems">查询行项目</el-button>
    </div>
    <el-table :data="quoteItems" stripe>
      <el-table-column prop="quoteId" label="报价单ID" width="100" />
      <el-table-column prop="productName" label="产品名称" min-width="160" />
      <el-table-column prop="specification" label="规格型号" min-width="160" />
      <el-table-column prop="brand" label="品牌" width="120" />
      <el-table-column prop="qty" label="数量" width="90" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="unitPrice" label="单价" width="120" />
      <el-table-column prop="lineAmount" label="金额" width="120" />
      <el-table-column prop="deliveryCycleDays" label="交期" width="90" />
      <el-table-column prop="warrantyMonths" label="质保" width="90" />
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增询价单" width="720px">
    <el-form :model="form" label-width="100px">
      <div class="form-grid-2">
        <el-form-item label="询价编号"><el-input v-model="form.inquiryNo" /></el-form-item>
        <el-form-item label="询价标题"><el-input v-model="form.inquiryTitle" /></el-form-item>
        <el-form-item label="项目ID"><el-input v-model="form.projectId" /></el-form-item>
        <el-form-item label="需求ID"><el-input v-model="form.reqId" /></el-form-item>
        <el-form-item label="截止时间"><el-input v-model="form.deadlineTime" placeholder="YYYY-MM-DD HH:mm:ss" /></el-form-item>
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

  <el-dialog v-model="quoteDialogVisible" title="录入报价" width="720px">
    <el-form :model="quoteForm" label-width="100px">
      <div class="form-grid-2">
        <el-form-item label="询价单ID"><el-input v-model="quoteForm.inquiryId" /></el-form-item>
        <el-form-item label="供应商ID"><el-input v-model="quoteForm.supplierId" /></el-form-item>
        <el-form-item label="供应商名称"><el-input v-model="quoteForm.supplierName" /></el-form-item>
        <el-form-item label="供货周期"><el-input v-model="quoteForm.deliveryCycleDays" /></el-form-item>
        <el-form-item label="质保(月)"><el-input v-model="quoteForm.warrantyMonths" /></el-form-item>
        <el-form-item label="报价总额"><el-input :model-value="quoteTotalAmountText" disabled /></el-form-item>
      </div>
      <el-form-item label="备注"><el-input v-model="quoteForm.remark" type="textarea" :rows="3" /></el-form-item>

      <el-divider>报价行项目</el-divider>
      <div class="toolbar-row">
        <el-button type="primary" plain @click="addQuoteItem">新增行项目</el-button>
      </div>
      <div v-for="(item, index) in quoteForm.items" :key="index" class="quote-item-block">
        <div class="card-header-row quote-item-header">
          <span>行项目 {{ index + 1 }}</span>
          <el-button link type="danger" @click="removeQuoteItem(index)">删除</el-button>
        </div>
        <div class="form-grid-2">
          <el-form-item label="产品名称"><el-input v-model="item.productName" /></el-form-item>
          <el-form-item label="规格型号"><el-input v-model="item.specification" /></el-form-item>
          <el-form-item label="品牌"><el-input v-model="item.brand" /></el-form-item>
          <el-form-item label="单位"><el-input v-model="item.unit" /></el-form-item>
          <el-form-item label="数量"><el-input v-model="item.qty" /></el-form-item>
          <el-form-item label="单价"><el-input v-model="item.unitPrice" /></el-form-item>
          <el-form-item label="金额"><el-input :model-value="formatLineAmount(item)" disabled /></el-form-item>
          <el-form-item label="税率"><el-input v-model="item.taxRate" /></el-form-item>
          <el-form-item label="交期(天)"><el-input v-model="item.deliveryCycleDays" /></el-form-item>
          <el-form-item label="质保(月)"><el-input v-model="item.warrantyMonths" /></el-form-item>
        </div>
        <el-form-item label="备注"><el-input v-model="item.remark" /></el-form-item>
      </div>
    </el-form>
    <template #footer>
      <div class="drawer-footer">
        <el-button @click="quoteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitQuote">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { inquiryApi } from "@/api/inquiry";
import { quoteApi } from "@/api/quote";

const loading = ref(false);
const dialogVisible = ref(false);
const quoteDialogVisible = ref(false);
const rows = ref<any[]>([]);
const quotes = ref<any[]>([]);
const quoteItems = ref<any[]>([]);
const query = reactive({ keyword: "", status: "" });
const quoteQuery = reactive({ inquiryId: "" });
const quoteItemQuery = reactive({ inquiryId: "", quoteId: "" });
const form = reactive({
  inquiryNo: "",
  inquiryTitle: "",
  projectId: "",
  reqId: "",
  deadlineTime: "",
  remark: "",
});
const quoteForm = reactive({
  inquiryId: "",
  supplierId: "",
  supplierName: "",
  deliveryCycleDays: "",
  warrantyMonths: "",
  remark: "",
  items: [createQuoteItem()],
});

const quoteTotalAmountText = computed(() => {
  const total = quoteForm.items.reduce((sum, item) => sum + calculateLineAmount(item), 0);
  return total.toFixed(2);
});

async function loadData() {
  loading.value = true;
  try {
    const res = await inquiryApi.list(query);
    rows.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

async function loadQuotes() {
  const res = await quoteApi.list({ inquiryId: quoteQuery.inquiryId || undefined });
  quotes.value = res.data || [];
}

async function loadQuoteItems() {
  const res = await quoteApi.itemList({
    inquiryId: quoteItemQuery.inquiryId ? Number(quoteItemQuery.inquiryId) : undefined,
    quoteId: quoteItemQuery.quoteId ? Number(quoteItemQuery.quoteId) : undefined,
  });
  quoteItems.value = res.data || [];
}

function openCreate() {
  Object.assign(form, {
    inquiryNo: "",
    inquiryTitle: "",
    projectId: "",
    reqId: "",
    deadlineTime: "",
    remark: "",
  });
  dialogVisible.value = true;
}

function openQuoteCreate() {
  Object.assign(quoteForm, {
    inquiryId: "", supplierId: "", supplierName: "", deliveryCycleDays: "", warrantyMonths: "", remark: "", items: [createQuoteItem()],
  });
  quoteDialogVisible.value = true;
}

function createQuoteItem() {
  return {
    productName: "",
    specification: "",
    brand: "",
    unit: "",
    qty: "",
    unitPrice: "",
    taxRate: "",
    deliveryCycleDays: "",
    warrantyMonths: "",
    remark: "",
  };
}

function addQuoteItem() {
  quoteForm.items.push(createQuoteItem());
}

function removeQuoteItem(index: number) {
  if (quoteForm.items.length === 1) {
    quoteForm.items.splice(0, 1, createQuoteItem());
    return;
  }
  quoteForm.items.splice(index, 1);
}

function calculateLineAmount(item: Record<string, string>) {
  const qty = Number(item.qty || 0);
  const unitPrice = Number(item.unitPrice || 0);
  return qty * unitPrice;
}

function formatLineAmount(item: Record<string, string>) {
  return calculateLineAmount(item).toFixed(2);
}

async function submitForm() {
  await inquiryApi.create({
    ...form,
    projectId: Number(form.projectId || 0) || null,
    reqId: Number(form.reqId || 0) || null,
  });
  ElMessage.success("询价单已创建");
  dialogVisible.value = false;
  await loadData();
}

async function compare(id: number) {
  await inquiryApi.compare(id);
  ElMessage.success("已执行基础比价");
  await loadData();
}

async function recommend(id: number) {
  await quoteApi.recommend(id);
  ElMessage.success("已完成基础推荐中选");
  await loadData();
}

async function submitQuote() {
  await quoteApi.create({
    inquiryId: Number(quoteForm.inquiryId),
    supplierId: Number(quoteForm.supplierId),
    supplierName: quoteForm.supplierName,
    totalAmount: Number(quoteTotalAmountText.value || 0),
    deliveryCycleDays: Number(quoteForm.deliveryCycleDays || 0),
    warrantyMonths: Number(quoteForm.warrantyMonths || 0),
    remark: quoteForm.remark,
    items: quoteForm.items.map((item) => ({
      productName: item.productName,
      specification: item.specification,
      brand: item.brand,
      unit: item.unit,
      qty: Number(item.qty || 0),
      unitPrice: Number(item.unitPrice || 0),
      lineAmount: calculateLineAmount(item),
      taxRate: Number(item.taxRate || 0),
      deliveryCycleDays: Number(item.deliveryCycleDays || 0),
      warrantyMonths: Number(item.warrantyMonths || 0),
      remark: item.remark,
    })),
  });
  ElMessage.success("报价已录入");
  quoteDialogVisible.value = false;
  await loadQuotes();
  await loadQuoteItems();
}

onMounted(async () => {
  await loadData();
  await loadQuotes();
  await loadQuoteItems();
});
</script>

<style scoped>
.quote-item-block {
  padding: 14px;
  margin-bottom: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  background: #f9fbff;
}

.quote-item-header {
  margin-bottom: 8px;
}
</style>
