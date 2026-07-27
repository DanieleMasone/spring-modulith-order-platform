(function () {
  "use strict";

  const storageKey = "spring-modulith-order-platform-theme";
  const root = document.documentElement;
  const systemTheme = window.matchMedia("(prefers-color-scheme: dark)");

  function readStoredTheme() {
    try {
      const theme = localStorage.getItem(storageKey);
      return theme === "light" || theme === "dark" ? theme : null;
    } catch (error) {
      return null;
    }
  }

  function storeTheme(theme) {
    try {
      localStorage.setItem(storageKey, theme);
    } catch (error) {
      // The selected theme still applies for the current page view.
    }
  }

  const storedTheme = readStoredTheme();
  if (storedTheme) {
    root.dataset.theme = storedTheme;
  }

  function activeTheme() {
    return root.dataset.theme || (systemTheme.matches ? "dark" : "light");
  }

  function initializePageControls() {
    const themeToggle = document.querySelector("[data-theme-toggle]");
    const themeColor = document.querySelector('meta[name="theme-color"]');
    const navToggle = document.querySelector("[data-nav-toggle]");
    const navList = document.querySelector("[data-nav-list]");

    function updateThemeControl() {
      const theme = activeTheme();
      const nextTheme = theme === "dark" ? "light" : "dark";

      if (themeToggle) {
        themeToggle.dataset.themeState = theme;
        themeToggle.setAttribute("aria-pressed", String(theme === "dark"));
        themeToggle.setAttribute("aria-label", "Switch to " + nextTheme + " mode");
        themeToggle.setAttribute("title", "Switch to " + nextTheme + " mode");
      }

      if (themeColor) {
        themeColor.setAttribute("content", theme === "dark" ? "#0d141d" : "#f6f8fb");
      }
    }

    if (themeToggle) {
      themeToggle.addEventListener("click", function () {
        const nextTheme = activeTheme() === "dark" ? "light" : "dark";
        root.dataset.theme = nextTheme;
        storeTheme(nextTheme);
        updateThemeControl();
      });
    }

    systemTheme.addEventListener("change", function () {
      if (!readStoredTheme()) {
        updateThemeControl();
      }
    });

    function setNavigationOpen(open) {
      if (!navToggle || !navList) {
        return;
      }

      navToggle.setAttribute("aria-expanded", String(open));
      navToggle.setAttribute("aria-label", open ? "Close navigation" : "Open navigation");
      navToggle.setAttribute("title", open ? "Close navigation" : "Open navigation");
      navList.dataset.open = String(open);
    }

    if (navToggle && navList) {
      navToggle.addEventListener("click", function () {
        setNavigationOpen(navToggle.getAttribute("aria-expanded") !== "true");
      });

      navList.addEventListener("click", function (event) {
        if (event.target.closest("a")) {
          setNavigationOpen(false);
        }
      });

      document.addEventListener("keydown", function (event) {
        if (event.key === "Escape" && navToggle.getAttribute("aria-expanded") === "true") {
          setNavigationOpen(false);
          navToggle.focus();
        }
      });
    }

    updateThemeControl();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initializePageControls);
  } else {
    initializePageControls();
  }
}());
