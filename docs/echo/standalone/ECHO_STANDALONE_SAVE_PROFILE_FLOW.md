# ECHO Standalone Save Profile Flow

Phase 15.8 adds the first user-facing save profile flow on top of the Phase 14.7 save runtime. It keeps the underlying save system deterministic and headless-safe while exposing the launcher/game actions a player expects: new game, continue, autosave, manual save, corruption warning, backup restore, compatibility-safe migration prompts, and incompatible-mod recovery blocking.

This is still a runtime foundation, not a full save browser. The flow proves the contracts and UI surface needed by the desktop launcher and windowed runtime.

## Runtime Pieces

- `EchoSaveProfileFlowRuntime` orchestrates a deterministic Ashfall profile flow.
- `EchoSaveProfileFlowResult` captures the save runtime, UI runtime, commits, warnings, restore result, migration prompt, and summary.
- `EchoSaveProfileSlotSummary` describes visible save slots with health, backup, continue, and warning state.
- `EchoSaveProfileContinueFlow` records the selected continue target and available actions.
- `EchoSaveProfileRestoreResult` proves a corrupted slot can be restored from a known backup.
- `EchoSaveProfileMigrationPrompt` exposes plan-only format migration requirements without executing migration.
- `EchoSaveModSetCompatibilityChecker` compares saved module ids with the current module set and blocks unsafe slot loading until backup restore or migration approval.
- The OpenGL client save path can attach a 160x90 framebuffer-sourced thumbnail to `client/thumbnail.png`; headless/runtime smokes keep the generated saved-world camera thumbnail fallback.
- `reports/echo/standalone/client-save-continue.json` is the durable client evidence for this path: it proves captured OpenGL framebuffer thumbnail persistence, manifest metadata, World Select texture eligibility, corrupt-thumbnail deterministic fallback, disk Continue restore, incompatible content blocking, backup/migration readiness, and delete-world Continue disabling.

## User Flow

The deterministic smoke flow creates one primary Ashfall slot, one recovered warning slot, and one incompatible-mod recovery slot:

```text
New Game -> creates ashfall-camp-01
Autosave -> updates ashfall-camp-01 and creates backup 1
Manual Save -> updates ashfall-camp-01 and creates backup 2
Continue -> selects ashfall-camp-01 manual save
Corruption Warning -> detects checksum mismatch on ashfall-corrupt-01
Restore Backup -> restores ashfall-corrupt-01 from a previous backup
Migration Prompt -> shows format 1 -> 2, backup required, manual approval required
Incompatible Mods -> blocks ashfall-missing-module-01 because echoworldcore is absent from the current module set
Recovery -> offers backup restore or migration prompt before loading the blocked slot
```

The UI surface is an `EchoStaticScreen` rendered through `EchoUiRuntime`, so it remains safe for headless smoke testing and later windowed presentation.

## Safety Rules

- New game, autosave, and manual save go through `EchoSaveTransaction`.
- Existing slot updates create backups before overwrite.
- Corruption warnings are based on the manifest checksum checker.
- Backup restore is explicit and journaled.
- Migration is prompt-only in this phase. The runtime plans a migration and shows requirements, but does not mutate save data automatically.
- Incompatible module sets are checked before Continue opens a world; blocked slots stay visible with backup and migration recovery actions.
- The flow does not inspect Minecraft saves, import platform saves, or depend on launcher state.

## Smoke Harness Coverage

`EchoRuntimeSaveProfileFlowSmokeHarness` proves:

- save profile flow, save runtime, and UI runtime are service-bound.
- new game writes three files.
- autosave and manual save create backups.
- manual client saves persist a readable save-slot thumbnail PNG, expose source metadata, validate World Select texture eligibility, and validate corrupt thumbnail fallback.
- continue selects the latest healthy primary slot.
- corrupted slot detection reports `CHECKSUM_MISMATCH`.
- backup restore returns the corrupted slot to a healthy state.
- migration prompt is visible, backup-required, manual-approval-only, and non-executing.
- incompatible module set detection names missing/added modules, blocks Continue, and exposes backup restore or migration prompt recovery.
- UI lines include New Game, Continue, Autosave, Manual Save, Corruption Warning, Restore Backup, and Migration Prompt.

## Out Of Scope

Phase 15.8 does not add cloud saves, arbitrary user profile browsing, automatic migration execution, Minecraft save import, or full campaign progression.
