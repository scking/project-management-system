import { request } from "./request";

export const weeklyReportApi = {
  list: (params?: Record<string, unknown>) => request.get("/pm/weekly-reports", { params }),
  getById: (id: number) => request.get(`/pm/weekly-reports/${id}`),
  create: (data: Record<string, unknown>) => request.post("/pm/weekly-reports", data),
};
