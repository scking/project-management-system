import { request } from "./request";

export const authApi = {
  ssoLogin: (ticket: string) => request.post("/auth/sso-login", { ticket }),
  currentUser: () => request.get("/auth/current-user"),
};

