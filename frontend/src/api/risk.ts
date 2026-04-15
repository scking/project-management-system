import { request } from "./request";

export const riskApi = {
  list: (params: Record<string, unknown>) => request.get("/risks", { params }),
  handle: (id: number, data: Record<string, unknown>) => request.post(`/risks/${id}/handle`, data),
  ignore: (id: number, data: Record<string, unknown>) => request.post(`/risks/${id}/ignore`, data),
};

