import { request } from "./request";

export const statsApi = {
  overview: () => request.get("/stats/overview"),
  analysis: () => request.get("/stats/analysis"),
};
