<template>
  <div class="login-page">
    <div class="login-card">
      <h1>项目管理系统</h1>
      <p>本系统通过统一门户单点登录进入，无需单独维护账号密码。</p>
      <el-button type="primary" :loading="loading" @click="redirectToPortalSso">前往统一门户登录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth";
import { redirectToPortalSso } from "@/composables/usePortalSso";
import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);

onMounted(async () => {
  const ticket = route.query.sso_ticket;
  const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "/dashboard";
  if (!ticket || typeof ticket !== "string") {
    return;
  }
  loading.value = true;
  try {
    await authStore.ssoLogin(ticket);
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || "SSO 登录失败，正在返回统一门户");
    setTimeout(() => redirectToPortalSso(), 800);
    loading.value = false;
    return;
  }

  try {
    ElMessage.success("已通过统一门户自动登录");
    await router.replace(redirect);
  } catch (error: any) {
    ElMessage.error(error?.message || "登录成功，但进入系统首页失败");
    await router.replace("/");
  } finally {
    loading.value = false;
  }
});
</script>
