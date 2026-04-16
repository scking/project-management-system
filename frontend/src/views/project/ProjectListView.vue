<template>
  <div class="saas-list-page">
    <div class="saas-page-header">
      <div>
        <h2 class="saas-page-title">项目基础管理</h2>
        <p class="saas-page-subtitle">维护项目基础档案、建设单位、合同金额与进度状态</p>
      </div>
      <div class="saas-row-actions">
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </el-button>
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          <span>新增项目</span>
        </el-button>
      </div>
    </div>

    <div class="saas-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索项目编号 / 名称 / 地点 / 项目经理"
        clearable
        class="toolbar-input"
        :prefix-icon="Search"
        @keyup.enter="loadData"
      />
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <section class="saas-card is-flush">
      <el-table :data="rows" stripe>
        <el-table-column prop="projectCode" label="项目编号" width="150" />
        <el-table-column prop="projectName" label="项目名称" min-width="220" />
        <el-table-column prop="projectType" label="项目类型" width="140" />
        <el-table-column prop="location" label="项目地点" min-width="180" />
        <el-table-column prop="ownerOrg" label="建设单位" min-width="180" />
        <el-table-column prop="projectManagerName" label="项目经理" width="120" />
        <el-table-column prop="projectStatus" label="状态" width="120" />
        <el-table-column label="合同金额" width="140">
          <template #default="{ row }">¥{{ row.contractAmount || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="saas-row-actions">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="removeRow(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form :model="form" label-width="100px">
        <div class="form-grid-2">
          <el-form-item label="项目编号"><el-input v-model="form.projectCode" /></el-form-item>
          <el-form-item label="项目名称"><el-input v-model="form.projectName" /></el-form-item>
          <el-form-item label="项目类型"><el-input v-model="form.projectType" /></el-form-item>
          <el-form-item label="项目地点"><el-input v-model="form.location" /></el-form-item>
          <el-form-item label="建设单位"><el-input v-model="form.ownerOrg" /></el-form-item>
          <el-form-item label="合同金额"><el-input v-model="form.contractAmount" /></el-form-item>
          <el-form-item label="开始日期"><el-input v-model="form.startDate" placeholder="2026-04-15" /></el-form-item>
          <el-form-item label="计划完工"><el-input v-model="form.plannedFinishDate" placeholder="2026-12-31" /></el-form-item>
          <el-form-item label="项目状态"><el-input v-model="form.projectStatus" /></el-form-item>
          <el-form-item label="项目经理"><el-input v-model="form.projectManagerName" /></el-form-item>
        </div>
        <el-form-item label="项目说明">
          <el-input v-model="form.projectDesc" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Refresh, Search } from "@element-plus/icons-vue";
import { projectApi } from "@/api/project";

const keyword = ref("");
const rows = ref<any[]>([]);
const dialogVisible = ref(false);
const submitting = ref(false);
const currentId = ref<number | null>(null);
const mode = ref<"create" | "edit">("create");
const form = reactive({
  projectCode: "",
  projectName: "",
  projectType: "",
  location: "",
  ownerOrg: "",
  contractAmount: "",
  startDate: "",
  plannedFinishDate: "",
  projectStatus: "在建",
  projectManagerName: "",
  projectDesc: "",
});

const dialogTitle = computed(() => (mode.value === "create" ? "新增项目" : "编辑项目"));

async function loadData() {
  const res = await projectApi.projectList({ keyword: keyword.value });
  rows.value = res.data || [];
}

function resetForm() {
  Object.assign(form, {
    projectCode: "",
    projectName: "",
    projectType: "",
    location: "",
    ownerOrg: "",
    contractAmount: "",
    startDate: "",
    plannedFinishDate: "",
    projectStatus: "在建",
    projectManagerName: "",
    projectDesc: "",
  });
  currentId.value = null;
}

function openCreate() {
  mode.value = "create";
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: any) {
  mode.value = "edit";
  currentId.value = row.id;
  Object.assign(form, {
    projectCode: row.projectCode || "",
    projectName: row.projectName || "",
    projectType: row.projectType || "",
    location: row.location || "",
    ownerOrg: row.ownerOrg || "",
    contractAmount: row.contractAmount != null ? String(row.contractAmount) : "",
    startDate: row.startDate || "",
    plannedFinishDate: row.plannedFinishDate || "",
    projectStatus: row.projectStatus || "在建",
    projectManagerName: row.projectManagerName || "",
    projectDesc: row.projectDesc || "",
  });
  dialogVisible.value = true;
}

async function submitForm() {
  if (!form.projectCode.trim()) {
    ElMessage.warning("请填写项目编号");
    return;
  }
  if (!form.projectName.trim()) {
    ElMessage.warning("请填写项目名称");
    return;
  }
  submitting.value = true;
  try {
    if (mode.value === "create") {
      await projectApi.createProject({ ...form });
      ElMessage.success("项目已新增");
    } else if (currentId.value != null) {
      await projectApi.updateProject(currentId.value, { ...form });
      ElMessage.success("项目已更新");
    }
    dialogVisible.value = false;
    await loadData();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || "项目保存失败");
  } finally {
    submitting.value = false;
  }
}

async function removeRow(id: number) {
  await ElMessageBox.confirm("确认删除该项目吗？相关成员、周报、任务、请假也会一并删除。", "删除确认", { type: "warning" });
  await projectApi.deleteProject(id);
  ElMessage.success("项目已删除");
  await loadData();
}

onMounted(loadData);
</script>
