import { request } from "./request";

export const taskApi = {
  list: (params?: Record<string, unknown>) => request.get("/pm/tasks", { params }),
  create: (data: Record<string, unknown>) => request.post("/pm/tasks", data),
  updateStatus: (id: number, data: Record<string, unknown>) => request.post(`/pm/tasks/${id}/status`, data),
};
