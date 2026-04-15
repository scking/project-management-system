<template>
  <div class="page-shell">
    <el-card>
      <template #header>
        <div class="card-header-row">
          <span>任务分配管理</span>
          <el-button type="primary" @click="openCreate">新增任务</el-button>
        </div>
      </template>
      <div class="toolbar-row">
        <el-input v-model="query.keyword" placeholder="搜索任务标题 / 项目 / 责任人" clearable class="toolbar-input" />
        <el-select v-model="query.status" clearable placeholder="任务状态" class="toolbar-select">
          <el-option v-for="item in taskStatusOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
      <el-table :data="rows" stripe>
        <el-table-column prop="taskCode" label="任务编号" width="160" />
        <el-table-column prop="taskTitle" label="任务标题" min-width="220" />
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="assigneeName" label="指派对象" width="120" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column prop="requiredFinishDate" label="要求完成时间" width="140" />
        <el-table-column prop="taskStatus" label="状态" width="110" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-select
              :model-value="row.taskStatus"
              size="small"
              style="width: 130px"
              @change="(value: string) => updateStatus(row, value)"
            >
              <el-option v-for="item in taskStatusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增任务" width="760px">
      <el-form :model="form" label-width="110px">
        <div class="form-grid-2">
          <el-form-item label="所属项目">
            <el-input v-model="form.projectName" />
          </el-form-item>
          <el-form-item label="所属项目部">
            <el-input v-model="form.projectDeptName" />
          </el-form-item>
          <el-form-item label="任务标题">
            <el-input v-model="form.taskTitle" />
          </el-form-item>
          <el-form-item label="指派对象">
            <el-input v-model="form.assigneeName" />
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="form.priority">
              <el-option label="高" value="高" />
              <el-option label="中" value="中" />
              <el-option label="低" value="低" />
            </el-select>
          </el-form-item>
          <el-form-item label="要求完成时间">
            <el-date-picker v-model="form.requiredFinishDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
        </div>
        <el-form-item label="任务内容">
          <el-input v-model="form.taskContent" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { taskApi } from "@/api/task";

const taskStatusOptions = ["待接收", "进行中", "已完成", "未完成", "已延期", "已关闭"];
const query = reactive({ keyword: "", status: "" });
const rows = ref<any[]>([]);
const dialogVisible = ref(false);
const form = reactive({
  projectName: "",
  projectDeptName: "",
  taskTitle: "",
  assigneeName: "",
  priority: "中",
  requiredFinishDate: "",
  taskContent: "",
  remark: "",
});

async function loadData() {
  const res = await taskApi.list(query);
  rows.value = res.data || [];
}

function openCreate() {
  Object.assign(form, {
    projectName: "",
    projectDeptName: "",
    taskTitle: "",
    assigneeName: "",
    priority: "中",
    requiredFinishDate: "",
    taskContent: "",
    remark: "",
  });
  dialogVisible.value = true;
}

async function submitForm() {
  await taskApi.create(form);
  ElMessage.success("任务已创建");
  dialogVisible.value = false;
  await loadData();
}

async function updateStatus(row: any, status: string) {
  await taskApi.updateStatus(row.id, { taskStatus: status });
  ElMessage.success("任务状态已更新");
  await loadData();
}

onMounted(loadData);
</script>
