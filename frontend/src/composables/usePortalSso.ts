const PORTAL_ORIGIN_KEY = "project_portal_origin";

function normalizePortalOrigin(value: string) {
  try {
    const url = new URL(value);
    url.search = "";
    url.hash = "";
    return url.origin + (url.pathname === "/" ? "" : url.pathname.replace(/\/+$/, ""));
  } catch {
    return value.replace(/\/+$/, "");
  }
}

function readStoredPortalOrigin() {
  if (typeof window === "undefined") {
    return "";
  }
  return window.sessionStorage.getItem(PORTAL_ORIGIN_KEY) || "";
}

function writeStoredPortalOrigin(value: string) {
  if (typeof window === "undefined" || !value) {
    return;
  }
  window.sessionStorage.setItem(PORTAL_ORIGIN_KEY, normalizePortalOrigin(value));
}

export function getPortalOrigin() {
  if (typeof window === "undefined") {
    return "http://localhost:5173";
  }
  const { protocol, hostname, port, origin, search } = window.location;
  const queryPortalOrigin = new URLSearchParams(search).get("portal_origin");
  if (queryPortalOrigin) {
    writeStoredPortalOrigin(queryPortalOrigin);
    return normalizePortalOrigin(queryPortalOrigin);
  }
  if (document.referrer) {
    try {
      const referrer = new URL(document.referrer);
      if (referrer.origin !== window.location.origin) {
        writeStoredPortalOrigin(referrer.origin);
        return referrer.origin;
      }
    } catch {
      // ignore invalid referrer
    }
  }
  const storedOrigin = readStoredPortalOrigin();
  if (storedOrigin) {
    return storedOrigin;
  }
  const isLocal = hostname === "127.0.0.1" || hostname === "localhost";
  if (isLocal) {
    const fallback = `${protocol}//localhost:5173`;
    writeStoredPortalOrigin(fallback);
    return fallback;
  }
  if (!port || port === "80" || port === "443") {
    writeStoredPortalOrigin(origin);
    return origin;
  }
  const fallback = `${protocol}//${hostname}`;
  writeStoredPortalOrigin(fallback);
  return fallback;
}

export function syncPortalOrigin() {
  return getPortalOrigin();
}

export function getPortalEntry() {
  const base = getPortalOrigin();
  try {
    const url = new URL(base);
    if (!url.pathname || url.pathname === "/") {
      url.pathname = "/portal";
    } else if (!url.pathname.endsWith("/portal")) {
      url.pathname = `${url.pathname.replace(/\/+$/, "")}/portal`;
    }
    url.search = "";
    url.hash = "";
    return url.toString();
  } catch {
    return `${base.replace(/\/+$/, "")}/portal`;
  }
}

export function getProjectRoot() {
  if (typeof window === "undefined") {
    return "http://127.0.0.1:3280/";
  }
  const base = (import.meta.env.BASE_URL || "/").replace(/\/+$/, "");
  return `${window.location.origin}${base || ""}/`;
}

export function redirectToPortalSso() {
  writeStoredPortalOrigin(getPortalOrigin());
  const redirect = encodeURIComponent(getProjectRoot());
  window.location.href = `${getPortalOrigin()}/api/sso/login?appCode=project&redirect=${redirect}`;
}
