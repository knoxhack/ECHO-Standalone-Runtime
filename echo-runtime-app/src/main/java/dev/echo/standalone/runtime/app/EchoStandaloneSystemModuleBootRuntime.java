package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.assets.EchoAssetMount;
import dev.echo.standalone.runtime.assets.EchoAssetRuntime;
import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageAuditor;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageReport;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneRegistry;
import dev.echo.standalone.runtime.compat.EchoRequiredSystemModuleRuntimeResult;
import dev.echo.standalone.runtime.compat.EchoRequiredSystemModuleStandaloneRuntime;
import dev.echo.standalone.runtime.contracts.EchoRuntimeContext;
import dev.echo.standalone.runtime.data.EchoDataRegistryStore;
import dev.echo.standalone.runtime.data.EchoDataRuntime;
import dev.echo.standalone.runtime.data.EchoDataRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoStandaloneSystemModuleBootRuntime {
    public EchoStandaloneSystemModuleBootResult boot(EchoRuntimeContext context) {
        Objects.requireNonNull(context, "context");
        List<Path> roots = moduleRoots(context.environment().workspaceRoot());
        if (roots.isEmpty()) {
            EchoStandaloneSystemModuleBootResult inactive = EchoStandaloneSystemModuleBootResult.inactive();
            context.services().register(EchoStandaloneSystemModuleBootResult.class, inactive);
            return inactive;
        }

        loadAssetAndDataRuntime(context, roots);

        EchoDataRegistryStore dataStore = context.services()
                .find(EchoDataRegistryStore.class)
                .orElse(null);
        EchoAdapterCoreStandaloneContentBridge bridge = context.services()
                .find(EchoAdapterCoreStandaloneContentBridge.class)
                .orElseGet(() -> EchoAdapterCoreStandaloneContentBridge.ashfallLive(dataStore));
        context.services().register(EchoAdapterCoreStandaloneContentBridge.class, bridge);
        context.services().register(EchoAdapterCoreStandaloneRegistry.class, bridge.registry());

        EchoRuntimeModuleRuntimeResult modules = EchoRuntimeModuleManager.executableAbiV1()
                .run(roots, context.services());
        EchoAdapterCoreModuleCoverageReport coverage =
                new EchoAdapterCoreModuleCoverageAuditor().audit(modules, bridge);
        context.services().register(EchoAdapterCoreModuleCoverageReport.class, coverage);

        EchoRequiredSystemModuleRuntimeResult requiredSystemModules =
                new EchoRequiredSystemModuleStandaloneRuntime().activate(context.services(), modules, bridge);
        EchoStandaloneSystemModuleBootResult result = new EchoStandaloneSystemModuleBootResult(
                modules.registry().descriptors().size(),
                coverage.totalCount(),
                requiredSystemModules.activationCount(),
                requiredSystemModules.executableCount(),
                bridge.readyBindingCount() == bridge.bindingCount()
                        && coverage.contractLockedForBeta()
                        && requiredSystemModules.allExecutable()
        );
        context.services().register(EchoStandaloneSystemModuleBootResult.class, result);
        context.diagnostics().info(
                "ECHO-STANDALONE-SYSTEM-MODULES-ACTIVE",
                "system_modules",
                "AdapterCore system module boot active: descriptors="
                        + result.moduleDescriptors()
                        + " coverage=" + result.adapterCoreCoverageTotal()
                        + " required=" + result.executableSystemModules()
                        + "/" + result.requiredSystemModules()
        );
        return result;
    }

    private static void loadAssetAndDataRuntime(EchoRuntimeContext context, List<Path> roots) {
        List<EchoAssetMount> mounts = new ArrayList<>();
        for (int i = 0; i < roots.size(); i++) {
            mounts.add(new EchoAssetMount(i, "module", roots.get(i), roots.get(i).toString()));
        }
        try {
            EchoAssetRuntimeResult assetResult = new EchoAssetRuntime(mounts).load(context.services(), List.of());
            EchoDataRuntimeResult dataResult = new EchoDataRuntime().load(context.services(), assetResult);
            context.diagnostics().info(
                    "ECHO-ASSET-DATA-LOAD",
                    "asset_data",
                    "Asset and data runtime loaded: assets=" + assetResult.index().entries().size()
                            + " documents=" + dataResult.documents().size()
                            + " registries=" + dataResult.registries().totalEntries()
            );
        } catch (IOException e) {
            context.diagnostics().warning(
                    "ECHO-ASSET-DATA-LOAD-FAILED",
                    "asset_data",
                    "Failed to load asset/data runtime: " + e.getMessage()
            );
        }
    }

    private static List<Path> moduleRoots(Path workspaceRoot) {
        return EchoStandaloneModuleRoots.resolve(workspaceRoot);
    }
}
