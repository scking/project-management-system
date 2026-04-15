import { request } from "./request";

export const leaveApi = {
  list: (params?: Record<string, unknown>) => request.get("/pm/leaves", { params }),
  create: (data: Record<string, unknown>) => request.post("/pm/leaves", data),
  approve: (id: number, data: Record<string, unknown>) => request.post(`/pm/leaves/${id}/approve`, data),
};
