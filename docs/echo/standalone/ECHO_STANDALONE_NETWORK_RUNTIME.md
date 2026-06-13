# ECHO Standalone Networking Runtime

Phase 14.15 adds the first standalone networking runtime. It introduces protocol contracts, a packet registry, a local client/server handshake, simple entity sync, inventory sync, a device-free transport, and diagnostics.

This phase deliberately does not open sockets or depend on platform networking libraries. The debug transport records packets in memory so protocol behavior and snapshot payloads can be validated deterministically.

## Runtime Pieces

- `EchoNetworkRuntime` creates and service-binds the debug local network.
- `EchoNetworkProtocol` defines the protocol id, version, compatibility floor, and advertised features.
- `EchoNetworkEndpoint` describes client and server endpoints.
- `EchoNetworkPacketRegistry` stores deterministic packet type definitions.
- `EchoLocalNetworkTransport` records sent packets without opening sockets.
- `EchoNetworkHandshakeService` performs the client/server protocol handshake.
- `EchoNetworkSyncService` emits entity and inventory snapshot packets.
- `EchoEntitySyncSnapshot` converts entity state into a stable sync payload.
- `EchoInventorySyncSnapshot` converts inventory containers into a stable sync payload.
- `EchoNetworkDiagnostics` records transport and handshake diagnostics.

## Debug Protocol

The Phase 14.15 debug protocol is:

```text
protocol: echo.standalone.protocol
version: 1
minCompatibleVersion: 1
features: diagnostics, entity_sync, inventory_sync
```

The debug packet registry contains five packet types:

```text
echo:handshake
echo:handshake_ack
echo:entity_sync
echo:inventory_sync
echo:diagnostic
```

## Local Handshake

The debug runtime creates two endpoints:

```text
client: echo:debug-client
server: echo:debug-server
```

The client sends a protocol offer and the server replies with an acknowledgement. Matching protocol id and compatible versions are required for acceptance.

## Sync Coverage

The initial sync layer emits authoritative snapshots from existing runtime state:

- entity sync snapshots `player-001` and `scavenger-001`
- inventory sync snapshots `inventory:player-001` and `container:crash-cache`
- five item stacks across both inventories

The transport records four packets in a stable order: handshake, handshake acknowledgement, entity sync, and inventory sync.

## Transport Safety

The local transport is intentionally in-memory. It records packets, target endpoints, payloads, sequence numbers, and diagnostics. It does not import or start socket transports, Java networking APIs, Netty, Minecraft, NeoForge, renderer backends, or audio devices.

## Smoke Harness Coverage

The Phase 14.15 smoke harness proves:

- network runtime result, packet registry, local transport, diagnostics, handshake service, sync service, and handshake result are service-bound.
- the debug protocol id, version, endpoint roles, and feature list are stable.
- five packet types are registered.
- the local client/server handshake is accepted.
- the first two packets are the handshake request and acknowledgement.
- entity sync emits two snapshots.
- inventory sync emits two container snapshots and five item stacks.
- packet sequence numbers remain deterministic.
- the client receives acknowledgement, entity sync, and inventory sync packets.
- diagnostics include four sends and one handshake result, with no errors.

The harness also writes concrete non-placeholder evidence to the Phase 14.15 network reports under `reports/echo/standalone`. `verifyStandaloneNetworkRuntime` regenerates those reports, rejects bootstrap schemas, and requires PASS evidence for service binding, protocol compatibility, packet registry and ordering, accepted handshake, socketless local transport, entity sync, inventory sync, diagnostics, and networking boundary checks.

## Out Of Scope

Phase 14.15 does not:

- open sockets
- run a multiplayer server
- use Java networking APIs
- use Netty or websocket APIs
- serialize binary packets
- perform prediction or rollback
- reconcile remote authority
- persist sessions

The next phase is Phase 14.16, the ECHO Scripting / Rules Runtime.
