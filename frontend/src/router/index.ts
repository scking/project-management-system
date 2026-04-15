import { createRouter, createWebHistory } from "vue-router";
import { routes } from "./routes";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

router.beforeEach((to, _, next) => {
  const isLogin = to.path === "/login";
  const token = localStorage.getItem("project_access_token");
  if (!isLogin && !token) {
    next({
      path: "/login",
      query: {
        ...to.query,
        redirect: to.fullPath,
      },
    });
    return;
  }
  next();
});

export default router;
