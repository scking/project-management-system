import axios from "axios";

function resolveBaseUrl() {
  if (typeof window === "undefined") {
    return "http://127.0.0.1:9280/api";
  }
  const { hostname, origin } = window.location;
  if (hostname === "127.0.0.1" || hostname === "localhost") {
    return "http://127.0.0.1:9280/api";
  }
  return `${origin}/project-api`;
}

export const request = axios.create({
  baseURL: resolveBaseUrl(),
  timeout: 15000,
  withCredentials: true,
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem("project_access_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use((response) => response.data, (error) => Promise.reject(error));
