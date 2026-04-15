<template>
  <div class="contract-page">
    <div class="contract-stats">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-label">全部合同</div>
        <div class="stat-value">{{ rows.length }}</div>
        <div class="stat-sub">供应商系统统一合同台账</div>
      </el-card>
      <el-card class="stat-card danger" shadow="hover">
        <div class="stat-label">已逾期</div>
        <div class="stat-value">{{ overdueCount }}</div>
        <div class="stat-sub">合同已过截止日期</div>
      </el-card>
      <el-card class="stat-card warn" shadow="hover">
        <div class="stat-label">近7天到期</div>
        <div class="stat-value">{{ upcomingCount }}</div>
        <div class="stat-sub">建议优先跟进</div>
      </el-card>
    </div>

    <el-row :gutter="16" class="content-grid">
      <el-col :lg="16" :xs="24">
        <el-card>
          <template #header>
            <div class="card-header-row">
              <span>合同管理</span>
              <el-button type="primary" @click="openCreate">新增合同</el-button>
            </div>
          </template>

          <div class="toolbar-row">
            <el-input
              v-model="query.keyword"
              placeholder="搜索合同编号 / 标题 / 供应商 / 项目"
              clearable
              class="toolbar-input"
              @keyup.enter="loadData"
            />
            <el-select v-model="query.contractStatus" placeholder="状态" clearable class="toolbar-select">
              <el-option v-for="item in contractStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="filterType" class="toolbar-select">
              <el-option label="全部" value="all" />
              <el-option label="已逾期" value="overdue" />
              <el-option label="近7天" value="upcoming" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
          </div>

          <el-table :data="filteredRows" stripe v-loading="loading" @row-click="handlePickContract">
            <el-table-column prop="contractNo" label="合同编号" width="160" />
            <el-table-column prop="contractTitle" label="合同标题" min-width="220" />
            <el-table-column prop="supplierName" label="供应商" min-width="180" />
            <el-table-column prop="projectName" label="项目" min-width="180" />
            <el-table-column prop="contractAmount" label="金额" width="140">
              <template #default="{ row }">¥{{ row.contractAmount || 0 }}</template>
            </el-table-column>
            <el-table-column prop="expireDate" label="截止日期" width="140" />
            <el-table-column label="合同状态" width="120">
              <template #default="{ row }">
                <el-tag effect="light">{{ contractStatusLabel(row.contractStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提醒状态" width="130">
              <template #default="{ row }">
                <el-tag :type="reminderTagType(row.reminderStatus)" effect="light">
                  {{ row.reminderStatus || "正常" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="paymentStatus" label="付款状态" width="120" />
            <el-table-column label="操作" width="110" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="handlePickContract(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :lg="8" :xs="24">
        <el-card class="reminder-card">
          <template #header>
            <div class="card-header-row">
              <span>到期提醒</span>
              <el-button text type="primary" @click="loadReminders">刷新</el-button>
            </div>
          </template>
          <div v-if="reminders.length" class="reminder-list">
            <button
              v-for="item in reminders"
              :key="item.contractId"
              type="button"
              class="reminder-item"
              @click="handlePickReminder(item)"
            >
              <div class="reminder-title">{{ item.contractTitle }}</div>
              <div class="reminder-meta">{{ item.contractNo }} · {{ item.supplierName || "未关联供应商" }}</div>
              <div class="reminder-footer">
                <el-tag :type="reminderTagType(item.reminderStatus)" effect="light">{{ item.reminderStatus }}</el-tag>
                <span>{{ item.expireDate || "未设置截止日期" }}</span>
              </div>
            </button>
          </div>
          <el-empty v-else description="暂无需要跟进的合同提醒" :image-size="72" />
        </el-card>
      </el-col>
    </el-row>
  </div>

  <el-drawer v-model="detailVisible" title="合同详情" size="48%">
    <template v-if="detailData">
      <div class="detail-top">
        <div>
          <div class="detail-title">{{ detailData.contract.contractTitle }}</div>
          <div class="detail-sub">{{ detailData.contract.contractNo }} · {{ detailData.contract.projectName || "未关联项目" }}</div>
          <div class="detail-tags">
            <el-tag effect="light">{{ detailContractType || "未分类" }}</el-tag>
            <el-tag effect="light" type="success">{{ detailPartyRole || "甲方" }}</el-tag>
            <el-tag effect="light" type="info">{{ contractStatusLabel(detailData.contract.contractStatus) }}</el-tag>
          </div>
        </div>
        <el-tag :type="reminderTagType(detailData.reminderStatus)" effect="light">
          {{ detailData.reminderStatus || "正常" }}
        </el-tag>
      </div>

      <el-descriptions :column="2" border class="detail-desc">
        <el-descriptions-item label="供应商">{{ detailData.contract.supplierName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ detailData.contract.contractAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ detailContractType || "-" }}</el-descriptions-item>
        <el-descriptions-item label="我方角色">{{ detailPartyRole || "-" }}</el-descriptions-item>
        <el-descriptions-item label="不含税金额">¥{{ detailAmountTaxExclusive }}</el-descriptions-item>
        <el-descriptions-item label="税额">¥{{ detailTaxAmount }}</el-descriptions-item>
        <el-descriptions-item label="签署日期">{{ detailData.contract.signDate || "-" }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ detailData.contract.effectiveDate || "-" }}</el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ detailData.contract.expireDate || "-" }}</el-descriptions-item>
        <el-descriptions-item label="付款状态">{{ detailData.contract.paymentStatus || "-" }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailData.contract.contractStatus || "-" }}</el-descriptions-item>
        <el-descriptions-item label="剩余天数">
          {{ detailData.daysToExpire === null || detailData.daysToExpire === undefined ? "-" : detailData.daysToExpire }}
        </el-descriptions-item>
      </el-descriptions>

      <el-card shadow="never" class="detail-section">
        <template #header>
          <div class="card-header-row">
            <span>合同信息补充</span>
          </div>
        </template>
        <div class="detail-info-grid">
          <div class="detail-info-block">
            <div class="detail-info-title">合同概要</div>
            <div class="remark-box">{{ detailSummary || "暂无合同概要" }}</div>
          </div>
          <div class="detail-info-block">
            <div class="detail-info-title">补充备注</div>
            <div class="remark-box">{{ detailRemark || "暂无补充备注" }}</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="detail-section">
        <template #header>
          <div class="card-header-row">
            <span>收付款计划</span>
          </div>
        </template>
        <el-table v-if="detailPaymentPlans.length" :data="detailPaymentPlans" stripe>
          <el-table-column prop="index" label="#" width="60" />
          <el-table-column prop="phase" label="节点名称" min-width="180" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="amount" label="金额" width="140">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column prop="planDate" label="计划日期" width="140" />
        </el-table>
        <el-empty v-else description="暂无收付款计划" :image-size="68" />
      </el-card>

      <el-card shadow="never" class="detail-section">
        <template #header>
          <div class="card-header-row">
            <span>合同附件</span>
            <el-upload
              :show-file-list="false"
              :auto-upload="false"
              :on-change="handleUploadAttachment"
            >
              <el-button type="primary" plain>上传附件</el-button>
            </el-upload>
          </div>
        </template>

        <el-table :data="detailData.attachments" stripe>
          <el-table-column prop="fileName" label="文件名" min-width="220" />
          <el-table-column prop="fileType" label="类型" width="180" />
          <el-table-column prop="uploadedAt" label="上传时间" width="190" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button link type="primary" @click="previewAttachment(row.id)">查看</el-button>
              <el-button link type="success" @click="downloadAttachment(row.id)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </el-drawer>

  <el-dialog v-model="dialogVisible" title="新增合同" width="980px">
    <el-form :model="form" label-width="100px" class="contract-create-form">
      <div class="form-section">
        <h4>基本信息</h4>
        <div class="form-grid-3">
          <el-form-item label="合同编号">
            <el-input v-model="form.contractNo" placeholder="请输入合同编号" />
          </el-form-item>
          <el-form-item label="合同名称">
            <el-input v-model="form.contractTitle" placeholder="请输入合同名称" />
          </el-form-item>
          <el-form-item label="合同类型">
            <el-select v-model="form.contractType">
              <el-option v-for="item in contractTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>

          <el-form-item label="我方角色">
            <el-select v-model="form.partyRole">
              <el-option label="甲方" value="甲方" />
              <el-option label="乙方" value="乙方" />
            </el-select>
          </el-form-item>
          <el-form-item label="合同状态">
            <el-select v-model="form.contractStatusDraft">
              <el-option v-for="item in contractStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="付款状态">
            <el-select v-model="form.paymentStatus">
              <el-option label="未付款" value="UNPAID" />
              <el-option label="部分付款" value="PARTIAL" />
              <el-option label="已完成" value="PAID" />
            </el-select>
          </el-form-item>

          <el-form-item label="相对方">
            <el-select
              v-model="form.supplierId"
              filterable
              clearable
              placeholder="请选择合作方"
              @change="handleSupplierChange"
            >
              <el-option
                v-for="item in supplierOptions"
                :key="item.id"
                :label="`${item.supplierName}（${item.supplierCode}）`"
                :value="String(item.id)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="相对方名称">
            <el-input v-model="form.supplierName" placeholder="可手动填写相对方名称" />
          </el-form-item>
          <el-form-item label="项目名称">
            <el-input v-model="form.projectName" placeholder="请输入项目名称" />
          </el-form-item>

          <el-form-item label="项目ID">
            <el-input v-model="form.projectId" placeholder="请输入项目ID" />
          </el-form-item>
          <el-form-item label="询价单ID">
            <el-input v-model="form.inquiryId" placeholder="可选" />
          </el-form-item>
          <el-form-item label="税率">
            <el-select v-model="form.taxRate">
              <el-option
                v-for="item in taxRateOptions"
                :key="item"
                :label="`${item}%`"
                :value="String(item)"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="合同金额（含税）">
            <el-input v-model="form.amountTaxInclusive" placeholder="请输入含税金额" />
          </el-form-item>
          <el-form-item label="合同金额（不含税）">
            <el-input :model-value="amountTaxExclusiveDisplay" readonly />
          </el-form-item>
          <el-form-item label="签订日期">
            <el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>

          <el-form-item label="生效日期">
            <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="到期日期">
            <el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
        </div>
      </div>

      <div class="form-section">
        <h4>补充说明</h4>
        <div class="form-grid-2">
          <el-form-item label="合同概要">
            <el-input v-model="form.summary" type="textarea" :rows="4" placeholder="请输入合同概要" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="4" placeholder="请输入备注" />
          </el-form-item>
        </div>
        <div class="amount-summary-card">
          <div class="amount-summary-item">
            <span>不含税金额</span>
            <strong>¥{{ amountTaxExclusiveDisplay || "0.00" }}</strong>
          </div>
          <div class="amount-summary-item">
            <span>税额</span>
            <strong>¥{{ taxAmountDisplay }}</strong>
          </div>
          <div class="amount-summary-item">
            <span>合同类型 / 角色</span>
            <strong class="amount-summary-text">{{ form.contractType }} / {{ form.partyRole }}</strong>
          </div>
          <div class="amount-summary-item">
            <span>当前状态</span>
            <strong class="amount-summary-text">{{ contractStatusLabel(form.contractStatusDraft) }}</strong>
          </div>
        </div>
      </div>

      <div class="form-section">
        <div class="panel-head">
          <h4>收付款计划</h4>
          <el-button type="primary" plain @click="addPaymentPlan">新增节点</el-button>
        </div>
        <div v-if="paymentPlans.length" class="plan-list">
          <div v-for="plan in paymentPlans" :key="plan.id" class="plan-row">
            <el-input v-model="plan.phase" placeholder="节点名称" />
            <el-select v-model="plan.type">
              <el-option label="应收" value="应收" />
              <el-option label="应付" value="应付" />
            </el-select>
            <el-input v-model="plan.amount" placeholder="金额" />
            <el-date-picker v-model="plan.planDate" type="date" value-format="YYYY-MM-DD" />
            <el-button type="danger" plain @click="removePaymentPlan(plan.id)">删除</el-button>
          </div>
        </div>
        <el-empty v-else description="还没有收付款节点，点击“新增节点”开始配置。" :image-size="60" />
      </div>

      <div class="form-section">
        <h4>合同附件</h4>
        <el-upload
          multiple
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handlePendingAttachmentChange"
        >
          <el-button type="primary" plain>选择附件</el-button>
        </el-upload>
        <div class="upload-tip">支持多附件上传，创建合同后会自动一并上传。</div>
        <div v-if="pendingFiles.length" class="pending-file-list">
          <div v-for="file in pendingFiles" :key="file.uid" class="pending-file-item">
            <div>
              <strong>{{ file.name }}</strong>
              <div class="muted-text">{{ formatFileSize(file.size || 0) }}</div>
            </div>
            <el-button link type="danger" @click="removePendingAttachment(file.uid)">移除</el-button>
          </div>
        </div>
      </div>
    </el-form>
    <template #footer>
      <div class="drawer-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">创建合同</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import type { UploadFile } from "element-plus";
import { contractApi } from "@/api/contract";
import { contractAttachmentApi } from "@/api/contractAttachment";
import { supplierApi } from "@/api/supplier";

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const rows = ref<any[]>([]);
const reminders = ref<any[]>([]);
const detailData = ref<any | null>(null);
const selectedContractId = ref<number | null>(null);
const filterType = ref("all");
const supplierOptions = ref<any[]>([]);
const pendingFiles = ref<UploadFile[]>([]);

const contractTypeOptions = ["销售", "采购", "服务", "劳务", "租赁"];
const contractStatusOptions = [
  { label: "草稿", value: "DRAFT" },
  { label: "已生效", value: "ACTIVE" },
  { label: "已完结", value: "FINISHED" },
];
const taxRateOptions = [0, 1, 3, 6, 9, 13];

const query = reactive({ keyword: "", contractStatus: "" });
const form = reactive({
  contractNo: "",
  projectId: "",
  projectName: "",
  supplierId: "",
  supplierName: "",
  inquiryId: "",
  contractTitle: "",
  amountTaxInclusive: "",
  taxRate: "6",
  signDate: "",
  effectiveDate: "",
  expireDate: "",
  paymentStatus: "UNPAID",
  contractType: "销售",
  partyRole: "甲方",
  contractStatusDraft: "DRAFT",
  summary: "",
  remark: "",
});

const paymentPlans = ref<Array<{ id: number; phase: string; type: string; amount: string; planDate: string }>>([]);
let paymentPlanSeed = 1;

const reminderById = computed(() => {
  const map = new Map<number, any>();
  for (const item of reminders.value) {
    map.set(item.contractId, item);
  }
  return map;
});

const filteredRows = computed(() => {
  const mapped = rows.value.map((item) => ({
    ...item,
    reminderStatus: reminderById.value.get(item.id)?.reminderStatus || "正常",
  }));
  if (filterType.value === "overdue") {
    return mapped.filter((item) => item.reminderStatus === "已逾期");
  }
  if (filterType.value === "upcoming") {
    return mapped.filter((item) => item.reminderStatus === "近7天");
  }
  return mapped;
});

const overdueCount = computed(() => reminders.value.filter((item) => item.reminderStatus === "已逾期").length);
const upcomingCount = computed(() => reminders.value.filter((item) => item.reminderStatus === "近7天").length);
const amountTaxExclusiveDisplay = computed(() => {
  const inclusive = Number(form.amountTaxInclusive || 0);
  const taxRate = Number(form.taxRate || 0);
  if (!inclusive) return "";
  const exclusive = taxRate >= 0 ? inclusive / (1 + taxRate / 100) : inclusive;
  return exclusive.toFixed(2);
});
const taxAmountDisplay = computed(() => {
  const inclusive = Number(form.amountTaxInclusive || 0);
  const exclusive = Number(amountTaxExclusiveDisplay.value || 0);
  if (!inclusive) return "0.00";
  return Math.max(inclusive - exclusive, 0).toFixed(2);
});
const detailParsedRemark = computed(() => parseContractRemark(detailData.value?.contract?.remark || ""));
const detailSummary = computed(() => detailParsedRemark.value.summary);
const detailRemark = computed(() => detailParsedRemark.value.remark);
const detailPaymentPlans = computed(() => detailParsedRemark.value.paymentPlans);
const detailContractType = computed(() => detailParsedRemark.value.contractType);
const detailPartyRole = computed(() => detailParsedRemark.value.partyRole);
const detailAmountTaxExclusive = computed(() => {
  const inclusive = Number(detailData.value?.contract?.contractAmount || 0);
  const taxRate = Number(detailData.value?.contract?.taxRate || 0);
  if (!inclusive) return "0.00";
  const exclusive = taxRate >= 0 ? inclusive / (1 + taxRate / 100) : inclusive;
  return exclusive.toFixed(2);
});
const detailTaxAmount = computed(() => {
  const inclusive = Number(detailData.value?.contract?.contractAmount || 0);
  const exclusive = Number(detailAmountTaxExclusive.value || 0);
  if (!inclusive) return "0.00";
  return Math.max(inclusive - exclusive, 0).toFixed(2);
});

function parseContractRemark(rawRemark: string) {
  const text = String(rawRemark || "").trim();
  if (!text) {
    return {
      summary: "",
      remark: "",
      contractType: "",
      partyRole: "",
      paymentPlans: [] as Array<{ index: number; phase: string; type: string; amount: string; planDate: string }>,
    };
  }

  const normalized = text.replace(/\r\n/g, "\n");
  const contractTypeMatch = normalized.match(/合同类型：([^\n]*)/);
  const partyRoleMatch = normalized.match(/我方角色：([^\n]*)/);
  const summaryMatch = normalized.match(/合同概要：([\s\S]*?)(?:\n\n收付款计划：|\n\n|$)/);
  const paymentMatch = normalized.match(/收付款计划：\n([\s\S]*)$/);

  let remark = normalized;
  if (contractTypeMatch?.[0]) {
    remark = remark.replace(contractTypeMatch[0], "").trim();
  }
  if (partyRoleMatch?.[0]) {
    remark = remark.replace(partyRoleMatch[0], "").trim();
  }
  if (summaryMatch?.[0]) {
    remark = remark.replace(summaryMatch[0], "").trim();
  }
  if (paymentMatch?.[0]) {
    remark = remark.replace(paymentMatch[0], "").trim();
  }

  const paymentPlans = (paymentMatch?.[1] || "")
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line, index) => {
      const cleaned = line.replace(/^\d+\.\s*/, "");
      const [phase = "", type = "", amount = "", planDate = ""] = cleaned.split(" / ").map((item) => item.trim());
      return {
        index: index + 1,
        phase: phase || "未命名节点",
        type: type || "-",
        amount: amount || "0",
        planDate: planDate || "-",
      };
    });

  return {
    contractType: contractTypeMatch?.[1]?.trim() || "",
    partyRole: partyRoleMatch?.[1]?.trim() || "",
    summary: summaryMatch?.[1]?.trim() || "",
    remark: remark.trim(),
    paymentPlans,
  };
}

function contractStatusLabel(value?: string) {
  return contractStatusOptions.find((item) => item.value === value)?.label || value || "-";
}

function reminderTagType(status?: string) {
  if (status === "已逾期") return "danger";
  if (status === "近7天") return "warning";
  if (status === "后续") return "info";
  return "success";
}

async function loadData() {
  loading.value = true;
  try {
    const [contractRes, reminderRes] = await Promise.all([
      contractApi.list(query),
      contractApi.reminders(),
    ]);
    rows.value = contractRes.data || [];
    reminders.value = reminderRes.data || [];
  } finally {
    loading.value = false;
  }
}

async function loadSuppliers() {
  const res = await supplierApi.list({ pageSize: 200 });
  supplierOptions.value = res.data || [];
}

async function loadReminders() {
  const res = await contractApi.reminders();
  reminders.value = res.data || [];
}

async function loadDetail(id: number) {
  const res = await contractApi.detail(id);
  detailData.value = res.data;
  selectedContractId.value = id;
  detailVisible.value = true;
}

function handlePickContract(row: any) {
  loadDetail(row.id);
}

function handlePickReminder(row: any) {
  loadDetail(row.contractId);
}

function openCreate() {
  Object.assign(form, {
    contractNo: generateContractNo(),
    projectId: "",
    projectName: "",
    supplierId: "",
    supplierName: "",
    inquiryId: "",
    contractTitle: "",
    amountTaxInclusive: "",
    taxRate: "6",
    signDate: "",
    effectiveDate: "",
    expireDate: "",
    paymentStatus: "UNPAID",
    contractType: "销售",
    partyRole: "甲方",
    contractStatusDraft: "DRAFT",
    summary: "",
    remark: "",
  });
  paymentPlans.value = [];
  pendingFiles.value = [];
  dialogVisible.value = true;
}

function handleSupplierChange(value: string) {
  const matched = supplierOptions.value.find((item) => String(item.id) === String(value));
  if (matched) {
    form.supplierName = matched.supplierName;
  }
}

function addPaymentPlan() {
  paymentPlans.value.push({
    id: paymentPlanSeed++,
    phase: "",
    type: "应收",
    amount: "",
    planDate: "",
  });
}

function removePaymentPlan(id: number) {
  paymentPlans.value = paymentPlans.value.filter((item) => item.id !== id);
}

function handlePendingAttachmentChange(uploadFile: UploadFile) {
  if (!pendingFiles.value.some((item) => item.uid === uploadFile.uid)) {
    pendingFiles.value.push(uploadFile);
  }
}

function removePendingAttachment(uid: number | string) {
  pendingFiles.value = pendingFiles.value.filter((item) => item.uid !== uid);
}

function formatFileSize(size: number) {
  if (!size) return "0 B";
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}

function generateContractNo() {
  const now = new Date();
  const datePart = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, "0")}${String(now.getDate()).padStart(2, "0")}`;
  const timePart = `${String(now.getHours()).padStart(2, "0")}${String(now.getMinutes()).padStart(2, "0")}${String(now.getSeconds()).padStart(2, "0")}`;
  return `SUP-HT-${datePart}-${timePart}`;
}

async function submitForm() {
  if (!form.contractNo || !form.contractTitle || !form.supplierName) {
    ElMessage.warning("请先填写合同编号、合同名称和相对方");
    return;
  }
  saving.value = true;
  try {
    const summaryText = form.summary?.trim();
    const paymentPlanText = paymentPlans.value.length
      ? paymentPlans.value
          .map((item, index) => `${index + 1}. ${item.phase || "未命名节点"} / ${item.type} / ${item.amount || 0} / ${item.planDate || "未设置日期"}`)
          .join("\n")
      : "";
    const mergedRemark = [
      `合同类型：${form.contractType}`,
      `我方角色：${form.partyRole}`,
      form.remark?.trim(),
      summaryText ? `合同概要：${summaryText}` : "",
      paymentPlanText ? `收付款计划：\n${paymentPlanText}` : "",
    ]
      .filter(Boolean)
      .join("\n\n");
    const createRes = await contractApi.create({
      contractNo: form.contractNo,
      projectId: Number(form.projectId || 0) || null,
      projectName: form.projectName,
      supplierId: Number(form.supplierId || 0) || 1,
      supplierName: form.supplierName,
      inquiryId: Number(form.inquiryId || 0) || null,
      contractTitle: form.contractTitle,
      contractAmount: Number(form.amountTaxInclusive || 0),
      taxRate: Number(form.taxRate || 0),
      signDate: form.signDate,
      effectiveDate: form.effectiveDate,
      expireDate: form.expireDate,
      contractStatus: form.contractStatusDraft,
      paymentStatus: form.paymentStatus,
      remark: mergedRemark,
    });
    const createdId = createRes.data?.id;
    if (createdId && pendingFiles.value.length) {
      for (const item of pendingFiles.value) {
        if (item.raw) {
          await contractAttachmentApi.upload(createdId, item.raw);
        }
      }
    }
    ElMessage.success("合同已创建");
    dialogVisible.value = false;
    await loadData();
  } finally {
    saving.value = false;
  }
}

async function handleUploadAttachment(uploadFile: UploadFile) {
  if (!selectedContractId.value || !uploadFile.raw) {
    ElMessage.warning("请先打开合同详情，再上传附件");
    return;
  }
  await contractAttachmentApi.upload(selectedContractId.value, uploadFile.raw);
  ElMessage.success("附件上传成功");
  await loadDetail(selectedContractId.value);
}

function previewAttachment(id: number) {
  window.open(contractAttachmentApi.previewUrl(id), "_blank");
}

function downloadAttachment(id: number) {
  window.open(contractAttachmentApi.downloadUrl(id), "_blank");
}

onMounted(() => {
  loadData();
  loadSuppliers();
});
</script>

<style scoped>
.contract-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contract-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  border-radius: 16px;
}

.stat-card :deep(.el-card__body) {
  padding: 18px 20px;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
}

.stat-value {
  margin-top: 8px;
  font-size: 30px;
  font-weight: 700;
  color: #0f172a;
}

.stat-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.danger {
  background: linear-gradient(180deg, #fff1f2 0%, #ffffff 100%);
}

.warn {
  background: linear-gradient(180deg, #fffbeb 0%, #ffffff 100%);
}

.content-grid {
  margin: 0;
}

.reminder-card,
.detail-section {
  border-radius: 16px;
}

.contract-create-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-section {
  padding: 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.form-section h4 {
  margin: 0 0 16px;
  font-size: 15px;
  color: #0f172a;
}

.form-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px 16px;
}

.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-head h4 {
  margin: 0;
}

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-row {
  display: grid;
  grid-template-columns: 1.3fr 0.8fr 0.8fr 1fr auto;
  gap: 10px;
}

.upload-tip,
.muted-text {
  margin-top: 10px;
  font-size: 12px;
  color: #64748b;
}

.pending-file-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pending-file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: #eff6ff;
}

.pending-file-item strong {
  color: #0f172a;
}

.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reminder-item {
  text-align: left;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #f8fafc;
  cursor: pointer;
}

.reminder-item:hover {
  border-color: #93c5fd;
  background: #eff6ff;
}

.reminder-title {
  font-weight: 600;
  color: #0f172a;
}

.reminder-meta,
.reminder-footer {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.reminder-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.detail-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.detail-sub {
  margin-top: 6px;
  color: #64748b;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.detail-desc {
  margin-bottom: 16px;
}

.remark-box {
  min-height: 72px;
  color: #334155;
  white-space: pre-wrap;
  line-height: 1.7;
}

.detail-info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.detail-info-block {
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #f8fbff;
}

.detail-info-title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.amount-summary-card {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 8px;
}

.amount-summary-item {
  padding: 14px 16px;
  border: 1px solid #dbeafe;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #eff6ff 100%);
}

.amount-summary-item span {
  display: block;
  font-size: 12px;
  color: #64748b;
}

.amount-summary-item strong {
  display: block;
  margin-top: 8px;
  font-size: 24px;
  color: #0f172a;
}

.amount-summary-text {
  font-size: 18px !important;
  line-height: 1.5;
}

@media (max-width: 960px) {
  .contract-stats {
    grid-template-columns: 1fr;
  }

  .form-grid-3,
  .form-grid-2,
  .plan-row,
  .detail-info-grid,
  .amount-summary-card {
    grid-template-columns: 1fr;
  }
}
</style>
