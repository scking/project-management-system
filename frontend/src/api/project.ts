import { request } from "./request";

export const projectApi = {
  dashboard: () => request.get("/pm/dashboard"),
  projectList: (params?: Record<string, unknown>) => request.get("/pm/projects", { params }),
  createProject: (data: Record<string, unknown>) => request.post("/pm/projects", data),
  updateProject: (id: number, data: Record<string, unknown>) => request.put(`/pm/projects/${id}`, data),
  deleteProject: (id: number) => request.delete(`/pm/projects/${id}`),
  memberList: (params?: Record<string, unknown>) => request.get("/pm/project-members", { params }),
  createMember: (data: Record<string, unknown>) => request.post("/pm/project-members", data),
  updateMember: (id: number, data: Record<string, unknown>) => request.put(`/pm/project-members/${id}`, data),
  deleteMember: (id: number) => request.delete(`/pm/project-members/${id}`),
};
