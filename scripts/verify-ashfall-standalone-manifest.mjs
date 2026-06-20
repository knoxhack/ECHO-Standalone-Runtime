#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";

const manifestPath = path.resolve(process.argv[2] || "release-manifest.template.json");
const failures = [];
const warnings = [];

function fail(message) { failures.push(message); }
function warn(message) { warnings.push(message); }
function array(value) { return Array.isArray(value) ? value : []; }

let manifest;
try {
  manifest = JSON.parse(fs.readFileSync(manifestPath, "utf8"));
} catch (error) {
  console.error(`FAIL cannot parse ${manifestPath}: ${error.message}`);
  process.exit(2);
}

if (manifest.loader !== "echo-standalone-runtime") fail(`loader must be echo-standalone-runtime, got ${manifest.loader}`);
if (String(manifest.minecraft).toLowerCase() !== "standalone") fail(`minecraft/runtime marker must be Standalone`);
if (manifest.launch?.mainClass !== "dev.echo.standalone.runtime.client.EchoClientMain") {
  fail(`launch.mainClass must be dev.echo.standalone.runtime.client.EchoClientMain`);
}

const allArgs = [...array(manifest.launch?.gameArgs), ...array(manifest.launch?.jvmArgs)].map(String);
const forbidden = allArgs.filter(v => /(?:fml|neoforge|forge|net\.minecraft)/i.test(v));
if (forbidden.length) fail(`NeoForge/Minecraft launch tokens remain: ${forbidden.join(", ")}`);
if (!allArgs.includes("--enable-native-access=ALL-UNNAMED")) warn(`recommended JVM native-access flag is absent`);
if (!array(manifest.launch?.gameArgs).includes("--pack-root")) fail(`gameArgs must pass --pack-root`);
if (!array(manifest.launch?.gameArgs).includes("--modules-root")) fail(`gameArgs must pass --modules-root`);

const modules = array(manifest.modules).map(String);
const files = array(manifest.files);
const requirements = array(manifest.moduleRequirements);
if (!modules.length) fail(`modules is empty`);
if (!files.length) fail(`files is empty`);
if (!requirements.length) fail(`moduleRequirements is empty`);

const duplicate = values => [...new Set(values.filter((v, i) => values.indexOf(v) !== i))];
for (const id of duplicate(modules)) fail(`duplicate module id: ${id}`);

const fileIds = files.map(row => String(row.moduleId || row.id || ""));
const reqIds = requirements.map(row => String(row.moduleId || row.id || ""));
for (const id of modules) {
  if (!fileIds.includes(id)) fail(`module ${id} has no files row`);
  if (!reqIds.includes(id)) fail(`module ${id} has no moduleRequirements row`);
}
for (const id of fileIds) if (id && !modules.includes(id)) fail(`files row ${id} is not declared in modules`);
for (const id of reqIds) if (id && !modules.includes(id)) fail(`moduleRequirements row ${id} is not declared in modules`);

for (const row of files) {
  const id = String(row.moduleId || row.id || "<unknown>");
  if (row.artifactFamily !== "standalone") fail(`${id}: artifactFamily must be standalone`);
  const artifact = String(row.assetName || row.artifactName || "");
  if (!/-standalone\.jar$/i.test(artifact)) fail(`${id}: artifact must end in -standalone.jar`);
  if (!String(row.path || "").startsWith("mods/")) fail(`${id}: install path must be under mods/`);
  if (!/^[a-f0-9]{64}$/i.test(String(row.sha256 || ""))) fail(`${id}: invalid SHA-256`);
  if (!(Number(row.size) > 0)) fail(`${id}: size must be positive`);
}
for (const row of requirements) {
  const id = String(row.moduleId || row.id || "<unknown>");
  if (row.artifactFamily !== "standalone") fail(`${id}: requirement artifactFamily must be standalone`);
}

for (const message of warnings) console.warn(`WARN ${message}`);
if (failures.length) {
  for (const message of failures) console.error(`FAIL ${message}`);
  console.error(`Manifest gate FAILED (${failures.length} failure(s), ${warnings.length} warning(s))`);
  process.exit(1);
}
console.log(`Manifest gate PASS modules=${modules.length} files=${files.length} requirements=${requirements.length}`);
