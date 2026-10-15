'use strict';
const EXT_API = (typeof browser !== "undefined") ? browser : globalThis.chrome;

const FLOW_URL = 'https://flow.google.com/';

document.getElementById('open').addEventListener('click', async () => {
  // Reuse an already-open Flow tab rather than stacking duplicates.
  const [existing] = await EXT_API.tabs.query({ url: `${FLOW_URL}*` });
  if (existing) {
    await EXT_API.tabs.update(existing.id, { active: true });
    if (typeof EXT_API.windows?.update === 'function') {
      try { await EXT_API.windows.update(existing.windowId, { focused: true }); } catch {}
    }
  } else {
    await EXT_API.tabs.create({ url: FLOW_URL });
  }
});
