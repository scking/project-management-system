import { defineStore } from "pinia";

export interface TabItem {
  name: string;
  path: string;
  title: string;
  cacheKey?: string;
  closable?: boolean;
}

const STORAGE_KEY = "project-open-tabs";

export const useTabsStore = defineStore("project-tabs", {
  state: () => ({
    tabs: [] as TabItem[],
    activePath: "/dashboard",
    cachedNames: [] as string[],
  }),
  actions: {
    restore() {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return;
      try {
        const parsed = JSON.parse(raw);
        this.tabs = parsed.tabs || [];
        this.activePath = parsed.activePath || "/dashboard";
        this.cachedNames = parsed.cachedNames || [];
      } catch {
        localStorage.removeItem(STORAGE_KEY);
        this.tabs = [];
        this.activePath = "/dashboard";
        this.cachedNames = [];
      }
    },
    persist() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        tabs: this.tabs,
        activePath: this.activePath,
        cachedNames: this.cachedNames,
      }));
    },
    open(tab: TabItem) {
      if (!this.tabs.find((item) => item.path === tab.path)) {
        this.tabs.push(tab);
      }
      if (tab.cacheKey && !this.cachedNames.includes(tab.name)) {
        this.cachedNames.push(tab.name);
      }
      this.activePath = tab.path;
      this.persist();
    },
    remove(path: string) {
      const target = this.tabs.find((item) => item.path === path);
      this.tabs = this.tabs.filter((item) => item.path !== path);
      if (target?.name) {
        this.cachedNames = this.cachedNames.filter((item) => item !== target.name);
      }
      if (this.activePath === path) {
        const fallback = this.tabs[this.tabs.length - 1];
        this.activePath = fallback?.path || "/dashboard";
      }
      this.persist();
    },
    closeLeft(path: string) {
      const currentIndex = this.tabs.findIndex((item) => item.path === path);
      if (currentIndex <= 0) return;
      const removed = this.tabs.slice(0, currentIndex).filter((item) => item.closable !== false);
      const removedNames = removed.map((item) => item.name);
      this.tabs = this.tabs.filter((item, index) => index >= currentIndex || item.closable === false);
      this.cachedNames = this.cachedNames.filter((item) => !removedNames.includes(item));
      this.persist();
    },
    closeRight(path: string) {
      const currentIndex = this.tabs.findIndex((item) => item.path === path);
      if (currentIndex < 0) return;
      const removed = this.tabs.slice(currentIndex + 1).filter((item) => item.closable !== false);
      const removedNames = removed.map((item) => item.name);
      this.tabs = this.tabs.filter((item, index) => index <= currentIndex || item.closable === false);
      this.cachedNames = this.cachedNames.filter((item) => !removedNames.includes(item));
      this.persist();
    },
    closeAll() {
      const pinned = this.tabs.filter((item) => item.closable === false);
      this.tabs = pinned.length ? pinned : [];
      this.cachedNames = pinned.map((item) => item.name);
      this.activePath = pinned[0]?.path || "/dashboard";
      this.persist();
    },
  },
});
