package dev.echo.standalone.runtime.testkit;

import dev.echo.nativeplatform.contracts.EchoNativeRegisteredService;
import dev.echo.nativeplatform.contracts.EchoNativeServiceRegistry;
import dev.echo.standalone.runtime.app.EchoRuntimeLogBridge;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleDescriptor;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleLifecycle;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EchoRuntimeRealModuleExecutionSmokeHarness {
    private static final List<String> DATA_ONLY_DEPENDENCY_MODULES = List.of(
            "echofoundationcore",
            "echomaterialcore",
            "echotoolcore",
            "echostationcore",
            "echoworldstarter",
            "echocommonloot",
            "echocreatureroles"
    );

    private static final List<RealModuleSpec> REAL_MODULES = List.of(
            new RealModuleSpec(
                    "echocore",
                    "com.knoxhack.echocore.EchoCoreNativeModule",
                    "src/main/java/com/knoxhack/echocore/EchoCoreNativeModule.java",
                    "legacyNativeBootstrapExecuted"
            ),
            new RealModuleSpec(
                    "echoplatformcore",
                    "com.knoxhack.echo.platformcore.EchoPlatformCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/platformcore/EchoPlatformCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoschemacore",
                    "com.knoxhack.echo.schemacore.EchoSchemaCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/schemacore/EchoSchemaCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echovalidationcore",
                    "com.knoxhack.echo.validationcore.EchoValidationCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/validationcore/EchoValidationCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoadaptercore",
                    "com.knoxhack.echo.adaptercore.EchoAdapterCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/adaptercore/EchoAdapterCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echocontentcore",
                    "com.knoxhack.echo.contentcore.EchoContentCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/contentcore/EchoContentCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoassetcore",
                    "com.knoxhack.echo.assetcore.EchoAssetCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/assetcore/EchoAssetCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echorecipecore",
                    "com.knoxhack.echo.recipecore.EchoRecipeCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/recipecore/EchoRecipeCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echopackcore",
                    "com.knoxhack.echo.packcore.EchoPackCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/packcore/EchoPackCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echometadatacore",
                    "com.knoxhack.echo.metadatacore.EchoMetadataCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/metadatacore/EchoMetadataCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echomodulegraph",
                    "com.knoxhack.echo.modulegraph.EchoModuleGraphNativeModule",
                    "src/main/java/com/knoxhack/echo/modulegraph/EchoModuleGraphNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echonetcore",
                    "com.knoxhack.echonetcore.EchoNetCoreNativeModule",
                    "src/main/java/com/knoxhack/echonetcore/EchoNetCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echodatacore",
                    "com.knoxhack.echodatacore.EchoDataCoreNativeModule",
                    "src/main/java/com/knoxhack/echodatacore/EchoDataCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoworldcore",
                    "com.knoxhack.echoworldcore.EchoWorldCoreNativeModule",
                    "src/main/java/com/knoxhack/echoworldcore/EchoWorldCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoatmospherecore",
                    "com.knoxhack.echo.atmospherecore.EchoAtmosphereCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/atmospherecore/EchoAtmosphereCoreNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echoweathercore",
                    "com.knoxhack.echoweathercore.EchoWeatherCoreNativeModule",
                    "src/main/java/com/knoxhack/echoweathercore/EchoWeatherCoreNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echoprogressioncore",
                    "com.knoxhack.echo.progressioncore.EchoProgressionCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/progressioncore/EchoProgressionCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echosocialcore",
                    "com.knoxhack.echo.socialcore.EchoSocialCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/socialcore/EchoSocialCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoeventcore",
                    "com.knoxhack.echo.eventcore.EchoEventCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/eventcore/EchoEventCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoencountercore",
                    "com.knoxhack.echo.encountercore.EchoEncounterCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/encountercore/EchoEncounterCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoeconomycore",
                    "com.knoxhack.echo.economycore.EchoEconomyCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/economycore/EchoEconomyCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoquestdirector",
                    "com.knoxhack.echo.questdirector.EchoQuestDirectorNativeModule",
                    "src/main/java/com/knoxhack/echo/questdirector/EchoQuestDirectorNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echonpcore",
                    "com.knoxhack.echo.npcore.EchoNpcCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/npcore/EchoNpcCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoguidecore",
                    "com.knoxhack.echo.guidecore.EchoGuideCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/guidecore/EchoGuideCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echolorecore",
                    "com.knoxhack.echo.lorecore.EchoLoreCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/lorecore/EchoLoreCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echostatuscore",
                    "com.knoxhack.echo.statuscore.EchoStatusCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/statuscore/EchoStatusCoreNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echospawncore",
                    "com.knoxhack.echo.spawncore.EchoSpawnCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/spawncore/EchoSpawnCoreNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echostructurecore",
                    "com.knoxhack.echo.structurecore.EchoStructureCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/structurecore/EchoStructureCoreNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echolootcore",
                    "com.knoxhack.echo.lootcore.EchoLootCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/lootcore/EchoLootCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echobiomecore",
                    "com.knoxhack.echo.biomecore.EchoBiomeCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/biomecore/EchoBiomeCoreNativeModule.java",
                    "nativeModuleEntrypoint"
            ),
            new RealModuleSpec(
                    "echodifficultycore",
                    "com.knoxhack.echo.difficultycore.EchoDifficultyCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/difficultycore/EchoDifficultyCoreNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echocombatcore",
                    "com.knoxhack.echo.combatcore.EchoCombatCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/combatcore/EchoCombatCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echocreaturecore",
                    "com.knoxhack.echo.creaturecore.EchoCreatureCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/creaturecore/EchoCreatureCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoarmory",
                    "com.knoxhack.echoarmory.EchoArmoryNativeModule",
                    "src/main/java/com/knoxhack/echoarmory/EchoArmoryNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoagriculturereclamation",
                    "com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamationNativeModule",
                    "src/main/java/com/knoxhack/echoagriculturereclamation/EchoAgricultureReclamationNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echomachinecore",
                    "com.knoxhack.echo.machinecore.EchoMachineCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/machinecore/EchoMachineCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echopowercore",
                    "com.knoxhack.echo.powercore.EchoPowerCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/powercore/EchoPowerCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echologisticscore",
                    "com.knoxhack.echo.logisticscore.EchoLogisticsCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/logisticscore/EchoLogisticsCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echosoundcore",
                    "com.knoxhack.echosoundcore.EchoSoundCoreNativeModule",
                    "src/main/java/com/knoxhack/echosoundcore/EchoSoundCoreNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echohealthcore",
                    "com.knoxhack.echo.healthcore.EchoHealthCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/healthcore/EchoHealthCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoagentcore",
                    "com.knoxhack.echo.agentcore.EchoAgentCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/agentcore/EchoAgentCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echobridgecore",
                    "com.knoxhack.echo.bridgecore.EchoBridgeCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/bridgecore/EchoBridgeCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoreportcore",
                    "com.knoxhack.echo.reportcore.EchoReportCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/reportcore/EchoReportCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echocameracore",
                    "com.knoxhack.echo.cameracore.EchoCameraCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/cameracore/EchoCameraCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echocinematiccore",
                    "com.knoxhack.echo.cinematiccore.EchoCinematicCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/cinematiccore/EchoCinematicCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echocodexcore",
                    "com.knoxhack.echo.codexcore.EchoCodexCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/codexcore/EchoCodexCoreNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echocreatorcore",
                    "com.knoxhack.echo.creatorcore.EchoCreatorCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/creatorcore/EchoCreatorCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echorendercore",
                    "com.knoxhack.echorendercore.EchoRenderCoreNativeModule",
                    "src/main/java/com/knoxhack/echorendercore/EchoRenderCoreNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echoscreencore",
                    "com.knoxhack.echoscreencore.EchoScreenCoreNativeModule",
                    "src/main/java/com/knoxhack/echoscreencore/EchoScreenCoreNativeModule.java",
                    "nativeModuleEntrypoint"
            ),
            new RealModuleSpec(
                    "echoscriptcore",
                    "com.knoxhack.echo.scriptcore.EchoScriptCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/scriptcore/EchoScriptCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoruntimeguard",
                    "com.knoxhack.echoruntimeguard.EchoRuntimeGuardNativeModule",
                    "src/main/java/com/knoxhack/echoruntimeguard/EchoRuntimeGuardNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echocommunitybridge",
                    "com.knoxhack.echocommunitybridge.EchoCommunityBridgeNativeModule",
                    "src/main/java/com/knoxhack/echocommunitybridge/EchoCommunityBridgeNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoindex",
                    "com.knoxhack.echoindex.EchoIndexNativeModule",
                    "src/main/java/com/knoxhack/echoindex/EchoIndexNativeModule.java",
                    "echoIndexNativeLifecycle"
            ),
            new RealModuleSpec(
                    "echoholomap",
                    "com.knoxhack.echoholomap.EchoHoloMapNativeModule",
                    "src/main/java/com/knoxhack/echoholomap/EchoHoloMapNativeModule.java",
                    "nativeModuleEntrypoint"
            ),
            new RealModuleSpec(
                    "echolens",
                    "com.knoxhack.echolens.EchoLensNativeModule",
                    "src/main/java/com/knoxhack/echolens/EchoLensNativeModule.java",
                    "nativeModuleEntrypoint"
            ),
            new RealModuleSpec(
                    "echoterminal",
                    "com.knoxhack.echoterminal.EchoTerminalNativeModule",
                    "src/main/java/com/knoxhack/echoterminal/EchoTerminalNativeModule.java",
                    "nativeModuleEntrypoint"
            ),
            new RealModuleSpec(
                    "echotextureforge",
                    "com.knoxhack.echotextureforge.EchoTextureForgeNativeModule",
                    "src/main/java/com/knoxhack/echotextureforge/EchoTextureForgeNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echothemecore",
                    "com.knoxhack.echothemecore.EchoThemeCoreNativeModule",
                    "src/main/java/com/knoxhack/echothemecore/EchoThemeCoreNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echotutorialcore",
                    "com.knoxhack.echotutorialcore.EchoTutorialCoreNativeModule",
                    "src/main/java/com/knoxhack/echotutorialcore/EchoTutorialCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echowiki",
                    "com.knoxhack.echowiki.EchoWikiNativeModule",
                    "src/main/java/com/knoxhack/echowiki/EchoWikiNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echobasegrid",
                    "com.knoxhack.echobasegrid.EchoBaseGridNativeModule",
                    "src/main/java/com/knoxhack/echobasegrid/EchoBaseGridNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoindustrialnexus",
                    "com.knoxhack.echoindustrialnexus.EchoIndustrialNexusNativeModule",
                    "src/main/java/com/knoxhack/echoindustrialnexus/EchoIndustrialNexusNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echologisticsnetwork",
                    "com.knoxhack.echologisticsnetwork.EchoLogisticsNetworkNativeModule",
                    "src/main/java/com/knoxhack/echologisticsnetwork/EchoLogisticsNetworkNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echomultiblockcore",
                    "com.knoxhack.echomultiblockcore.EchoMultiblockCoreNativeModule",
                    "src/main/java/com/knoxhack/echomultiblockcore/EchoMultiblockCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echopowergrid",
                    "com.knoxhack.echopowergrid.EchoPowerGridNativeModule",
                    "src/main/java/com/knoxhack/echopowergrid/EchoPowerGridNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echorecovery",
                    "com.knoxhack.echorecovery.EchoRecoveryNativeModule",
                    "src/main/java/com/knoxhack/echorecovery/EchoRecoveryNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echomissioncore",
                    "com.knoxhack.echomissioncore.EchoMissionCoreNativeModule",
                    "src/main/java/com/knoxhack/echomissioncore/EchoMissionCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoplayercore",
                    "com.knoxhack.echoplayercore.EchoPlayerCoreNativeModule",
                    "src/main/java/com/knoxhack/echoplayercore/EchoPlayerCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoinputcore",
                    "com.knoxhack.echo.inputcore.EchoInputCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/inputcore/EchoInputCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echohudcore",
                    "com.knoxhack.echo.hudcore.EchoHudCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/hudcore/EchoHudCoreNativeModule.java",
                    "nativeActivationSurface"
            ),
            new RealModuleSpec(
                    "echonotificationcore",
                    "com.knoxhack.echo.notificationcore.EchoNotificationCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/notificationcore/EchoNotificationCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echovehiclecore",
                    "com.knoxhack.echo.vehiclecore.EchoVehicleCoreNativeModule",
                    "src/main/java/com/knoxhack/echo/vehiclecore/EchoVehicleCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echoblockworks",
                    "com.knoxhack.echoblockworks.EchoBlockworksNativeModule",
                    "src/main/java/com/knoxhack/echoblockworks/EchoBlockworksNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echopresencelink",
                    "com.knoxhack.echopresencelink.EchoPresenceLinkNativeModule",
                    "src/main/java/com/knoxhack/echopresencelink/EchoPresenceLinkNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoconvoyprotocol",
                    "com.knoxhack.echoconvoyprotocol.EchoConvoyProtocolNativeModule",
                    "src/main/java/com/knoxhack/echoconvoyprotocol/EchoConvoyProtocolNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "echofamiliarcore",
                    "com.knoxhack.echofamiliarcore.EchoFamiliarCoreNativeModule",
                    "src/main/java/com/knoxhack/echofamiliarcore/EchoFamiliarCoreNativeModule.java",
                    "nativeSurfaceEntrypoint"
            ),
            new RealModuleSpec(
                    "signalos",
                    "com.knoxhack.signalos.EchoSignalOsNativeModule",
                    "src/main/java/com/knoxhack/signalos/EchoSignalOsNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echospellcore",
                    "com.knoxhack.echospellcore.EchoSpellCoreNativeModule",
                    "src/main/java/com/knoxhack/echospellcore/EchoSpellCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoritualcore",
                    "com.knoxhack.echoritualcore.EchoRitualCoreNativeModule",
                    "src/main/java/com/knoxhack/echoritualcore/EchoRitualCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echocursecore",
                    "com.knoxhack.echocursecore.EchoCurseCoreNativeModule",
                    "src/main/java/com/knoxhack/echocursecore/EchoCurseCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoriftworlds",
                    "com.knoxhack.echoriftworlds.EchoRiftWorldsNativeModule",
                    "src/main/java/com/knoxhack/echoriftworlds/EchoRiftWorldsNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoblackboxprotocol",
                    "com.knoxhack.echoblackboxprotocol.EchoBlackboxProtocolNativeModule",
                    "src/main/java/com/knoxhack/echoblackboxprotocol/EchoBlackboxProtocolNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echonexusprotocol",
                    "com.knoxhack.echonexusprotocol.EchoNexusProtocolNativeModule",
                    "src/main/java/com/knoxhack/echonexusprotocol/EchoNexusProtocolNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoorbitalremnants",
                    "com.knoxhack.echoorbitalremnants.EchoOrbitalRemnantsNativeModule",
                    "src/main/java/com/knoxhack/echoorbitalremnants/EchoOrbitalRemnantsNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoprimecore",
                    "com.knoxhack.echoprimecore.EchoPrimeCoreNativeModule",
                    "src/main/java/com/knoxhack/echoprimecore/EchoPrimeCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echostationfall",
                    "com.knoxhack.echostationfall.EchoStationfallNativeModule",
                    "src/main/java/com/knoxhack/echostationfall/EchoStationfallNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echogrimoire",
                    "com.knoxhack.echogrimoire.EchoGrimoireNativeModule",
                    "src/main/java/com/knoxhack/echogrimoire/EchoGrimoireNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoarcanacore",
                    "com.knoxhack.echoarcanacore.EchoArcanaCoreNativeModule",
                    "src/main/java/com/knoxhack/echoarcanacore/EchoArcanaCoreNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echorelictech",
                    "com.knoxhack.echorelictech.EchoRelicTechNativeModule",
                    "src/main/java/com/knoxhack/echorelictech/EchoRelicTechNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoarcaneindex",
                    "com.knoxhack.echoarcaneindex.EchoArcaneIndexNativeModule",
                    "src/main/java/com/knoxhack/echoarcaneindex/EchoArcaneIndexNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoaetherworks",
                    "com.knoxhack.echoaetherworks.EchoAetherWorksNativeModule",
                    "src/main/java/com/knoxhack/echoaetherworks/EchoAetherWorksNativeModule.java",
                    "adaptercoreNativeEntrypoint"
            ),
            new RealModuleSpec(
                    "echoashfallprotocol",
                    "com.knoxhack.echoashfallprotocol.EchoAshfallNativeModule",
                    "src/main/java/com/knoxhack/echoashfallprotocol/EchoAshfallNativeModule.java",
                    "nativeModuleEntrypoint"
            ),
            new RealModuleSpec(
                    "echogalacticcore",
                    "com.knoxhack.echogalacticcore.EchoGalacticCoreNativeModule",
                    "src/main/java/com/knoxhack/echogalacticcore/EchoGalacticCoreNativeModule.java",
                    "derivedFrom"
            ),
            new RealModuleSpec(
                    "echoaddonapi",
                    "dev.echo.api.addon.EchoAddonApiNativeModule",
                    "src/main/java/dev/echo/api/addon/EchoAddonApiNativeModule.java",
                    "echoAddonApiNativeReady"
            )
    );
    private static final String SCHEMA_CORE_MODULE_ID = "echoschemacore";
    private static final String SCHEMA_CORE_SERVICE_ID = "service.echoschemacore.native_activation";
    private static final String VALIDATION_CORE_MODULE_ID = "echovalidationcore";
    private static final String VALIDATION_CORE_SERVICE_ID = "service.echovalidationcore.native_activation";
    private static final String CONTENT_CORE_MODULE_ID = "echocontentcore";
    private static final String CONTENT_CORE_SERVICE_ID = "service.echocontentcore.native_activation";
    private static final String ASSET_CORE_MODULE_ID = "echoassetcore";
    private static final String ASSET_CORE_SERVICE_ID = "service.echoassetcore.native_activation";
    private static final String RECIPE_CORE_MODULE_ID = "echorecipecore";
    private static final String RECIPE_CORE_SERVICE_ID = "service.echorecipecore.native_activation";
    private static final String PACK_CORE_MODULE_ID = "echopackcore";
    private static final String PACK_CORE_SERVICE_ID = "adaptercore.echopackcore.contract";
    private static final String METADATA_CORE_MODULE_ID = "echometadatacore";
    private static final String METADATA_CORE_SERVICE_ID = "service.echometadatacore.native_activation";
    private static final String MODULE_GRAPH_MODULE_ID = "echomodulegraph";
    private static final String MODULE_GRAPH_SERVICE_ID = "service.echomodulegraph.native_activation";
    private static final String NET_CORE_MODULE_ID = "echonetcore";
    private static final String NET_CORE_SERVICE_ID = "adaptercore.echonetcore.contract";
    private static final String DATA_CORE_MODULE_ID = "echodatacore";
    private static final String DATA_CORE_SERVICE_ID = "adaptercore.echodatacore.contract";
    private static final String WORLD_CORE_MODULE_ID = "echoworldcore";
    private static final String WORLD_CORE_SERVICE_ID = "adaptercore.echoworldcore.contract";
    private static final String ATMOSPHERE_CORE_MODULE_ID = "echoatmospherecore";
    private static final String ATMOSPHERE_CORE_SERVICE_ID = "service.echoatmospherecore.native_activation";
    private static final String WEATHER_CORE_MODULE_ID = "echoweathercore";
    private static final String WEATHER_CORE_SERVICE_ID = "service.echoweathercore.native_activation";
    private static final String PROGRESSION_CORE_MODULE_ID = "echoprogressioncore";
    private static final String PROGRESSION_CORE_SERVICE_ID = "service.echoprogressioncore.native_activation";
    private static final String SOCIAL_CORE_MODULE_ID = "echosocialcore";
    private static final String SOCIAL_CORE_SERVICE_ID = "service.echosocialcore.native_activation";
    private static final String EVENT_CORE_MODULE_ID = "echoeventcore";
    private static final String EVENT_CORE_SERVICE_ID = "service.echoeventcore.native_activation";
    private static final String ENCOUNTER_CORE_MODULE_ID = "echoencountercore";
    private static final String ENCOUNTER_CORE_SERVICE_ID = "service.echoencountercore.native_activation";
    private static final String ECONOMY_CORE_MODULE_ID = "echoeconomycore";
    private static final String ECONOMY_CORE_SERVICE_ID = "service.echoeconomycore.native_activation";
    private static final String QUEST_DIRECTOR_MODULE_ID = "echoquestdirector";
    private static final String QUEST_DIRECTOR_SERVICE_ID = "service.echoquestdirector.native_activation";
    private static final String NPC_CORE_MODULE_ID = "echonpcore";
    private static final String NPC_CORE_SERVICE_ID = "service.echonpcore.native_activation";
    private static final String GUIDE_CORE_MODULE_ID = "echoguidecore";
    private static final String GUIDE_CORE_SERVICE_ID = "service.echoguidecore.native_activation";
    private static final String LORE_CORE_MODULE_ID = "echolorecore";
    private static final String LORE_CORE_SERVICE_ID = "service.echolorecore.native_activation";
    private static final String STATUS_CORE_MODULE_ID = "echostatuscore";
    private static final String STATUS_CORE_SERVICE_ID = "service.echostatuscore.native_activation";
    private static final String SPAWN_CORE_MODULE_ID = "echospawncore";
    private static final String SPAWN_CORE_SERVICE_ID = "service.echospawncore.native_activation";
    private static final String STRUCTURE_CORE_MODULE_ID = "echostructurecore";
    private static final String STRUCTURE_CORE_SERVICE_ID = "service.echostructurecore.native_activation";
    private static final String LOOT_CORE_MODULE_ID = "echolootcore";
    private static final String LOOT_CORE_SERVICE_ID = "service.echolootcore.native_activation";
    private static final String BIOME_CORE_MODULE_ID = "echobiomecore";
    private static final String BIOME_CORE_SERVICE_ID = "service.echobiomecore.native_activation";
    private static final String DIFFICULTY_CORE_MODULE_ID = "echodifficultycore";
    private static final String DIFFICULTY_CORE_SERVICE_ID = "service.echodifficultycore.native_activation";
    private static final String COMBAT_CORE_MODULE_ID = "echocombatcore";
    private static final String COMBAT_CORE_SERVICE_ID = "service.echocombatcore.native_activation";
    private static final String CREATURE_CORE_MODULE_ID = "echocreaturecore";
    private static final String CREATURE_CORE_SERVICE_ID = "service.echocreaturecore.native_activation";
    private static final String ARMORY_MODULE_ID = "echoarmory";
    private static final String ARMORY_SERVICE_ID = "service.echoarmory.native_activation";
    private static final String AGRICULTURE_RECLAMATION_MODULE_ID = "echoagriculturereclamation";
    private static final String AGRICULTURE_RECLAMATION_SERVICE_ID =
            "service.echoagriculturereclamation.native_activation";
    private static final String MACHINE_CORE_MODULE_ID = "echomachinecore";
    private static final String MACHINE_CORE_SERVICE_ID = "service.echomachinecore.native_activation";
    private static final String POWER_CORE_MODULE_ID = "echopowercore";
    private static final String POWER_CORE_SERVICE_ID = "service.echopowercore.native_activation";
    private static final String LOGISTICS_CORE_MODULE_ID = "echologisticscore";
    private static final String LOGISTICS_CORE_SERVICE_ID = "service.echologisticscore.native_activation";
    private static final String SOUND_CORE_MODULE_ID = "echosoundcore";
    private static final String SOUND_CORE_SERVICE_ID = "service.echosoundcore.native_activation";
    private static final String RECOVERY_MODULE_ID = "echorecovery";
    private static final String RECOVERY_SERVICE_ID = "service.echorecovery.native_activation";
    private static final String HEALTH_CORE_MODULE_ID = "echohealthcore";
    private static final String HEALTH_CORE_SERVICE_ID = "adaptercore.echohealthcore.contract";
    private static final String AGENT_CORE_MODULE_ID = "echoagentcore";
    private static final String AGENT_CORE_SERVICE_ID = "service.echoagentcore.native_activation";
    private static final String BRIDGE_CORE_MODULE_ID = "echobridgecore";
    private static final String BRIDGE_CORE_SERVICE_ID = "service.echobridgecore.native_activation";
    private static final String REPORT_CORE_MODULE_ID = "echoreportcore";
    private static final String REPORT_CORE_SERVICE_ID = "service.echoreportcore.native_activation";
    private static final String CAMERA_CORE_MODULE_ID = "echocameracore";
    private static final String CAMERA_CORE_SERVICE_ID = "service.echocameracore.native_activation";
    private static final String CINEMATIC_CORE_MODULE_ID = "echocinematiccore";
    private static final String CINEMATIC_CORE_SERVICE_ID = "service.echocinematiccore.native_activation";
    private static final String CODEX_CORE_MODULE_ID = "echocodexcore";
    private static final String CODEX_CORE_SERVICE_ID = "service.echocodexcore.native_activation";
    private static final String CREATOR_CORE_MODULE_ID = "echocreatorcore";
    private static final String CREATOR_CORE_SERVICE_ID = "service.echocreatorcore.native_activation";
    private static final String RENDER_CORE_MODULE_ID = "echorendercore";
    private static final String RENDER_CORE_SERVICE_ID = "service.echorendercore.native_activation";
    private static final String SCREEN_CORE_MODULE_ID = "echoscreencore";
    private static final String SCREEN_CORE_SERVICE_ID = "service.echoscreencore.native_activation";
    private static final String SCRIPT_CORE_MODULE_ID = "echoscriptcore";
    private static final String SCRIPT_CORE_SERVICE_ID = "service.echoscriptcore.native_activation";
    private static final String RUNTIME_GUARD_MODULE_ID = "echoruntimeguard";
    private static final String RUNTIME_GUARD_SERVICE_ID = "service.echoruntimeguard.native_activation";
    private static final String COMMUNITY_BRIDGE_MODULE_ID = "echocommunitybridge";
    private static final String COMMUNITY_BRIDGE_SERVICE_ID = "service.echocommunitybridge.native_activation";
    private static final String INDEX_MODULE_ID = "echoindex";
    private static final String INDEX_SERVICE_ID = "adaptercore.echoindex.contract";
    private static final String HOLOMAP_MODULE_ID = "echoholomap";
    private static final String HOLOMAP_SERVICE_ID = "service.echoholomap.native_activation";
    private static final String LENS_MODULE_ID = "echolens";
    private static final String LENS_SERVICE_ID = "service.echolens.native_activation";
    private static final String TERMINAL_MODULE_ID = "echoterminal";
    private static final String TERMINAL_SERVICE_ID = "service.echoterminal.native_activation";
    private static final String TEXTURE_FORGE_MODULE_ID = "echotextureforge";
    private static final String TEXTURE_FORGE_SERVICE_ID = "service.echotextureforge.native_activation";
    private static final String THEME_CORE_MODULE_ID = "echothemecore";
    private static final String THEME_CORE_SERVICE_ID = "service.echothemecore.native_activation";
    private static final String TUTORIAL_CORE_MODULE_ID = "echotutorialcore";
    private static final String TUTORIAL_CORE_SERVICE_ID = "service.echotutorialcore.native_activation";
    private static final String WIKI_MODULE_ID = "echowiki";
    private static final String WIKI_SERVICE_ID = "service.echowiki.native_activation";
    private static final String BASE_GRID_MODULE_ID = "echobasegrid";
    private static final String BASE_GRID_SERVICE_ID = "service.echobasegrid.native_activation";
    private static final String INDUSTRIAL_NEXUS_MODULE_ID = "echoindustrialnexus";
    private static final String INDUSTRIAL_NEXUS_SERVICE_ID = "service.echoindustrialnexus.native_activation";
    private static final String LOGISTICS_NETWORK_MODULE_ID = "echologisticsnetwork";
    private static final String LOGISTICS_NETWORK_SERVICE_ID = "service.echologisticsnetwork.native_activation";
    private static final String MULTIBLOCK_CORE_MODULE_ID = "echomultiblockcore";
    private static final String MULTIBLOCK_CORE_SERVICE_ID = "service.echomultiblockcore.native_activation";
    private static final String POWER_GRID_MODULE_ID = "echopowergrid";
    private static final String POWER_GRID_SERVICE_ID = "service.echopowergrid.native_activation";
    private static final String MISSION_CORE_MODULE_ID = "echomissioncore";
    private static final String MISSION_CORE_SERVICE_ID = "adaptercore.echomissioncore.contract";
    private static final String PLAYER_CORE_MODULE_ID = "echoplayercore";
    private static final String PLAYER_CORE_SERVICE_ID = "service.echoplayercore.native_activation";
    private static final String INPUT_CORE_MODULE_ID = "echoinputcore";
    private static final String INPUT_CORE_SERVICE_ID = "service.echoinputcore.native_activation";
    private static final String HUD_CORE_MODULE_ID = "echohudcore";
    private static final String HUD_CORE_SERVICE_ID = "service.echohudcore.native_activation";
    private static final String NOTIFICATION_CORE_MODULE_ID = "echonotificationcore";
    private static final String NOTIFICATION_CORE_SERVICE_ID = "service.echonotificationcore.native_activation";
    private static final String VEHICLE_CORE_MODULE_ID = "echovehiclecore";
    private static final String VEHICLE_CORE_SERVICE_ID = "service.echovehiclecore.native_activation";
    private static final String BLOCKWORKS_MODULE_ID = "echoblockworks";
    private static final String BLOCKWORKS_SERVICE_ID = "service.echoblockworks.native_activation";
    private static final String PRESENCE_LINK_MODULE_ID = "echopresencelink";
    private static final String PRESENCE_LINK_SERVICE_ID = "adaptercore.echopresencelink.contract";
    private static final String CONVOY_PROTOCOL_MODULE_ID = "echoconvoyprotocol";
    private static final String CONVOY_PROTOCOL_SERVICE_ID = "service.echoconvoyprotocol.native_activation";
    private static final String FAMILIAR_CORE_MODULE_ID = "echofamiliarcore";
    private static final String FAMILIAR_CORE_SERVICE_ID = "service.echofamiliarcore.native_activation";
    private static final String SIGNAL_OS_MODULE_ID = "signalos";
    private static final String SIGNAL_OS_SERVICE_ID = "adaptercore.signalos.contract";
    private static final String SPELL_CORE_MODULE_ID = "echospellcore";
    private static final String SPELL_CORE_SERVICE_ID = "adaptercore.echospellcore.contract";
    private static final String RITUAL_CORE_MODULE_ID = "echoritualcore";
    private static final String RITUAL_CORE_SERVICE_ID = "adaptercore.echoritualcore.contract";
    private static final String CURSE_CORE_MODULE_ID = "echocursecore";
    private static final String CURSE_CORE_SERVICE_ID = "adaptercore.echocursecore.contract";
    private static final String RIFT_WORLDS_MODULE_ID = "echoriftworlds";
    private static final String RIFT_WORLDS_SERVICE_ID = "adaptercore.echoriftworlds.contract";
    private static final String BLACKBOX_PROTOCOL_MODULE_ID = "echoblackboxprotocol";
    private static final String BLACKBOX_PROTOCOL_SERVICE_ID = "adaptercore.echoblackboxprotocol.contract";
    private static final String NEXUS_PROTOCOL_MODULE_ID = "echonexusprotocol";
    private static final String NEXUS_PROTOCOL_SERVICE_ID = "adaptercore.echonexusprotocol.contract";
    private static final String ORBITAL_REMNANTS_MODULE_ID = "echoorbitalremnants";
    private static final String ORBITAL_REMNANTS_SERVICE_ID = "adaptercore.echoorbitalremnants.contract";
    private static final String PRIME_CORE_MODULE_ID = "echoprimecore";
    private static final String PRIME_CORE_SERVICE_ID = "adaptercore.echoprimecore.contract";
    private static final String STATIONFALL_MODULE_ID = "echostationfall";
    private static final String STATIONFALL_SERVICE_ID = "adaptercore.echostationfall.contract";
    private static final String GRIMOIRE_MODULE_ID = "echogrimoire";
    private static final String GRIMOIRE_SERVICE_ID = "adaptercore.echogrimoire.contract";
    private static final String ARCANA_CORE_MODULE_ID = "echoarcanacore";
    private static final String ARCANA_CORE_SERVICE_ID = "adaptercore.echoarcanacore.contract";
    private static final String RELIC_TECH_MODULE_ID = "echorelictech";
    private static final String RELIC_TECH_SERVICE_ID = "adaptercore.echorelictech.contract";
    private static final String ARCANE_INDEX_MODULE_ID = "echoarcaneindex";
    private static final String ARCANE_INDEX_SERVICE_ID = "adaptercore.echoarcaneindex.contract";
    private static final String AETHER_WORKS_MODULE_ID = "echoaetherworks";
    private static final String AETHER_WORKS_SERVICE_ID = "adaptercore.echoaetherworks.contract";
    private static final String ASHFALL_PROTOCOL_MODULE_ID = "echoashfallprotocol";
    private static final String ASHFALL_PROTOCOL_SERVICE_ID = "service.echoashfallprotocol.native_activation";
    private static final String GALACTIC_CORE_MODULE_ID = "echogalacticcore";
    private static final String GALACTIC_CORE_RUNTIME_SERVICE_ID = "echogalacticcore:runtime";
    private static final String GALACTIC_CORE_RUNTIME_GATEWAY_SERVICE_ID = "echogalacticcore:runtime_gateway";
    private static final String GALACTIC_CORE_HOST_EXECUTION_SERVICE_ID = "echogalacticcore:host_execution_bridge";
    private static final String GALACTIC_CORE_LIVE_SESSION_MUTATIONS_SERVICE_ID =
            "echogalacticcore:live_session_mutations";
    private static final String ADDON_API_MODULE_ID = "echoaddonapi";
    private static final String ADDON_API_SERVICE_ID = "service.echoaddonapi.public_api";
    private static final List<Pair> MECHANICS_SERVICE_IDS = List.of(
            new Pair(METADATA_CORE_MODULE_ID, METADATA_CORE_SERVICE_ID),
            new Pair(MODULE_GRAPH_MODULE_ID, MODULE_GRAPH_SERVICE_ID),
            new Pair(ATMOSPHERE_CORE_MODULE_ID, ATMOSPHERE_CORE_SERVICE_ID),
            new Pair(WEATHER_CORE_MODULE_ID, WEATHER_CORE_SERVICE_ID),
            new Pair(PROGRESSION_CORE_MODULE_ID, PROGRESSION_CORE_SERVICE_ID),
            new Pair(SOCIAL_CORE_MODULE_ID, SOCIAL_CORE_SERVICE_ID),
            new Pair(EVENT_CORE_MODULE_ID, EVENT_CORE_SERVICE_ID),
            new Pair(ENCOUNTER_CORE_MODULE_ID, ENCOUNTER_CORE_SERVICE_ID),
            new Pair(ECONOMY_CORE_MODULE_ID, ECONOMY_CORE_SERVICE_ID),
            new Pair(QUEST_DIRECTOR_MODULE_ID, QUEST_DIRECTOR_SERVICE_ID),
            new Pair(NPC_CORE_MODULE_ID, NPC_CORE_SERVICE_ID),
            new Pair(GUIDE_CORE_MODULE_ID, GUIDE_CORE_SERVICE_ID),
            new Pair(LORE_CORE_MODULE_ID, LORE_CORE_SERVICE_ID),
            new Pair(STATUS_CORE_MODULE_ID, STATUS_CORE_SERVICE_ID),
            new Pair(SPAWN_CORE_MODULE_ID, SPAWN_CORE_SERVICE_ID),
            new Pair(STRUCTURE_CORE_MODULE_ID, STRUCTURE_CORE_SERVICE_ID),
            new Pair(LOOT_CORE_MODULE_ID, LOOT_CORE_SERVICE_ID),
            new Pair(BIOME_CORE_MODULE_ID, BIOME_CORE_SERVICE_ID),
            new Pair(DIFFICULTY_CORE_MODULE_ID, DIFFICULTY_CORE_SERVICE_ID),
            new Pair(COMBAT_CORE_MODULE_ID, COMBAT_CORE_SERVICE_ID),
            new Pair(CREATURE_CORE_MODULE_ID, CREATURE_CORE_SERVICE_ID),
            new Pair(ARMORY_MODULE_ID, ARMORY_SERVICE_ID),
            new Pair(AGRICULTURE_RECLAMATION_MODULE_ID, AGRICULTURE_RECLAMATION_SERVICE_ID),
            new Pair(MACHINE_CORE_MODULE_ID, MACHINE_CORE_SERVICE_ID),
            new Pair(POWER_CORE_MODULE_ID, POWER_CORE_SERVICE_ID),
            new Pair(LOGISTICS_CORE_MODULE_ID, LOGISTICS_CORE_SERVICE_ID),
            new Pair(SOUND_CORE_MODULE_ID, SOUND_CORE_SERVICE_ID),
            new Pair(RECOVERY_MODULE_ID, RECOVERY_SERVICE_ID),
            new Pair(AGENT_CORE_MODULE_ID, AGENT_CORE_SERVICE_ID),
            new Pair(BRIDGE_CORE_MODULE_ID, BRIDGE_CORE_SERVICE_ID),
            new Pair(REPORT_CORE_MODULE_ID, REPORT_CORE_SERVICE_ID),
            new Pair(CAMERA_CORE_MODULE_ID, CAMERA_CORE_SERVICE_ID),
            new Pair(CINEMATIC_CORE_MODULE_ID, CINEMATIC_CORE_SERVICE_ID),
            new Pair(CODEX_CORE_MODULE_ID, CODEX_CORE_SERVICE_ID),
            new Pair(CREATOR_CORE_MODULE_ID, CREATOR_CORE_SERVICE_ID),
            new Pair(RENDER_CORE_MODULE_ID, RENDER_CORE_SERVICE_ID),
            new Pair(SCREEN_CORE_MODULE_ID, SCREEN_CORE_SERVICE_ID),
            new Pair(SCRIPT_CORE_MODULE_ID, SCRIPT_CORE_SERVICE_ID),
            new Pair(RUNTIME_GUARD_MODULE_ID, RUNTIME_GUARD_SERVICE_ID),
            new Pair(COMMUNITY_BRIDGE_MODULE_ID, COMMUNITY_BRIDGE_SERVICE_ID),
            new Pair(INDEX_MODULE_ID, INDEX_SERVICE_ID),
            new Pair(HOLOMAP_MODULE_ID, HOLOMAP_SERVICE_ID),
            new Pair(LENS_MODULE_ID, LENS_SERVICE_ID),
            new Pair(TERMINAL_MODULE_ID, TERMINAL_SERVICE_ID),
            new Pair(TEXTURE_FORGE_MODULE_ID, TEXTURE_FORGE_SERVICE_ID),
            new Pair(THEME_CORE_MODULE_ID, THEME_CORE_SERVICE_ID),
            new Pair(TUTORIAL_CORE_MODULE_ID, TUTORIAL_CORE_SERVICE_ID),
            new Pair(WIKI_MODULE_ID, WIKI_SERVICE_ID),
            new Pair(BASE_GRID_MODULE_ID, BASE_GRID_SERVICE_ID),
            new Pair(INDUSTRIAL_NEXUS_MODULE_ID, INDUSTRIAL_NEXUS_SERVICE_ID),
            new Pair(LOGISTICS_NETWORK_MODULE_ID, LOGISTICS_NETWORK_SERVICE_ID),
            new Pair(MULTIBLOCK_CORE_MODULE_ID, MULTIBLOCK_CORE_SERVICE_ID),
            new Pair(POWER_GRID_MODULE_ID, POWER_GRID_SERVICE_ID),
            new Pair(INPUT_CORE_MODULE_ID, INPUT_CORE_SERVICE_ID),
            new Pair(HUD_CORE_MODULE_ID, HUD_CORE_SERVICE_ID),
            new Pair(NOTIFICATION_CORE_MODULE_ID, NOTIFICATION_CORE_SERVICE_ID),
            new Pair(VEHICLE_CORE_MODULE_ID, VEHICLE_CORE_SERVICE_ID),
            new Pair(CONVOY_PROTOCOL_MODULE_ID, CONVOY_PROTOCOL_SERVICE_ID),
            new Pair(FAMILIAR_CORE_MODULE_ID, FAMILIAR_CORE_SERVICE_ID),
            new Pair(ASHFALL_PROTOCOL_MODULE_ID, ASHFALL_PROTOCOL_SERVICE_ID),
            new Pair(SIGNAL_OS_MODULE_ID, SIGNAL_OS_SERVICE_ID),
            new Pair(SPELL_CORE_MODULE_ID, SPELL_CORE_SERVICE_ID),
            new Pair(RITUAL_CORE_MODULE_ID, RITUAL_CORE_SERVICE_ID),
            new Pair(CURSE_CORE_MODULE_ID, CURSE_CORE_SERVICE_ID),
            new Pair(RIFT_WORLDS_MODULE_ID, RIFT_WORLDS_SERVICE_ID),
            new Pair(BLACKBOX_PROTOCOL_MODULE_ID, BLACKBOX_PROTOCOL_SERVICE_ID),
            new Pair(NEXUS_PROTOCOL_MODULE_ID, NEXUS_PROTOCOL_SERVICE_ID),
            new Pair(ORBITAL_REMNANTS_MODULE_ID, ORBITAL_REMNANTS_SERVICE_ID),
            new Pair(PRIME_CORE_MODULE_ID, PRIME_CORE_SERVICE_ID),
            new Pair(STATIONFALL_MODULE_ID, STATIONFALL_SERVICE_ID),
            new Pair(GRIMOIRE_MODULE_ID, GRIMOIRE_SERVICE_ID),
            new Pair(ARCANA_CORE_MODULE_ID, ARCANA_CORE_SERVICE_ID),
            new Pair(RELIC_TECH_MODULE_ID, RELIC_TECH_SERVICE_ID),
            new Pair(ARCANE_INDEX_MODULE_ID, ARCANE_INDEX_SERVICE_ID),
            new Pair(AETHER_WORKS_MODULE_ID, AETHER_WORKS_SERVICE_ID)
    );

    private EchoRuntimeRealModuleExecutionSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path modulesRepoRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of("..", "ECHO-Modules").toAbsolutePath().normalize();
        Path fixtureRoot = Files.createTempDirectory("echo-real-module-execution");
        List<Path> compiledClassRoots = new ArrayList<>();
        materializeRepoShape(modulesRepoRoot, fixtureRoot);
        materializeDataOnlyDependencies(modulesRepoRoot, fixtureRoot);

        for (RealModuleSpec spec : REAL_MODULES) {
            Path realModuleRoot = modulesRepoRoot.resolve("addons").resolve(spec.addonDirectory());
            Path realDescriptor = realModuleRoot.resolve("src/main/resources/META-INF/echo.mod.json");
            Path realEntrypoint = realModuleRoot.resolve(spec.entrypointSource());
            require(Files.isRegularFile(realDescriptor),
                    "real descriptor should exist for " + spec.moduleId() + ": " + realDescriptor);
            require(Files.isRegularFile(realEntrypoint),
                    "real native entrypoint source should exist for " + spec.moduleId() + ": " + realEntrypoint);

            Path materializedModule = fixtureRoot.resolve(spec.moduleId());
            Path materializedDescriptor = materializedModule.resolve("src/main/resources/META-INF/echo.mod.json");
            Path classesRoot = materializedModule.resolve("classes");
            materializeDescriptor(realDescriptor, materializedDescriptor);
            compileRealEntrypoint(realEntrypoint, realModuleRoot.resolve("src/main/java"), classesRoot, compiledClassRoots);
            compiledClassRoots.add(classesRoot);
        }

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoRuntimeLogBridge diagnostics = new EchoRuntimeLogBridge();
        services.register(EchoRuntimeDiagnosticSink.class, diagnostics);

        EchoRuntimeModuleManager manager = EchoRuntimeModuleManager.executableAbiV1();
        EchoRuntimeModuleRuntimeResult result = manager.run(List.of(fixtureRoot), services);
        EchoRuntimeModuleRegistry registry = result.registry();
        EchoNativeServiceRegistry nativeServices = services.require(EchoNativeServiceRegistry.class);

        for (RealModuleSpec spec : REAL_MODULES) {
            EchoRuntimeModuleDescriptor descriptor = registry.find(spec.moduleId()).orElseThrow();
            require(descriptor.nativeEntrypoint().equals(spec.nativeEntrypoint()),
                    "parser should preserve access.nativeEntrypoint for " + spec.moduleId());
            require(descriptor.classPath().equals(List.of("classes")),
                    "parser should bind access.nativeClasspath for " + spec.moduleId());
            require(registry.runtimeStatus(spec.moduleId()) == EchoRuntimeModuleStatus.RUNTIME_ACTIVE,
                    "real " + spec.moduleId() + " should be runtime-active"
                            + " status=" + registry.runtimeStatus(spec.moduleId()).id()
                            + " lifecycle=" + registry.lifecycle(spec.moduleId())
                            + " notes=" + String.join("; ", registry.notes(spec.moduleId())));
            require(registry.lifecycle(spec.moduleId()) == EchoRuntimeModuleLifecycle.READY,
                    "real " + spec.moduleId() + " should reach READY");
            require(String.join("; ", registry.notes(spec.moduleId())).contains(spec.noteProbe()),
                    "notes should prove real " + spec.moduleId() + " executed through " + spec.noteProbe());
        }

        require(nativeServices.hasService("echocore", "service.echocore.legacy_native_bootstrap"),
                "real echocore legacy native bootstrap should register a native service");
        require(nativeServices.hasService("echoplatformcore", "service.echoplatformcore.native_activation"),
                "real echoplatformcore surface native entrypoint should register a native activation service");
        require(nativeServices.hasService(SCHEMA_CORE_MODULE_ID, SCHEMA_CORE_SERVICE_ID),
                "real echoschemacore native adapter should register a schema activation service");
        EchoNativeRegisteredService schemaCoreService = nativeServices.servicesForModule(SCHEMA_CORE_MODULE_ID).stream()
                .filter(service -> service.serviceId().equals(SCHEMA_CORE_SERVICE_ID))
                .findFirst()
                .orElseThrow();
        require(schemaCoreService.surfaces().containsAll(List.of(
                        "surface.module.entrypoint",
                        "diagnostics",
                        "data",
                        "echo.native"
                )),
                "real echoschemacore service should preserve schema activation surfaces");
        require(nativeServices.hasService(VALIDATION_CORE_MODULE_ID, VALIDATION_CORE_SERVICE_ID),
                "real echovalidationcore native adapter should register a validation activation service");
        EchoNativeRegisteredService validationCoreService = nativeServices.servicesForModule(VALIDATION_CORE_MODULE_ID).stream()
                .filter(service -> service.serviceId().equals(VALIDATION_CORE_SERVICE_ID))
                .findFirst()
                .orElseThrow();
        require(validationCoreService.surfaces().containsAll(List.of(
                        "surface.module.entrypoint",
                        "diagnostics",
                        "data",
                        "echo.native"
                )),
                "real echovalidationcore service should preserve validation activation surfaces");
        require(nativeServices.hasService(CONTENT_CORE_MODULE_ID, CONTENT_CORE_SERVICE_ID),
                "real echocontentcore native adapter should register its content activation service");
        EchoNativeRegisteredService contentCoreService = registeredService(
                nativeServices,
                CONTENT_CORE_MODULE_ID,
                CONTENT_CORE_SERVICE_ID
        );
        require(contentCoreService.surfaces().containsAll(List.of(
                        "surface.module.entrypoint",
                        "diagnostics",
                        "blocks",
                        "data",
                        "items",
                        "recipes",
                        "structures"
                )),
                "real echocontentcore service should preserve content activation surfaces");
        Map<String, Object> contentCoreActivation = serviceActivation(
                nativeServices,
                CONTENT_CORE_MODULE_ID,
                CONTENT_CORE_SERVICE_ID
        );
        require(Boolean.TRUE.equals(contentCoreActivation.get("ownerLookupRoundTrip")),
                "real echocontentcore should execute content ownership lookup behavior");
        require(Boolean.TRUE.equals(contentCoreActivation.get("referenceLookupRoundTrip")),
                "real echocontentcore should execute content reference lookup behavior");
        require(nativeServices.hasService(ASSET_CORE_MODULE_ID, ASSET_CORE_SERVICE_ID),
                "real echoassetcore native adapter should register its asset activation service");
        EchoNativeRegisteredService assetCoreService = registeredService(
                nativeServices,
                ASSET_CORE_MODULE_ID,
                ASSET_CORE_SERVICE_ID
        );
        require(assetCoreService.surfaces().containsAll(List.of("surface.module.entrypoint", "assets", "data")),
                "real echoassetcore service should preserve asset activation surfaces");
        Map<String, Object> assetCoreActivation = serviceActivation(
                nativeServices,
                ASSET_CORE_MODULE_ID,
                ASSET_CORE_SERVICE_ID
        );
        require(Boolean.TRUE.equals(assetCoreActivation.get("assetRegistryRoundTrip")),
                "real echoassetcore should execute asset registry behavior");
        require(Boolean.TRUE.equals(assetCoreActivation.get("assetValidationRoundTrip")),
                "real echoassetcore should execute asset validation behavior");
        require(Boolean.TRUE.equals(assetCoreActivation.get("textureForgePromptReady")),
                "real echoassetcore should execute TextureForge prompt readiness behavior");
        require(nativeServices.hasService(RECIPE_CORE_MODULE_ID, RECIPE_CORE_SERVICE_ID),
                "real echorecipecore native adapter should register its recipe activation service");
        EchoNativeRegisteredService recipeCoreService = registeredService(
                nativeServices,
                RECIPE_CORE_MODULE_ID,
                RECIPE_CORE_SERVICE_ID
        );
        require(recipeCoreService.surfaces().containsAll(List.of("surface.module.entrypoint", "echo.native")),
                "real echorecipecore service should preserve recipe activation surfaces");
        Map<String, Object> recipeCoreActivation = serviceActivation(
                nativeServices,
                RECIPE_CORE_MODULE_ID,
                RECIPE_CORE_SERVICE_ID
        );
        require("PASS".equals(recipeCoreActivation.get("status")),
                "real echorecipecore should execute its Agent9 machine recipe runtime adapter");
        require(Boolean.TRUE.equals(recipeCoreActivation.get("hostLoadedEntrypoint")),
                "real echorecipecore should load its native host entrypoint");
        require(!nativeServices.servicesForModule("echoadaptercore").isEmpty(),
                "real echoadaptercore native adapter should register Native Platform services");
        Map<String, Map<String, Object>> mechanicsActivations = new LinkedHashMap<>();
        require(nativeServices.hasService(PACK_CORE_MODULE_ID, PACK_CORE_SERVICE_ID),
                "real echopackcore native adapter should register its AdapterCore pack contract");
        EchoNativeRegisteredService packCoreService = registeredService(
                nativeServices,
                PACK_CORE_MODULE_ID,
                PACK_CORE_SERVICE_ID
        );
        require(packCoreService.surfaces().contains("adaptercore"),
                "real echopackcore service should preserve AdapterCore contract surface");
        Map<String, Object> packCoreActivation = serviceActivation(
                nativeServices,
                PACK_CORE_MODULE_ID,
                PACK_CORE_SERVICE_ID
        );
        require(Boolean.TRUE.equals(packCoreActivation.get("packLoadPlanExecuted")),
                "real echopackcore should execute its pack load-plan contract");
        Map<String, Object> metadataCoreActivation = requireActivationService(
                nativeServices,
                METADATA_CORE_MODULE_ID,
                METADATA_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "data", "diagnostics", "packs")
        );
        requireTrue(metadataCoreActivation, "manifestNormalizationRoundTrip",
                "real echometadatacore should execute manifest normalization behavior");
        requireTrue(metadataCoreActivation, "schemaValidationRoundTrip",
                "real echometadatacore should execute schema validation behavior");
        requireTrue(metadataCoreActivation, "conflictDetectionRoundTrip",
                "real echometadatacore should execute conflict detection behavior");
        mechanicsActivations.put(METADATA_CORE_MODULE_ID, metadataCoreActivation);
        Map<String, Object> moduleGraphActivation = requireActivationService(
                nativeServices,
                MODULE_GRAPH_MODULE_ID,
                MODULE_GRAPH_SERVICE_ID,
                List.of("surface.module.entrypoint", "data", "diagnostics")
        );
        requireTrue(moduleGraphActivation, "moduleGraphRoundTrip",
                "real echomodulegraph should execute graph behavior");
        requireTrue(moduleGraphActivation, "graphValidationRoundTrip",
                "real echomodulegraph should execute graph validation behavior");
        mechanicsActivations.put(MODULE_GRAPH_MODULE_ID, moduleGraphActivation);
        require(nativeServices.hasService(NET_CORE_MODULE_ID, NET_CORE_SERVICE_ID),
                "real echonetcore native adapter should register its AdapterCore packet service contract");
        EchoNativeRegisteredService netCoreService = nativeServices.servicesForModule(NET_CORE_MODULE_ID).stream()
                .filter(service -> service.serviceId().equals(NET_CORE_SERVICE_ID))
                .findFirst()
                .orElseThrow();
        require(netCoreService.surfaces().contains("adaptercore"),
                "real echonetcore service should preserve AdapterCore contract surface");
        require(nativeServices.hasService(DATA_CORE_MODULE_ID, DATA_CORE_SERVICE_ID),
                "real echodatacore native adapter should register its AdapterCore data contract");
        EchoNativeRegisteredService dataCoreService = registeredService(
                nativeServices,
                DATA_CORE_MODULE_ID,
                DATA_CORE_SERVICE_ID
        );
        require(dataCoreService.surfaces().containsAll(List.of("adaptercore", "data", "saves")),
                "real echodatacore service should preserve AdapterCore data surfaces");
        Map<String, Object> dataCoreActivation = serviceActivation(
                nativeServices,
                DATA_CORE_MODULE_ID,
                DATA_CORE_SERVICE_ID
        );
        require(Boolean.TRUE.equals(dataCoreActivation.get("dataRuntimeProfileExecuted")),
                "real echodatacore should execute its runtime profile sync contract");
        require(nativeServices.hasService(WORLD_CORE_MODULE_ID, WORLD_CORE_SERVICE_ID),
                "real echoworldcore native adapter should register its AdapterCore world contract");
        EchoNativeRegisteredService worldCoreService = registeredService(
                nativeServices,
                WORLD_CORE_MODULE_ID,
                WORLD_CORE_SERVICE_ID
        );
        require(worldCoreService.surfaces().contains("adaptercore"),
                "real echoworldcore service should preserve AdapterCore contract surface");
        Map<String, Object> worldCoreActivation = serviceActivation(
                nativeServices,
                WORLD_CORE_MODULE_ID,
                WORLD_CORE_SERVICE_ID
        );
        require(Boolean.TRUE.equals(worldCoreActivation.get("worldEffectsRuntimeContract")),
                "real echoworldcore should execute its world effects runtime contract");
        require(Boolean.TRUE.equals(worldCoreActivation.get("worldDataCatalogRuntimeContract")),
                "real echoworldcore should execute its world data catalog contract");
        require(Integer.valueOf(8).equals(worldCoreActivation.get("worldDataCatalogRegionCount")),
                "real echoworldcore should load the real world region catalog");
        require(Integer.valueOf(12).equals(worldCoreActivation.get("worldDataCatalogHazardCount")),
                "real echoworldcore should load the real world hazard catalog");
        require(Integer.valueOf(193).equals(worldCoreActivation.get("worldDataCatalogSourceFileCount")),
                "real echoworldcore should scan the expected world definition source files");
        Map<String, Object> atmosphereCoreActivation = requireActivationService(
                nativeServices,
                ATMOSPHERE_CORE_MODULE_ID,
                ATMOSPHERE_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics")
        );
        requireTrue(atmosphereCoreActivation, "atmosphereProfileTickExecuted",
                "real echoatmospherecore should execute atmosphere profile tick behavior");
        requireTrue(atmosphereCoreActivation, "atmosphereStateApplyRuntimeContract",
                "real echoatmospherecore should execute atmosphere state application behavior");
        requireTrue(atmosphereCoreActivation, "liveAtmosphereLevelTickRuntimeContract",
                "real echoatmospherecore should materialize live level tick atmosphere state");
        require(Double.valueOf(0.31D).equals(atmosphereCoreActivation.get("atmosphereVisibility")),
                "real echoatmospherecore should expose reference atmosphere visibility");
        mechanicsActivations.put(ATMOSPHERE_CORE_MODULE_ID, atmosphereCoreActivation);
        Map<String, Object> weatherCoreActivation = requireActivationService(
                nativeServices,
                WEATHER_CORE_MODULE_ID,
                WEATHER_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics")
        );
        requireTrue(weatherCoreActivation, "weatherScheduleRuntimeContract",
                "real echoweathercore should execute weather schedule behavior");
        requireTrue(weatherCoreActivation, "weatherScheduleTickRuntimeContract",
                "real echoweathercore should execute live schedule tick behavior");
        requireTrue(weatherCoreActivation, "weatherStateApplyRuntimeContract",
                "real echoweathercore should execute weather state application behavior");
        requireTrue(weatherCoreActivation, "weatherForecastRuntimeContract",
                "real echoweathercore should execute weather forecast behavior");
        requireTrue(weatherCoreActivation, "weatherWarningRuntimeContract",
                "real echoweathercore should execute weather warning behavior");
        requireTrue(weatherCoreActivation, "weatherExposureMitigationRuntimeContract",
                "real echoweathercore should execute weather exposure mitigation behavior");
        requireTrue(weatherCoreActivation, "weatherRouteRiskRuntimeContract",
                "real echoweathercore should execute route risk behavior");
        requireTrue(weatherCoreActivation, "weatherRadioRuntimeContract",
                "real echoweathercore should execute Weather Radio behavior");
        requireTrue(weatherCoreActivation, "weatherStationRuntimeContract",
                "real echoweathercore should execute Weather Station behavior");
        requireTrue(weatherCoreActivation, "emergencySirenRuntimeContract",
                "real echoweathercore should execute Emergency Siren behavior");
        requireTrue(weatherCoreActivation, "climateSensorRuntimeContract",
                "real echoweathercore should execute Climate Sensor behavior");
        requireTrue(weatherCoreActivation, "routeWarningPostRuntimeContract",
                "real echoweathercore should execute Route Warning Post behavior");
        require("ACTIVE".equals(weatherCoreActivation.get("scheduleTickPhase")),
                "real echoweathercore should advance the reference schedule to ACTIVE");
        require("SAFE".equals(weatherCoreActivation.get("mitigatedRouteRisk")),
                "real echoweathercore should expose mitigated route risk");
        mechanicsActivations.put(WEATHER_CORE_MODULE_ID, weatherCoreActivation);
        Map<String, Object> progressionCoreActivation = requireActivationService(
                nativeServices,
                PROGRESSION_CORE_MODULE_ID,
                PROGRESSION_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "missions", "recipes")
        );
        requireTrue(progressionCoreActivation, "featureContractRoundTrip",
                "real echoprogressioncore should execute unlock graph and objective contracts");
        mechanicsActivations.put(PROGRESSION_CORE_MODULE_ID, progressionCoreActivation);
        Map<String, Object> socialCoreActivation = requireActivationService(
                nativeServices,
                SOCIAL_CORE_MODULE_ID,
                SOCIAL_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "data", "entities")
        );
        requireTrue(socialCoreActivation, "factionDataRoundTrip",
                "real echosocialcore should execute faction data behavior");
        requireTrue(socialCoreActivation, "dialogueDataRoundTrip",
                "real echosocialcore should execute dialogue data behavior");
        requireTrue(socialCoreActivation, "npcEntityRoundTrip",
                "real echosocialcore should execute NPC profile behavior");
        mechanicsActivations.put(SOCIAL_CORE_MODULE_ID, socialCoreActivation);
        Map<String, Object> eventCoreActivation = requireActivationService(
                nativeServices,
                EVENT_CORE_MODULE_ID,
                EVENT_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        requireTrue(eventCoreActivation, "featureContractRoundTrip",
                "real echoeventcore should execute world event, scheduler, and validation contracts");
        mechanicsActivations.put(EVENT_CORE_MODULE_ID, eventCoreActivation);
        Map<String, Object> encounterCoreActivation = requireActivationService(
                nativeServices,
                ENCOUNTER_CORE_MODULE_ID,
                ENCOUNTER_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        requireTrue(encounterCoreActivation, "featureContractRoundTrip",
                "real echoencountercore should execute encounter, boss-gate, and patrol contracts");
        mechanicsActivations.put(ENCOUNTER_CORE_MODULE_ID, encounterCoreActivation);
        Map<String, Object> economyCoreActivation = requireActivationService(
                nativeServices,
                ECONOMY_CORE_MODULE_ID,
                ECONOMY_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(economyCoreActivation.get("status")),
                "real echoeconomycore should execute the Agent9 economy runtime adapter");
        requireTrue(economyCoreActivation, "activated",
                "real echoeconomycore should expose activated Agent9 evidence");
        mechanicsActivations.put(ECONOMY_CORE_MODULE_ID, economyCoreActivation);
        Map<String, Object> questDirectorActivation = requireActivationService(
                nativeServices,
                QUEST_DIRECTOR_MODULE_ID,
                QUEST_DIRECTOR_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        requireTrue(questDirectorActivation, "featureContractRoundTrip",
                "real echoquestdirector should execute mission selection and route-pacing contracts");
        mechanicsActivations.put(QUEST_DIRECTOR_MODULE_ID, questDirectorActivation);
        Map<String, Object> npcCoreActivation = requireActivationService(
                nativeServices,
                NPC_CORE_MODULE_ID,
                NPC_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        requireTrue(npcCoreActivation, "featureContractRoundTrip",
                "real echonpcore should execute NPC profile, dialogue, screen, service, trade, and replacement contracts");
        mechanicsActivations.put(NPC_CORE_MODULE_ID, npcCoreActivation);
        Map<String, Object> guideCoreActivation = requireActivationService(
                nativeServices,
                GUIDE_CORE_MODULE_ID,
                GUIDE_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        requireTrue(guideCoreActivation, "featureContractRoundTrip",
                "real echoguidecore should execute guide page, search, and unlock visibility contracts");
        mechanicsActivations.put(GUIDE_CORE_MODULE_ID, guideCoreActivation);
        Map<String, Object> loreCoreActivation = requireActivationService(
                nativeServices,
                LORE_CORE_MODULE_ID,
                LORE_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        requireTrue(loreCoreActivation, "featureContractRoundTrip",
                "real echolorecore should execute lore, audio log, blackbox, and environmental story contracts");
        mechanicsActivations.put(LORE_CORE_MODULE_ID, loreCoreActivation);
        Map<String, Object> statusCoreActivation = requireActivationService(
                nativeServices,
                STATUS_CORE_MODULE_ID,
                STATUS_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics")
        );
        requireTrue(statusCoreActivation, "effectApplyRuntimeContract",
                "real echostatuscore should execute status effect application behavior");
        requireTrue(statusCoreActivation, "effectStackingRuntimeContract",
                "real echostatuscore should execute status stacking behavior");
        requireTrue(statusCoreActivation, "exposureMitigationRuntimeContract",
                "real echostatuscore should execute exposure mitigation behavior");
        requireTrue(statusCoreActivation, "liveStatusRegistryRuntimeContract",
                "real echostatuscore should materialize live status registry state");
        mechanicsActivations.put(STATUS_CORE_MODULE_ID, statusCoreActivation);
        Map<String, Object> spawnCoreActivation = requireActivationService(
                nativeServices,
                SPAWN_CORE_MODULE_ID,
                SPAWN_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics")
        );
        requireTrue(spawnCoreActivation, "spawnRuleEventRuntimeContract",
                "real echospawncore should execute spawn rule event behavior");
        requireTrue(spawnCoreActivation, "spawnZoneStateRuntimeContract",
                "real echospawncore should execute spawn-zone state behavior");
        requireTrue(spawnCoreActivation, "liveSpawnFinalizeRuntimeContract",
                "real echospawncore should materialize live finalize-spawn state");
        require("SPAWN_ALLOWED".equals(spawnCoreActivation.get("eventType")),
                "real echospawncore should allow the reference spawn event");
        mechanicsActivations.put(SPAWN_CORE_MODULE_ID, spawnCoreActivation);
        Map<String, Object> structureCoreActivation = requireActivationService(
                nativeServices,
                STRUCTURE_CORE_MODULE_ID,
                STRUCTURE_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics")
        );
        requireTrue(structureCoreActivation, "poiLookupRuntimeContract",
                "real echostructurecore should execute POI lookup behavior");
        requireTrue(structureCoreActivation, "poiMarkerStateRuntimeContract",
                "real echostructurecore should execute POI marker persistence behavior");
        requireTrue(structureCoreActivation, "discoveryStateRuntimeContract",
                "real echostructurecore should execute discovery state behavior");
        requireTrue(structureCoreActivation, "liveStructureLevelTickRuntimeContract",
                "real echostructurecore should materialize live level tick structure state");
        require("DISCOVERED".equals(structureCoreActivation.get("discoveryState")),
                "real echostructurecore should retain discovered state");
        mechanicsActivations.put(STRUCTURE_CORE_MODULE_ID, structureCoreActivation);
        Map<String, Object> lootCoreActivation = requireActivationService(
                nativeServices,
                LOOT_CORE_MODULE_ID,
                LOOT_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(lootCoreActivation.get("status")),
                "real echolootcore should execute the Agent9 loot runtime adapter");
        requireTrue(lootCoreActivation, "activated",
                "real echolootcore should expose activated Agent9 evidence");
        mechanicsActivations.put(LOOT_CORE_MODULE_ID, lootCoreActivation);
        Map<String, Object> biomeCoreActivation = requireActivationService(
                nativeServices,
                BIOME_CORE_MODULE_ID,
                BIOME_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "assets", "worldgen")
        );
        requireTrue(biomeCoreActivation, "ambientStateRuntimeContract",
                "real echobiomecore should execute ambient state behavior");
        requireTrue(biomeCoreActivation, "hazardOverlayRoundTrip",
                "real echobiomecore should execute hazard overlay behavior");
        requireTrue(biomeCoreActivation, "liveBiomeLevelTickRuntimeContract",
                "real echobiomecore should execute live biome tick behavior");
        mechanicsActivations.put(BIOME_CORE_MODULE_ID, biomeCoreActivation);
        Map<String, Object> difficultyCoreActivation = requireActivationService(
                nativeServices,
                DIFFICULTY_CORE_MODULE_ID,
                DIFFICULTY_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "data", "packs")
        );
        requireTrue(difficultyCoreActivation, "difficultyProfileRoundTrip",
                "real echodifficultycore should execute difficulty profile behavior");
        requireTrue(difficultyCoreActivation, "profileSelectionRuntimeContract",
                "real echodifficultycore should execute profile selection behavior");
        requireTrue(difficultyCoreActivation, "difficultyApplicationRuntimeContract",
                "real echodifficultycore should execute difficulty application behavior");
        mechanicsActivations.put(DIFFICULTY_CORE_MODULE_ID, difficultyCoreActivation);
        Map<String, Object> combatCoreActivation = requireActivationService(
                nativeServices,
                COMBAT_CORE_MODULE_ID,
                COMBAT_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "entities", "items", "player")
        );
        requireTrue(combatCoreActivation, "damageItemRoundTrip",
                "real echocombatcore should execute damage and weapon trait behavior");
        requireTrue(combatCoreActivation, "entityScalingRoundTrip",
                "real echocombatcore should execute enemy scaling behavior");
        requireTrue(combatCoreActivation, "playerDefenseRoundTrip",
                "real echocombatcore should execute player defense behavior");
        mechanicsActivations.put(COMBAT_CORE_MODULE_ID, combatCoreActivation);
        Map<String, Object> creatureCoreActivation = requireActivationService(
                nativeServices,
                CREATURE_CORE_MODULE_ID,
                CREATURE_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "data", "entities")
        );
        requireTrue(creatureCoreActivation, "featureContractRoundTrip",
                "real echocreaturecore should execute creature feature contracts");
        mechanicsActivations.put(CREATURE_CORE_MODULE_ID, creatureCoreActivation);
        Map<String, Object> armoryActivation = requireActivationService(
                nativeServices,
                ARMORY_MODULE_ID,
                ARMORY_SERVICE_ID,
                List.of("surface.module.entrypoint", "items", "recipes", "player")
        );
        requireTrue(armoryActivation, "gearStateRoundTrip",
                "real echoarmory should execute gear state behavior");
        requireTrue(armoryActivation, "stationPreviewRoundTrip",
                "real echoarmory should execute station recipe preview behavior");
        requireTrue(armoryActivation, "routeReadinessRoundTrip",
                "real echoarmory should execute player route readiness behavior");
        mechanicsActivations.put(ARMORY_MODULE_ID, armoryActivation);
        Map<String, Object> agricultureActivation = requireActivationService(
                nativeServices,
                AGRICULTURE_RECLAMATION_MODULE_ID,
                AGRICULTURE_RECLAMATION_SERVICE_ID,
                List.of("surface.module.entrypoint", "blocks", "items", "worldgen")
        );
        requireTrue(agricultureActivation, "greenhouseMachineRulesRoundTrip",
                "real echoagriculturereclamation should execute greenhouse machine behavior");
        requireTrue(agricultureActivation, "seedSupplyProcessRoundTrip",
                "real echoagriculturereclamation should execute seed process behavior");
        requireTrue(agricultureActivation, "restorationEnvelopeRoundTrip",
                "real echoagriculturereclamation should execute restoration worldgen behavior");
        mechanicsActivations.put(AGRICULTURE_RECLAMATION_MODULE_ID, agricultureActivation);
        Map<String, Object> machineCoreActivation = requireActivationService(
                nativeServices,
                MACHINE_CORE_MODULE_ID,
                MACHINE_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(machineCoreActivation.get("status")),
                "real echomachinecore should execute the Agent9 machine runtime adapter");
        requireTrue(machineCoreActivation, "activated",
                "real echomachinecore should expose activated Agent9 evidence");
        mechanicsActivations.put(MACHINE_CORE_MODULE_ID, machineCoreActivation);
        Map<String, Object> powerCoreActivation = requireActivationService(
                nativeServices,
                POWER_CORE_MODULE_ID,
                POWER_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(powerCoreActivation.get("status")),
                "real echopowercore should execute the Agent9 power runtime adapter");
        requireTrue(powerCoreActivation, "activated",
                "real echopowercore should expose activated Agent9 evidence");
        mechanicsActivations.put(POWER_CORE_MODULE_ID, powerCoreActivation);
        Map<String, Object> logisticsCoreActivation = requireActivationService(
                nativeServices,
                LOGISTICS_CORE_MODULE_ID,
                LOGISTICS_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(logisticsCoreActivation.get("status")),
                "real echologisticscore should execute the Agent9 logistics runtime adapter");
        requireTrue(logisticsCoreActivation, "activated",
                "real echologisticscore should expose activated Agent9 evidence");
        mechanicsActivations.put(LOGISTICS_CORE_MODULE_ID, logisticsCoreActivation);
        Map<String, Object> soundCoreActivation = requireActivationService(
                nativeServices,
                SOUND_CORE_MODULE_ID,
                SOUND_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics")
        );
        requireTrue(soundCoreActivation, "audioDispatchExecuted",
                "real echosoundcore should execute audio dispatch behavior");
        mechanicsActivations.put(SOUND_CORE_MODULE_ID, soundCoreActivation);
        Map<String, Object> recoveryActivation = requireActivationService(
                nativeServices,
                RECOVERY_MODULE_ID,
                RECOVERY_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics")
        );
        requireTrue(recoveryActivation, "recoveryPlanExecuted",
                "real echorecovery should execute field recovery behavior");
        mechanicsActivations.put(RECOVERY_MODULE_ID, recoveryActivation);
        require(nativeServices.hasService(HEALTH_CORE_MODULE_ID, HEALTH_CORE_SERVICE_ID),
                "real echohealthcore native adapter should register its AdapterCore health contract");
        EchoNativeRegisteredService healthCoreService = registeredService(
                nativeServices,
                HEALTH_CORE_MODULE_ID,
                HEALTH_CORE_SERVICE_ID
        );
        require(healthCoreService.surfaces().contains("adaptercore"),
                "real echohealthcore service should preserve AdapterCore contract surface");
        Map<String, Object> healthCoreActivation = serviceActivation(
                nativeServices,
                HEALTH_CORE_MODULE_ID,
                HEALTH_CORE_SERVICE_ID
        );
        require(Boolean.TRUE.equals(healthCoreActivation.get("healthReportExecuted")),
                "real echohealthcore should execute its runtime health report contract");
        Map<String, Object> agentCoreActivation = requireActivationService(
                nativeServices,
                AGENT_CORE_MODULE_ID,
                AGENT_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "commands", "data")
        );
        requireTrue(agentCoreActivation, "safeCommandPolicyRoundTrip",
                "real echoagentcore should execute safe command policy behavior");
        requireTrue(agentCoreActivation, "taskQueueRoundTrip",
                "real echoagentcore should execute task queue behavior");
        requireTrue(agentCoreActivation, "promptBundleRoundTrip",
                "real echoagentcore should execute prompt bundle behavior");
        requireTrue(agentCoreActivation, "runReportRoundTrip",
                "real echoagentcore should execute run report behavior");
        mechanicsActivations.put(AGENT_CORE_MODULE_ID, agentCoreActivation);
        Map<String, Object> bridgeCoreActivation = requireActivationService(
                nativeServices,
                BRIDGE_CORE_MODULE_ID,
                BRIDGE_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "data", "networking")
        );
        requireTrue(bridgeCoreActivation, "sessionDataRoundTrip",
                "real echobridgecore should execute session data behavior");
        requireTrue(bridgeCoreActivation, "safeActionGateRoundTrip",
                "real echobridgecore should execute safe action gate behavior");
        requireTrue(bridgeCoreActivation, "localTransportRoundTrip",
                "real echobridgecore should execute local transport behavior");
        mechanicsActivations.put(BRIDGE_CORE_MODULE_ID, bridgeCoreActivation);
        Map<String, Object> reportCoreActivation = requireActivationService(
                nativeServices,
                REPORT_CORE_MODULE_ID,
                REPORT_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "data")
        );
        requireTrue(reportCoreActivation, "supportBundleRoundTrip",
                "real echoreportcore should execute support bundle behavior");
        requireTrue(reportCoreActivation, "releaseReadinessRoundTrip",
                "real echoreportcore should execute release readiness behavior");
        mechanicsActivations.put(REPORT_CORE_MODULE_ID, reportCoreActivation);
        Map<String, Object> cameraCoreActivation = requireActivationService(
                nativeServices,
                CAMERA_CORE_MODULE_ID,
                CAMERA_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "input", "rendering")
        );
        requireTrue(cameraCoreActivation, "renderProfileRoundTrip",
                "real echocameracore should execute camera render profile behavior");
        requireTrue(cameraCoreActivation, "shakeSafetyRoundTrip",
                "real echocameracore should execute shake safety behavior");
        requireTrue(cameraCoreActivation, "inputTargetRoundTrip",
                "real echocameracore should execute input target behavior");
        mechanicsActivations.put(CAMERA_CORE_MODULE_ID, cameraCoreActivation);
        Map<String, Object> cinematicCoreActivation = requireActivationService(
                nativeServices,
                CINEMATIC_CORE_MODULE_ID,
                CINEMATIC_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "rendering", "ui.screens")
        );
        requireTrue(cinematicCoreActivation, "sequenceRenderRoundTrip",
                "real echocinematiccore should execute sequence render behavior");
        requireTrue(cinematicCoreActivation, "pacingRenderRoundTrip",
                "real echocinematiccore should execute pacing render behavior");
        requireTrue(cinematicCoreActivation, "triggerUiRoundTrip",
                "real echocinematiccore should execute trigger UI behavior");
        mechanicsActivations.put(CINEMATIC_CORE_MODULE_ID, cinematicCoreActivation);
        Map<String, Object> codexCoreActivation = requireActivationService(
                nativeServices,
                CODEX_CORE_MODULE_ID,
                CODEX_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics")
        );
        requireTrue(codexCoreActivation, "codexLookupExecuted",
                "real echocodexcore should execute Codex lookup behavior");
        requireTrue(codexCoreActivation, "serviceBridgeStarted",
                "real echocodexcore should start its service bridge");
        mechanicsActivations.put(CODEX_CORE_MODULE_ID, codexCoreActivation);
        Map<String, Object> creatorCoreActivation = requireActivationService(
                nativeServices,
                CREATOR_CORE_MODULE_ID,
                CREATOR_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "commands", "data", "packs", "ui.screens")
        );
        requireTrue(creatorCoreActivation, "commandPermissionRoundTrip",
                "real echocreatorcore should execute command permission behavior");
        requireTrue(creatorCoreActivation, "sessionDataRoundTrip",
                "real echocreatorcore should execute session project behavior");
        requireTrue(creatorCoreActivation, "packProjectRoundTrip",
                "real echocreatorcore should execute pack project behavior");
        requireTrue(creatorCoreActivation, "dashboardUiRoundTrip",
                "real echocreatorcore should execute dashboard UI behavior");
        mechanicsActivations.put(CREATOR_CORE_MODULE_ID, creatorCoreActivation);
        Map<String, Object> renderCoreActivation = requireActivationService(
                nativeServices,
                RENDER_CORE_MODULE_ID,
                RENDER_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics")
        );
        requireTrue(renderCoreActivation, "previewFrameExecuted",
                "real echorendercore should execute preview frame behavior");
        requireTrue(renderCoreActivation, "requiresRenderBridge",
                "real echorendercore should expose the render bridge requirement");
        mechanicsActivations.put(RENDER_CORE_MODULE_ID, renderCoreActivation);
        Map<String, Object> screenCoreActivation = requireActivationService(
                nativeServices,
                SCREEN_CORE_MODULE_ID,
                SCREEN_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "direct.native.module.entrypoint")
        );
        requireTrue(screenCoreActivation, "screenCompositionExecuted",
                "real echoscreencore should execute screen composition behavior");
        requireTrue(screenCoreActivation, "nativeProjectionPerformed",
                "real echoscreencore should project native screen surfaces");
        mechanicsActivations.put(SCREEN_CORE_MODULE_ID, screenCoreActivation);
        Map<String, Object> scriptCoreActivation = requireActivationService(
                nativeServices,
                SCRIPT_CORE_MODULE_ID,
                SCRIPT_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "commands", "data", "saves", "ui.screens")
        );
        requireTrue(scriptCoreActivation, "featureContractRoundTrip",
                "real echoscriptcore should execute script feature contracts");
        mechanicsActivations.put(SCRIPT_CORE_MODULE_ID, scriptCoreActivation);
        Map<String, Object> runtimeGuardActivation = requireActivationService(
                nativeServices,
                RUNTIME_GUARD_MODULE_ID,
                RUNTIME_GUARD_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics")
        );
        requireTrue(runtimeGuardActivation, "requiresRuntimeBudgetBridge",
                "real echoruntimeguard should expose runtime budget bridge behavior");
        requireTrue(runtimeGuardActivation, "requiresDiagnosticsBridge",
                "real echoruntimeguard should expose diagnostics bridge behavior");
        requireTrue(runtimeGuardActivation, "requiresNetworkBridge",
                "real echoruntimeguard should expose network bridge behavior");
        requireTrue(runtimeGuardActivation, "requiresCommandBridge",
                "real echoruntimeguard should expose command bridge behavior");
        mechanicsActivations.put(RUNTIME_GUARD_MODULE_ID, runtimeGuardActivation);
        Map<String, Object> communityBridgeActivation = requireActivationService(
                nativeServices,
                COMMUNITY_BRIDGE_MODULE_ID,
                COMMUNITY_BRIDGE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "data", "networking")
        );
        requireTrue(communityBridgeActivation, "activated",
                "real echocommunitybridge should activate its reference probe");
        requireTrue(communityBridgeActivation, "publicTextSanitized",
                "real echocommunitybridge should sanitize public text");
        requireTrue(communityBridgeActivation, "discordTextSanitized",
                "real echocommunitybridge should sanitize Discord text");
        requireTrue(communityBridgeActivation, "launcherSlashCommandBlocked",
                "real echocommunitybridge should block launcher slash-command relay");
        mechanicsActivations.put(COMMUNITY_BRIDGE_MODULE_ID, communityBridgeActivation);
        require(nativeServices.hasService(INDEX_MODULE_ID, INDEX_SERVICE_ID),
                "real echoindex native adapter should register its AdapterCore Index contract");
        EchoNativeRegisteredService indexService = registeredService(
                nativeServices,
                INDEX_MODULE_ID,
                INDEX_SERVICE_ID
        );
        require(indexService.surfaces().containsAll(List.of("adaptercore", "recipes", "ui_screens", "inventory")),
                "real echoindex service should preserve Index AdapterCore surfaces");
        Map<String, Object> indexActivation = serviceActivation(
                nativeServices,
                INDEX_MODULE_ID,
                INDEX_SERVICE_ID
        );
        requireTrue(indexActivation, "queryServiceExecuted",
                "real echoindex should execute Index query service behavior");
        requireTrue(indexActivation, "inventoryOverlayReady",
                "real echoindex should expose inventory overlay behavior");
        requireTrue(indexActivation, "nativeProjectionPerformed",
                "real echoindex should project its native inventory overlay");
        mechanicsActivations.put(INDEX_MODULE_ID, indexActivation);
        Map<String, Object> holomapActivation = requireActivationService(
                nativeServices,
                HOLOMAP_MODULE_ID,
                HOLOMAP_SERVICE_ID,
                List.of("native.module.entrypoint", "direct.native.module.entrypoint")
        );
        requireTrue(holomapActivation, "routeSnapshotExecuted",
                "real echoholomap should execute route snapshot behavior");
        requireTrue(holomapActivation, "serviceBridgeStarted",
                "real echoholomap should start its map service bridge");
        requireTrue(holomapActivation, "nativeProjectionPerformed",
                "real echoholomap should project native map surfaces");
        mechanicsActivations.put(HOLOMAP_MODULE_ID, holomapActivation);
        Map<String, Object> lensActivation = requireActivationService(
                nativeServices,
                LENS_MODULE_ID,
                LENS_SERVICE_ID,
                List.of("native.module.entrypoint", "direct.native.module.entrypoint")
        );
        requireTrue(lensActivation, "fieldScanExecuted",
                "real echolens should execute field scan behavior");
        requireTrue(lensActivation, "serviceBridgeStarted",
                "real echolens should start its inspection service bridge");
        requireTrue(lensActivation, "nativeProjectionPerformed",
                "real echolens should project native lens surfaces");
        mechanicsActivations.put(LENS_MODULE_ID, lensActivation);
        Map<String, Object> terminalActivation = requireActivationService(
                nativeServices,
                TERMINAL_MODULE_ID,
                TERMINAL_SERVICE_ID,
                List.of("native.module.entrypoint", "direct.native.module.entrypoint")
        );
        requireTrue(terminalActivation, "dashboardSurfaceExecuted",
                "real echoterminal should execute dashboard surface behavior");
        requireTrue(terminalActivation, "nativeProjectionPerformed",
                "real echoterminal should project native terminal surfaces");
        mechanicsActivations.put(TERMINAL_MODULE_ID, terminalActivation);
        Map<String, Object> textureForgeActivation = requireActivationService(
                nativeServices,
                TEXTURE_FORGE_MODULE_ID,
                TEXTURE_FORGE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "assets", "data", "ui.screens")
        );
        requireTrue(textureForgeActivation, "specRegistryRoundTrip",
                "real echotextureforge should execute spec registry behavior");
        requireTrue(textureForgeActivation, "promptExportRoundTrip",
                "real echotextureforge should execute prompt export behavior");
        requireTrue(textureForgeActivation, "textureAuditRoundTrip",
                "real echotextureforge should execute texture audit behavior");
        requireTrue(textureForgeActivation, "dashboardSurfaceResolved",
                "real echotextureforge should resolve its dashboard surface");
        mechanicsActivations.put(TEXTURE_FORGE_MODULE_ID, textureForgeActivation);
        Map<String, Object> themeCoreActivation = requireActivationService(
                nativeServices,
                THEME_CORE_MODULE_ID,
                THEME_CORE_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics", "assets", "rendering", "themes")
        );
        requireTrue(themeCoreActivation, "themeApplicationExecuted",
                "real echothemecore should execute theme application behavior");
        requireTrue(themeCoreActivation, "requiresThemeBridge",
                "real echothemecore should expose the theme bridge requirement");
        mechanicsActivations.put(THEME_CORE_MODULE_ID, themeCoreActivation);
        Map<String, Object> tutorialCoreActivation = requireActivationService(
                nativeServices,
                TUTORIAL_CORE_MODULE_ID,
                TUTORIAL_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "ui.screens", "data", "player")
        );
        requireTrue(tutorialCoreActivation, "featureContractRoundTrip",
                "real echotutorialcore should execute tutorial feature contracts");
        mechanicsActivations.put(TUTORIAL_CORE_MODULE_ID, tutorialCoreActivation);
        Map<String, Object> wikiActivation = requireActivationService(
                nativeServices,
                WIKI_MODULE_ID,
                WIKI_SERVICE_ID,
                List.of("native.module.entrypoint", "diagnostics")
        );
        requireTrue(wikiActivation, "guideSurfaceExecuted",
                "real echowiki should execute guide surface behavior");
        requireTrue(wikiActivation, "serviceBridgeStarted",
                "real echowiki should start its documentation service bridge");
        mechanicsActivations.put(WIKI_MODULE_ID, wikiActivation);
        Map<String, Object> baseGridActivation = requireActivationService(
                nativeServices,
                BASE_GRID_MODULE_ID,
                BASE_GRID_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(baseGridActivation.get("status")),
                "real echobasegrid should execute the Agent9 base grid runtime adapter");
        requireTrue(baseGridActivation, "activated",
                "real echobasegrid should expose activated Agent9 evidence");
        mechanicsActivations.put(BASE_GRID_MODULE_ID, baseGridActivation);
        Map<String, Object> industrialNexusActivation = requireActivationService(
                nativeServices,
                INDUSTRIAL_NEXUS_MODULE_ID,
                INDUSTRIAL_NEXUS_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(industrialNexusActivation.get("status")),
                "real echoindustrialnexus should execute the Agent9 industrial runtime adapter");
        requireTrue(industrialNexusActivation, "activated",
                "real echoindustrialnexus should expose activated Agent9 evidence");
        mechanicsActivations.put(INDUSTRIAL_NEXUS_MODULE_ID, industrialNexusActivation);
        Map<String, Object> logisticsNetworkActivation = requireActivationService(
                nativeServices,
                LOGISTICS_NETWORK_MODULE_ID,
                LOGISTICS_NETWORK_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(logisticsNetworkActivation.get("status")),
                "real echologisticsnetwork should execute the Agent9 logistics network runtime adapter");
        requireTrue(logisticsNetworkActivation, "activated",
                "real echologisticsnetwork should expose activated Agent9 evidence");
        mechanicsActivations.put(LOGISTICS_NETWORK_MODULE_ID, logisticsNetworkActivation);
        Map<String, Object> multiblockCoreActivation = requireActivationService(
                nativeServices,
                MULTIBLOCK_CORE_MODULE_ID,
                MULTIBLOCK_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(multiblockCoreActivation.get("status")),
                "real echomultiblockcore should execute the Agent9 multiblock runtime adapter");
        requireTrue(multiblockCoreActivation, "activated",
                "real echomultiblockcore should expose activated Agent9 evidence");
        mechanicsActivations.put(MULTIBLOCK_CORE_MODULE_ID, multiblockCoreActivation);
        Map<String, Object> powerGridActivation = requireActivationService(
                nativeServices,
                POWER_GRID_MODULE_ID,
                POWER_GRID_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(powerGridActivation.get("status")),
                "real echopowergrid should execute the Agent9 power grid runtime adapter");
        requireTrue(powerGridActivation, "activated",
                "real echopowergrid should expose activated Agent9 evidence");
        mechanicsActivations.put(POWER_GRID_MODULE_ID, powerGridActivation);
        require(nativeServices.hasService(MISSION_CORE_MODULE_ID, MISSION_CORE_SERVICE_ID),
                "real echomissioncore native adapter should register its AdapterCore mission contract");
        EchoNativeRegisteredService missionCoreService = registeredService(
                nativeServices,
                MISSION_CORE_MODULE_ID,
                MISSION_CORE_SERVICE_ID
        );
        require(missionCoreService.surfaces().contains("adaptercore"),
                "real echomissioncore service should preserve AdapterCore contract surface");
        Map<String, Object> missionCoreActivation = serviceActivation(
                nativeServices,
                MISSION_CORE_MODULE_ID,
                MISSION_CORE_SERVICE_ID
        );
        require(Boolean.TRUE.equals(missionCoreActivation.get("missionProgressionExecuted")),
                "real echomissioncore should execute its objective progression contract");
        require(nativeServices.hasService(PLAYER_CORE_MODULE_ID, PLAYER_CORE_SERVICE_ID),
                "real echoplayercore native adapter should register its player activation service");
        EchoNativeRegisteredService playerCoreService = registeredService(
                nativeServices,
                PLAYER_CORE_MODULE_ID,
                PLAYER_CORE_SERVICE_ID
        );
        require(playerCoreService.surfaces().containsAll(List.of("surface.module.entrypoint", "player", "networking")),
                "real echoplayercore service should preserve player activation surfaces");
        Map<String, Object> playerCoreActivation = serviceActivation(
                nativeServices,
                PLAYER_CORE_MODULE_ID,
                PLAYER_CORE_SERVICE_ID
        );
        require(Boolean.TRUE.equals(playerCoreActivation.get("featureContractRoundTrip")),
                "real echoplayercore should execute its player feature contract round trip");
        Map<String, Object> inputCoreActivation = requireActivationService(
                nativeServices,
                INPUT_CORE_MODULE_ID,
                INPUT_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "input")
        );
        requireTrue(inputCoreActivation, "featureContractRoundTrip",
                "real echoinputcore should execute input feature contracts");
        requireTrue(inputCoreActivation, "routePriorityExecuted",
                "real echoinputcore should execute route priority behavior");
        mechanicsActivations.put(INPUT_CORE_MODULE_ID, inputCoreActivation);
        Map<String, Object> hudCoreActivation = requireActivationService(
                nativeServices,
                HUD_CORE_MODULE_ID,
                HUD_CORE_SERVICE_ID,
                List.of("native.module.entrypoint")
        );
        requireTrue(hudCoreActivation, "hudSnapshotExecuted",
                "real echohudcore should execute HUD snapshot behavior");
        requireTrue(hudCoreActivation, "nativeProjectionPerformed",
                "real echohudcore should project native HUD surface behavior");
        mechanicsActivations.put(HUD_CORE_MODULE_ID, hudCoreActivation);
        Map<String, Object> notificationCoreActivation = requireActivationService(
                nativeServices,
                NOTIFICATION_CORE_MODULE_ID,
                NOTIFICATION_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics", "ui.screens")
        );
        requireTrue(notificationCoreActivation, "featureContractRoundTrip",
                "real echonotificationcore should execute notification feature contracts");
        mechanicsActivations.put(NOTIFICATION_CORE_MODULE_ID, notificationCoreActivation);
        Map<String, Object> vehicleCoreActivation = requireActivationService(
                nativeServices,
                VEHICLE_CORE_MODULE_ID,
                VEHICLE_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(vehicleCoreActivation.get("status")),
                "real echovehiclecore should execute the Agent9 vehicle runtime adapter");
        requireTrue(vehicleCoreActivation, "activated",
                "real echovehiclecore should expose activated Agent9 evidence");
        mechanicsActivations.put(VEHICLE_CORE_MODULE_ID, vehicleCoreActivation);
        require(nativeServices.hasService(BLOCKWORKS_MODULE_ID, BLOCKWORKS_SERVICE_ID),
                "real echoblockworks native adapter should register its blockworks activation service");
        EchoNativeRegisteredService blockworksService = registeredService(
                nativeServices,
                BLOCKWORKS_MODULE_ID,
                BLOCKWORKS_SERVICE_ID
        );
        require(blockworksService.surfaces().containsAll(List.of("surface.module.entrypoint", "blocks", "worldgen")),
                "real echoblockworks service should preserve block and worldgen activation surfaces");
        Map<String, Object> blockworksActivation = serviceActivation(
                nativeServices,
                BLOCKWORKS_MODULE_ID,
                BLOCKWORKS_SERVICE_ID
        );
        require(Boolean.TRUE.equals(blockworksActivation.get("blockCatalogRoundTrip")),
                "real echoblockworks should execute block catalog behavior");
        require(Boolean.TRUE.equals(blockworksActivation.get("paletteConversionRoundTrip")),
                "real echoblockworks should execute palette conversion behavior");
        require(Boolean.TRUE.equals(blockworksActivation.get("worldgenSiteRoundTrip")),
                "real echoblockworks should execute worldgen site behavior");
        require(nativeServices.hasService(PRESENCE_LINK_MODULE_ID, PRESENCE_LINK_SERVICE_ID),
                "real echopresencelink native adapter should register its AdapterCore presence contract");
        EchoNativeRegisteredService presenceLinkService = nativeServices.servicesForModule(PRESENCE_LINK_MODULE_ID).stream()
                .filter(service -> service.serviceId().equals(PRESENCE_LINK_SERVICE_ID))
                .findFirst()
                .orElseThrow();
        require(presenceLinkService.surfaces().contains("adaptercore"),
                "real echopresencelink service should preserve AdapterCore contract surface");
        Map<String, Object> convoyProtocolActivation = requireActivationService(
                nativeServices,
                CONVOY_PROTOCOL_MODULE_ID,
                CONVOY_PROTOCOL_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        require("PASS".equals(convoyProtocolActivation.get("status")),
                "real echoconvoyprotocol should execute the Agent9 convoy runtime adapter");
        requireTrue(convoyProtocolActivation, "activated",
                "real echoconvoyprotocol should expose activated convoy runtime evidence");
        requireTrue(convoyProtocolActivation, "serviceCodeExecuted",
                "real echoconvoyprotocol should execute convoy route service code");
        mechanicsActivations.put(CONVOY_PROTOCOL_MODULE_ID, convoyProtocolActivation);
        Map<String, Object> familiarCoreActivation = requireActivationService(
                nativeServices,
                FAMILIAR_CORE_MODULE_ID,
                FAMILIAR_CORE_SERVICE_ID,
                List.of("surface.module.entrypoint", "diagnostics")
        );
        requireTrue(familiarCoreActivation, "featureContractRoundTrip",
                "real echofamiliarcore should execute familiar registry, bond, command, and upgrade contracts");
        requireTrue(familiarCoreActivation, "serviceCodeExecuted",
                "real echofamiliarcore should execute familiar service code");
        mechanicsActivations.put(FAMILIAR_CORE_MODULE_ID, familiarCoreActivation);
        Map<String, Object> ashfallProtocolActivation = requireActivationService(
                nativeServices,
                ASHFALL_PROTOCOL_MODULE_ID,
                ASHFALL_PROTOCOL_SERVICE_ID,
                List.of("native.module.entrypoint", "direct.native.module.entrypoint")
        );
        requireTrue(ashfallProtocolActivation, "gameplayHookEvidence",
                "real echoashfallprotocol should execute Ashfall gameplay hook evidence");
        requireTrue(ashfallProtocolActivation, "activated",
                "real echoashfallprotocol should activate its Ashfall Protocol native surface");
        requireTrue(ashfallProtocolActivation, "adapterCoreUsed",
                "real echoashfallprotocol should execute through AdapterCore surfaces");
        requireTrue(ashfallProtocolActivation, "nativeAdapterCodeExecuted",
                "real echoashfallprotocol should execute native adapter code");
        require(ashfallProtocolActivation.containsKey("serviceCodeExecuted"),
                "real echoashfallprotocol should report Ashfall service bridge execution state");
        require(ashfallProtocolActivation.containsKey("adapterCoreRuntimeTargetStatus"),
                "real echoashfallprotocol should report its AdapterCore runtime target status");
        require("PASS".equals(ashfallProtocolActivation.get("majorRouteAdapterCoreCommandStatus")),
                "real echoashfallprotocol should execute the major route AdapterCore command contract");
        require("PASS".equals(ashfallProtocolActivation.get("midgameRouteAdapterCoreReplayStatus")),
                "real echoashfallprotocol should execute the midgame route AdapterCore replay contract");
        require("PASS".equals(ashfallProtocolActivation.get("lateGameRouteAdapterCoreReplayStatus")),
                "real echoashfallprotocol should execute the late-game route AdapterCore replay contract");
        require(ashfallProtocolActivation.containsKey("machineRuntimeBindingStatus"),
                "real echoashfallprotocol should report its machine runtime binding status");
        require(ashfallProtocolActivation.containsKey("agent9NativeTechRuntimeStatus"),
                "real echoashfallprotocol should report its Agent9 native tech runtime status");
        require(ashfallProtocolActivation.containsKey("agent9TechModuleEntrypointsStatus"),
                "real echoashfallprotocol should report its Agent9 tech module entrypoint status");
        mechanicsActivations.put(ASHFALL_PROTOCOL_MODULE_ID, ashfallProtocolActivation);
        EchoNativeRegisteredService galacticCoreRuntimeService = requireRegisteredService(
                nativeServices,
                GALACTIC_CORE_MODULE_ID,
                GALACTIC_CORE_RUNTIME_SERVICE_ID,
                List.of("gameplay", "machines", "oxygen", "energy", "player_gear", "rockets")
        );
        EchoNativeRegisteredService galacticCoreRuntimeGatewayService = requireRegisteredService(
                nativeServices,
                GALACTIC_CORE_MODULE_ID,
                GALACTIC_CORE_RUNTIME_GATEWAY_SERVICE_ID,
                List.of("events", "network", "screens", "gameplay")
        );
        EchoNativeRegisteredService galacticCoreHostExecutionService = requireRegisteredService(
                nativeServices,
                GALACTIC_CORE_MODULE_ID,
                GALACTIC_CORE_HOST_EXECUTION_SERVICE_ID,
                List.of("host_execution", "dimension_transfer", "dungeons", "screens", "events")
        );
        EchoNativeRegisteredService galacticCoreLiveSessionMutationsService = requireRegisteredService(
                nativeServices,
                GALACTIC_CORE_MODULE_ID,
                GALACTIC_CORE_LIVE_SESSION_MUTATIONS_SERVICE_ID,
                List.of("live_session", "host_mutation", "world", "entities", "screens")
        );
        Map<String, Object> galacticCoreRuntimeGatewayEvidence = serviceActivation(
                nativeServices,
                GALACTIC_CORE_MODULE_ID,
                GALACTIC_CORE_RUNTIME_GATEWAY_SERVICE_ID
        );
        Map<String, Object> galacticCoreHostExecutionEvidence = serviceActivation(
                nativeServices,
                GALACTIC_CORE_MODULE_ID,
                GALACTIC_CORE_HOST_EXECUTION_SERVICE_ID
        );
        Map<String, Object> galacticCoreLiveSessionEvidence = serviceActivation(
                nativeServices,
                GALACTIC_CORE_MODULE_ID,
                GALACTIC_CORE_LIVE_SESSION_MUTATIONS_SERVICE_ID
        );
        requireTrue(galacticCoreRuntimeGatewayEvidence, "typedReceiptsOnly",
                "real echogalacticcore runtime gateway should expose typed receipt evidence");
        requireTrue(galacticCoreHostExecutionEvidence, "typedReceiptsOnly",
                "real echogalacticcore host execution bridge should expose typed receipt evidence");
        requireTrue(galacticCoreLiveSessionEvidence, "hostOwnedMutationBoundary",
                "real echogalacticcore live session mutations should stay host-owned");
        int galacticCoreRuntimeGatewaySmokeActionCount = requireReflectiveListResult(
                serviceObject(nativeServices, GALACTIC_CORE_MODULE_ID, GALACTIC_CORE_RUNTIME_GATEWAY_SERVICE_ID),
                "releaseSmokeActions",
                20,
                "real echogalacticcore runtime gateway should execute release smoke actions"
        ).size();
        int galacticCoreHostExecutionSmokeActionCount = requireReflectiveListResult(
                serviceObject(nativeServices, GALACTIC_CORE_MODULE_ID, GALACTIC_CORE_HOST_EXECUTION_SERVICE_ID),
                "releaseHostExecutionSmokeActions",
                5,
                "real echogalacticcore host execution bridge should execute host smoke actions"
        ).size();
        int galacticCoreLiveSessionMutationCount = requireReflectiveListResult(
                serviceObject(nativeServices, GALACTIC_CORE_MODULE_ID, GALACTIC_CORE_LIVE_SESSION_MUTATIONS_SERVICE_ID),
                "releaseLiveSessionMutationSmokeResults",
                5,
                "real echogalacticcore live session mutations should execute live mutation smoke results"
        ).size();
        Map<String, Object> signalOsActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                SIGNAL_OS_MODULE_ID,
                SIGNAL_OS_SERVICE_ID,
                "SignalOS terminal, archive, drive, mission, and chapter route"
        );
        requireTrue(signalOsActivation, "terminalSessionExecuted",
                "real signalos should execute its terminal session contract");
        mechanicsActivations.put(SIGNAL_OS_MODULE_ID, signalOsActivation);
        Map<String, Object> spellCoreActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                SPELL_CORE_MODULE_ID,
                SPELL_CORE_SERVICE_ID,
                "SpellCore Signal Focus cast route"
        );
        requireTrue(spellCoreActivation, "spellCastResolved",
                "real echospellcore should execute its Signal Focus cast resolution");
        mechanicsActivations.put(SPELL_CORE_MODULE_ID, spellCoreActivation);
        Map<String, Object> ritualCoreActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                RITUAL_CORE_MODULE_ID,
                RITUAL_CORE_SERVICE_ID,
                "RitualCore relic stabilization route"
        );
        requireTrue(ritualCoreActivation, "ritualActivationExecuted",
                "real echoritualcore should execute its altar activation service");
        mechanicsActivations.put(RITUAL_CORE_MODULE_ID, ritualCoreActivation);
        Map<String, Object> curseCoreActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                CURSE_CORE_MODULE_ID,
                CURSE_CORE_SERVICE_ID,
                "CurseCore Echo Rot state route"
        );
        requireTrue(curseCoreActivation, "curseStateResolved",
                "real echocursecore should execute its curse state service");
        mechanicsActivations.put(CURSE_CORE_MODULE_ID, curseCoreActivation);
        Map<String, Object> riftWorldsActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                RIFT_WORLDS_MODULE_ID,
                RIFT_WORLDS_SERVICE_ID,
                "RiftWorlds pocket rift route"
        );
        requireTrue(riftWorldsActivation, "pocketLifecycleExecuted",
                "real echoriftworlds should execute its pocket rift lifecycle");
        mechanicsActivations.put(RIFT_WORLDS_MODULE_ID, riftWorldsActivation);
        Map<String, Object> blackboxProtocolActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                BLACKBOX_PROTOCOL_MODULE_ID,
                BLACKBOX_PROTOCOL_SERVICE_ID,
                "Blackbox Protocol Prime route archive"
        );
        mechanicsActivations.put(BLACKBOX_PROTOCOL_MODULE_ID, blackboxProtocolActivation);
        Map<String, Object> nexusProtocolActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                NEXUS_PROTOCOL_MODULE_ID,
                NEXUS_PROTOCOL_SERVICE_ID,
                "Nexus Protocol Prime route signal"
        );
        mechanicsActivations.put(NEXUS_PROTOCOL_MODULE_ID, nexusProtocolActivation);
        Map<String, Object> orbitalRemnantsActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                ORBITAL_REMNANTS_MODULE_ID,
                ORBITAL_REMNANTS_SERVICE_ID,
                "Orbital Remnants Prime route data drive"
        );
        mechanicsActivations.put(ORBITAL_REMNANTS_MODULE_ID, orbitalRemnantsActivation);
        Map<String, Object> primeCoreActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                PRIME_CORE_MODULE_ID,
                PRIME_CORE_SERVICE_ID,
                "PrimeCore route mission and flag"
        );
        mechanicsActivations.put(PRIME_CORE_MODULE_ID, primeCoreActivation);
        Map<String, Object> stationfallActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                STATIONFALL_MODULE_ID,
                STATIONFALL_SERVICE_ID,
                "Stationfall chapter unlock route"
        );
        mechanicsActivations.put(STATIONFALL_MODULE_ID, stationfallActivation);
        Map<String, Object> grimoireActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                GRIMOIRE_MODULE_ID,
                GRIMOIRE_SERVICE_ID,
                "Grimoire Arcane Codex archive"
        );
        mechanicsActivations.put(GRIMOIRE_MODULE_ID, grimoireActivation);
        Map<String, Object> arcanaCoreActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                ARCANA_CORE_MODULE_ID,
                ARCANA_CORE_SERVICE_ID,
                "ArcanaCore codex mission and flag"
        );
        mechanicsActivations.put(ARCANA_CORE_MODULE_ID, arcanaCoreActivation);
        Map<String, Object> relicTechActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                RELIC_TECH_MODULE_ID,
                RELIC_TECH_SERVICE_ID,
                "RelicTech containment route"
        );
        requireTrue(relicTechActivation, "containmentPlanExecuted",
                "real echorelictech should execute its containment plan service");
        mechanicsActivations.put(RELIC_TECH_MODULE_ID, relicTechActivation);
        Map<String, Object> arcaneIndexActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                ARCANE_INDEX_MODULE_ID,
                ARCANE_INDEX_SERVICE_ID,
                "ArcaneIndex chapter unlock route"
        );
        mechanicsActivations.put(ARCANE_INDEX_MODULE_ID, arcaneIndexActivation);
        Map<String, Object> aetherWorksActivation = requireStoryAdapterCoreActivation(
                nativeServices,
                AETHER_WORKS_MODULE_ID,
                AETHER_WORKS_SERVICE_ID,
                "AetherWorks presence link route"
        );
        mechanicsActivations.put(AETHER_WORKS_MODULE_ID, aetherWorksActivation);
        require(nativeServices.hasService(ADDON_API_MODULE_ID, ADDON_API_SERVICE_ID),
                "real echoaddonapi Native Platform entrypoint should register its public API service");
        EchoNativeRegisteredService addonApiService = nativeServices.servicesForModule(ADDON_API_MODULE_ID).stream()
                .filter(service -> service.serviceId().equals(ADDON_API_SERVICE_ID))
                .findFirst()
                .orElseThrow();
        require(addonApiService.surfaces().containsAll(List.of("public_api", "sdk_spine", "addon_contracts")),
                "real echoaddonapi service should preserve declared native service surfaces");

        manager.reloadData(result, services);
        for (RealModuleSpec spec : REAL_MODULES) {
            require(registry.lifecycle(spec.moduleId()) == EchoRuntimeModuleLifecycle.DATA_RELOADED,
                    "real " + spec.moduleId() + " should survive data reload");
        }
        require(nativeServices.hasService(ADDON_API_MODULE_ID, ADDON_API_SERVICE_ID),
                "real echoaddonapi service should remain available after reload");
        require(nativeServices.hasService(SCHEMA_CORE_MODULE_ID, SCHEMA_CORE_SERVICE_ID),
                "real echoschemacore schema activation service should remain available after reload");
        require(nativeServices.hasService(VALIDATION_CORE_MODULE_ID, VALIDATION_CORE_SERVICE_ID),
                "real echovalidationcore validation activation service should remain available after reload");
        require(nativeServices.hasService(CONTENT_CORE_MODULE_ID, CONTENT_CORE_SERVICE_ID),
                "real echocontentcore content activation service should remain available after reload");
        require(nativeServices.hasService(ASSET_CORE_MODULE_ID, ASSET_CORE_SERVICE_ID),
                "real echoassetcore asset activation service should remain available after reload");
        require(nativeServices.hasService(RECIPE_CORE_MODULE_ID, RECIPE_CORE_SERVICE_ID),
                "real echorecipecore recipe activation service should remain available after reload");
        require(nativeServices.hasService(PACK_CORE_MODULE_ID, PACK_CORE_SERVICE_ID),
                "real echopackcore pack contract should remain available after reload");
        require(nativeServices.hasService(METADATA_CORE_MODULE_ID, METADATA_CORE_SERVICE_ID),
                "real echometadatacore activation service should remain available after reload");
        require(nativeServices.hasService(MODULE_GRAPH_MODULE_ID, MODULE_GRAPH_SERVICE_ID),
                "real echomodulegraph activation service should remain available after reload");
        require(nativeServices.hasService(NET_CORE_MODULE_ID, NET_CORE_SERVICE_ID),
                "real echonetcore packet service contract should remain available after reload");
        require(nativeServices.hasService(DATA_CORE_MODULE_ID, DATA_CORE_SERVICE_ID),
                "real echodatacore data contract should remain available after reload");
        require(nativeServices.hasService(WORLD_CORE_MODULE_ID, WORLD_CORE_SERVICE_ID),
                "real echoworldcore world contract should remain available after reload");
        require(nativeServices.hasService(ATMOSPHERE_CORE_MODULE_ID, ATMOSPHERE_CORE_SERVICE_ID),
                "real echoatmospherecore activation service should remain available after reload");
        require(nativeServices.hasService(WEATHER_CORE_MODULE_ID, WEATHER_CORE_SERVICE_ID),
                "real echoweathercore activation service should remain available after reload");
        require(nativeServices.hasService(PROGRESSION_CORE_MODULE_ID, PROGRESSION_CORE_SERVICE_ID),
                "real echoprogressioncore activation service should remain available after reload");
        require(nativeServices.hasService(SOCIAL_CORE_MODULE_ID, SOCIAL_CORE_SERVICE_ID),
                "real echosocialcore activation service should remain available after reload");
        require(nativeServices.hasService(EVENT_CORE_MODULE_ID, EVENT_CORE_SERVICE_ID),
                "real echoeventcore activation service should remain available after reload");
        require(nativeServices.hasService(ENCOUNTER_CORE_MODULE_ID, ENCOUNTER_CORE_SERVICE_ID),
                "real echoencountercore activation service should remain available after reload");
        require(nativeServices.hasService(ECONOMY_CORE_MODULE_ID, ECONOMY_CORE_SERVICE_ID),
                "real echoeconomycore activation service should remain available after reload");
        require(nativeServices.hasService(QUEST_DIRECTOR_MODULE_ID, QUEST_DIRECTOR_SERVICE_ID),
                "real echoquestdirector activation service should remain available after reload");
        require(nativeServices.hasService(NPC_CORE_MODULE_ID, NPC_CORE_SERVICE_ID),
                "real echonpcore activation service should remain available after reload");
        require(nativeServices.hasService(GUIDE_CORE_MODULE_ID, GUIDE_CORE_SERVICE_ID),
                "real echoguidecore activation service should remain available after reload");
        require(nativeServices.hasService(LORE_CORE_MODULE_ID, LORE_CORE_SERVICE_ID),
                "real echolorecore activation service should remain available after reload");
        require(nativeServices.hasService(STATUS_CORE_MODULE_ID, STATUS_CORE_SERVICE_ID),
                "real echostatuscore activation service should remain available after reload");
        require(nativeServices.hasService(SPAWN_CORE_MODULE_ID, SPAWN_CORE_SERVICE_ID),
                "real echospawncore activation service should remain available after reload");
        require(nativeServices.hasService(STRUCTURE_CORE_MODULE_ID, STRUCTURE_CORE_SERVICE_ID),
                "real echostructurecore activation service should remain available after reload");
        require(nativeServices.hasService(LOOT_CORE_MODULE_ID, LOOT_CORE_SERVICE_ID),
                "real echolootcore activation service should remain available after reload");
        require(nativeServices.hasService(BIOME_CORE_MODULE_ID, BIOME_CORE_SERVICE_ID),
                "real echobiomecore activation service should remain available after reload");
        require(nativeServices.hasService(DIFFICULTY_CORE_MODULE_ID, DIFFICULTY_CORE_SERVICE_ID),
                "real echodifficultycore activation service should remain available after reload");
        require(nativeServices.hasService(COMBAT_CORE_MODULE_ID, COMBAT_CORE_SERVICE_ID),
                "real echocombatcore activation service should remain available after reload");
        require(nativeServices.hasService(CREATURE_CORE_MODULE_ID, CREATURE_CORE_SERVICE_ID),
                "real echocreaturecore activation service should remain available after reload");
        require(nativeServices.hasService(ARMORY_MODULE_ID, ARMORY_SERVICE_ID),
                "real echoarmory activation service should remain available after reload");
        require(nativeServices.hasService(AGRICULTURE_RECLAMATION_MODULE_ID, AGRICULTURE_RECLAMATION_SERVICE_ID),
                "real echoagriculturereclamation activation service should remain available after reload");
        require(nativeServices.hasService(MACHINE_CORE_MODULE_ID, MACHINE_CORE_SERVICE_ID),
                "real echomachinecore activation service should remain available after reload");
        require(nativeServices.hasService(POWER_CORE_MODULE_ID, POWER_CORE_SERVICE_ID),
                "real echopowercore activation service should remain available after reload");
        require(nativeServices.hasService(LOGISTICS_CORE_MODULE_ID, LOGISTICS_CORE_SERVICE_ID),
                "real echologisticscore activation service should remain available after reload");
        require(nativeServices.hasService(SOUND_CORE_MODULE_ID, SOUND_CORE_SERVICE_ID),
                "real echosoundcore activation service should remain available after reload");
        require(nativeServices.hasService(RECOVERY_MODULE_ID, RECOVERY_SERVICE_ID),
                "real echorecovery activation service should remain available after reload");
        require(nativeServices.hasService(HEALTH_CORE_MODULE_ID, HEALTH_CORE_SERVICE_ID),
                "real echohealthcore health contract should remain available after reload");
        require(nativeServices.hasService(AGENT_CORE_MODULE_ID, AGENT_CORE_SERVICE_ID),
                "real echoagentcore activation service should remain available after reload");
        require(nativeServices.hasService(BRIDGE_CORE_MODULE_ID, BRIDGE_CORE_SERVICE_ID),
                "real echobridgecore activation service should remain available after reload");
        require(nativeServices.hasService(REPORT_CORE_MODULE_ID, REPORT_CORE_SERVICE_ID),
                "real echoreportcore activation service should remain available after reload");
        require(nativeServices.hasService(CAMERA_CORE_MODULE_ID, CAMERA_CORE_SERVICE_ID),
                "real echocameracore activation service should remain available after reload");
        require(nativeServices.hasService(CINEMATIC_CORE_MODULE_ID, CINEMATIC_CORE_SERVICE_ID),
                "real echocinematiccore activation service should remain available after reload");
        require(nativeServices.hasService(CODEX_CORE_MODULE_ID, CODEX_CORE_SERVICE_ID),
                "real echocodexcore activation service should remain available after reload");
        require(nativeServices.hasService(CREATOR_CORE_MODULE_ID, CREATOR_CORE_SERVICE_ID),
                "real echocreatorcore activation service should remain available after reload");
        require(nativeServices.hasService(RENDER_CORE_MODULE_ID, RENDER_CORE_SERVICE_ID),
                "real echorendercore activation service should remain available after reload");
        require(nativeServices.hasService(SCREEN_CORE_MODULE_ID, SCREEN_CORE_SERVICE_ID),
                "real echoscreencore activation service should remain available after reload");
        require(nativeServices.hasService(SCRIPT_CORE_MODULE_ID, SCRIPT_CORE_SERVICE_ID),
                "real echoscriptcore activation service should remain available after reload");
        require(nativeServices.hasService(RUNTIME_GUARD_MODULE_ID, RUNTIME_GUARD_SERVICE_ID),
                "real echoruntimeguard activation service should remain available after reload");
        require(nativeServices.hasService(COMMUNITY_BRIDGE_MODULE_ID, COMMUNITY_BRIDGE_SERVICE_ID),
                "real echocommunitybridge activation service should remain available after reload");
        require(nativeServices.hasService(INDEX_MODULE_ID, INDEX_SERVICE_ID),
                "real echoindex AdapterCore contract should remain available after reload");
        require(nativeServices.hasService(HOLOMAP_MODULE_ID, HOLOMAP_SERVICE_ID),
                "real echoholomap activation service should remain available after reload");
        require(nativeServices.hasService(LENS_MODULE_ID, LENS_SERVICE_ID),
                "real echolens activation service should remain available after reload");
        require(nativeServices.hasService(TERMINAL_MODULE_ID, TERMINAL_SERVICE_ID),
                "real echoterminal activation service should remain available after reload");
        require(nativeServices.hasService(TEXTURE_FORGE_MODULE_ID, TEXTURE_FORGE_SERVICE_ID),
                "real echotextureforge activation service should remain available after reload");
        require(nativeServices.hasService(THEME_CORE_MODULE_ID, THEME_CORE_SERVICE_ID),
                "real echothemecore activation service should remain available after reload");
        require(nativeServices.hasService(TUTORIAL_CORE_MODULE_ID, TUTORIAL_CORE_SERVICE_ID),
                "real echotutorialcore activation service should remain available after reload");
        require(nativeServices.hasService(WIKI_MODULE_ID, WIKI_SERVICE_ID),
                "real echowiki activation service should remain available after reload");
        require(nativeServices.hasService(BASE_GRID_MODULE_ID, BASE_GRID_SERVICE_ID),
                "real echobasegrid activation service should remain available after reload");
        require(nativeServices.hasService(INDUSTRIAL_NEXUS_MODULE_ID, INDUSTRIAL_NEXUS_SERVICE_ID),
                "real echoindustrialnexus activation service should remain available after reload");
        require(nativeServices.hasService(LOGISTICS_NETWORK_MODULE_ID, LOGISTICS_NETWORK_SERVICE_ID),
                "real echologisticsnetwork activation service should remain available after reload");
        require(nativeServices.hasService(MULTIBLOCK_CORE_MODULE_ID, MULTIBLOCK_CORE_SERVICE_ID),
                "real echomultiblockcore activation service should remain available after reload");
        require(nativeServices.hasService(POWER_GRID_MODULE_ID, POWER_GRID_SERVICE_ID),
                "real echopowergrid activation service should remain available after reload");
        require(nativeServices.hasService(MISSION_CORE_MODULE_ID, MISSION_CORE_SERVICE_ID),
                "real echomissioncore mission contract should remain available after reload");
        require(nativeServices.hasService(PLAYER_CORE_MODULE_ID, PLAYER_CORE_SERVICE_ID),
                "real echoplayercore player activation service should remain available after reload");
        require(nativeServices.hasService(INPUT_CORE_MODULE_ID, INPUT_CORE_SERVICE_ID),
                "real echoinputcore activation service should remain available after reload");
        require(nativeServices.hasService(HUD_CORE_MODULE_ID, HUD_CORE_SERVICE_ID),
                "real echohudcore activation service should remain available after reload");
        require(nativeServices.hasService(NOTIFICATION_CORE_MODULE_ID, NOTIFICATION_CORE_SERVICE_ID),
                "real echonotificationcore activation service should remain available after reload");
        require(nativeServices.hasService(VEHICLE_CORE_MODULE_ID, VEHICLE_CORE_SERVICE_ID),
                "real echovehiclecore activation service should remain available after reload");
        require(nativeServices.hasService(BLOCKWORKS_MODULE_ID, BLOCKWORKS_SERVICE_ID),
                "real echoblockworks blockworks activation service should remain available after reload");
        require(nativeServices.hasService(PRESENCE_LINK_MODULE_ID, PRESENCE_LINK_SERVICE_ID),
                "real echopresencelink presence contract should remain available after reload");
        require(nativeServices.hasService(CONVOY_PROTOCOL_MODULE_ID, CONVOY_PROTOCOL_SERVICE_ID),
                "real echoconvoyprotocol activation service should remain available after reload");
        require(nativeServices.hasService(FAMILIAR_CORE_MODULE_ID, FAMILIAR_CORE_SERVICE_ID),
                "real echofamiliarcore activation service should remain available after reload");
        require(nativeServices.hasService(ASHFALL_PROTOCOL_MODULE_ID, ASHFALL_PROTOCOL_SERVICE_ID),
                "real echoashfallprotocol activation service should remain available after reload");
        require(nativeServices.hasService(GALACTIC_CORE_MODULE_ID, GALACTIC_CORE_RUNTIME_SERVICE_ID),
                "real echogalacticcore runtime service should remain available after reload");
        require(nativeServices.hasService(GALACTIC_CORE_MODULE_ID, GALACTIC_CORE_RUNTIME_GATEWAY_SERVICE_ID),
                "real echogalacticcore runtime gateway should remain available after reload");
        require(nativeServices.hasService(GALACTIC_CORE_MODULE_ID, GALACTIC_CORE_HOST_EXECUTION_SERVICE_ID),
                "real echogalacticcore host execution bridge should remain available after reload");
        require(nativeServices.hasService(GALACTIC_CORE_MODULE_ID, GALACTIC_CORE_LIVE_SESSION_MUTATIONS_SERVICE_ID),
                "real echogalacticcore live session mutations should remain available after reload");
        require(nativeServices.hasService(SIGNAL_OS_MODULE_ID, SIGNAL_OS_SERVICE_ID),
                "real signalos AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(SPELL_CORE_MODULE_ID, SPELL_CORE_SERVICE_ID),
                "real echospellcore AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(RITUAL_CORE_MODULE_ID, RITUAL_CORE_SERVICE_ID),
                "real echoritualcore AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(CURSE_CORE_MODULE_ID, CURSE_CORE_SERVICE_ID),
                "real echocursecore AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(RIFT_WORLDS_MODULE_ID, RIFT_WORLDS_SERVICE_ID),
                "real echoriftworlds AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(BLACKBOX_PROTOCOL_MODULE_ID, BLACKBOX_PROTOCOL_SERVICE_ID),
                "real echoblackboxprotocol AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(NEXUS_PROTOCOL_MODULE_ID, NEXUS_PROTOCOL_SERVICE_ID),
                "real echonexusprotocol AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(ORBITAL_REMNANTS_MODULE_ID, ORBITAL_REMNANTS_SERVICE_ID),
                "real echoorbitalremnants AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(PRIME_CORE_MODULE_ID, PRIME_CORE_SERVICE_ID),
                "real echoprimecore AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(STATIONFALL_MODULE_ID, STATIONFALL_SERVICE_ID),
                "real echostationfall AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(GRIMOIRE_MODULE_ID, GRIMOIRE_SERVICE_ID),
                "real echogrimoire AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(ARCANA_CORE_MODULE_ID, ARCANA_CORE_SERVICE_ID),
                "real echoarcanacore AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(RELIC_TECH_MODULE_ID, RELIC_TECH_SERVICE_ID),
                "real echorelictech AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(ARCANE_INDEX_MODULE_ID, ARCANE_INDEX_SERVICE_ID),
                "real echoarcaneindex AdapterCore story contract should remain available after reload");
        require(nativeServices.hasService(AETHER_WORKS_MODULE_ID, AETHER_WORKS_SERVICE_ID),
                "real echoaetherworks AdapterCore story contract should remain available after reload");

        List<Pair> nativeServiceCountsBeforeUnload = nativeServiceCounts(nativeServices);
        List<Pair> nativeServiceIdsBeforeUnload = nativeServiceIds(nativeServices);

        manager.unload(result, services);
        for (RealModuleSpec spec : REAL_MODULES) {
            require(registry.lifecycle(spec.moduleId()) == EchoRuntimeModuleLifecycle.UNLOADED,
                    "real " + spec.moduleId() + " should unload cleanly");
            require(nativeServices.servicesForModule(spec.moduleId()).isEmpty(),
                    "real " + spec.moduleId() + " native services should be revoked on unload");
        }

        writeReport(
                Path.of(".").toAbsolutePath().normalize(),
                modulesRepoRoot,
                registry,
                schemaCoreService,
                validationCoreService,
                contentCoreService,
                contentCoreActivation,
                assetCoreService,
                assetCoreActivation,
                recipeCoreService,
                recipeCoreActivation,
                packCoreService,
                packCoreActivation,
                netCoreService,
                dataCoreService,
                dataCoreActivation,
                worldCoreService,
                worldCoreActivation,
                mechanicsActivations,
                healthCoreService,
                healthCoreActivation,
                missionCoreService,
                missionCoreActivation,
                playerCoreService,
                playerCoreActivation,
                blockworksService,
                blockworksActivation,
                presenceLinkService,
                ashfallProtocolActivation,
                galacticCoreRuntimeService,
                galacticCoreRuntimeGatewayService,
                galacticCoreHostExecutionService,
                galacticCoreLiveSessionMutationsService,
                galacticCoreRuntimeGatewaySmokeActionCount,
                galacticCoreHostExecutionSmokeActionCount,
                galacticCoreLiveSessionMutationCount,
                addonApiService,
                nativeServiceCountsBeforeUnload,
                nativeServiceIdsBeforeUnload,
                diagnostics
        );
        System.out.println("real module execution smoke PASS modules="
                + REAL_MODULES.size()
                + " lifecycle=load,reload,unload"
                + " schemaService=" + SCHEMA_CORE_SERVICE_ID
                + " validationService=" + VALIDATION_CORE_SERVICE_ID
                + " contentService=" + CONTENT_CORE_SERVICE_ID
                + " assetService=" + ASSET_CORE_SERVICE_ID
                + " recipeService=" + RECIPE_CORE_SERVICE_ID
                + " packService=" + PACK_CORE_SERVICE_ID
                + " netService=" + NET_CORE_SERVICE_ID
                + " dataService=" + DATA_CORE_SERVICE_ID
                + " worldService=" + WORLD_CORE_SERVICE_ID
                + " mechanicsServices=" + MECHANICS_SERVICE_IDS.size()
                + " healthService=" + HEALTH_CORE_SERVICE_ID
                + " missionService=" + MISSION_CORE_SERVICE_ID
                + " playerService=" + PLAYER_CORE_SERVICE_ID
                + " blockworksService=" + BLOCKWORKS_SERVICE_ID
                + " presenceService=" + PRESENCE_LINK_SERVICE_ID
                + " nativeService=" + ADDON_API_SERVICE_ID
                + " surfaces=" + addonApiService.surfaces().size()
                + " diagnostics=" + diagnostics.diagnostics().size());
    }

    private static void materializeDescriptor(Path realDescriptor, Path materializedDescriptor) throws IOException {
        Files.createDirectories(materializedDescriptor.getParent());
        String descriptor = Files.readString(realDescriptor, StandardCharsets.UTF_8)
                .replace("\"nativeClasspath\": []", "\"nativeClasspath\": [\"classes\"]");
        if (!descriptor.contains("\"forceStandaloneExecution\"")) {
            String accessToken = "\"access\": {";
            int accessIndex = descriptor.indexOf(accessToken);
            require(accessIndex >= 0, "real descriptor should declare access object: " + realDescriptor);
            descriptor = descriptor.substring(0, accessIndex)
                    + accessToken
                    + "\n    \"forceStandaloneExecution\": true,"
                    + descriptor.substring(accessIndex + accessToken.length());
        }
        require(descriptor.contains("\"nativeClasspath\": [\"classes\"]"),
                "materialized real module descriptor should declare a native classpath: " + realDescriptor);
        require(descriptor.contains("\"forceStandaloneExecution\": true"),
                "materialized real module descriptor should force standalone execution: " + realDescriptor);
        Files.writeString(materializedDescriptor, descriptor, StandardCharsets.UTF_8);
    }

    private static void materializeDataOnlyDependencies(Path modulesRepoRoot, Path fixtureRoot) throws IOException {
        for (String moduleId : DATA_ONLY_DEPENDENCY_MODULES) {
            Path realDescriptor = modulesRepoRoot.resolve("addons")
                    .resolve(moduleId)
                    .resolve("src/main/resources/META-INF/echo.mod.json");
            Path materializedDescriptor = fixtureRoot.resolve(moduleId)
                    .resolve("src/main/resources/META-INF/echo.mod.json");
            require(Files.isRegularFile(realDescriptor),
                    "data-only dependency descriptor should exist for " + moduleId + ": " + realDescriptor);
            materializeDataOnlyDescriptor(realDescriptor, materializedDescriptor);
        }
    }

    private static void materializeDataOnlyDescriptor(Path realDescriptor, Path materializedDescriptor) throws IOException {
        Files.createDirectories(materializedDescriptor.getParent());
        String descriptor = Files.readString(realDescriptor, StandardCharsets.UTF_8)
                .replaceFirst("(?m)(\\s*\"entrypoint\"\\s*:\\s*)\"[^\"]*\"", "$1\"\"");
        descriptor = descriptor.replaceFirst("(?m)(\\s*\"nativeEntrypoint\"\\s*:\\s*)\"[^\"]*\"", "$1\"\"");
        require(descriptor.contains("\"entrypoint\": \"\""),
                "materialized data-only dependency descriptor should not declare an executable entrypoint: "
                        + realDescriptor);
        Files.writeString(materializedDescriptor, descriptor, StandardCharsets.UTF_8);
    }

    private static void materializeRepoShape(Path modulesRepoRoot, Path fixtureRoot) throws IOException {
        Files.writeString(
                fixtureRoot.resolve("settings.gradle"),
                "rootProject.name = 'echo-real-module-execution-fixture'\n",
                StandardCharsets.UTF_8
        );
        Files.createDirectories(fixtureRoot.resolve("echo-native-platform"));
        copyDirectory(
                modulesRepoRoot.resolve("src/main/resources/data"),
                fixtureRoot.resolve("src/main/resources/data")
        );
        copyDirectory(
                modulesRepoRoot.resolve("addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/echoworldcore/world_regions"),
                fixtureRoot.resolve("src/main/resources/data/echoashfallprotocol/echoworldcore/world_regions")
        );
        copyDirectory(
                modulesRepoRoot.resolve("addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/echoworldcore/world_hazards"),
                fixtureRoot.resolve("src/main/resources/data/echoashfallprotocol/echoworldcore/world_hazards")
        );
        copyDirectory(
                modulesRepoRoot.resolve("addons/echoworldcore/src/main/resources/data/echoworldcore/echoworldcore/world_hazards"),
                fixtureRoot.resolve("src/main/resources/data/echoworldcore/echoworldcore/world_hazards")
        );
        copyDirectory(
                modulesRepoRoot.resolve("addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/echoweathercore/weather_profiles"),
                fixtureRoot.resolve("src/main/resources/data/echoashfallprotocol/echoweathercore/weather_profiles")
        );
        copyDirectory(
                modulesRepoRoot.resolve("addons/echoweathercore/src/main/resources/data/echoweathercore/weather_profiles"),
                fixtureRoot.resolve("src/main/resources/data/echoweathercore/weather_profiles")
        );
        copyDirectory(
                modulesRepoRoot.resolve("addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/worldgen/biome"),
                fixtureRoot.resolve("addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/worldgen/biome")
        );
        copyDirectory(
                modulesRepoRoot.resolve("addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/worldgen/structure"),
                fixtureRoot.resolve("addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/worldgen/structure")
        );
        copyDirectory(
                modulesRepoRoot.resolve("addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/missioncore/missions"),
                fixtureRoot.resolve("addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/missioncore/missions")
        );
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.isDirectory(source)) {
            return;
        }
        Path safeTarget = target.toAbsolutePath().normalize();
        try (var paths = Files.walk(source)) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .sorted()
                    .toList();
            for (Path file : files) {
                Path targetFile = safeTarget.resolve(source.relativize(file)).toAbsolutePath().normalize();
                require(targetFile.startsWith(safeTarget),
                        "materialized repo resource should stay inside fixture root: " + targetFile);
                Files.createDirectories(targetFile.getParent());
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void compileRealEntrypoint(
            Path realEntrypoint,
            Path sourceRoot,
            Path classesRoot,
            List<Path> dependencyClassRoots
    ) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JDK compiler is required for real module execution smoke");
        }
        Files.createDirectories(classesRoot);
        String classPath = System.getProperty("java.class.path") + File.pathSeparator + classesRoot;
        for (Path dependencyClassRoot : dependencyClassRoots) {
            classPath += File.pathSeparator + dependencyClassRoot;
        }
        List<String> arguments = new ArrayList<>();
        arguments.add("-classpath");
        arguments.add(classPath);
        arguments.add("-d");
        arguments.add(classesRoot.toString());
        List<Path> sources = standaloneSafeSources(sourceRoot, realEntrypoint);
        sources.stream()
                .map(Path::toString)
                .forEach(arguments::add);
        require(sources.contains(realEntrypoint),
                "real native entrypoint should be included in module source compile: " + realEntrypoint);
        int exitCode = compiler.run(null, null, null, arguments.toArray(String[]::new));
        if (exitCode != 0) {
            throw new IllegalStateException("Real module native entrypoint compilation failed: " + realEntrypoint);
        }
    }

    private static List<Path> standaloneSafeSources(Path sourceRoot, Path realEntrypoint) throws IOException {
        List<Path> allSources;
        try (var paths = Files.walk(sourceRoot)) {
            allSources = paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList();
        }

        Map<Path, String> sourceText = new LinkedHashMap<>();
        Set<Path> excludedSources = new LinkedHashSet<>();
        for (Path source : allSources) {
            String text = Files.readString(source, StandardCharsets.UTF_8);
            sourceText.put(source, text);
            if (hasRuntimeBoundImport(text)) {
                excludedSources.add(source);
            }
        }

        boolean changed;
        do {
            changed = false;
            Set<ExcludedType> excludedTypes = excludedTypes(sourceText, excludedSources);
            for (Path source : allSources) {
                if (excludedSources.contains(source)) {
                    continue;
                }
                if (referencesExcludedSource(sourceText.get(source), excludedTypes)) {
                    excludedSources.add(source);
                    changed = true;
                }
            }
        } while (changed);

        List<Path> safeSources = allSources.stream()
                .filter(source -> !excludedSources.contains(source))
                .toList();
        require(safeSources.contains(realEntrypoint),
                "real native entrypoint should be standalone safe: " + realEntrypoint);
        if (!usesEntrypointDependencyClosure(sourceRoot)) {
            return safeSources;
        }
        return transitiveEntrypointSources(safeSources, sourceText, realEntrypoint);
    }

    private static boolean usesEntrypointDependencyClosure(Path sourceRoot) {
        String path = sourceRoot.toString().replace('\\', '/');
        return path.contains("/echoholomap/")
                || path.contains("/echotextureforge/")
                || path.contains("/echothemecore/")
                || path.contains("/echobasegrid/")
                || path.contains("/echoindustrialnexus/")
                || path.contains("/echologisticsnetwork/")
                || path.contains("/echomultiblockcore/")
                || path.contains("/echopowergrid/")
                || path.contains("/echoconvoyprotocol/")
                || path.contains("/echofamiliarcore/")
                || path.contains("/echosignalos/")
                || path.contains("/echospellcore/")
                || path.contains("/echoritualcore/")
                || path.contains("/echocursecore/")
                || path.contains("/echoriftworlds/")
                || path.contains("/echoblackboxprotocol/")
                || path.contains("/echonexusprotocol/")
                || path.contains("/echoorbitalremnants/")
                || path.contains("/echoprimecore/")
                || path.contains("/echostationfall/")
                || path.contains("/echogrimoire/")
                || path.contains("/echoarcanacore/")
                || path.contains("/echorelictech/")
                || path.contains("/echoarcaneindex/")
                || path.contains("/echoaetherworks/")
                || path.contains("/echoashfallprotocol/")
                || path.contains("/echogalacticcore/");
    }

    private static List<Path> transitiveEntrypointSources(
            List<Path> safeSources,
            Map<Path, String> sourceText,
            Path realEntrypoint
    ) {
        Map<Path, ExcludedType> sourceTypes = new LinkedHashMap<>();
        for (Path source : safeSources) {
            sourceTypes.put(source, sourceType(source, sourceText.get(source)));
        }
        Set<Path> includedSources = new LinkedHashSet<>();
        includedSources.add(realEntrypoint);
        boolean changed;
        do {
            changed = false;
            for (Path source : safeSources) {
                if (includedSources.contains(source)) {
                    continue;
                }
                ExcludedType type = sourceTypes.get(source);
                for (Path includedSource : List.copyOf(includedSources)) {
                    if (referencesType(sourceText.get(includedSource), type)) {
                        includedSources.add(source);
                        changed = true;
                        break;
                    }
                }
            }
        } while (changed);
        return safeSources.stream()
                .filter(includedSources::contains)
                .toList();
    }

    private static boolean hasRuntimeBoundImport(String text) {
        return text.contains("import net.minecraft.")
                || text.contains("import net.neoforged.")
                || text.contains("import com.mojang.")
                || text.contains("import com.google.")
                || text.contains("import org.slf4j.");
    }

    private static Set<ExcludedType> excludedTypes(Map<Path, String> sourceText, Set<Path> excludedSources) {
        Set<ExcludedType> types = new LinkedHashSet<>();
        for (Path source : excludedSources) {
            ExcludedType type = sourceType(source, sourceText.get(source));
            if (!type.canonicalName().isBlank()) {
                types.add(type);
            }
        }
        return types;
    }

    private static ExcludedType sourceType(Path source, String text) {
        String fileName = source.getFileName().toString();
        String simpleName = fileName.substring(0, fileName.length() - ".java".length());
        String packageName = packageName(text);
        String canonicalName = packageName.isBlank() ? simpleName : packageName + "." + simpleName;
        return new ExcludedType(packageName, simpleName, canonicalName);
    }

    private static String packageName(String text) {
        for (String line : text.lines().toList()) {
            String trimmed = line.trim();
            if (trimmed.startsWith("package ") && trimmed.endsWith(";")) {
                return trimmed.substring("package ".length(), trimmed.length() - 1).trim();
            }
        }
        return "";
    }

    private static boolean referencesExcludedSource(String text, Set<ExcludedType> excludedTypes) {
        String code = codeWithoutCommentsOrLiterals(text);
        String packageName = packageName(code);
        for (ExcludedType type : excludedTypes) {
            if (referencesType(code, packageName, type)) {
                return true;
            }
        }
        return false;
    }

    private static boolean referencesType(String text, ExcludedType type) {
        String code = codeWithoutCommentsOrLiterals(text);
        return referencesType(code, packageName(code), type);
    }

    private static boolean referencesType(String code, String packageName, ExcludedType type) {
        String typeName = type.canonicalName();
        if (code.contains("import " + typeName + ";")) {
            return true;
        }
        int packageEnd = typeName.lastIndexOf('.');
        if (packageEnd > 0 && code.contains("import " + typeName.substring(0, packageEnd) + ".*;")) {
            return true;
        }
        if (containsToken(code, typeName)) {
            return true;
        }
        return packageName.equals(type.packageName()) && containsToken(code, type.simpleName());
    }

    private static boolean containsToken(String text, String token) {
        if (token.isBlank()) {
            return false;
        }
        int index = text.indexOf(token);
        while (index >= 0) {
            boolean leftBoundary = index == 0 || !isIdentifierPart(text.charAt(index - 1));
            int end = index + token.length();
            boolean rightBoundary = end >= text.length() || !isIdentifierPart(text.charAt(end));
            if (leftBoundary && rightBoundary) {
                return true;
            }
            index = text.indexOf(token, index + token.length());
        }
        return false;
    }

    private static boolean isIdentifierPart(char ch) {
        return Character.isJavaIdentifierPart(ch);
    }

    private static String codeWithoutCommentsOrLiterals(String text) {
        StringBuilder code = new StringBuilder(text.length());
        boolean lineComment = false;
        boolean blockComment = false;
        boolean stringLiteral = false;
        boolean charLiteral = false;
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            char next = index + 1 < text.length() ? text.charAt(index + 1) : '\0';
            if (lineComment) {
                if (current == '\n') {
                    lineComment = false;
                    code.append(current);
                } else {
                    code.append(' ');
                }
                continue;
            }
            if (blockComment) {
                if (current == '*' && next == '/') {
                    blockComment = false;
                    code.append("  ");
                    index++;
                } else {
                    code.append(current == '\n' ? '\n' : ' ');
                }
                continue;
            }
            if (stringLiteral) {
                if (current == '\\' && next != '\0') {
                    code.append("  ");
                    index++;
                    continue;
                }
                if (current == '"') {
                    stringLiteral = false;
                }
                code.append(current == '\n' ? '\n' : ' ');
                continue;
            }
            if (charLiteral) {
                if (current == '\\' && next != '\0') {
                    code.append("  ");
                    index++;
                    continue;
                }
                if (current == '\'') {
                    charLiteral = false;
                }
                code.append(current == '\n' ? '\n' : ' ');
                continue;
            }
            if (current == '/' && next == '/') {
                lineComment = true;
                code.append("  ");
                index++;
                continue;
            }
            if (current == '/' && next == '*') {
                blockComment = true;
                code.append("  ");
                index++;
                continue;
            }
            if (current == '"') {
                stringLiteral = true;
                code.append(' ');
                continue;
            }
            if (current == '\'') {
                charLiteral = true;
                code.append(' ');
                continue;
            }
            code.append(current);
        }
        return code.toString();
    }

    private static EchoNativeRegisteredService registeredService(
            EchoNativeServiceRegistry nativeServices,
            String moduleId,
            String serviceId
    ) {
        return nativeServices.servicesForModule(moduleId).stream()
                .filter(service -> service.serviceId().equals(serviceId))
                .findFirst()
                .orElseThrow();
    }

    private static Map<String, Object> serviceActivation(
            EchoNativeServiceRegistry nativeServices,
            String moduleId,
            String serviceId
    ) {
        Object service = nativeServices.service(moduleId, serviceId).orElseThrow();
        Map<String, Object> activation = mapObject(service);
        if (activation.isEmpty()) {
            activation = recordEvidence(service);
        }
        if (activation.isEmpty()) {
            throw new IllegalStateException("Native service should expose activation evidence map: " + serviceId);
        }
        return Map.copyOf(activation);
    }

    private static Map<String, Object> recordEvidence(Object service) {
        if (service == null) {
            return Map.of();
        }
        try {
            java.lang.reflect.Method evidence = service.getClass().getDeclaredMethod("evidence");
            evidence.setAccessible(true);
            return mapObject(evidence.invoke(service));
        } catch (ReflectiveOperationException exception) {
            return Map.of();
        }
    }

    private static Map<String, Object> mapObject(Object service) {
        if (!(service instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }

    private static EchoNativeRegisteredService requireRegisteredService(
            EchoNativeServiceRegistry nativeServices,
            String moduleId,
            String serviceId,
            List<String> expectedSurfaces
    ) {
        require(nativeServices.hasService(moduleId, serviceId),
                "real " + moduleId + " native adapter should register " + serviceId);
        EchoNativeRegisteredService service = registeredService(nativeServices, moduleId, serviceId);
        require(service.surfaces().containsAll(expectedSurfaces),
                "real " + moduleId + " service should preserve surfaces " + expectedSurfaces);
        return service;
    }

    private static Object serviceObject(
            EchoNativeServiceRegistry nativeServices,
            String moduleId,
            String serviceId
    ) {
        return nativeServices.service(moduleId, serviceId)
                .orElseThrow(() -> new IllegalStateException("Native service should exist: " + serviceId));
    }

    private static List<?> requireReflectiveListResult(
            Object service,
            String methodName,
            int minimumCount,
            String message
    ) {
        try {
            java.lang.reflect.Method method = service.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            Object result = method.invoke(service);
            require(result instanceof List<?>, message + " and return a list");
            List<?> list = (List<?>) result;
            require(list.size() >= minimumCount,
                    message + " expected>=" + minimumCount + " actual=" + list.size());
            return list;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(message, exception);
        }
    }

    private static Map<String, Object> requireActivationService(
            EchoNativeServiceRegistry nativeServices,
            String moduleId,
            String serviceId,
            List<String> expectedSurfaces
    ) {
        require(nativeServices.hasService(moduleId, serviceId),
                "real " + moduleId + " native adapter should register " + serviceId);
        EchoNativeRegisteredService service = registeredService(nativeServices, moduleId, serviceId);
        require(service.surfaces().containsAll(expectedSurfaces),
                "real " + moduleId + " service should preserve surfaces " + expectedSurfaces);
        return serviceActivation(nativeServices, moduleId, serviceId);
    }

    private static Map<String, Object> requireStoryAdapterCoreActivation(
            EchoNativeServiceRegistry nativeServices,
            String moduleId,
            String serviceId,
            String routeDescription
    ) {
        require(nativeServices.hasService(moduleId, serviceId),
                "real " + moduleId + " native adapter should register " + routeDescription);
        EchoNativeRegisteredService service = registeredService(nativeServices, moduleId, serviceId);
        require(service.surfaces().contains("adaptercore"),
                "real " + moduleId + " story service should preserve AdapterCore contract surface");
        Map<String, Object> activation = serviceActivation(nativeServices, moduleId, serviceId);
        requireTrue(activation, "activated",
                "real " + moduleId + " should activate its " + routeDescription);
        requireTrue(activation, "adapterCoreUsed",
                "real " + moduleId + " should execute through AdapterCore for " + routeDescription);
        requireTrue(activation, "nativeAdapterCodeExecuted",
                "real " + moduleId + " should execute native adapter code for " + routeDescription);
        requireTrue(activation, "serviceCodeExecuted",
                "real " + moduleId + " should execute service code for " + routeDescription);
        Map<String, Object> storyRuntimeBridge = mapObject(activation.get("storyRuntimeBridge"));
        boolean storyRuntimeExecuted = Boolean.TRUE.equals(activation.get("storyRuntimeServiceCodeExecuted"))
                || Boolean.TRUE.equals(storyRuntimeBridge.get("serviceCodeExecuted"));
        require(storyRuntimeExecuted,
                "real " + moduleId + " should execute AdapterCore story runtime service code for "
                        + routeDescription);
        return activation;
    }

    private static void requireTrue(Map<String, Object> activation, String key, String message) {
        require(Boolean.TRUE.equals(activation.get(key)), message);
    }

    private static List<Pair> nativeServiceCounts(EchoNativeServiceRegistry nativeServices) {
        return REAL_MODULES.stream()
                .map(spec -> new Pair(
                        spec.moduleId(),
                        String.valueOf(nativeServices.servicesForModule(spec.moduleId()).size())
                ))
                .toList();
    }

    private static List<Pair> nativeServiceIds(EchoNativeServiceRegistry nativeServices) {
        return REAL_MODULES.stream()
                .map(spec -> new Pair(
                        spec.moduleId(),
                        String.join("|", nativeServices.servicesForModule(spec.moduleId()).stream()
                                .map(EchoNativeRegisteredService::serviceId)
                                .sorted()
                                .toList())
                ))
                .toList();
    }

    private static List<Pair> mechanicsEvidence(Map<String, Map<String, Object>> activations) {
        return MECHANICS_SERVICE_IDS.stream()
                .map(pair -> new Pair(pair.key(), activationEvidence(activations.getOrDefault(pair.key(), Map.of()))))
                .toList();
    }

    private static String activationEvidence(Map<String, Object> activation) {
        List<String> trueKeys = activation.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
        return String.join("|", trueKeys);
    }

    private static void writeReport(
            Path standaloneRoot,
            Path modulesRepoRoot,
            EchoRuntimeModuleRegistry registry,
            EchoNativeRegisteredService schemaCoreService,
            EchoNativeRegisteredService validationCoreService,
            EchoNativeRegisteredService contentCoreService,
            Map<String, Object> contentCoreActivation,
            EchoNativeRegisteredService assetCoreService,
            Map<String, Object> assetCoreActivation,
            EchoNativeRegisteredService recipeCoreService,
            Map<String, Object> recipeCoreActivation,
            EchoNativeRegisteredService packCoreService,
            Map<String, Object> packCoreActivation,
            EchoNativeRegisteredService netCoreService,
            EchoNativeRegisteredService dataCoreService,
            Map<String, Object> dataCoreActivation,
            EchoNativeRegisteredService worldCoreService,
            Map<String, Object> worldCoreActivation,
            Map<String, Map<String, Object>> mechanicsActivations,
            EchoNativeRegisteredService healthCoreService,
            Map<String, Object> healthCoreActivation,
            EchoNativeRegisteredService missionCoreService,
            Map<String, Object> missionCoreActivation,
            EchoNativeRegisteredService playerCoreService,
            Map<String, Object> playerCoreActivation,
            EchoNativeRegisteredService blockworksService,
            Map<String, Object> blockworksActivation,
            EchoNativeRegisteredService presenceLinkService,
            Map<String, Object> ashfallProtocolActivation,
            EchoNativeRegisteredService galacticCoreRuntimeService,
            EchoNativeRegisteredService galacticCoreRuntimeGatewayService,
            EchoNativeRegisteredService galacticCoreHostExecutionService,
            EchoNativeRegisteredService galacticCoreLiveSessionMutationsService,
            int galacticCoreRuntimeGatewaySmokeActionCount,
            int galacticCoreHostExecutionSmokeActionCount,
            int galacticCoreLiveSessionMutationCount,
            EchoNativeRegisteredService addonApiService,
            List<Pair> nativeServiceCountsBeforeUnload,
            List<Pair> nativeServiceIdsBeforeUnload,
            EchoRuntimeLogBridge diagnostics
    ) throws IOException {
        Path report = standaloneRoot.resolve("reports/echo/standalone/real-module-execution-smoke.json");
        Files.createDirectories(report.getParent());
        String json = "{\n"
                + "  \"schema\": \"echo.standalone.real_module_execution_smoke.v16\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"modulesRepoRoot\": \"" + escape(modulesRepoRoot.toString()) + "\",\n"
                + "  \"moduleIds\": " + stringArray(REAL_MODULES.stream().map(RealModuleSpec::moduleId).toList()) + ",\n"
                + "  \"nativeEntrypoints\": " + stringArray(REAL_MODULES.stream().map(RealModuleSpec::nativeEntrypoint).toList()) + ",\n"
                + "  \"sourceDescriptors\": " + stringArray(REAL_MODULES.stream()
                        .map(spec -> "addons/" + spec.addonDirectory() + "/src/main/resources/META-INF/echo.mod.json")
                        .toList()) + ",\n"
                + "  \"sourceEntrypoints\": " + stringArray(REAL_MODULES.stream()
                        .map(spec -> "addons/" + spec.addonDirectory() + "/" + spec.entrypointSource().replace('\\', '/'))
                        .toList()) + ",\n"
                + "  \"runtimeStatuses\": " + stringMap(REAL_MODULES.stream()
                        .map(spec -> new Pair(spec.moduleId(), registry.runtimeStatus(spec.moduleId()).id()))
                        .toList()) + ",\n"
                + "  \"finalLifecycles\": " + stringMap(REAL_MODULES.stream()
                        .map(spec -> new Pair(spec.moduleId(), registry.lifecycle(spec.moduleId()).name()))
                        .toList()) + ",\n"
                + "  \"loadReloadUnloadExecuted\": true,\n"
                + "  \"realDependencyChainExecuted\": true,\n"
                + "  \"legacyNativeBootstrapExecuted\": true,\n"
                + "  \"surfaceNativeEntrypointExecuted\": true,\n"
                + "  \"adapterCoreNativeEntrypointExecuted\": true,\n"
                + "  \"schemaCoreNativeEntrypointExecuted\": true,\n"
                + "  \"validationCoreNativeEntrypointExecuted\": true,\n"
                + "  \"contentCoreNativeEntrypointExecuted\": true,\n"
                + "  \"assetCoreNativeEntrypointExecuted\": true,\n"
                + "  \"recipeCoreNativeEntrypointExecuted\": true,\n"
                + "  \"packCoreNativeEntrypointExecuted\": true,\n"
                + "  \"netCoreNativeEntrypointExecuted\": true,\n"
                + "  \"dataCoreNativeEntrypointExecuted\": true,\n"
                + "  \"worldCoreNativeEntrypointExecuted\": true,\n"
                + "  \"healthCoreNativeEntrypointExecuted\": true,\n"
                + "  \"missionCoreNativeEntrypointExecuted\": true,\n"
                + "  \"playerCoreNativeEntrypointExecuted\": true,\n"
                + "  \"blockworksNativeEntrypointExecuted\": true,\n"
                + "  \"presenceLinkNativeEntrypointExecuted\": true,\n"
                + "  \"runtimeFoundationNativeEntrypointsExecuted\": true,\n"
                + "  \"runtimeFoundationNativeServiceIds\": " + stringMap(List.of(
                        new Pair(AGENT_CORE_MODULE_ID, AGENT_CORE_SERVICE_ID),
                        new Pair(BRIDGE_CORE_MODULE_ID, BRIDGE_CORE_SERVICE_ID),
                        new Pair(REPORT_CORE_MODULE_ID, REPORT_CORE_SERVICE_ID),
                        new Pair(CAMERA_CORE_MODULE_ID, CAMERA_CORE_SERVICE_ID),
                        new Pair(CINEMATIC_CORE_MODULE_ID, CINEMATIC_CORE_SERVICE_ID),
                        new Pair(CODEX_CORE_MODULE_ID, CODEX_CORE_SERVICE_ID),
                        new Pair(CREATOR_CORE_MODULE_ID, CREATOR_CORE_SERVICE_ID),
                        new Pair(RENDER_CORE_MODULE_ID, RENDER_CORE_SERVICE_ID),
                        new Pair(SCREEN_CORE_MODULE_ID, SCREEN_CORE_SERVICE_ID),
                        new Pair(SCRIPT_CORE_MODULE_ID, SCRIPT_CORE_SERVICE_ID),
                        new Pair(RUNTIME_GUARD_MODULE_ID, RUNTIME_GUARD_SERVICE_ID),
                        new Pair(COMMUNITY_BRIDGE_MODULE_ID, COMMUNITY_BRIDGE_SERVICE_ID))) + ",\n"
                + "  \"uiNavigationNativeEntrypointsExecuted\": true,\n"
                + "  \"uiNavigationNativeServiceIds\": " + stringMap(List.of(
                        new Pair(INDEX_MODULE_ID, INDEX_SERVICE_ID),
                        new Pair(HOLOMAP_MODULE_ID, HOLOMAP_SERVICE_ID),
                        new Pair(LENS_MODULE_ID, LENS_SERVICE_ID),
                        new Pair(TERMINAL_MODULE_ID, TERMINAL_SERVICE_ID),
                        new Pair(TEXTURE_FORGE_MODULE_ID, TEXTURE_FORGE_SERVICE_ID),
                        new Pair(THEME_CORE_MODULE_ID, THEME_CORE_SERVICE_ID),
                        new Pair(TUTORIAL_CORE_MODULE_ID, TUTORIAL_CORE_SERVICE_ID),
                        new Pair(WIKI_MODULE_ID, WIKI_SERVICE_ID))) + ",\n"
                + "  \"techGridNativeEntrypointsExecuted\": true,\n"
                + "  \"techGridNativeServiceIds\": " + stringMap(List.of(
                        new Pair(BASE_GRID_MODULE_ID, BASE_GRID_SERVICE_ID),
                        new Pair(INDUSTRIAL_NEXUS_MODULE_ID, INDUSTRIAL_NEXUS_SERVICE_ID),
                        new Pair(LOGISTICS_NETWORK_MODULE_ID, LOGISTICS_NETWORK_SERVICE_ID),
                        new Pair(MULTIBLOCK_CORE_MODULE_ID, MULTIBLOCK_CORE_SERVICE_ID),
                        new Pair(POWER_GRID_MODULE_ID, POWER_GRID_SERVICE_ID))) + ",\n"
                + "  \"storyArcanaNativeEntrypointsExecuted\": true,\n"
                + "  \"storyArcanaNativeServiceIds\": " + stringMap(List.of(
                        new Pair(SIGNAL_OS_MODULE_ID, SIGNAL_OS_SERVICE_ID),
                        new Pair(SPELL_CORE_MODULE_ID, SPELL_CORE_SERVICE_ID),
                        new Pair(RITUAL_CORE_MODULE_ID, RITUAL_CORE_SERVICE_ID),
                        new Pair(CURSE_CORE_MODULE_ID, CURSE_CORE_SERVICE_ID),
                        new Pair(RIFT_WORLDS_MODULE_ID, RIFT_WORLDS_SERVICE_ID),
                        new Pair(BLACKBOX_PROTOCOL_MODULE_ID, BLACKBOX_PROTOCOL_SERVICE_ID),
                        new Pair(NEXUS_PROTOCOL_MODULE_ID, NEXUS_PROTOCOL_SERVICE_ID),
                        new Pair(ORBITAL_REMNANTS_MODULE_ID, ORBITAL_REMNANTS_SERVICE_ID),
                        new Pair(PRIME_CORE_MODULE_ID, PRIME_CORE_SERVICE_ID),
                        new Pair(STATIONFALL_MODULE_ID, STATIONFALL_SERVICE_ID),
                        new Pair(GRIMOIRE_MODULE_ID, GRIMOIRE_SERVICE_ID),
                        new Pair(ARCANA_CORE_MODULE_ID, ARCANA_CORE_SERVICE_ID),
                        new Pair(RELIC_TECH_MODULE_ID, RELIC_TECH_SERVICE_ID),
                        new Pair(ARCANE_INDEX_MODULE_ID, ARCANE_INDEX_SERVICE_ID),
                        new Pair(AETHER_WORKS_MODULE_ID, AETHER_WORKS_SERVICE_ID))) + ",\n"
                + "  \"gameplayBridgeNativeEntrypointsExecuted\": true,\n"
                + "  \"gameplayBridgeNativeServiceIds\": " + stringMap(List.of(
                        new Pair(CONVOY_PROTOCOL_MODULE_ID, CONVOY_PROTOCOL_SERVICE_ID),
                        new Pair(FAMILIAR_CORE_MODULE_ID, FAMILIAR_CORE_SERVICE_ID),
                        new Pair(ASHFALL_PROTOCOL_MODULE_ID, ASHFALL_PROTOCOL_SERVICE_ID))) + ",\n"
                + "  \"ashfallProtocolNativeEntrypointExecuted\": true,\n"
                + "  \"ashfallProtocolServiceCodeExecuted\": " + bool(ashfallProtocolActivation, "serviceCodeExecuted") + ",\n"
                + "  \"ashfallProtocolGameplayHookEvidence\": " + bool(ashfallProtocolActivation, "gameplayHookEvidence") + ",\n"
                + "  \"ashfallProtocolMajorRouteStatus\": \"" + escape(text(ashfallProtocolActivation, "majorRouteAdapterCoreCommandStatus")) + "\",\n"
                + "  \"ashfallProtocolMidgameRouteStatus\": \"" + escape(text(ashfallProtocolActivation, "midgameRouteAdapterCoreReplayStatus")) + "\",\n"
                + "  \"ashfallProtocolLateGameRouteStatus\": \"" + escape(text(ashfallProtocolActivation, "lateGameRouteAdapterCoreReplayStatus")) + "\",\n"
                + "  \"ashfallProtocolMachineRuntimeBindingStatus\": \"" + escape(text(ashfallProtocolActivation, "machineRuntimeBindingStatus")) + "\",\n"
                + "  \"ashfallProtocolAgent9TechRuntimeStatus\": \"" + escape(text(ashfallProtocolActivation, "agent9NativeTechRuntimeStatus")) + "\",\n"
                + "  \"galacticCoreNativeEntrypointExecuted\": true,\n"
                + "  \"galacticCoreNativeServiceIds\": " + stringMap(List.of(
                        new Pair(GALACTIC_CORE_MODULE_ID + ".runtime", GALACTIC_CORE_RUNTIME_SERVICE_ID),
                        new Pair(GALACTIC_CORE_MODULE_ID + ".runtimeGateway", GALACTIC_CORE_RUNTIME_GATEWAY_SERVICE_ID),
                        new Pair(GALACTIC_CORE_MODULE_ID + ".hostExecution", GALACTIC_CORE_HOST_EXECUTION_SERVICE_ID),
                        new Pair(GALACTIC_CORE_MODULE_ID + ".liveSessionMutations", GALACTIC_CORE_LIVE_SESSION_MUTATIONS_SERVICE_ID))) + ",\n"
                + "  \"galacticCoreRuntimeServiceSurfaces\": " + stringArray(galacticCoreRuntimeService.surfaces()) + ",\n"
                + "  \"galacticCoreRuntimeGatewayServiceSurfaces\": " + stringArray(galacticCoreRuntimeGatewayService.surfaces()) + ",\n"
                + "  \"galacticCoreHostExecutionServiceSurfaces\": " + stringArray(galacticCoreHostExecutionService.surfaces()) + ",\n"
                + "  \"galacticCoreLiveSessionMutationsServiceSurfaces\": " + stringArray(galacticCoreLiveSessionMutationsService.surfaces()) + ",\n"
                + "  \"galacticCoreRuntimeGatewaySmokeActionCount\": " + galacticCoreRuntimeGatewaySmokeActionCount + ",\n"
                + "  \"galacticCoreHostExecutionSmokeActionCount\": " + galacticCoreHostExecutionSmokeActionCount + ",\n"
                + "  \"galacticCoreLiveSessionMutationCount\": " + galacticCoreLiveSessionMutationCount + ",\n"
                + "  \"nativeServiceRegisteredBeforeUnload\": true,\n"
                + "  \"nativeServiceRevokedOnUnload\": true,\n"
                + "  \"nativeServiceCountsBeforeUnload\": " + stringMap(nativeServiceCountsBeforeUnload) + ",\n"
                + "  \"nativeServiceIdsBeforeUnload\": " + stringMap(nativeServiceIdsBeforeUnload) + ",\n"
                + "  \"schemaCoreNativeServiceId\": \"" + escape(schemaCoreService.serviceId()) + "\",\n"
                + "  \"schemaCoreNativeServiceSurfaces\": " + stringArray(schemaCoreService.surfaces()) + ",\n"
                + "  \"validationCoreNativeServiceId\": \"" + escape(validationCoreService.serviceId()) + "\",\n"
                + "  \"validationCoreNativeServiceSurfaces\": " + stringArray(validationCoreService.surfaces()) + ",\n"
                + "  \"contentCoreNativeServiceId\": \"" + escape(contentCoreService.serviceId()) + "\",\n"
                + "  \"contentCoreNativeServiceSurfaces\": " + stringArray(contentCoreService.surfaces()) + ",\n"
                + "  \"contentCoreReferenceRoundTripExecuted\": " + bool(contentCoreActivation, "referenceLookupRoundTrip") + ",\n"
                + "  \"assetCoreNativeServiceId\": \"" + escape(assetCoreService.serviceId()) + "\",\n"
                + "  \"assetCoreNativeServiceSurfaces\": " + stringArray(assetCoreService.surfaces()) + ",\n"
                + "  \"assetCoreAssetRegistryRoundTripExecuted\": " + bool(assetCoreActivation, "assetRegistryRoundTrip") + ",\n"
                + "  \"assetCoreAssetValidationRoundTripExecuted\": " + bool(assetCoreActivation, "assetValidationRoundTrip") + ",\n"
                + "  \"assetCoreTextureForgePromptReady\": " + bool(assetCoreActivation, "textureForgePromptReady") + ",\n"
                + "  \"recipeCoreNativeServiceId\": \"" + escape(recipeCoreService.serviceId()) + "\",\n"
                + "  \"recipeCoreNativeServiceSurfaces\": " + stringArray(recipeCoreService.surfaces()) + ",\n"
                + "  \"recipeCoreNativeHostStatus\": \"" + escape(text(recipeCoreActivation, "status")) + "\",\n"
                + "  \"recipeCoreHostLoadedEntrypoint\": " + bool(recipeCoreActivation, "hostLoadedEntrypoint") + ",\n"
                + "  \"packCoreNativeServiceId\": \"" + escape(packCoreService.serviceId()) + "\",\n"
                + "  \"packCoreNativeServiceSurfaces\": " + stringArray(packCoreService.surfaces()) + ",\n"
                + "  \"packCoreLoadPlanExecuted\": " + bool(packCoreActivation, "packLoadPlanExecuted") + ",\n"
                + "  \"netCoreNativeServiceId\": \"" + escape(netCoreService.serviceId()) + "\",\n"
                + "  \"netCoreNativeServiceSurfaces\": " + stringArray(netCoreService.surfaces()) + ",\n"
                + "  \"dataCoreNativeServiceId\": \"" + escape(dataCoreService.serviceId()) + "\",\n"
                + "  \"dataCoreNativeServiceSurfaces\": " + stringArray(dataCoreService.surfaces()) + ",\n"
                + "  \"dataCoreRuntimeProfileExecuted\": " + bool(dataCoreActivation, "dataRuntimeProfileExecuted") + ",\n"
                + "  \"worldCoreNativeServiceId\": \"" + escape(worldCoreService.serviceId()) + "\",\n"
                + "  \"worldCoreNativeServiceSurfaces\": " + stringArray(worldCoreService.surfaces()) + ",\n"
                + "  \"worldCoreWorldEffectsRuntimeContract\": " + bool(worldCoreActivation, "worldEffectsRuntimeContract") + ",\n"
                + "  \"worldCoreDataCatalogRuntimeContract\": " + bool(worldCoreActivation, "worldDataCatalogRuntimeContract") + ",\n"
                + "  \"worldCoreRegionCellSampleExecuted\": " + bool(worldCoreActivation, "regionCellSampleExecuted") + ",\n"
                + "  \"worldCoreDataCatalogRegionCount\": " + integer(worldCoreActivation, "worldDataCatalogRegionCount") + ",\n"
                + "  \"worldCoreDataCatalogHazardCount\": " + integer(worldCoreActivation, "worldDataCatalogHazardCount") + ",\n"
                + "  \"worldCoreDataCatalogSourceFileCount\": " + integer(worldCoreActivation, "worldDataCatalogSourceFileCount") + ",\n"
                + "  \"worldSurvivalNativeEntrypointsExecuted\": true,\n"
                + "  \"worldSurvivalNativeServiceIds\": " + stringMap(List.of(
                        new Pair(ATMOSPHERE_CORE_MODULE_ID, ATMOSPHERE_CORE_SERVICE_ID),
                        new Pair(WEATHER_CORE_MODULE_ID, WEATHER_CORE_SERVICE_ID),
                        new Pair(STATUS_CORE_MODULE_ID, STATUS_CORE_SERVICE_ID),
                        new Pair(SPAWN_CORE_MODULE_ID, SPAWN_CORE_SERVICE_ID),
                        new Pair(STRUCTURE_CORE_MODULE_ID, STRUCTURE_CORE_SERVICE_ID),
                        new Pair(LOOT_CORE_MODULE_ID, LOOT_CORE_SERVICE_ID))) + ",\n"
                + "  \"betaLoopNativeEntrypointsExecuted\": true,\n"
                + "  \"betaLoopNativeServiceIds\": " + stringMap(List.of(
                        new Pair(EVENT_CORE_MODULE_ID, EVENT_CORE_SERVICE_ID),
                        new Pair(ENCOUNTER_CORE_MODULE_ID, ENCOUNTER_CORE_SERVICE_ID),
                        new Pair(ECONOMY_CORE_MODULE_ID, ECONOMY_CORE_SERVICE_ID),
                        new Pair(QUEST_DIRECTOR_MODULE_ID, QUEST_DIRECTOR_SERVICE_ID),
                        new Pair(NPC_CORE_MODULE_ID, NPC_CORE_SERVICE_ID),
                        new Pair(GUIDE_CORE_MODULE_ID, GUIDE_CORE_SERVICE_ID),
                        new Pair(LORE_CORE_MODULE_ID, LORE_CORE_SERVICE_ID),
                        new Pair(VEHICLE_CORE_MODULE_ID, VEHICLE_CORE_SERVICE_ID))) + ",\n"
                + "  \"techAndUiNativeEntrypointsExecuted\": true,\n"
                + "  \"techAndUiNativeServiceIds\": " + stringMap(List.of(
                        new Pair(MACHINE_CORE_MODULE_ID, MACHINE_CORE_SERVICE_ID),
                        new Pair(POWER_CORE_MODULE_ID, POWER_CORE_SERVICE_ID),
                        new Pair(LOGISTICS_CORE_MODULE_ID, LOGISTICS_CORE_SERVICE_ID),
                        new Pair(INPUT_CORE_MODULE_ID, INPUT_CORE_SERVICE_ID),
                        new Pair(HUD_CORE_MODULE_ID, HUD_CORE_SERVICE_ID),
                        new Pair(NOTIFICATION_CORE_MODULE_ID, NOTIFICATION_CORE_SERVICE_ID))) + ",\n"
                + "  \"mechanicsCoreNativeEntrypointsExecuted\": true,\n"
                + "  \"mechanicsCoreNativeServiceIds\": " + stringMap(MECHANICS_SERVICE_IDS) + ",\n"
                + "  \"mechanicsCoreActivationEvidence\": " + stringMap(mechanicsEvidence(mechanicsActivations)) + ",\n"
                + "  \"healthCoreNativeServiceId\": \"" + escape(healthCoreService.serviceId()) + "\",\n"
                + "  \"healthCoreNativeServiceSurfaces\": " + stringArray(healthCoreService.surfaces()) + ",\n"
                + "  \"healthCoreRuntimeReportExecuted\": " + bool(healthCoreActivation, "healthReportExecuted") + ",\n"
                + "  \"missionCoreNativeServiceId\": \"" + escape(missionCoreService.serviceId()) + "\",\n"
                + "  \"missionCoreNativeServiceSurfaces\": " + stringArray(missionCoreService.surfaces()) + ",\n"
                + "  \"missionCoreObjectiveProgressionExecuted\": " + bool(missionCoreActivation, "missionProgressionExecuted") + ",\n"
                + "  \"playerCoreNativeServiceId\": \"" + escape(playerCoreService.serviceId()) + "\",\n"
                + "  \"playerCoreNativeServiceSurfaces\": " + stringArray(playerCoreService.surfaces()) + ",\n"
                + "  \"playerCoreFeatureContractRoundTripExecuted\": " + bool(playerCoreActivation, "featureContractRoundTrip") + ",\n"
                + "  \"blockworksNativeServiceId\": \"" + escape(blockworksService.serviceId()) + "\",\n"
                + "  \"blockworksNativeServiceSurfaces\": " + stringArray(blockworksService.surfaces()) + ",\n"
                + "  \"blockworksBlockCatalogRoundTripExecuted\": " + bool(blockworksActivation, "blockCatalogRoundTrip") + ",\n"
                + "  \"blockworksPaletteConversionRoundTripExecuted\": " + bool(blockworksActivation, "paletteConversionRoundTrip") + ",\n"
                + "  \"blockworksWorldgenSiteRoundTripExecuted\": " + bool(blockworksActivation, "worldgenSiteRoundTrip") + ",\n"
                + "  \"presenceLinkNativeServiceId\": \"" + escape(presenceLinkService.serviceId()) + "\",\n"
                + "  \"presenceLinkNativeServiceSurfaces\": " + stringArray(presenceLinkService.surfaces()) + ",\n"
                + "  \"nativeServiceId\": \"" + escape(addonApiService.serviceId()) + "\",\n"
                + "  \"nativeServiceSurfaces\": " + stringArray(addonApiService.surfaces()) + ",\n"
                + "  \"diagnosticCount\": " + diagnostics.diagnostics().size() + ",\n"
                + "  \"notes\": " + stringMap(REAL_MODULES.stream()
                        .map(spec -> new Pair(spec.moduleId(), String.join("; ", registry.notes(spec.moduleId()))))
                        .toList()) + "\n"
                + "}\n";
        Files.writeString(report, json, StandardCharsets.UTF_8);
    }

    private static String stringArray(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            json.append("\"").append(escape(values.get(i))).append("\"");
            if (i + 1 < values.size()) {
                json.append(", ");
            }
        }
        return json.append("]").toString();
    }

    private static String stringMap(List<Pair> values) {
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < values.size(); i++) {
            Pair pair = values.get(i);
            json.append("\"").append(escape(pair.key())).append("\": \"").append(escape(pair.value())).append("\"");
            if (i + 1 < values.size()) {
                json.append(", ");
            }
        }
        return json.append("}").toString();
    }

    private static String bool(Map<String, Object> values, String key) {
        return String.valueOf(Boolean.TRUE.equals(values.get(key)));
    }

    private static String integer(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value instanceof Number number) {
            return String.valueOf(number.intValue());
        }
        return "0";
    }

    private static String text(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record RealModuleSpec(
            String moduleId,
            String nativeEntrypoint,
            String entrypointSource,
            String noteProbe
    ) {
        private String addonDirectory() {
            if (SIGNAL_OS_MODULE_ID.equals(moduleId)) {
                return "echosignalos";
            }
            return moduleId;
        }
    }

    private record ExcludedType(String packageName, String simpleName, String canonicalName) {
    }

    private record Pair(String key, String value) {
    }
}
