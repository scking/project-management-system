import { request } from "./request";

export const performanceApi = {
  list: (params: Record<string, unknown>) => request.get("/performances", { params }),
  create: (data: Record<string, unknown>) => request.post("/performances", data),
};

