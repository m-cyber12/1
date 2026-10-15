# AI Flow Bypasser for Firefox

Firefox port of AI Flow Bypasser (Google Flow regional-availability bypass), tuned for Firefox for Android.

## Install (unsigned)
Use Firefox Nightly/Developer and `about:debugging`, or sign through Mozilla AMO using the included GitHub Actions workflow.

## Build locally
```
npx web-ext build --source-dir . --artifacts-dir ./dist
```

## Notes
- Runs only on `https://flow.google.com/*`.
- Fetches descriptor from `https://flow.cfcnode.com/v1/flow-spec` every 6 hours; validates shape.
- Adds `tabs` permission for Flow-tab status/detection.
- `windows` API calls are guarded for Firefox Android.
