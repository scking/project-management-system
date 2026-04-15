import { request } from "./request";

export const quoteApi = {
  list: (params: Record<string, unknown>) => request.get("/quotes", { params }),
  itemList: (params: Record<string, unknown>) => request.get("/quote-items", { params }),
  create: (data: Record<string, unknown>) => request.post("/quotes", data),
  recommend: (inquiryId: number) => request.post(`/quotes/recommend/${inquiryId}`),
};
