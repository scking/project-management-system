import { request } from "./request";

export const inquiryApi = {
  list: (params: Record<string, unknown>) => request.get("/inquiries", { params }),
  create: (data: Record<string, unknown>) => request.post("/inquiries", data),
  compare: (id: number) => request.post(`/inquiries/${id}/compare`),
};
