<template>
  <div class="page-shell">
    <el-card>
      <template #header>
        <div class="card-header-row">
          <span>项目人员管理</span>
          <div style="display: flex; gap: 8px">
            <el-button type="primary" @click="openCreate">新增成员</el-button>
            <el-button @click="loadData">刷新</el-button>
          </div>
        </div>
      </template>
      <el-table :data="rows" stripe>
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="projectDeptName" label="所属项目部" width="160" />
        <el-table-column prop="employeeName" label="员工姓名" width="120" />
        <el-table-column prop="positionName" label="岗位" width="140" />
        <el-table-column prop="arrivalDate" label="到岗时间" width="130" />
        <el-table-column prop="leaveDate" label="离岗时间" width="130" />
        <el-table-column prop="onDuty" label="是否在岗" width="110">
          <template #default="{ row }">
            <el-tag :type="row.onDuty ? 'success' : 'info'" effect="light">{{ row.onDuty ? "在岗" : "离岗" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeRow(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form :model="form" label-width="100px">
        <div class="form-grid-2">
          <el-form-item label="所属项目">
            <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" @change="handleProjectChange">
              <el-option v-for="item in projectOptions" :key="item.id" :label="item.projectName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属项目部"><el-input v-model="form.projectDeptName" /></el-form-item>
          <el-form-item label="员工姓名"><el-input v-model="form.employeeName" /></el-form-item>
          <el-form-item label="岗位"><el-input v-model="form.positionName" /></el-form-item>
          <el-form-item label="到岗时间">
            <el-date-picker v-model="form.arrivalDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="离岗时间">
            <el-date-picker v-model="form.leaveDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="是否在岗">
            <el-switch v-model="onDutyBool" />
          </el-form-item>
        </div>
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
import { projectApi } from "@/api/project";

const rows = ref<any[]>([]);
const projectOptions = ref<any[]>([]);
const dialogVisible = ref(false);
const submitting = ref(false);
const currentId = ref<number | null>(null);
const mode = ref<"create" | "edit">("create");
const form = reactive({
  projectId: undefined as number | undefined,
  projectDeptName: "",
  employeeName: "",
  positionName: "",
  arrivalDate: "",
  leaveDate: "",
  onDuty: 1,
});
const onDutyBool = computed({
  get: () => form.onDuty === 1,
  set: (value: boolean) => {
    form.onDuty = value ? 1 : 0;
  },
});
const dialogTitle = computed(() => (mode.value === "create" ? "新增项目成员" : "编辑项目成员"));

async function loadData() {
  const res = await projectApi.memberList();
  rows.value = res.data || [];
}

async function loadProjects() {
  const res = await projectApi.projectList();
  projectOptions.value = res.data || [];
}

function handleProjectChange(projectId: number) {
  const current = projectOptions.value.find((item) => item.id === projectId);
  if (current) {
    form.projectDeptName = `${current.projectName}项目部`;
  }
}

function resetForm() {
  Object.assign(form, {
    projectId: undefined,
    projectDeptName: "",
    employeeName: "",
    positionName: "",
    arrivalDate: "",
    leaveDate: "",
    onDuty: 1,
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
    projectId: row.projectId,
    projectDeptName: row.projectDeptName || "",
    employeeName: row.employeeName || "",
    positionName: row.positionName || "",
    arrivalDate: row.arrivalDate || "",
    leaveDate: row.leaveDate || "",
    onDuty: row.onDuty ? 1 : 0,
  });
  dialogVisible.value = true;
}

async function submitForm() {
  if (!form.projectId) {
    ElMessage.warning("请选择所属项目");
    return;
  }
  if (!form.employeeName.trim()) {
    ElMessage.warning("请填写员工姓名");
    return;
  }
  if (!form.positionName.trim()) {
    ElMessage.warning("请填写岗位");
    return;
  }
  submitting.value = true;
  try {
    if (mode.value === "create") {
      await projectApi.createMember({ ...form });
      ElMessage.success("成员已新增");
    } else if (currentId.value != null) {
      await projectApi.updateMember(currentId.value, { ...form });
      ElMessage.success("成员已更新");
    }
    dialogVisible.value = false;
    await loadData();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || "成员保存失败");
  } finally {
    submitting.value = false;
  }
}

async function removeRow(id: number) {
  await ElMessageBox.confirm("确认删除该项目成员吗？", "删除确认", { type: "warning" });
  await projectApi.deleteMember(id);
  ElMessage.success("成员已删除");
  await loadData();
}

onMounted(async () => {
  await Promise.all([loadData(), loadProjects()]);
});
</script>
