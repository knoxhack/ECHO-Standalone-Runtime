# ECHO Standalone Beta Recovery Kit

Target snapshots:

- `knoxhack/ECHO-Standalone-Runtime` main at `188852c567b51178a31898ec9698959c3b5fb7bc`
- `knoxhack/ECHO-Ashfall-Standalone-Edition` main at `ef3d4897b742ea02c505e4a37e90d8c0b5ac0d50`

This kit records the highest-confidence source patches and fail-closed verification tooling produced from the static audit that preceded the current beta hardening pass.

## Important result

The original audit found that the player-facing LWJGL client did not prove it executed the installed `-standalone.jar` module set. The client created a hard-coded Ashfall template, its Mods service used descriptor-only scanning, and the real executable module manager was used by the older runtime-app/test paths rather than the game client boot path.

Those blockers have now been applied in the runtime tree. The beta gate can report `READY_WITH_WARNINGS` when deterministic runtime evidence, strict pack bootstrap, executable ABI-v1 module loading, and packaged OpenGL probes pass. The remaining warning is human playtest evidence from the exact release bytes.

1. Replace the NeoForge/FML launch contract in the Ashfall Standalone manifest.
2. Remove the duplicate VSync plus manual 60 Hz limiter.
3. Prevent missing/skipped evidence from becoming a false PASS.
4. Add explicit installed-pack root discovery.
5. Add static and release-evidence validators for client-module wiring.

## Patch order

1. `patches/runtime/0001-fix-double-frame-limiter.patch`
2. `patches/runtime/0002-fail-closed-evidence.patch`
3. `patches/runtime/0003-installed-pack-root-discovery.patch`
4. Finish the implementation in `docs/CLIENT_MODULE_EXECUTION_WIRING.md`.
5. `patches/ashfall/0001-fix-standalone-launch-contract.patch`
6. Regenerate the Ashfall pack asset, checksums, audit reports, release metadata, and Release Index rows.

Do not publish release metadata unless `verify-runtime-wiring.mjs`, `verify-ashfall-standalone-manifest.mjs`, `compare-ashfall-manifests.mjs`, and `verify-standalone-evidence.mjs` are green for non-release local evidence.

## Apply

From PowerShell:

```powershell
.\scripts\apply-and-validate.ps1 -WorkspaceRoot C:\Development\Github
```

The default mode only runs `git apply --check`. Add `-Apply` to apply the low-risk patches.

## Validators

```powershell
node scripts\verify-ashfall-standalone-manifest.mjs C:\Development\Github\ECHO-Ashfall-Standalone-Edition\release-manifest.template.json
node scripts\compare-ashfall-manifests.mjs C:\Development\Github\ECHO-Ashfall-NeoForge-Edition\release-manifest.template.json C:\Development\Github\ECHO-Ashfall-Standalone-Edition\release-manifest.template.json
node scripts\verify-runtime-wiring.mjs C:\Development\Github\ECHO-Standalone-Runtime C:\Development\Github\ECHO-Ashfall-Standalone-Edition\release-manifest.template.json
node scripts\verify-standalone-evidence.mjs C:\Development\Github\ECHO-Standalone-Runtime --release
```

`verify-runtime-wiring.mjs` should pass once the client executes the installed module graph and imports its runtime content. `verify-standalone-evidence.mjs --release` remains intentionally red until real machine-run evidence replaces placeholders.

## What still requires a real machine run

A beta claim requires a compiled Windows distribution and real play evidence: clean install/uninstall, signed artifacts, visible OpenGL launch, 30- and 60-minute wall-clock sessions, hardware audio, save/reload, corruption recovery, and an Ashfall route playthrough from the exact release bytes.
