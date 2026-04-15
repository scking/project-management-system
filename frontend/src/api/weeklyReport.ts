import { request } from "./request";

export const weeklyReportApi = {
  list: (params?: Record<string, unknown>) => request.get("/pm/weekly-reports", { params }),
  create: (data: Record<string, unknown>) => request.post("/pm/weekly-reports", data),
};
