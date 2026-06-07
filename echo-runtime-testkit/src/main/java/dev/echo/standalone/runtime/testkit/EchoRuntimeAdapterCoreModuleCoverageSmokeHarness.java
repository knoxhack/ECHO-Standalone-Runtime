package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoRuntimeLogBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageAuditor;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCompatibilityReport;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageReport;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageStatus;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreParityMatrix;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoCompatDiagnostic;
import dev.echo.standalone.runtime.compat.EchoNeoForgeDependency;
import dev.echo.standalone.runtime.compat.EchoNeoForgeMetadataScanResult;
import dev.echo.standalone.runtime.compat.EchoNeoForgeMetadataScanner;
import dev.echo.standalone.runtime.compat.EchoNeoForgeModCandidate;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;
import dev.echo.standalone.runtime.modules.EchoRuntimeSystemModuleStatusReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class EchoRuntimeAdapterCoreModuleCoverageSmokeHarness {
    private static final int MIN_ADAPTERCORE_NATIVE_BRIDGE_MODULES = 90;

    private EchoRuntimeAdapterCoreModuleCoverageSmokeHarness() {
    }

    public static void main(String[] args) {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons")),
                "coverage smoke requires the ECHO repo root with addons/");

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoRuntimeLogBridge diagnostics = new EchoRuntimeLogBridge();
        services.register(EchoRuntimeDiagnosticSink.class, diagnostics);

        List<Path> roots = moduleRoots(repoRoot);
        EchoRuntimeModuleRuntimeResult modules = EchoRuntimeModuleManager.descriptorOnly()
                .run(roots, services);
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        EchoAdapterCoreParityMatrix parityMatrix = EchoAdapterCoreParityMatrix.ashfall(bridge);
        EchoAdapterCoreModuleCoverageReport coverage = new EchoAdapterCoreModuleCoverageAuditor()
                .audit(modules, bridge);
        EchoNeoForgeMetadataScanResult neoForgeMetadata = new EchoNeoForgeMetadataScanner().scan(roots);
        services.register(EchoAdapterCoreStandaloneContentBridge.class, bridge);
        services.register(EchoAdapterCoreModuleCoverageReport.class, coverage);
        services.register(EchoNeoForgeMetadataScanResult.class, neoForgeMetadata);

        try {
            writeCoverageReport(standaloneRoot, coverage, coverage.contractLockedForBeta() ? "PASS" : "FAIL");
            writeNeoForgeMetadataReport(standaloneRoot, neoForgeMetadata);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write AdapterCore module coverage report", exception);
        }

        require(coverage.totalCount() >= 20,
                "real module coverage should discover the ECHO module catalog");
        require(coverage.activeCount() > 0,
                "module coverage should mark at least AdapterCore/core modules active");
        require(coverage.activeCount() >= MIN_ADAPTERCORE_NATIVE_BRIDGE_MODULES,
                "AdapterCore standalone coverage should keep the expanded native bridge metadata module floor");
        require(coverage.activeCount() + coverage.adapterGapCount() + coverage.unsupportedCount() == coverage.totalCount(),
                "every module must be classified exactly once");
        require(coverage.entries().stream()
                        .filter(entry -> entry.status() != EchoAdapterCoreModuleCoverageStatus.ACTIVE)
                        .allMatch(entry -> !entry.gaps().isEmpty()),
                "every non-active module must explain its adapter gap or unsupported reason");
        require(coverage.contractLockedForBeta(),
                "AdapterCore beta contract should cover all required domains and runtime targets: "
                        + coverage.contractSummary()
                        + ", graphIssues=" + coverage.graphIssues()
                        + ", nonActive=" + nonActiveSummary(coverage));
        require(coverage.moduleReports().size() == coverage.totalCount(),
                "coverage should expose one compatibility report per scanned module");
        require(coverage.incompleteModuleReports().isEmpty(),
                "every module compatibility report should include status, domains, runtimes, adapter keys, and gap reasons");
        require(coverage.moduleReports().stream().allMatch(EchoAdapterCoreModuleCompatibilityReport::allRuntimeTargetsDeclared),
                "every module compatibility report should declare NeoForge, ECHO Native Loader, and standalone targets");
        require(neoForgeMetadata.errorCount() == 0,
                "NeoForge metadata compatibility scan should not contain parse errors: "
                        + diagnosticMessages(neoForgeMetadata.diagnostics()));
        require(neoForgeMetadata.candidateCount() >= 20,
                "NeoForge metadata compatibility scan should discover the real ECHO catalog candidates");
        EchoNeoForgeModCandidate ashfallMetadata = neoForgeMetadata.find("echoashfallprotocol")
                .orElseThrow(() -> new AssertionError("Ashfall NeoForge metadata should be discovered"));
        require(ashfallMetadata.runtimeStatus().equals("runtime-disabled-with-reason"),
                "Ashfall NeoForge metadata should be diagnostics-only");
        require(ashfallMetadata.platformDependencies().stream().anyMatch(dependency -> dependency.modId().equals("neoforge")),
                "Ashfall NeoForge metadata should preserve the NeoForge platform dependency");
        require(ashfallMetadata.platformDependencies().stream().anyMatch(dependency -> dependency.modId().equals("minecraft")),
                "Ashfall NeoForge metadata should preserve the Minecraft platform dependency");
        require(ashfallMetadata.nonPlatformRequiredDependencies().stream()
                        .anyMatch(dependency -> dependency.modId().equals("echocore")
                                && dependency.reason().contains("shared ECHO")),
                "Ashfall NeoForge metadata should preserve required ECHO dependency reasons");
        require(ashfallMetadata.optionalDependencies().stream()
                        .anyMatch(dependency -> dependency.modId().equals("echoworldcore")
                                && dependency.reason().contains("world regions")),
                "Ashfall NeoForge metadata should preserve optional ECHO dependency reasons");
        require(neoForgeMetadata.find("echocore").isPresent(),
                "Core NeoForge metadata should be discovered from template metadata");
        require(neoForgeMetadata.find("echoweathercore").isPresent(),
                "resource-backed NeoForge metadata should be discovered");
        require(bridge.coversEveryRequiredBetaDomain(),
                "AdapterCore bridge should expose live bindings for all beta domains: "
                        + bridge.missingRequiredBetaDomains());
        require(parityMatrix.completeForBeta(),
                "AdapterCore parity matrix should map every required NeoForge feature domain to standalone behavior: "
                        + parityMatrix.missingRequiredBetaDomains());

        EchoAdapterCoreModuleCoverageEntry adapterCore = coverage.require("echoadaptercore");
        require(adapterCore.status() == EchoAdapterCoreModuleCoverageStatus.ACTIVE,
                "echoadaptercore must be active in standalone coverage");
        require(adapterCore.adapterCoreProvider(),
                "echoadaptercore should be classified as the bridge provider");
        require(adapterCore.adapterKeys().contains("adaptercore.bootstrap"),
                "echoadaptercore should expose the bootstrap adapter key");
        requireAllAdapterRuntimes(adapterCore);

        EchoAdapterCoreModuleCoverageEntry echoCore = coverage.require("echocore");
        require(echoCore.status() == EchoAdapterCoreModuleCoverageStatus.ACTIVE,
                "echocore must remain active as an AdapterCore bootstrap dependency");
        requireAllAdapterRuntimes(echoCore);
        requireActiveDomain(coverage, "echoplatformcore", EchoAdapterCoreDomain.DATA);
        requireActiveDomain(coverage, "echoschemacore", EchoAdapterCoreDomain.DATA);
        requireActiveDomain(coverage, "echovalidationcore", EchoAdapterCoreDomain.DIAGNOSTICS);
        requireActiveDomain(coverage, "echohealthcore", EchoAdapterCoreDomain.DIAGNOSTICS);
        requireActiveDomain(coverage, "echonetcore", EchoAdapterCoreDomain.NETWORKING);
        requireActiveDomain(coverage, "echoassetcore", EchoAdapterCoreDomain.ASSETS);
        requireActiveDomain(coverage, "echodatacore", EchoAdapterCoreDomain.DATA);
        requireActiveDomain(coverage, "echoworldcore", EchoAdapterCoreDomain.WORLDGEN);
        requireActiveDomain(coverage, "echorendercore", EchoAdapterCoreDomain.RENDERING);
        requireActiveDomain(coverage, "echoinputcore", EchoAdapterCoreDomain.INPUT);
        requireActiveDomain(coverage, "echoplayercore", EchoAdapterCoreDomain.PLAYER);
        requireActiveDomain(coverage, "echocontentcore", EchoAdapterCoreDomain.ITEMS);
        requireActiveDomain(coverage, "echoblockworks", EchoAdapterCoreDomain.BLOCKS);
        requireActiveDomain(coverage, "echoholomap", EchoAdapterCoreDomain.MAPS);
        requireActiveDomain(coverage, "echoindex", EchoAdapterCoreDomain.RECIPES);
        requireActiveDomain(coverage, "echolens", EchoAdapterCoreDomain.UI_SCREENS);
        requireActiveDomain(coverage, "echorecipecore", EchoAdapterCoreDomain.RECIPES);
        requireActiveDomain(coverage, "echoprogressioncore", EchoAdapterCoreDomain.MISSIONS);
        requireActiveDomain(coverage, "echosocialcore", EchoAdapterCoreDomain.ENTITIES);
        requireActiveDomain(coverage, "echolootcore", EchoAdapterCoreDomain.LOOT);
        requireActiveDomain(coverage, "echostructurecore", EchoAdapterCoreDomain.STRUCTURES);
        requireActiveDomain(coverage, "echospawncore", EchoAdapterCoreDomain.WORLDGEN);
        requireActiveDomain(coverage, "echoeventcore", EchoAdapterCoreDomain.MISSIONS);
        requireActiveDomain(coverage, "echostatuscore", EchoAdapterCoreDomain.DATA);
        requireActiveDomain(coverage, "echodifficultycore", EchoAdapterCoreDomain.PACKS);
        requireActiveDomain(coverage, "echobiomecore", EchoAdapterCoreDomain.WORLDGEN);
        requireActiveDomain(coverage, "echoatmospherecore", EchoAdapterCoreDomain.WORLDGEN);
        requireActiveDomain(coverage, "echopowercore", EchoAdapterCoreDomain.DIAGNOSTICS);
        requireActiveDomain(coverage, "echohudcore", EchoAdapterCoreDomain.UI_SCREENS);
        requireActiveDomain(coverage, "echoguidecore", EchoAdapterCoreDomain.WIKI);
        requireActiveDomain(coverage, "echocameracore", EchoAdapterCoreDomain.RENDERING);
        requireActiveDomain(coverage, "echocinematiccore", EchoAdapterCoreDomain.RENDERING);
        requireActiveDomain(coverage, "echocodexcore", EchoAdapterCoreDomain.WIKI);
        requireActiveDomain(coverage, "echocombatcore", EchoAdapterCoreDomain.PLAYER);
        requireActiveDomain(coverage, "echocreaturecore", EchoAdapterCoreDomain.ENTITIES);
        requireActiveDomain(coverage, "echoeconomycore", EchoAdapterCoreDomain.ITEMS);
        requireActiveDomain(coverage, "echoencountercore", EchoAdapterCoreDomain.MISSIONS);
        requireActiveDomain(coverage, "echologisticscore", EchoAdapterCoreDomain.NETWORKING);
        requireActiveDomain(coverage, "echolorecore", EchoAdapterCoreDomain.WIKI);
        requireActiveDomain(coverage, "echomachinecore", EchoAdapterCoreDomain.RECIPES);
        requireActiveDomain(coverage, "echonotificationcore", EchoAdapterCoreDomain.UI_SCREENS);
        requireActiveDomain(coverage, "echoquestdirector", EchoAdapterCoreDomain.MISSIONS);
        requireActiveDomain(coverage, "echoscriptcore", EchoAdapterCoreDomain.COMMANDS);
        requireActiveDomain(coverage, "echotutorialcore", EchoAdapterCoreDomain.UI_SCREENS);
        requireActiveDomain(coverage, "echovehiclecore", EchoAdapterCoreDomain.PLAYER);
        requireActiveDomain(coverage, "echoaetherworks", EchoAdapterCoreDomain.BLOCKS);
        requireActiveDomain(coverage, "echoagentcore", EchoAdapterCoreDomain.DIAGNOSTICS);
        requireActiveDomain(coverage, "echoagriculturereclamation", EchoAdapterCoreDomain.WORLDGEN);
        requireActiveDomain(coverage, "echoarcanacore", EchoAdapterCoreDomain.DATA);
        requireActiveDomain(coverage, "echoarcaneindex", EchoAdapterCoreDomain.WIKI);
        requireActiveDomain(coverage, "echoarmory", EchoAdapterCoreDomain.ITEMS);
        requireActiveDomain(coverage, "echobasegrid", EchoAdapterCoreDomain.COMMANDS);
        requireActiveDomain(coverage, "echoblackboxprotocol", EchoAdapterCoreDomain.MISSIONS);
        requireActiveDomain(coverage, "echobridgecore", EchoAdapterCoreDomain.DIAGNOSTICS);
        requireActiveDomain(coverage, "echocommunitybridge", EchoAdapterCoreDomain.NETWORKING);
        requireActiveDomain(coverage, "echoconvoyprotocol", EchoAdapterCoreDomain.ENTITIES);
        requireActiveDomain(coverage, "echocreatorcore", EchoAdapterCoreDomain.PACKS);
        requireActiveDomain(coverage, "echocursecore", EchoAdapterCoreDomain.PLAYER);
        requireActiveDomain(coverage, "echofamiliarcore", EchoAdapterCoreDomain.ENTITIES);
        requireActiveDomain(coverage, "echogrimoire", EchoAdapterCoreDomain.WIKI);
        requireActiveDomain(coverage, "echoindustrialnexus", EchoAdapterCoreDomain.BLOCKS);
        requireActiveDomain(coverage, "echologisticsnetwork", EchoAdapterCoreDomain.NETWORKING);
        requireActiveDomain(coverage, "echomultiblockcore", EchoAdapterCoreDomain.STRUCTURES);
        requireActiveDomain(coverage, "echonexusprotocol", EchoAdapterCoreDomain.MISSIONS);
        requireActiveDomain(coverage, "echonpcore", EchoAdapterCoreDomain.ENTITIES);
        requireActiveDomain(coverage, "echoorbitalremnants", EchoAdapterCoreDomain.WORLDGEN);
        requireActiveDomain(coverage, "echopresencelink", EchoAdapterCoreDomain.PLAYER);
        requireActiveDomain(coverage, "echoprimecore", EchoAdapterCoreDomain.MISSIONS);
        requireActiveDomain(coverage, "echoriftworlds", EchoAdapterCoreDomain.WORLDGEN);
        requireActiveDomain(coverage, "echoritualcore", EchoAdapterCoreDomain.RECIPES);
        requireActiveDomain(coverage, "echospellcore", EchoAdapterCoreDomain.PLAYER);
        requireActiveDomain(coverage, "echostationfall", EchoAdapterCoreDomain.WORLDGEN);
        requireActiveDomain(coverage, "signalos", EchoAdapterCoreDomain.MISSIONS);
        requireActiveDomain(coverage, "echomissioncore", EchoAdapterCoreDomain.MISSIONS);
        requireActiveDomain(coverage, "echometadatacore", EchoAdapterCoreDomain.DATA);
        requireActiveDomain(coverage, "echomodulegraph", EchoAdapterCoreDomain.DIAGNOSTICS);
        requireActiveDomain(coverage, "echopackcore", EchoAdapterCoreDomain.PACKS);
        requireActiveDomain(coverage, "echorecovery", EchoAdapterCoreDomain.SAVES);
        requireActiveDomain(coverage, "echorelictech", EchoAdapterCoreDomain.ITEMS);
        requireActiveDomain(coverage, "echoreportcore", EchoAdapterCoreDomain.DIAGNOSTICS);
        requireActiveDomain(coverage, "echoscreencore", EchoAdapterCoreDomain.UI_SCREENS);
        requireActiveDomain(coverage, "echosoundcore", EchoAdapterCoreDomain.SOUNDS);
        requireActiveDomain(coverage, "echoterminal", EchoAdapterCoreDomain.UI_SCREENS);
        requireActiveDomain(coverage, "echothemecore", EchoAdapterCoreDomain.THEMES);
        requireActiveDomain(coverage, "echoweathercore", EchoAdapterCoreDomain.WORLDGEN);
        requireActiveDomain(coverage, "echowiki", EchoAdapterCoreDomain.WIKI);

        EchoAdapterCoreModuleCoverageEntry ashfall = coverage.require("echoashfallprotocol");
        require(ashfall.status() == EchoAdapterCoreModuleCoverageStatus.ACTIVE,
                "Ashfall must be active for the standalone beta slice");
        require(ashfall.standaloneDeclared(),
                "Ashfall metadata must declare standalone support");
        require(ashfall.adapterCoreDeclared(),
                "Ashfall metadata must declare echoadaptercore as its bridge dependency");
        require(ashfall.liveBindingAvailable(),
                "Ashfall should have live AdapterCore voxel/content bindings");
        require(ashfall.adapterDomains().contains(EchoAdapterCoreDomain.MISSIONS),
                "Ashfall should declare mission adapter coverage");
        require(ashfall.adapterDomains().contains(EchoAdapterCoreDomain.WORLDGEN),
                "Ashfall should declare worldgen adapter coverage");
        requireAllAdapterRuntimes(ashfall);
        require(coverage.activeCountsByDomain().containsKey(EchoAdapterCoreDomain.RENDERING),
                "domain coverage should include rendering after RenderCore declaration");

        require(modules.moduleGraph().dependencyEdges().stream()
                        .anyMatch(edge -> edge.fromModuleId().equals("echoashfallprotocol")
                                && edge.toModuleId().equals("echoadaptercore")
                                && edge.kind().equals("requires")),
                "module graph should include Ashfall -> AdapterCore required edge");
        EchoRuntimeSystemModuleStatusReport systemStatus = EchoRuntimeSystemModuleStatusReport.forRequiredModules(
                modules.registry(),
                List.of(
                        "echomodpackcommandcenter",
                        "signalos",
                        "signalosexample",
                        "echobridgecore",
                        "echoagentcore",
                        "echoreportcore",
                        "echometadatacore",
                        "echomodulegraph"
                )
        );
        require(systemStatus.require("signalos").status() == EchoRuntimeModuleStatus.RUNTIME_ACTIVE,
                "signalos should load as runtime-active");
        require(systemStatus.require("signalosexample").status() == EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY,
                "signalosexample should load as runtime-dev-only");
        require(systemStatus.require("echobridgecore").status() == EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY,
                "echobridgecore should load as runtime-dev-only");
        require(systemStatus.require("echoagentcore").status() == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "echoagentcore should load as runtime-tooling-only");
        require(systemStatus.require("echoreportcore").status() == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "echoreportcore should load as runtime-tooling-only");
        require(systemStatus.require("echometadatacore").status() == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "echometadatacore should load as runtime-tooling-only");
        require(systemStatus.require("echomodulegraph").status() == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "echomodulegraph should load as runtime-tooling-only");
        require(systemStatus.require("echomodpackcommandcenter").status()
                        == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "echomodpackcommandcenter should load as runtime-tooling-only");
        require(systemStatus.require("echomodpackcommandcenter").reason().contains("runtime-tooling-only"),
                "echomodpackcommandcenter status should explain its tooling-only runtime role");

        System.out.println("adaptercore module coverage smoke PASS total="
                + coverage.totalCount()
                + " active=" + coverage.activeCount()
                + " gaps=" + coverage.adapterGapCount()
                + " unsupported=" + coverage.unsupportedCount()
                + " graphIssues=" + coverage.graphIssues().size()
                + " contractLocked=" + coverage.contractLockedForBeta()
                + " moduleReports=" + coverage.moduleReports().size()
                + " neoforgeMetadata=" + neoForgeMetadata.candidateCount()
                + " diagnostics=" + diagnostics.diagnostics().size());
    }

    private static void writeNeoForgeMetadataReport(
            Path standaloneRoot,
            EchoNeoForgeMetadataScanResult metadata
    ) throws IOException {
        Path report = standaloneRoot.resolve("reports/echo/standalone/neoforge-compat-candidates.json");
        Files.createDirectories(report.getParent());
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schema\": \"echo.standalone.neoforge_compat_candidates.v1\",\n");
        json.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        json.append("  \"status\": \"").append(metadata.errorCount() == 0 ? "PASS" : "FAIL").append("\",\n");
        json.append("  \"summary\": \"NeoForge mods.toml metadata is discovered as compatibility-candidate input only; candidates are not activated as standalone modules.\",\n");
        json.append("  \"source\": \"META-INF/neoforge.mods.toml\",\n");
        json.append("  \"counts\": {\n");
        json.append("    \"candidates\": ").append(metadata.candidateCount()).append(",\n");
        json.append("    \"warnings\": ").append(metadata.warningCount()).append(",\n");
        json.append("    \"errors\": ").append(metadata.errorCount()).append("\n");
        json.append("  },\n");
        json.append("  \"safety\": {\n");
        json.append("    \"moduleActivation\": false,\n");
        json.append("    \"classloaderCreated\": false,\n");
        json.append("    \"moduleCodeExecuted\": false,\n");
        json.append("    \"nativeLoaderUsed\": false,\n");
        json.append("    \"runtimeStatus\": \"runtime-disabled-with-reason\"\n");
        json.append("  },\n");
        json.append("  \"diagnostics\": ").append(diagnosticArray(metadata.diagnostics())).append(",\n");
        json.append("  \"candidates\": [\n");
        for (int i = 0; i < metadata.candidates().size(); i++) {
            EchoNeoForgeModCandidate candidate = metadata.candidates().get(i);
            json.append("    {\n");
            json.append("      \"modId\": \"").append(escape(candidate.modId())).append("\",\n");
            json.append("      \"displayName\": \"").append(escape(candidate.displayName())).append("\",\n");
            json.append("      \"version\": \"").append(escape(candidate.version())).append("\",\n");
            json.append("      \"license\": \"").append(escape(candidate.license())).append("\",\n");
            json.append("      \"compatibilityKind\": \"").append(candidate.compatibilityKind()).append("\",\n");
            json.append("      \"runtimeStatus\": \"").append(candidate.runtimeStatus()).append("\",\n");
            json.append("      \"runtimeReason\": \"").append(escape(candidate.runtimeReason())).append("\",\n");
            json.append("      \"dependencyCounts\": {\n");
            json.append("        \"total\": ").append(candidate.dependencies().size()).append(",\n");
            json.append("        \"required\": ").append(candidate.requiredDependencies().size()).append(",\n");
            json.append("        \"optional\": ").append(candidate.optionalDependencies().size()).append(",\n");
            json.append("        \"platform\": ").append(candidate.platformDependencies().size()).append("\n");
            json.append("      },\n");
            json.append("      \"dependencies\": ").append(dependencyArray(candidate.dependencies())).append(",\n");
            json.append("      \"parseWarnings\": ").append(stringArray(candidate.parseWarnings())).append(",\n");
            json.append("      \"metadataPath\": \"")
                    .append(escape(standaloneRoot.relativize(candidate.metadataPath()).toString().replace('\\', '/')))
                    .append("\"\n");
            json.append("    }").append(i + 1 == metadata.candidates().size() ? "\n" : ",\n");
        }
        json.append("  ]\n");
        json.append("}\n");
        Files.writeString(report, json.toString());
    }

    private static void writeCoverageReport(
            Path standaloneRoot,
            EchoAdapterCoreModuleCoverageReport coverage,
            String status
    ) throws IOException {
        Path report = standaloneRoot.resolve("reports/echo/standalone/runtime-adaptercore-module-coverage.json");
        Files.createDirectories(report.getParent());
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schema\": \"echo.standalone.adaptercore_module_coverage.v2\",\n");
        json.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        json.append("  \"status\": \"").append(status).append("\",\n");
        json.append("  \"summary\": \"AdapterCore standalone coverage validates explicit NeoForge, ECHO Native Loader, and ECHO Runtime Standalone runtime metadata for every scanned catalog module.\",\n");
        json.append("  \"counts\": {\n");
        json.append("    \"total\": ").append(coverage.totalCount()).append(",\n");
        json.append("    \"active\": ").append(coverage.activeCount()).append(",\n");
        json.append("    \"adapterGaps\": ").append(coverage.adapterGapCount()).append(",\n");
        json.append("    \"unsupported\": ").append(coverage.unsupportedCount()).append(",\n");
        json.append("    \"graphIssues\": ").append(coverage.graphIssues().size()).append(",\n");
        json.append("    \"runtimeTargetGaps\": ")
                .append(coverage.standaloneModulesMissingRuntimeTargets().size()).append(",\n");
        json.append("    \"incompleteModuleReports\": ")
                .append(coverage.incompleteModuleReports().size()).append("\n");
        json.append("  },\n");
        json.append("  \"contractLockedForBeta\": ").append(coverage.contractLockedForBeta()).append(",\n");
        json.append("  \"requiredDomainsMissing\": ")
                .append(stringArray(coverage.missingRequiredBetaDomains().stream()
                        .map(EchoAdapterCoreDomain::id)
                        .toList()))
                .append(",\n");
        json.append("  \"graphIssues\": ").append(stringArray(coverage.graphIssues())).append(",\n");
        json.append("  \"modules\": [\n");
        for (int i = 0; i < coverage.entries().size(); i++) {
            EchoAdapterCoreModuleCoverageEntry entry = coverage.entries().get(i);
            json.append("    {\n");
            json.append("      \"moduleId\": \"").append(escape(entry.moduleId())).append("\",\n");
            json.append("      \"status\": \"").append(entry.status().name().toLowerCase()).append("\",\n");
            json.append("      \"standaloneDeclared\": ").append(entry.standaloneDeclared()).append(",\n");
            json.append("      \"adapterCoreDeclared\": ").append(entry.adapterCoreDeclared()).append(",\n");
            json.append("      \"adapterCoreProvider\": ").append(entry.adapterCoreProvider()).append(",\n");
            json.append("      \"nativeEntrypointDeclared\": ").append(entry.nativeEntrypointDeclared()).append(",\n");
            json.append("      \"liveBindingAvailable\": ").append(entry.liveBindingAvailable()).append(",\n");
            json.append("      \"domains\": ")
                    .append(stringArray(entry.adapterDomains().stream().map(EchoAdapterCoreDomain::id).toList()))
                    .append(",\n");
            json.append("      \"runtimes\": ")
                    .append(stringArray(entry.adapterRuntimes().stream()
                            .map(EchoAdapterCoreRuntimeKind::adapterId)
                            .toList()))
                    .append(",\n");
            json.append("      \"adapterKeys\": ").append(stringArray(entry.adapterKeys())).append(",\n");
            json.append("      \"gaps\": ").append(stringArray(entry.gaps())).append(",\n");
            json.append("      \"descriptorPath\": \"")
                    .append(escape(standaloneRoot.relativize(entry.descriptorPath()).toString().replace('\\', '/')))
                    .append("\"\n");
            json.append("    }").append(i + 1 == coverage.entries().size() ? "\n" : ",\n");
        }
        json.append("  ]\n");
        json.append("}\n");
        Files.writeString(report, json.toString());
    }

    private static void requireActiveDomain(
            EchoAdapterCoreModuleCoverageReport coverage,
            String moduleId,
            EchoAdapterCoreDomain domain
    ) {
        EchoAdapterCoreModuleCoverageEntry entry = coverage.require(moduleId);
        require(entry.status() == EchoAdapterCoreModuleCoverageStatus.ACTIVE,
                moduleId + " should be active through AdapterCore metadata");
        require(entry.adapterDomains().contains(domain),
                moduleId + " should declare AdapterCore domain " + domain.id());
        require(entry.adapterKeys().contains("adapterDomains"),
                moduleId + " should expose adapter domain metadata");
        requireAllAdapterRuntimes(entry);
    }

    private static void requireAllAdapterRuntimes(EchoAdapterCoreModuleCoverageEntry entry) {
        for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
            require(entry.adapterRuntimes().contains(runtimeKind),
                    entry.moduleId() + " should declare AdapterCore runtime " + runtimeKind.adapterId());
        }
        require(entry.adapterKeys().contains("adapterRuntimes"),
                entry.moduleId() + " should expose AdapterCore runtime metadata");
    }

    private static List<String> nonActiveSummary(EchoAdapterCoreModuleCoverageReport coverage) {
        return coverage.entries().stream()
                .filter(entry -> entry.status() != EchoAdapterCoreModuleCoverageStatus.ACTIVE)
                .map(entry -> entry.moduleId()
                        + "=" + entry.status().name().toLowerCase()
                        + " gaps=" + entry.gaps())
                .toList();
    }

    private static List<Path> moduleRoots(Path repoRoot) {
        ArrayList<Path> roots = new ArrayList<>();
        addIfDirectory(roots, repoRoot.resolve("core"));
        addIfDirectory(roots, repoRoot.resolve("addons"));
        addIfDirectory(roots, repoRoot.resolve("src/main/resources"));
        return List.copyOf(roots);
    }

    private static void addIfDirectory(List<Path> roots, Path path) {
        if (Files.isDirectory(path)) {
            roots.add(path);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static List<String> diagnosticMessages(List<EchoCompatDiagnostic> diagnostics) {
        return diagnostics.stream()
                .map(diagnostic -> diagnostic.severity() + ":" + diagnostic.subject() + ":" + diagnostic.message())
                .toList();
    }

    private static String diagnosticArray(List<EchoCompatDiagnostic> diagnostics) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < diagnostics.size(); i++) {
            EchoCompatDiagnostic diagnostic = diagnostics.get(i);
            json.append("{\"severity\":\"")
                    .append(diagnostic.severity().name().toLowerCase())
                    .append("\",\"subject\":\"")
                    .append(escape(diagnostic.subject()))
                    .append("\",\"message\":\"")
                    .append(escape(diagnostic.message()))
                    .append("\"}");
            if (i + 1 < diagnostics.size()) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String dependencyArray(List<EchoNeoForgeDependency> dependencies) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < dependencies.size(); i++) {
            EchoNeoForgeDependency dependency = dependencies.get(i);
            json.append("{\"modId\":\"")
                    .append(escape(dependency.modId()))
                    .append("\",\"type\":\"")
                    .append(escape(dependency.type()))
                    .append("\",\"versionRange\":\"")
                    .append(escape(dependency.versionRange()))
                    .append("\",\"ordering\":\"")
                    .append(escape(dependency.ordering()))
                    .append("\",\"side\":\"")
                    .append(escape(dependency.side()))
                    .append("\",\"reason\":\"")
                    .append(escape(dependency.reason()))
                    .append("\"}");
            if (i + 1 < dependencies.size()) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String stringArray(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            json.append("\"").append(escape(values.get(i))).append("\"");
            if (i + 1 < values.size()) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
