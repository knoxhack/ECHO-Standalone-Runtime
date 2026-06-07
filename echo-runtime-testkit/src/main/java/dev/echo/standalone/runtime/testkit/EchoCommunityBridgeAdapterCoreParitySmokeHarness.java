package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoCommunityBridgeContracts;

import java.util.Map;

public final class EchoCommunityBridgeAdapterCoreParitySmokeHarness {
    private EchoCommunityBridgeAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> standaloneProbe = EchoCommunityBridgeContracts.referenceProbe();
        require(EchoCommunityBridgeContracts.CONTRACT_IDS.size() == 4,
                "CommunityBridge AdapterCore contracts should cover four executable surfaces");
        require(EchoCommunityBridgeContracts.CONTRACT_IDS.contains(
                        EchoCommunityBridgeContracts.SERVER_STATUS_CONTRACT_ID),
                "CommunityBridge standalone bridge must expose server status contract");
        require(EchoCommunityBridgeContracts.CONTRACT_IDS.contains(
                        EchoCommunityBridgeContracts.LAUNCHER_CHAT_CONTRACT_ID),
                "CommunityBridge standalone bridge must expose launcher chat contract");
        require(EchoCommunityBridgeContracts.referenceProbePassed(standaloneProbe),
                "CommunityBridge standalone contract probe failed: " + standaloneProbe);
        require(EchoCommunityBridgeContracts.launcherChatLine("launcher", "User", "/op Knox").isEmpty(),
                "CommunityBridge standalone bridge must block slash-command relay");
        require(EchoCommunityBridgeContracts.sanitizeDiscordText("hello @everyone\u0000", 80).contains("@\u200B"),
                "CommunityBridge standalone bridge must defang Discord mentions");
        System.out.println("communitybridge adaptercore parity smoke PASS contracts="
                + EchoCommunityBridgeContracts.CONTRACT_IDS.size()
                + " surfaces=server_status,launcher_chat,discord_sanitization,player_identity");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
