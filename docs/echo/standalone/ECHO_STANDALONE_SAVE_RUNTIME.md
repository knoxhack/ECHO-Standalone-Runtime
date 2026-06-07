# ECHO Standalone Save Runtime

Phase 14.7 adds the standalone save runtime. It defines save profiles, save slots, manifests, transactional writes, backups, migration plans, corruption checks, and recovery journals without depending on Minecraft, NeoForge, or launcher state.

The runtime is filesystem-backed and intentionally conservative. Writes are staged first, committed through a manifest update, and recorded in a recovery journal so later launcher and vertical-slice phases can inspect what happened before attempting repair.

## Runtime Pieces

- `EchoSaveRuntime` opens a save profile and binds save services.
- `EchoSaveProfile` identifies the player/profile root, pack id, format version, and profile metadata.
- `EchoSaveSlot` maps a profile to a concrete slot directory.
- `EchoSaveManifest` records format version, tracked files, SHA-256 checksums, backup ids, and metadata.
- `EchoSaveTransaction` stages text payloads under `.transactions/<transactionId>/` and commits them into `data/`.
- `EchoSaveBackupService` copies the previous manifest and data files before an overwrite commit.
- `EchoSaveMigrationPlanner` produces backup-required forward migration plans.
- `EchoSaveCorruptionChecker` verifies manifests, file presence, checksums, and unfinished transaction state.
- `EchoSaveRecoveryJournal` appends deterministic transaction, backup, migration, and corruption-check events.

## Slot Layout

```text
profile-root/
  recovery-journal.log
  backups/
    slot-a-tx-002/
      manifest.json
      data/
  slots/
    slot-a/
      manifest.json
      data/
        player/state.json
        world/summary.json
      .transactions/
```

The manifest is the authority for save integrity. Each file entry stores a normalized relative path, SHA-256 checksum, and byte count.

## Transaction Model

Save writes use this flow:

1. `beginTransaction(slotId, transactionId)` appends `BEGIN`.
2. `writeText(relativePath, content)` records staged payloads in memory.
3. `commit(metadata)` writes staged files to a transaction directory and appends `STAGED`.
4. If a previous manifest exists, the backup service copies the old manifest and data and appends `BACKUP_CREATED`.
5. Staged files move into slot `data/`.
6. A new manifest is written through a temporary file and moved into place.
7. The transaction staging directory is removed and `COMMITTED` is appended.

This phase does not claim crash-proof persistence on every filesystem. It establishes the runtime boundary and deterministic evidence model needed for later launcher repair and support bundle work.

## Smoke Harness Coverage

The Phase 14.7 smoke harness proves:

- save services bind into the runtime service registry.
- the first transaction writes two files and produces a manifest.
- a fresh manifest passes corruption checking.
- a format `1 -> 2` migration plan is produced and requires backup.
- a second transaction creates a backup of previous data.
- latest metadata is written into the manifest.
- a deliberately corrupted file produces a checksum mismatch.
- the recovery journal records transaction, migration, backup, and corruption events.

## Out Of Scope

Phase 14.7 does not:

- serialize world chunks or entities
- import Minecraft saves
- automatically execute migrations
- repair corrupted files
- encrypt or cloud-sync saves
- launch a playable session
- depend on Minecraft or NeoForge save APIs

The next phase is Phase 14.8, the ECHO Data/Registry Runtime.

## Phase 15.8 Profile Flow

Phase 15.8 layers a user-facing save profile flow over this runtime. It uses the same transaction, manifest, backup, corruption, restore, journal, and migration planner contracts to expose New Game, Continue, Autosave, Manual Save, Corruption Warning, Restore Backup, and Migration Prompt states through the platform-independent UI runtime.

Migration remains plan-only: the profile flow shows backup and manual approval requirements, but does not execute save migration automatically.
