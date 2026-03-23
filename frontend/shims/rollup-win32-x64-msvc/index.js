// Shim package used on Windows where Rollup's native `.node` binding is blocked.
// We re-export the WASM implementation that lives in `@rollup/wasm-node/dist/native.js`.
'use strict';

module.exports = require('@rollup/wasm-node/dist/native.js');

