# ECHO Standalone Runtime

Standalone runtime shell and engine layer for running ECHO/Ashfall outside Minecraft.

## Purpose

Standalone runtime shell and engine layer for running ECHO/Ashfall outside Minecraft.

## What Lives Here

Gradle runtime code, runtime shell contracts, standalone docs, release notes, runtime integration guides, and `.ECHO Content Graph` loading for standalone module roots.

## Release And Update Role

Owns standalone runtime releases consumed by Ashfall Standalone Edition and local runtime tests.

## Public Or Private

Public is recommended if standalone runtime downloads or SDK integration docs are intended for external users.

## Build And Dev Commands

Run commands from the repository root.

- Windows: `.\gradlew.bat build`
- macOS/Linux: `./gradlew build`
- Public alpha staging: `.\gradlew.bat packagePublicAlphaRelease` writes `build/public-alpha/echo-standalone-runtime-0.1.0-alpha.zip`, readiness reports, and `checksums.txt` for GitHub release upload.
- Public alpha publishing: run the `Release Public Alpha` workflow with tag `v0.1.0-standalone-runtime-alpha`; it stages assets, attests `checksums.txt`, and uploads the exact files to the GitHub Release.
- Content graph smoke: `.\gradlew.bat runStandaloneContentGraphLoadSmoke -PechoModulesRepoRoot=..\ECHO-Modules` loads every per-module `content-graph.json`, validates node/edge references, and reports feature/export-plan diagnostics.

## Artifact Ownership

Standalone runtime binaries and runtime metadata belong here. Standalone Ashfall pack releases belong to `ECHO-Ashfall-Standalone-Edition`.

## Content Graph Loading

`echo-runtime-compat` provides `EchoContentGraphLoader`, which reads `.echo/content-graph/content-graph.json`, `.echo/content-graph/features.json`, and `.echo/content-graph/export-plans/*.json` from each module root. It produces an `EchoContentGraphLoadResult` containing nodes, edges, features, export-plan counts, Hytale plans, and diagnostics. The smoke report includes `echo.content_graph.evidence.v1` counts, compares them with `content-graph-evidence.json` when present, and asserts the current Openlands Hytale plan has 9 blocked entity nodes. This is optional evidence: the standalone runtime can start without a content graph, but missing or invalid graphs are reported for review.

## Release Index Product Routing

Runtime update metadata is routed through the canonical Release Index product entry `echo-standalone-runtime`. Run `node scripts/verify-release-index-product.mjs` to audit the indexed product record, add `--check-urls` to prove indexed GitHub artifact URLs are reachable, or add `--strict` in release gates once the entry has approved artifacts. The Gradle gate accepts `-PechoReleaseIndexCheckUrls=true` or `ECHO_RELEASE_INDEX_CHECK_URLS=true` for the same live URL check. The public alpha archive must be uploaded as `echo-standalone-runtime-0.1.0-alpha.zip` so the Release Index can approve the exact `archive` artifact.

## Docs Index

- [docs/echo/standalone/ASHFALL_PARITY_MATRIX.md](docs/echo/standalone/ASHFALL_PARITY_MATRIX.md)
- [docs/echo/standalone/BETA_RELEASE_READINESS.md](docs/echo/standalone/BETA_RELEASE_READINESS.md)
- [docs/echo/standalone/ECHO_ADAPTERCORE_PARITY_MATRIX.md](docs/echo/standalone/ECHO_ADAPTERCORE_PARITY_MATRIX.md)
- [docs/echo/standalone/ECHO_RUNTIME_BOUNDARIES.md](docs/echo/standalone/ECHO_RUNTIME_BOUNDARIES.md)
- [docs/echo/standalone/ECHO_STANDALONE_ALPHA_READINESS.md](docs/echo/standalone/ECHO_STANDALONE_ALPHA_READINESS.md)
- [docs/echo/standalone/ECHO_STANDALONE_APP_RUNTIME.md](docs/echo/standalone/ECHO_STANDALONE_APP_RUNTIME.md)
- [docs/echo/standalone/ECHO_STANDALONE_ASHFALL_PLAYABLE_MISSION.md](docs/echo/standalone/ECHO_STANDALONE_ASHFALL_PLAYABLE_MISSION.md)
- [docs/echo/standalone/ECHO_STANDALONE_ASSET_RUNTIME.md](docs/echo/standalone/ECHO_STANDALONE_ASSET_RUNTIME.md)
- [docs/echo/standalone/ECHO_STANDALONE_AUDIO_DEVICE_RUNTIME.md](docs/echo/standalone/ECHO_STANDALONE_AUDIO_DEVICE_RUNTIME.md)
- [docs/echo/standalone/ECHO_STANDALONE_AUDIO_RUNTIME.md](docs/echo/standalone/ECHO_STANDALONE_AUDIO_RUNTIME.md)
- [docs/echo/standalone/ECHO_STANDALONE_COMMAND_RUNTIME.md](docs/echo/standalone/ECHO_STANDALONE_COMMAND_RUNTIME.md)
- [docs/echo/standalone/ECHO_STANDALONE_COMPATIBILITY_MIGRATION.md](docs/echo/standalone/ECHO_STANDALONE_COMPATIBILITY_MIGRATION.md)
- [PUBLIC_ALPHA_RELEASE_STATUS.md](PUBLIC_ALPHA_RELEASE_STATUS.md)

## Related Repos

- [knoxhack/ECHO-Launcher](https://github.com/knoxhack/ECHO-Launcher)
- [knoxhack/ECHO-Modules](https://github.com/knoxhack/ECHO-Modules)
- [knoxhack/ECHO-Ashfall-Native-Edition](https://github.com/knoxhack/ECHO-Ashfall-Native-Edition)
- [knoxhack/ECHO-Ashfall-NeoForge-Edition](https://github.com/knoxhack/ECHO-Ashfall-NeoForge-Edition)
- [knoxhack/ECHO-Ashfall-Standalone-Edition](https://github.com/knoxhack/ECHO-Ashfall-Standalone-Edition)
- [knoxhack/ECHO-Release-Index](https://github.com/knoxhack/ECHO-Release-Index)
- [knoxhack/ECHO-Native-Platform](https://github.com/knoxhack/ECHO-Native-Platform)
- [knoxhack/ECHO-SDK](https://github.com/knoxhack/ECHO-SDK)
- [knoxhack/ECHO-Developer-Studio](https://github.com/knoxhack/ECHO-Developer-Studio)
- [knoxhack/ECHO-Addons-Studio](https://github.com/knoxhack/ECHO-Addons-Studio)
- [knoxhack/ECHO-Platform-Website](https://github.com/knoxhack/ECHO-Platform-Website)
