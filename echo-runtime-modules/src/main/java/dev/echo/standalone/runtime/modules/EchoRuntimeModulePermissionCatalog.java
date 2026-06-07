package dev.echo.standalone.runtime.modules;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class EchoRuntimeModulePermissionCatalog {
    public static final String CONTENT_REGISTER = "content.register";
    public static final String SERVICES_EXPORT = "services.export";
    public static final String SERVICES_IMPORT = "services.import";

    private static final Set<String> KNOWN_PERMISSIONS = Set.of(
            CONTENT_REGISTER,
            SERVICES_EXPORT,
            SERVICES_IMPORT,
            "adapter.echo_native",
            "adapter.echo_runtime_standalone",
            "adapter.neoforge",
            "ai.prompt_bundle",
            "assets.read",
            "bridge.codex_session_control",
            "bridge.local_loopback",
            "client.config",
            "command_center.local_loopback",
            "commands.register",
            "content.blocks",
            "content.recipes",
            "content.worldgen",
            "creator.codex_bridge",
            "creator.dashboard",
            "creator.drafts",
            "creator.exports",
            "data.persistence",
            "data.world",
            "diagnostics.read",
            "diagnostics.write",
            "discord.ipc",
            "discord.relay",
            "echo.api.public",
            "gameplay.content",
            "holomap.layers",
            "index.inventory_overlay",
            "index.recipes",
            "inventory.overlay",
            "lens.scanners",
            "missions.objectives",
            "missions.routes",
            "network.payloads",
            "network.sync",
            "pack.read",
            "pack.root",
            "platform.contracts",
            "platform.services",
            "player.activity_read",
            "player.data",
            "repair.plan",
            "reports.read",
            "reports.write",
            "resources.data",
            "runtime.health",
            "runtime.metrics",
            "schema.registry",
            "server.commands",
            "server.status.publish",
            "server.status.read",
            "textureforge.prompts",
            "ui.overlays",
            "ui.screens",
            "validation.pack",
            "workspace.read",
            "world.atmosphere",
            "world.claims",
            "world.hazards",
            "world.read",
            "world.regions",
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
