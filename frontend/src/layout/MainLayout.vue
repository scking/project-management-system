<template>
  <el-container class="layout-shell">
    <el-aside width="236px" class="layout-aside">
      <div class="brand-block">
        <div class="brand-title">项目管理系统</div>
        <div class="brand-subtitle">统一门户单点登录子系统</div>
      </div>
      <el-menu :default-active="route.path" class="side-menu" @select="handleSelect">
        <el-menu-item index="/dashboard">工作台</el-menu-item>
        <el-menu-item index="/projects/list">项目基础管理</el-menu-item>
        <el-menu-item index="/projects/members">项目人员管理</el-menu-item>
        <el-menu-item index="/tasks/list">任务分配管理</el-menu-item>
        <el-menu-item index="/weekly-reports/list">项目周报管理</el-menu-item>
        <el-menu-item index="/leaves/list">请假审批管理</el-menu-item>
        <el-menu-item index="/notifications/list">通知提醒</el-menu-item>
        <el-menu-item index="/stats/index">统计分析</el-menu-item>
        <el-menu-item index="/audit/operation-log">操作日志</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <div class="header-title">{{ currentTitle }}</div>
          <div class="header-desc">项目周报、任务分配、请假审批、统计分析一体化协同</div>
        </div>
        <div class="header-right">
          <el-button text @click="goPortal">返回统一门户</el-button>
          <el-tag type="info" effect="plain">{{ authStore.realName || "未登录" }}</el-tag>
        </div>
      </el-header>

      <div class="tabs-bar">
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
            effect="light"
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

      <el-main class="layout-main">
        <router-view v-slot="{ Component, route: currentRoute }">
          <keep-alive :include="tabsStore.cachedNames">
            <component :is="Component" :key="currentRoute.name" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, reactive, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth";
import { useTabsStore } from "@/store/tabs";
import { getPortalOrigin } from "@/composables/usePortalSso";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const tabsStore = useTabsStore();
const tabMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  path: "",
});

const currentTitle = computed(() => String(route.meta.title || "项目管理系统"));

function handleSelect(index: string) {
  router.push(index);
}

function goPortal() {
  window.location.href = getPortalOrigin();
}

function closeTab(path: string) {
  const isActive = tabsStore.activePath === path;
  tabsStore.remove(path);
  if (isActive) {
    router.push(tabsStore.activePath || "/dashboard");
  }
}

function openTabMenu(event: MouseEvent, path: string) {
  tabMenu.visible = true;
  tabMenu.x = event.clientX;
  tabMenu.y = event.clientY;
  tabMenu.path = path;
}

function hideTabMenu() {
  tabMenu.visible = false;
}

function handleTabMenu(action: "left" | "right" | "all") {
  const currentPath = tabMenu.path;
  if (!currentPath) return;
  if (action === "left") {
    tabsStore.closeLeft(currentPath);
  } else if (action === "right") {
    tabsStore.closeRight(currentPath);
  } else {
    tabsStore.closeAll();
  }
  hideTabMenu();
  router.push(tabsStore.activePath || "/dashboard");
}

onMounted(() => {
  window.addEventListener("click", hideTabMenu);
  tabsStore.restore();
  tabsStore.open({
    name: String(route.name),
    path: route.path,
    title: String(route.meta.title || "页面"),
    cacheKey: String(route.meta.cacheKey || ""),
    closable: route.path !== "/dashboard",
  });
});

watch(
  () => route.fullPath,
  () => {
    tabsStore.open({
      name: String(route.name),
      path: route.path,
      title: String(route.meta.title || "页面"),
      cacheKey: String(route.meta.cacheKey || ""),
      closable: route.path !== "/dashboard",
    });
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  window.removeEventListener("click", hideTabMenu);
});
</script>

<style scoped>
.tabs-tag-wrap {
  display: inline-flex;
}

.tab-context-menu {
  position: fixed;
  z-index: 3000;
  min-width: 132px;
  padding: 6px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.16);
}

.tab-menu-item {
  width: 100%;
  border: none;
  background: transparent;
  text-align: left;
  padding: 9px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #334155;
}

.tab-menu-item:hover {
  background: #eff6ff;
  color: #2563eb;
}

.tab-menu-item.danger:hover {
  background: #fff1f2;
  color: #dc2626;
}
</style>
