package dev.echo.standalone.runtime.modules;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class EchoRuntimeModulePermissionCatalog {
    public static final String CONTENT_REGISTER = "content.register";
    public static final String SERVICES_EXPORT = "services.export";
    public static final String SERVICES_IMPORT = "services.import";
    public static final String ASSETS_READ = "assets.read";
    public static final String CLIENT_CONFIG = "client.config";
    public static final String DATA_PERSISTENCE = "data.persistence";

    private static final Set<String> KNOWN_PERMISSIONS = Set.of(
            CONTENT_REGISTER,
            SERVICES_EXPORT,
            SERVICES_IMPORT,
            "adapter.echo_native",
            "adapter.echo_runtime_standalone",
            "adapter.neoforge",
            "accessibility.read",
            "accessibility.write",
            "ai.prompt_bundle",
            ASSETS_READ,
            "assets.write",
            "balance.read",
            "bridge.codex_session_control",
            "bridge.local_loopback",
            "blueprints.read",
            "blueprints.write",
            "capability.read",
            CLIENT_CONFIG,
            "command_center.local_loopback",
            "commands.register",
            "content.blocks",
            "content.creatures",
            "content.items",
            "content.recipes",
            "content.structures",
            "content.worldgen",
            "creator.codex_bridge",
            "creator.dashboard",
            "creator.drafts",
            "creator.exports",
            "curation.read",
            DATA_PERSISTENCE,
            "data.world",
            "data:foundation",
            "dependency.read",
            "diagnostics.read",
            "diagnostics.write",
            "disaster.read",
            "disaster.write",
            "discord.ipc",
            "discord.relay",
            "echo.api.public",
            "equipment.read",
            "equipment.write",
            "expedition.read",
            "expedition.write",
            "faction.read",
            "faction.write",
            "gameplay.content",
            "hazard.read",
            "hazard.write",
            "holomap.layers",
            "hud.widgets",
            "index.inventory_overlay",
            "index.recipes",
            "inventory.overlay",
            "lens.scanners",
            "launcher:dependency",
            "localization.read",
            "localization.write",
            "migration.read",
            "migration.write",
            "missions.objectives",
            "missions.routes",
            "network.payloads",
            "network.sync",
            "pack.read",
            "pack.root",
            "permissions.validate",
            "platform.contracts",
            "platform.services",
            "playtest.read",
            "playtest.write",
            "player.activity_read",
            "player.data",
            "policy.read",
            "policy.write",
            "repair.plan",
            "reports.read",
            "reports.write",
            "resources.data",
            "registry:foundation",
            "ruins.read",
            "ruins.write",
            "runtime.health",
            "runtime.metrics",
            "saves.read",
            "save.profile",
            "schema.registry",
            "season.read",
            "season.write",
            "server.commands",
            "server.rules",
            "serverops.read",
            "serverops.write",
            "server.status.publish",
            "server.status.read",
            "session.read",
            "session.write",
            "settlement.read",
            "settlement.write",
            "skill.read",
            "skill.write",
            "supply.read",
            "supply.write",
            "telemetry.read",
            "telemetry.write",
            "textureforge.prompts",
            "territory.read",
            "territory.write",
            "ui.overlays",
            "ui.screens",
            "validation.pack",
            "workspace.read",
            "world.atmosphere",
            "world.anomaly_state",
            "world.claims",
            "world.fragment_state",
            "world.hazards",
            "world.read",
            "world.region_state",
            "world.regions",
            "world.survey_state",
            "world.teleport",
            "world.write"
    );

    private EchoRuntimeModulePermissionCatalog() {
    }

    public static List<String> knownPermissions() {
        return KNOWN_PERMISSIONS.stream().sorted().toList();
    }

    public static List<String> unknownPermissions(EchoRuntimeModuleDescriptor descriptor) {
        TreeSet<String> unknown = new TreeSet<>(descriptor.permissions());
        unknown.removeAll(KNOWN_PERMISSIONS);
        return unknown.stream().toList();
    }
}
