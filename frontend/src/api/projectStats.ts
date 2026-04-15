import { request } from "./request";

export const projectStatsApi = {
  overview: () => request.get("/pm/stats/overview"),
};
