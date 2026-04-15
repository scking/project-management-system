import { request } from "./request";

export const supplierApi = {
  list: (params: Record<string, unknown>) => request.get("/suppliers", { params }),
  detail: (id: number) => request.get(`/suppliers/${id}`),
  create: (data: Record<string, unknown>) => request.post("/suppliers", data),
  update: (id: number, data: Record<string, unknown>) => request.put(`/suppliers/${id}`, data),
  freeze: (id: number) => request.post(`/suppliers/${id}/freeze`),
  unfreeze: (id: number) => request.post(`/suppliers/${id}/unfreeze`),
  blacklist: (id: number) => request.post(`/suppliers/${id}/blacklist`),
  removeBlacklist: (id: number) => request.post(`/suppliers/${id}/remove-blacklist`),
  qualificationList: (params: Record<string, unknown>) => request.get("/supplier-qualifications", { params }),
  qualificationCreate: (data: Record<string, unknown>) => request.post("/supplier-qualifications", data),
};
