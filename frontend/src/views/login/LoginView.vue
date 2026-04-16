<template>
  <div class="saas-auth-page">
    <div class="saas-auth-card">
      <div class="saas-auth-brand">
        <div class="saas-brand-logo">项</div>
        <div>
          <h1 class="saas-auth-title">项目管理系统</h1>
          <p class="saas-auth-subtitle">由统一门户 SSO 提供登录服务</p>
        </div>
      </div>
      <div class="sso-hint">
        <el-icon color="var(--saas-brand-600)" :size="18"><Lock /></el-icon>
        <span>本系统通过统一门户单点登录进入，无需单独维护账号密码。</span>
      </div>
      <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="redirectToPortalSso">
        前往统一门户登录
      </el-button>
      <p class="saas-text-mute saas-auth-footer">© {{ new Date().getFullYear() }} 供应链管理中心</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth";
import { redirectToPortalSso, syncPortalOrigin } from "@/composables/usePortalSso";
import { ElMessage } from "element-plus";
import { Lock } from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);

onMounted(async () => {
  syncPortalOrigin();
  const ticket = route.query.sso_ticket;
  const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "/dashboard";
  if (!ticket || typeof ticket !== "string") return;
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

<style scoped>
.sso-hint {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  margin-bottom: 22px;
  background: var(--saas-brand-50);
  border: 1px solid var(--saas-brand-100);
  border-radius: var(--saas-radius-md);
  color: var(--saas-text-2);
  font-size: var(--saas-fs-sm);
  line-height: 1.6;
}
.saas-auth-footer {
  margin-top: 22px;
  text-align: center;
  font-size: 12px;
}
</style>
