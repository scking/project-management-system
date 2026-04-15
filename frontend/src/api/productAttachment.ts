import { request } from "./request";

export const productAttachmentApi = {
  list: (params: Record<string, unknown>) => request.get("/product-attachments", { params }),
  create: (data: Record<string, unknown>) => request.post("/product-attachments", data),
};
