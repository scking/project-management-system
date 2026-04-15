import { request } from "./request";

export const notificationApi = {
  list: () => request.get("/pm/notifications"),
};
