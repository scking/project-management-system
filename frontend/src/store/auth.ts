import { defineStore } from "pinia";
import { authApi } from "@/api/auth";

export interface UserState {
  userId: number | null;
  username: string;
  realName: string;
  deptId: number | null;
  roleCodes: string[];
  permissionCodes: string[];
  dataScope: number;
}

export const useAuthStore = defineStore("project-auth", {
  state: (): UserState => ({
    userId: null,
    username: "",
    realName: "",
    deptId: null,
    roleCodes: [],
    permissionCodes: [],
    dataScope: 1,
  }),
  actions: {
    async ssoLogin(ticket: string) {
      const response = await authApi.ssoLogin(ticket);
      const data = response.data;
      localStorage.setItem("project_access_token", data.accessToken);
      this.userId = data.userId;
      this.username = data.username;
      this.realName = data.realName;
      this.deptId = data.deptId;
      this.roleCodes = data.roleCodes;
      this.permissionCodes = data.permissionCodes;
      this.dataScope = data.dataScope;
    },
    async loadCurrentUser() {
      const response: any = await authApi.currentUser();
      if (response.code !== 0 || !response.data) return;
      Object.assign(this, response.data);
    },
    hasPermission(code: string) {
      return this.permissionCodes.includes(code);
    },
    logout() {
      localStorage.removeItem("project_access_token");
      this.$reset();
    },
  },
});
