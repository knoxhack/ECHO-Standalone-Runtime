#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";

const root = path.resolve(process.argv[2] || ".");
const releaseMode = process.argv.includes("--release");
const reportsRoot = path.join(root, "reports", "echo", "standalone");
const failures = [];
const warnings = [];
const parsed = new Map();

function walk(directory) {
  if (!fs.existsSync(directory)) return [];
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const full = path.join(directory, entry.name);
    return entry.isDirectory() ? walk(full) : [full];
  });
}

if (!fs.existsSync(reportsRoot)) {
  console.error(`FAIL missing reports directory: ${reportsRoot}`);
  process.exit(1);
}

for (const file of walk(reportsRoot).filter(p => p.endsWith(".json"))) {
  let value;
  try {
    value = JSON.parse(fs.readFileSync(file, "utf8").replace(/^\uFEFF/, ""));
    parsed.set(path.relative(root, file).replaceAll("\\", "/"), value);
  } catch (error) {
    failures.push(`${path.relative(root, file)}: invalid JSON: ${error.message}`);
    continue;
  }
  const status = String(value.status || "").toUpperCase();
  if (value.schema === "echo.standalone.evidence.bootstrap.v1" && ["PASS", "READY"].includes(status)) {
    failures.push(`${path.relative(root, file)}: bootstrap placeholder may not claim ${status}`);
  }
}

const candidates = [
  "reports/echo/standalone/beta-readiness-gate.json",
  "reports/echo/standalone/beta-readiness-checks.json"
];
for (const relative of candidates) {
  const report = parsed.get(relative);
  if (!report) {
    warnings.push(`${relative}: not present`);
    continue;
  }
  const checks = Array.isArray(report.checks) ? report.checks :
    Array.isArray(report.items) ? report.items : [];
  for (const check of checks) {
    const id = String(check.id || "<unnamed>");
    const status = String(check.status || "UNKNOWN").toUpperCase();
    if (check.skipped === true && status === "PASS") {
      failures.push(`${relative}: ${id} is skipped but marked PASS`);
    }
    if (check.betaBlocking === true && status !== "PASS") {
      failures.push(`${relative}: blocking check ${id} is ${status}`);
    }
  }
  if (String(report.status || "").toUpperCase() === "READY") {
    const blockers = Array.isArray(report.blockers) ? report.blockers : [];
    if (blockers.length) failures.push(`${relative}: READY report still contains ${blockers.length} blocker(s)`);
  }
}

if (releaseMode) {
  const mandatory = [
    "release-signing-evidence.json",
    "manual-install-uninstall-evidence.json",
    "manual-playtest-report.json",
    "audio-hardware-verification.json",
    "packaged-opengl-client-image.json",
    "packaged-exe-wallclock-smoke.json",
    "packaged-exe-wallclock-strict-rehearsal.json"
  ];
  for (const name of mandatory) {
    const relative = `reports/echo/standalone/${name}`;
    const report = parsed.get(relative);
    if (!report) {
      failures.push(`${relative}: missing release evidence`);
      continue;
    }
    if (report.schema === "echo.standalone.evidence.bootstrap.v1") {
      failures.push(`${relative}: still a bootstrap placeholder`);
    }
    if (String(report.status || "").toUpperCase() !== "PASS") {
      failures.push(`${relative}: status is ${report.status || "UNKNOWN"}, expected PASS`);
    }
  }
}

for (const warning of warnings) console.warn(`WARN ${warning}`);
if (failures.length) {
  for (const failure of failures) console.error(`FAIL ${failure}`);
  console.error(`Evidence gate FAILED (${failures.length} failure(s))`);
  process.exit(1);
}
console.log(`Evidence gate PASS reports=${parsed.size} releaseMode=${releaseMode}`);
