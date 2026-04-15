import { request } from "./request";

export const productApi = {
  list: (params: Record<string, unknown>) => request.get("/products", { params }),
  create: (data: Record<string, unknown>) => request.post("/products", data),
  paramList: (params: Record<string, unknown>) => request.get("/product-params", { params }),
  paramCreate: (data: Record<string, unknown>) => request.post("/product-params", data),
  attachmentList: (params: Record<string, unknown>) => request.get("/product-attachments", { params }),
  attachmentCreate: (data: Record<string, unknown>) => request.post("/product-attachments", data),
};
