<template>
  <div class="saas-list-page">
    <div class="saas-page-header">
      <div>
        <h2 class="saas-page-title">项目周报管理</h2>
        <p class="saas-page-subtitle">周度汇报完成事项、未完成原因与下周计划</p>
      </div>
      <div class="saas-row-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><EditPen /></el-icon>
          <span>填写周报</span>
        </el-button>
      </div>
    </div>

    <div class="saas-toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索周报编号 / 填报人 / 项目"
        clearable
        class="toolbar-input"
        :prefix-icon="Search"
        @keyup.enter="loadData"
      />
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <section class="saas-card is-flush">
      <el-table :data="rows" stripe @row-click="openDetail">
        <el-table-column prop="reportCode" label="周报编号" width="170" />
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="reportUserName" label="填报人" width="120" />
        <el-table-column prop="weekLabel" label="周次" width="160" />
        <el-table-column prop="reportDate" label="填报日期" width="130" />
        <el-table-column prop="completedCount" label="已完成事项" width="110" />
        <el-table-column prop="unfinishedCount" label="未完成事项" width="110" />
        <el-table-column prop="nextWeekPlanCount" label="下周计划" width="100" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" title="填写周报" width="860px">
      <el-form :model="form" label-width="110px">
        <div class="form-grid-2">
          <el-form-item label="所属项目">
            <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" @change="handleProjectChange">
              <el-option v-for="item in projectOptions" :key="item.id" :label="item.projectName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属项目部">
            <el-input v-model="form.projectDeptName" />
          </el-form-item>
          <el-form-item label="填报人">
            <el-select v-model="form.reportUserName" placeholder="请选择项目成员" style="width: 100%" filterable clearable>
              <el-option
                v-for="item in filteredMemberOptions"
                :key="item.id"
                :label="`${item.employeeName}（${item.positionName}）`"
                :value="item.employeeName"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="周次">
            <el-input v-model="form.weekLabel" placeholder="例如 2026年第16周" />
          </el-form-item>
        </div>
        <el-form-item label="本周已完成工作">
          <el-input v-model="form.completedWorkText" type="textarea" :rows="4" placeholder="可逐条填写，换行分隔" />
        </el-form-item>
        <el-form-item label="本周未完成工作">
          <el-input v-model="form.unfinishedWorkText" type="textarea" :rows="4" placeholder="可逐条填写，换行分隔" />
        </el-form-item>
        <el-form-item label="未完成原因">
          <el-input v-model="form.unfinishedReasonText" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="下周计划">
          <el-input v-model="form.nextWeekPlanText" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="协调支持事项">
          <el-input v-model="form.supportNeeds" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">提交周报</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="周报详情" size="50%">
      <div v-if="detailLoading" style="padding: 40px; text-align: center">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p style="margin-top: 12px; color: #888">加载中...</p>
      </div>
      <template v-else-if="currentReport">
        <el-descriptions :column="2" border size="small" style="margin-bottom: 20px">
          <el-descriptions-item label="周报编号">{{ currentReport.reportCode }}</el-descriptions-item>
          <el-descriptions-item label="所属项目">{{ currentReport.projectName }}</el-descriptions-item>
          <el-descriptions-item label="项目部">{{ currentReport.projectDeptName || "-" }}</el-descriptions-item>
          <el-descriptions-item label="填报人">{{ currentReport.reportUserName }}</el-descriptions-item>
          <el-descriptions-item label="周次">{{ currentReport.weekLabel }}</el-descriptions-item>
          <el-descriptions-item label="填报日期">{{ currentReport.reportDate }}</el-descriptions-item>
        </el-descriptions>
        <div class="report-detail-blocks">
          <div class="report-block">
            <div class="report-block-label">本周已完成工作</div>
            <div class="report-block-content">{{ currentReport.completedWorkText || "暂无" }}</div>
          </div>
          <div class="report-block">
            <div class="report-block-label">本周未完成工作</div>
            <div class="report-block-content">{{ currentReport.unfinishedWorkText || "暂无" }}</div>
          </div>
          <div class="report-block">
            <div class="report-block-label">未完成原因</div>
            <div class="report-block-content">{{ currentReport.unfinishedReasonText || "暂无" }}</div>
          </div>
          <div class="report-block">
            <div class="report-block-label">下周计划</div>
            <div class="report-block-content">{{ currentReport.nextWeekPlanText || "暂无" }}</div>
          </div>
          <div class="report-block">
            <div class="report-block-label">协调支持事项</div>
            <div class="report-block-content">{{ currentReport.supportNeeds || "暂无" }}</div>
          </div>
          <div class="report-block">
            <div class="report-block-label">备注</div>
            <div class="report-block-content">{{ currentReport.remark || "暂无" }}</div>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { EditPen, Loading, Search } from "@element-plus/icons-vue";
import { projectApi } from "@/api/project";
import { weeklyReportApi } from "@/api/weeklyReport";

const query = reactive({ keyword: "" });
const rows = ref<any[]>([]);
const projectOptions = ref<any[]>([]);
const memberOptions = ref<any[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const detailLoading = ref(false);
const currentReport = ref<any | null>(null);
const form = reactive({
  projectId: undefined as number | undefined,
  projectName: "",
  projectDeptName: "",
  reportUserName: "",
  weekLabel: "",
  completedWorkText: "",
  unfinishedWorkText: "",
  unfinishedReasonText: "",
  nextWeekPlanText: "",
  supportNeeds: "",
  remark: "",
});
const filteredMemberOptions = computed(() =>
  memberOptions.value.filter((item) => !form.projectId || item.projectId === form.projectId),
);

async function loadData() {
  const res = await weeklyReportApi.list(query);
  rows.value = res.data || [];
}

async function loadProjects() {
  const [projectRes, memberRes] = await Promise.all([projectApi.projectList(), projectApi.memberList()]);
  projectOptions.value = projectRes.data || [];
  memberOptions.value = memberRes.data || [];
}

function handleProjectChange(projectId: number) {
  const current = projectOptions.value.find((item) => item.id === projectId);
  form.projectName = current?.projectName || "";
  form.projectDeptName = current?.projectName ? `${current.projectName}项目部` : "";
  form.reportUserName = "";
}

async function openDetail(row: any) {
  currentReport.value = null;
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await weeklyReportApi.getById(row.id);
    currentReport.value = res.data || row;
  } catch {
    currentReport.value = row;
  } finally {
    detailLoading.value = false;
  }
}

function openCreate() {
  Object.assign(form, {
    projectId: undefined,
    projectName: "",
    projectDeptName: "",
    reportUserName: "",
    weekLabel: "",
    completedWorkText: "",
    unfinishedWorkText: "",
    unfinishedReasonText: "",
    nextWeekPlanText: "",
    supportNeeds: "",
    remark: "",
  });
  dialogVisible.value = true;
}

async function submitForm() {
  if (!form.projectId) {
    ElMessage.warning("请先选择所属项目");
    return;
  }
  if (!form.reportUserName.trim()) {
    ElMessage.warning("请填写填报人");
    return;
  }
  if (!form.weekLabel.trim()) {
    ElMessage.warning("请填写周次");
    return;
  }
  try {
    await weeklyReportApi.create(form);
    ElMessage.success("周报已提交");
    dialogVisible.value = false;
    await loadData();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || "周报提交失败");
  }
}

onMounted(async () => {
  await Promise.all([loadData(), loadProjects()]);
});
</script>

<style scoped>
.report-detail-blocks { display: flex; flex-direction: column; gap: 16px; }
.report-block { background: #f9f9fb; border: 1px solid #e8e8ed; border-radius: 8px; padding: 14px 16px; }
.report-block-label { font-size: 13px; font-weight: 600; color: #555; margin-bottom: 8px; }
.report-block-content { font-size: 14px; color: #333; white-space: pre-wrap; line-height: 1.7; min-height: 24px; }
</style>
