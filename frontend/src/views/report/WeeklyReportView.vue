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
      <el-table :data="rows" stripe>
        <el-table-column prop="reportCode" label="周报编号" width="170" />
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="reportUserName" label="填报人" width="120" />
        <el-table-column prop="weekLabel" label="周次" width="160" />
        <el-table-column prop="reportDate" label="填报日期" width="130" />
        <el-table-column prop="completedCount" label="已完成事项" width="110" />
        <el-table-column prop="unfinishedCount" label="未完成事项" width="110" />
        <el-table-column prop="nextWeekPlanCount" label="下周计划" width="100" />
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
            <el-input v-model="form.reportUserName" />
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { EditPen, Search } from "@element-plus/icons-vue";
import { projectApi } from "@/api/project";
import { weeklyReportApi } from "@/api/weeklyReport";

const query = reactive({ keyword: "" });
const rows = ref<any[]>([]);
const projectOptions = ref<any[]>([]);
const dialogVisible = ref(false);
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

async function loadData() {
  const res = await weeklyReportApi.list(query);
  rows.value = res.data || [];
}

async function loadProjects() {
  const res = await projectApi.projectList();
  projectOptions.value = res.data || [];
}

function handleProjectChange(projectId: number) {
  const current = projectOptions.value.find((item) => item.id === projectId);
  form.projectName = current?.projectName || "";
  if (!form.projectDeptName) {
    form.projectDeptName = current?.projectName ? `${current.projectName}项目部` : "";
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
