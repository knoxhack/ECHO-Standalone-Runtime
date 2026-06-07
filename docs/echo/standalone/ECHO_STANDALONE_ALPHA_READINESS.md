# ECHO Standalone Alpha Readiness

Phase 14.20 adds the alpha readiness gate for the standalone runtime. The gate is intentionally conservative: if a required phase artifact, launcher verification check, repair-plan invariant, or support-bundle manifest check is missing, the alpha build is blocked.

## Gate Scope

`EchoStandaloneAlphaReadinessGate` checks the evidence trail for Phase 14.1 through Phase 14.20:

- runtime architecture, contracts, boundaries, boot, lifecycle, tick loop, and crash handling.
- module runtime, PackOS, asset/data loading, UI, saves, registries, world, entities, items, gameplay, renderer, audio, network, scripting, compatibility, vertical slice, and launcher reports.
- launcher support-bundle readiness through the Phase 14.19 verify-only path.
- planning-only repair behavior.

The gate does not launch the standalone runtime. It uses the launcher verify-only request so readiness checks cannot open devices, mutate saves, or disturb external launch paths.

## Readiness Result

The ready workspace returns:

```text
gateId: echo:standalone-alpha-readiness
status: READY
checks: 48
blocked: 0
supportBundleReady: true
```

The missing-workspace smoke fixture returns `BLOCKED` and reports blocking failures. That proves the gate fails closed instead of producing a permissive alpha result.

## Blocking Policy

Every Phase 14.20 readiness check is blocking. The alpha build is blocked when:

- a required documentation artifact is missing.
- a required deterministic report is missing.
- launcher verification fails.
- launcher repair planning is not planning-only or contains actions for the current workspace.
- the support bundle manifest is missing entries.

## Out Of Scope

Phase 14.20 does not:

- certify gameplay completeness.
- launch a desktop window.
- open a real audio device.
- open socket networking.
- execute arbitrary scripts.
- mutate user saves.
- perform automatic repair.

It certifies that the standalone runtime foundation has deterministic evidence and a blocking alpha gate.
