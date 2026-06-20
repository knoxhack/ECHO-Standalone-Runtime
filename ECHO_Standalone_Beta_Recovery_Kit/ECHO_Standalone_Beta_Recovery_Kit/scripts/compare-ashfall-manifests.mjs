#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";

const [neoArg, standaloneArg] = process.argv.slice(2);
if (!neoArg || !standaloneArg) {
  console.error("Usage: node compare-ashfall-manifests.mjs <neoforge-manifest> <standalone-manifest>");
  process.exit(2);
}
const read = p => JSON.parse(fs.readFileSync(path.resolve(p), "utf8"));
const neo = read(neoArg);
const standalone = read(standaloneArg);
const ids = m => new Set((m.modules || []).map(String));
const neoIds = ids(neo);
const standaloneIds = ids(standalone);
const missing = [...neoIds].filter(id => !standaloneIds.has(id)).sort();
const extra = [...standaloneIds].filter(id => !neoIds.has(id)).sort();

console.log(JSON.stringify({
  neoForgeModules: neoIds.size,
  standaloneModules: standaloneIds.size,
  missingFromStandalone: missing,
  extraInStandalone: extra
}, null, 2));

if (missing.length) {
  console.error(`FAIL Standalone is missing ${missing.length} NeoForge module(s)`);
  process.exit(1);
}
console.log("Module-set comparison PASS");
