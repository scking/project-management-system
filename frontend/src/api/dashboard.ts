import { request } from "./request";

export const dashboardApi = {
  overview: () => request.get("/dashboard/overview"),
};

