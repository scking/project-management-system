<template>
  <div class="saas-list-page">
    <div class="saas-page-header">
      <div>
        <h2 class="saas-page-title">请假审批管理</h2>
        <p class="saas-page-subtitle">发起请假申请,按流程进行项目经理/部门经理两级审批</p>
      </div>
      <div class="saas-row-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          <span>发起请假</span>
        </el-button>
      </div>
    </div>

    <div class="saas-toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索请假单编号 / 申请人 / 项目"
        clearable
        class="toolbar-input"
        :prefix-icon="Search"
        @keyup.enter="loadData"
      />
      <el-select v-model="query.status" clearable placeholder="审批状态" class="toolbar-select">
        <el-option label="待项目经理审批" value="待项目经理审批" />
        <el-option label="待部门经理审批" value="待部门经理审批" />
        <el-option label="已通过" value="已通过" />
        <el-option label="已驳回" value="已驳回" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <section class="saas-card is-flush">
      <el-table :data="rows" stripe>
        <el-table-column prop="leaveCode" label="请假单编号" width="170" />
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="leaveType" label="请假类型" width="110" />
        <el-table-column prop="dateRange" label="请假时间" min-width="180" />
        <el-table-column prop="leaveDays" label="天数" width="80" />
        <el-table-column prop="approvalStatus" label="审批状态" width="140" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="saas-row-actions">
              <el-button link type="primary" @click="openApprove(row)">审批</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" title="请假申请" width="720px">
      <el-form :model="form" label-width="110px">
        <div class="form-grid-2">
          <el-form-item label="所属项目">
            <el-select
              v-model="form.projectId"
              filterable
              placeholder="请选择项目"
              style="width: 100%"
              @change="handleProjectChange"
            >
              <el-option v-for="item in projectOptions" :key="item.id" :label="item.projectName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属项目部">
            <el-input v-model="form.projectDeptName" readonly />
          </el-form-item>
          <el-form-item label="申请人">
            <el-select
              v-model="form.applicantName"
              filterable
              clearable
              placeholder="请选择申请人"
              style="width: 100%"
            >
              <el-option
                v-for="item in filteredMemberOptions"
                :key="item.id"
                :label="`${item.employeeName}（${item.positionName}）`"
                :value="item.employeeName"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="请假类型">
            <el-select v-model="form.leaveType">
              <el-option v-for="item in leaveTypes" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
          <el-form-item label="请假天数">
            <el-input-number v-model="form.leaveDays" :min="0.5" :step="0.5" />
          </el-form-item>
        </div>
        <el-form-item label="请假原因">
          <el-input v-model="form.reason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="approveVisible" title="请假审批" width="520px">
      <el-form :model="approveForm" label-width="90px">
        <el-form-item label="审批动作">
          <el-radio-group v-model="approveForm.action">
            <el-radio label="同意">同意</el-radio>
            <el-radio label="退回">退回</el-radio>
            <el-radio label="驳回">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.comment" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApprove">提交审批</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { Plus, Search } from "@element-plus/icons-vue";
import { leaveApi } from "@/api/leave";
import { projectApi } from "@/api/project";

const leaveTypes = ["事假", "病假", "调休", "年假", "婚假", "丧假", "其他"];
const query = reactive({ keyword: "", status: "" });
const rows = ref<any[]>([]);
const projectOptions = ref<any[]>([]);
const memberOptions = ref<any[]>([]);
const dialogVisible = ref(false);
const approveVisible = ref(false);
const currentId = ref<number | null>(null);
const form = reactive({
  projectId: undefined as number | undefined,
  applicantName: "",
  projectName: "",
  projectDeptName: "",
  leaveType: "事假",
  startTime: "",
  endTime: "",
  leaveDays: 1,
  reason: "",
});
const filteredMemberOptions = computed(() =>
  memberOptions.value.filter((item) => !form.projectId || item.projectId === form.projectId),
);
const approveForm = reactive({
  action: "同意",
  comment: "",
});

async function loadData() {
  const res = await leaveApi.list(query);
  rows.value = res.data || [];
}

async function loadBaseOptions() {
  const [projectRes, memberRes] = await Promise.all([projectApi.projectList(), projectApi.memberList()]);
  projectOptions.value = projectRes.data || [];
  memberOptions.value = memberRes.data || [];
}

function handleProjectChange(projectId: number) {
  const current = projectOptions.value.find((item) => item.id === projectId);
  form.projectName = current?.projectName || "";
  form.projectDeptName = current?.projectName ? `${current.projectName}项目部` : "";
  form.applicantName = "";
}

function openCreate() {
  Object.assign(form, {
    projectId: undefined,
    applicantName: "",
    projectName: "",
    projectDeptName: "",
    leaveType: "事假",
    startTime: "",
    endTime: "",
    leaveDays: 1,
    reason: "",
  });
  dialogVisible.value = true;
}

async function submitForm() {
  if (!form.projectId) {
    ElMessage.warning("请选择所属项目");
    return;
  }
  if (!form.applicantName) {
    ElMessage.warning("请选择申请人");
    return;
  }
  await leaveApi.create(form);
  ElMessage.success("请假申请已提交");
  dialogVisible.value = false;
  await loadData();
}

function openApprove(row: any) {
  currentId.value = row.id;
  Object.assign(approveForm, { action: "同意", comment: "" });
  approveVisible.value = true;
}

async function submitApprove() {
  if (!currentId.value) return;
  await leaveApi.approve(currentId.value, approveForm);
  ElMessage.success("审批已处理");
  approveVisible.value = false;
  await loadData();
}

onMounted(async () => {
  await Promise.all([loadData(), loadBaseOptions()]);
});
</script>
