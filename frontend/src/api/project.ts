import { request } from "./request";

export const projectApi = {
  dashboard: () => request.get("/pm/dashboard"),
  projectList: (params?: Record<string, unknown>) => request.get("/pm/projects", { params }),
  memberList: (params?: Record<string, unknown>) => request.get("/pm/project-members", { params }),
};
