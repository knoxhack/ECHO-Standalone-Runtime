# ECHO Standalone Launcher MVP

Phase 14.19 adds a deterministic launcher integration layer for the standalone runtime. It does not replace the existing app launcher; it wraps it with detection, verification, repair planning, support-bundle manifesting, and handoff protection.

## Launcher Runtime

`EchoStandaloneLauncherRuntime` performs five steps:

1. detect whether the selected workspace is a standalone runtime workspace.
2. verify required launcher and vertical-slice artifacts.
3. produce a planning-only repair plan for missing artifacts.
4. create a deterministic support-bundle manifest.
5. launch the standalone runtime only when the request mode is `standalone-runtime`.

The launcher supports two request modes:

- `standalone-runtime` targets the OpenGL standalone client task, `:echo-runtime-client:run`; deterministic launcher smoke still drives the bounded app-runtime tick path for non-windowed verification.
- `platform-handoff` verifies the standalone workspace but preserves the external platform launch path.
  Handoff mode does not export or overwrite the standalone support bundle.

## Verification

The MVP verifies:

- `settings.gradle` and `build.gradle` are present.
- docs and reports roots exist.
- runtime version is a Phase 14 version.
- the Phase 14.18 vertical-slice documentation and reports are present.
- the Phase 14.19 launcher documentation and report are present.

Missing checks do not trigger automatic repair. They create repair-plan actions only.

## Support Bundle

The support bundle writes a deterministic manifest plus zip archive. It records presence and byte counts for:

- `build.gradle`
- `settings.gradle`
- `docs/echo/standalone/ECHO_STANDALONE_VERTICAL_SLICE.md`
- `docs/echo/standalone/ECHO_STANDALONE_LAUNCHER_MVP.md`
- `scripts/sign-release-artifacts.ps1`
- `scripts/capture-clean-install-uninstall-evidence.ps1`
- `scripts/capture-manual-wallclock-playtest.ps1`
- `reports/echo/standalone/runtime-vertical-slice.json`
- `reports/echo/standalone/vertical-slice-clean-exit.json`
- `reports/echo/standalone/runtime-launcher.json`
- release, distribution, full-mechanics parity, full command/keybind/debug/chat mechanics, full data/resource-pack mechanics, full addon extension mechanics, full progression/statistics/objectives mechanics, full worldgen/dimensions/structures mechanics, full entity AI/spawn mechanics, full survival/player-loop mechanics, full presentation mechanics, external-evidence handoff, install/uninstall, 30-/60-minute wall-clock monitor, audio, workspace-build, and acceptance evidence reports.
- `build/support/standalone-mod-diagnostics.json`, generated at bundle time with module graph status, AdapterCore coverage, bounded client save metadata, ScreenCore route, and renderer handoff metadata.
- `build/support/standalone-runtime-registry-fingerprint.json`, generated at bundle time with deterministic module graph and runtime registry fingerprints.
- `build/support/standalone-adaptercore-target-diff.json`, generated at bundle time with AdapterCore runtime target gaps and non-ready binding diagnostics.
- `build/support/standalone-module-lifecycle-traces.json`, generated at bundle time from descriptor-only module discovery and graph status for the OpenGL standalone runtime.

This gives future desktop launcher work a stable support-bundle contract without touching real user saves or external launcher state. Save metadata probing is bounded to workspace client save manifests.

## Smoke Harness Coverage

`EchoRuntimeLauncherSmokeHarness` proves:

- standalone workspace detection passes.
- launcher verification passes for the current workspace.
- repair planning is planning-only and empty for a ready workspace.
- the support bundle contains the launcher, tester, release signing, clean install/uninstall, and manual wall-clock capture helpers, release, distribution, full-mechanics parity, full command/keybind/debug/chat mechanics, full data/resource-pack mechanics, full addon extension mechanics, full progression/statistics/objectives mechanics, full worldgen/dimensions/structures mechanics, full entity AI/spawn mechanics, full survival/player-loop mechanics, full presentation mechanics, external-evidence handoff, 30-/60-minute wall-clock monitor, acceptance evidence, generated mod diagnostics, runtime registry fingerprints, AdapterCore target diffs, and descriptor-only module lifecycle trace entries.
- standalone mode runs a deterministic three-tick app-runtime smoke.
- standalone mode records the OpenGL client task as the player-facing launch target.
- platform handoff mode does not launch the standalone runtime.
- platform handoff mode does not overwrite the standalone OpenGL support bundle.
- a missing temporary workspace fails verification and produces planning-only repair actions.

## Out Of Scope

Phase 14.19 does not:

- start a desktop launcher UI.
- mutate launcher profiles.
- install or repair packs automatically.
- open network sockets.
- inspect real user saves.
- alter external platform or Native Loader launch behavior.

The next phase is Phase 14.20, the Standalone Alpha Readiness Gate.
