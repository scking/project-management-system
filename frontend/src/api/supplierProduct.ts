import { request } from "./request";

export const supplierProductApi = {
  list: (params: Record<string, unknown>) => request.get("/supplier-products", { params }),
  create: (data: Record<string, unknown>) => request.post("/supplier-products", data),
  priceHistoryList: (params: Record<string, unknown>) => request.get("/supplier-product-price-histories", { params }),
  priceHistoryCreate: (data: Record<string, unknown>) => request.post("/supplier-product-price-histories", data),
};
