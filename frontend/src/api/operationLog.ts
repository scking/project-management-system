import { request } from "./request";

export const operationLogApi = {
  list: (params: Record<string, unknown>) => request.get("/operation-logs", { params }),
};
