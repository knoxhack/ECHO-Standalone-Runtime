#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";

const runtimeRoot = path.resolve(process.argv[2] || ".");
const manifestPath = process.argv[3] ? path.resolve(process.argv[3]) : null;
const failures = [];
const passes = [];

function read(relative) {
  const file = path.join(runtimeRoot, relative);
  return fs.existsSync(file) ? fs.readFileSync(file, "utf8") : "";
}
function requireCheck(condition, message) {
  (condition ? passes : failures).push(message);
}
function javaFiles(directory) {
  if (!fs.existsSync(directory)) return [];
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const full = path.join(directory, entry.name);
    return entry.isDirectory() ? javaFiles(full) : (entry.name.endsWith(".java") ? [full] : []);
  });
}

const main = read("echo-runtime-client/src/main/java/dev/echo/standalone/runtime/client/EchoClientMain.java");
const launchContext = read("echo-runtime-client/src/main/java/dev/echo/standalone/runtime/client/EchoClientLaunchContext.java");
const roots = read("echo-runtime-client/src/main/java/dev/echo/standalone/runtime/client/EchoClientWorkspaceRoots.java");
const resources = read("echo-runtime-client/src/main/java/dev/echo/standalone/runtime/client/EchoClientResourcePackService.java");
const engine = read("echo-runtime-client/src/main/java/dev/echo/standalone/runtime/client/EchoClientEngine.java");
const build = read("build.gradle");
const clientSources = javaFiles(path.join(runtimeRoot, "echo-runtime-client", "src", "main", "java"))
  .map(file => fs.readFileSync(file, "utf8")).join("\n");

requireCheck(/EchoClientLaunchOptions\.parse\(args\)|EchoClientLaunchContext\.parse\(args\)/.test(main)
  && /--pack-root/.test(launchContext),
  "client parses an explicit installed pack root");
requireCheck(/echo\.pack\.root/.test(roots), "workspace roots honor echo.pack.root");
requireCheck(/addChildren\(result,\s*addonsRoot\)|resolve\("mods"\)/.test(resources), "installed module JARs are mounted as resource/data sources");
requireCheck(/EchoRuntimeModuleManager\.executableAbiV1\(\)/.test(clientSources), "player-facing client executes ABI-v1 modules");
requireCheck(/EchoClientModuleBootstrap|moduleRuntimeResult|EchoRuntimeModuleRuntimeResult/.test(clientSources), "client retains a module bootstrap/runtime result");
requireCheck(/importAdapterCoreContentRegistrations/.test(clientSources), "real module content can enter client runtime services");
requireCheck(/shouldManuallyPace\(runtime\.window\(\)\.vSync\(\)\)/.test(engine), "manual frame limiter is disabled while VSync is active");
requireCheck(!/externalPlaceholder\?\.status\s*\?:\s*'PASS'/.test(build), "evidence bootstrap does not default missing reports to PASS");
requireCheck(/!task\.state\.skipped/.test(build), "beta task checks reject skipped Gradle tasks");

if (manifestPath && fs.existsSync(manifestPath)) {
  const manifest = JSON.parse(fs.readFileSync(manifestPath, "utf8"));
  requireCheck(manifest.launch?.mainClass === "dev.echo.standalone.runtime.client.EchoClientMain",
    "Ashfall manifest launches the standalone client");
  requireCheck(!(manifest.launch?.gameArgs || []).some(v => /fml|neoforge/i.test(String(v))),
    "Ashfall manifest contains no FML/NeoForge game arguments");
}

for (const pass of passes) console.log(`PASS ${pass}`);
if (failures.length) {
  for (const failure of failures) console.error(`FAIL ${failure}`);
  console.error(`Runtime wiring gate FAILED (${failures.length} unresolved item(s))`);
  process.exit(1);
}
console.log("Runtime wiring gate PASS");
