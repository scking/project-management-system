import { request } from "./request";

export const purchaseApi = {
  list: (params: Record<string, unknown>) => request.get("/purchase-requests", { params }),
  create: (data: Record<string, unknown>) => request.post("/purchase-requests", data),
  submit: (id: number) => request.post(`/purchase-requests/${id}/submit`),
  itemList: (params: Record<string, unknown>) => request.get("/purchase-request-items", { params }),
  itemCreate: (data: Record<string, unknown>) => request.post("/purchase-request-items", data),
};
