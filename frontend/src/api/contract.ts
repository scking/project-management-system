import { request } from "./request";

export const contractApi = {
  list: (params: Record<string, unknown>) => request.get("/contracts", { params }),
  detail: (id: number) => request.get(`/contracts/${id}`),
  reminders: () => request.get("/contracts/reminders"),
  create: (data: Record<string, unknown>) => request.post("/contracts", data),
};
