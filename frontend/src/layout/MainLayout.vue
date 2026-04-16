<template>
  <div class="saas-shell">
    <aside class="saas-aside">
      <div class="saas-brand">
        <div class="saas-brand-logo">项</div>
        <div>
          <div class="saas-brand-title">项目管理</div>
          <div class="saas-brand-subtitle">Project Management</div>
        </div>
      </div>

      <el-scrollbar class="saas-aside-scroll">
        <el-menu class="saas-side-menu" :default-active="route.path" @select="handleSelect">
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>工作台</template>
          </el-menu-item>
          <el-sub-menu index="project">
            <template #title><el-icon><Management /></el-icon><span>项目</span></template>
            <el-menu-item index="/projects/list">项目基础管理</el-menu-item>
            <el-menu-item index="/projects/members">项目人员管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="task">
            <template #title><el-icon><List /></el-icon><span>任务协同</span></template>
            <el-menu-item index="/tasks/list">任务分配管理</el-menu-item>
            <el-menu-item index="/weekly-reports/list">项目周报管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="hr">
            <template #title><el-icon><User /></el-icon><span>考勤与通知</span></template>
            <el-menu-item index="/leaves/list">请假审批管理</el-menu-item>
            <el-menu-item index="/notifications/list">通知提醒</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/stats/index">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>统计分析</template>
          </el-menu-item>
          <el-menu-item index="/audit/operation-log">
            <el-icon><Document /></el-icon>
            <template #title>操作日志</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>

      <div class="saas-aside-footer">v1.0 · 本地开发</div>
    </aside>

    <main class="saas-main">
      <header class="saas-header">
        <div class="header-left">
          <h1 class="saas-header-title">{{ currentTitle }}</h1>
          <div class="saas-text-mute header-desc">项目 · 任务 · 周报 · 请假 · 统计 全过程协同</div>
        </div>
        <div class="saas-header-right">
          <el-button text type="primary" @click="goPortal">
            <el-icon><Back /></el-icon>
            <span>返回统一门户</span>
          </el-button>
          <div class="saas-user-chip">
            <div class="saas-avatar">{{ (authStore.realName || "U").charAt(0) }}</div>
            <span class="saas-user-name">{{ authStore.realName || "未登录" }}</span>
          </div>
        </div>
      </header>

      <div class="saas-tabs-bar">
        <div
          v-for="tab in tabsStore.tabs"
          :key="tab.path"
          class="tabs-tag-wrap"
          @contextmenu.prevent="openTabMenu($event, tab.path)"
        >
          <el-tag
            class="tabs-tag"
            :closable="tab.closable !== false"
            :type="tabsStore.activePath === tab.path ? 'primary' : 'info'"
            :effect="tabsStore.activePath === tab.path ? 'dark' : 'light'"
            @click="router.push(tab.path)"
            @close="closeTab(tab.path)"
          >
            {{ tab.title }}
          </el-tag>
        </div>
      </div>

      <div
        v-if="tabMenu.visible"
        class="tab-context-menu"
        :style="{ left: `${tabMenu.x}px`, top: `${tabMenu.y}px` }"
      >
        <button type="button" class="tab-menu-item" @click="handleTabMenu('left')">关闭左侧</button>
        <button type="button" class="tab-menu-item" @click="handleTabMenu('right')">关闭右侧</button>
        <button type="button" class="tab-menu-item danger" @click="handleTabMenu('all')">关闭全部</button>
      </div>

      <section class="saas-content">
        <router-view v-slot="{ Component, route: currentRoute }">
          <keep-alive :include="tabsStore.cachedNames">
            <component :is="Component" :key="currentRoute.name" />
          </keep-alive>
        </router-view>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, reactive, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth";
import { useTabsStore } from "@/store/tabs";
import { getPortalEntry, syncPortalOrigin } from "@/composables/usePortalSso";
import {
  Odometer, Management, List, User, DataAnalysis, Document, Back,
} from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const tabsStore = useTabsStore();
const tabMenu = reactive({ visible: false, x: 0, y: 0, path: "" });

const currentTitle = computed(() => String(route.meta.title || "项目管理系统"));

function handleSelect(index: string) { router.push(index); }
function goPortal() { window.location.assign(getPortalEntry()); }

function closeTab(path: string) {
  const isActive = tabsStore.activePath === path;
  tabsStore.remove(path);
  if (isActive) router.push(tabsStore.activePath || "/dashboard");
}
function openTabMenu(event: MouseEvent, path: string) {
  tabMenu.visible = true; tabMenu.x = event.clientX; tabMenu.y = event.clientY; tabMenu.path = path;
}
function hideTabMenu() { tabMenu.visible = false; }
function handleTabMenu(action: "left" | "right" | "all") {
  const currentPath = tabMenu.path;
  if (!currentPath) return;
  if (action === "left") tabsStore.closeLeft(currentPath);
  else if (action === "right") tabsStore.closeRight(currentPath);
  else tabsStore.closeAll();
  hideTabMenu();
  router.push(tabsStore.activePath || "/dashboard");
}

onMounted(() => {
  syncPortalOrigin();
  window.addEventListener("click", hideTabMenu);
  tabsStore.restore();
  tabsStore.open({
    name: String(route.name), path: route.path,
    title: String(route.meta.title || "页面"),
    cacheKey: String(route.meta.cacheKey || ""),
    closable: route.path !== "/dashboard",
  });
  const token = localStorage.getItem("project_access_token");
  if (token && !authStore.realName) {
    authStore.loadCurrentUser().catch(() => {
      authStore.logout();
      router.replace("/login");
    });
  }
});

watch(
  () => route.fullPath,
  () => {
    tabsStore.open({
      name: String(route.name), path: route.path,
      title: String(route.meta.title || "页面"),
      cacheKey: String(route.meta.cacheKey || ""),
      closable: route.path !== "/dashboard",
    });
  },
  { immediate: true }
);

onBeforeUnmount(() => { window.removeEventListener("click", hideTabMenu); });
</script>

<style scoped>
.header-left { display: flex; flex-direction: column; gap: 2px; }
.header-desc { font-size: 12px; line-height: 1.3; }

.saas-aside-scroll { flex: 1; min-height: 0; }
:deep(.saas-aside-scroll .el-scrollbar__view) { padding: 0 8px; }

.saas-tabs-bar {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 10px var(--saas-space-6) 0;
  flex-wrap: wrap;
  background: var(--saas-bg-card);
  border-bottom: 1px solid var(--saas-border-3);
}
.tabs-tag-wrap { display: inline-flex; }
.tabs-tag { cursor: pointer; }

.tab-context-menu {
  position: fixed;
  z-index: 3000;
  min-width: 132px;
  padding: 6px;
  border: 1px solid var(--saas-border-1);
  border-radius: var(--saas-radius-md);
  background: var(--saas-bg-card);
  box-shadow: var(--saas-shadow-lg);
}
.tab-menu-item {
  width: 100%;
  border: none;
  background: transparent;
  text-align: left;
  padding: 8px 12px;
  border-radius: var(--saas-radius-sm);
  cursor: pointer;
  color: var(--saas-text-2);
  font-size: var(--saas-fs-sm);
}
.tab-menu-item:hover { background: var(--saas-brand-50); color: var(--saas-brand-600); }
.tab-menu-item.danger:hover { background: var(--saas-danger-bg); color: var(--saas-danger); }
</style>
