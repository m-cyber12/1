(() => {
  if (window.__novaTheme) return;
  window.__novaTheme = true;

  const css = `
    :root {
      --nova-primary: #A78BFA;
      --nova-secondary: #22D3EE;
      --nova-bg: #111827;
      --md-sys-color-primary: #A78BFA;
      --md-sys-color-secondary: #22D3EE;
      --md-sys-color-surface: #111827;
      --md-sys-color-background: #111827;
    }

    html, body {
      background-color: var(--nova-bg) !important;
      color-scheme: dark !important;
    }

    img[alt*="Google" i],
    img[src*="googlelogo" i],
    img[src*="gstatic" i][alt*="Google" i] {
      visibility: hidden !important;
    }

    a[href*="support.google.com" i],
    a[href*="policies.google.com" i],
    a[href*="about.google" i] {
      display: none !important;
    }
  `;

  function apply() {
    if (document.title !== "Nova Web") {
      document.title = "Nova Web";
    }
    let style = document.getElementById("nova-theme");
    if (!style) {
      style = document.createElement("style");
      style.id = "nova-theme";
      (document.head || document.documentElement).appendChild(style);
    }
    if (style.textContent !== css) {
      style.textContent = css;
    }
  }

  apply();
})();
