package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoRuntimeLogBridge;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleLifecycle;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContentActivationRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleLifecycleBus;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleLifecycleEvent;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleSandboxPolicy;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleDescriptorSchema;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleServiceExportRegistry;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeNativeLoaderAbiSmokeHarness {
    private EchoRuntimeNativeLoaderAbiSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path fixtureRoot = Files.createTempDirectory("echo-native-loader-abi-v1-smoke");
        Path liveModule = fixtureRoot.resolve("live-addon");
        Path crashModule = fixtureRoot.resolve("crash-addon");
        Path dependentCrashModule = fixtureRoot.resolve("dependent-crash-addon");
        Path partialStateCrashModule = fixtureRoot.resolve("partial-state-crash-addon");
        Path reloadCrashModule = fixtureRoot.resolve("reload-crash-addon");
        Path deniedModule = fixtureRoot.resolve("denied-addon");
        Path deniedRegistryAccessModule = fixtureRoot.resolve("denied-registry-access-addon");
        Path unknownPermissionModule = fixtureRoot.resolve("unknown-permission-addon");
        Path classPathEscapeModule = fixtureRoot.resolve("classpath-escape-addon");
        Path incompatibleModule = fixtureRoot.resolve("incompatible-addon");
        Path serviceProviderModule = fixtureRoot.resolve("service-provider-addon");
        Path serviceConsumerModule = fixtureRoot.resolve("service-consumer-addon");
        Path optionalProviderModule = fixtureRoot.resolve("optional-provider-addon");
        Path optionalConsumerModule = fixtureRoot.resolve("optional-consumer-addon");
        Path optionalWarningModule = fixtureRoot.resolve("optional-warning-addon");
        Path unloadCrashModule = fixtureRoot.resolve("unload-crash-addon");
        Path deniedExportModule = fixtureRoot.resolve("denied-export-addon");
        Path cycleAModule = fixtureRoot.resolve("cycle-a-addon");
        Path cycleBModule = fixtureRoot.resolve("cycle-b-addon");
        Path adapterCoreModule = fixtureRoot.resolve("adaptercore-addon");

        writeDescriptor(fixtureRoot.resolve("echo-core/META-INF/echo.runtime.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echo-core",
                  "name": "ECHO Core Runtime Fixture",
                  "version": "1.0.0",
                  "kind": "runtime_module",
                  "side": "both",
                  "trust": "trusted",
                  "official": true,
                  "standalone": true,
                  "requires": [],
                  "optional": [],
                  "provides": ["echo:services"],
                  "consumes": [],
                  "access": {"services": true}
                }
                """);
        writeDescriptor(liveModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-field-generator",
                  "name": "ECHO ABI Field Generator Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:field_generator"],
                  "consumes": ["echo:services"],
                  "permissions": ["content.register", "services.export"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.live.FieldGeneratorEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(crashModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-crash-fixture",
                  "name": "ECHO ABI Crash Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:crash_fixture"],
                  "consumes": ["echo:services"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.crash.CrashEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(dependentCrashModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-dependent-on-crash",
                  "name": "ECHO ABI Dependent On Crash Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core", "echoabi-crash-fixture"],
                  "requiresVersions": {
                    "echo-core": "[1.0.0,2.0.0)",
                    "echoabi-crash-fixture": "[1.0.0,2.0.0)"
                  },
                  "optional": [],
                  "provides": ["echoabi:dependent_on_crash"],
                  "consumes": ["echo:services"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.crash.DependentOnCrashEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(partialStateCrashModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-partial-state-crash",
                  "name": "ECHO ABI Partial State Crash Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:partial_state_crash", "echoabi:partial_state_service"],
                  "consumes": ["echo:services"],
                  "permissions": ["content.register", "services.export"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.crash.PartialStateCrashEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(reloadCrashModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-reload-crash",
                  "name": "ECHO ABI Reload Crash Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:reload_crash"],
                  "consumes": ["echo:services"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.reload.ReloadCrashEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(deniedModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-denied-content",
                  "name": "ECHO ABI Denied Content Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:denied_content"],
                  "consumes": ["echo:services"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.denied.DeniedContentEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(deniedRegistryAccessModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-denied-registry-access",
                  "name": "ECHO ABI Denied Raw Registry Access Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:denied_registry_access"],
                  "consumes": ["echo:services"],
                  "permissions": ["content.register"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.denied.DeniedRegistryAccessEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(unknownPermissionModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-unknown-permission",
                  "name": "ECHO ABI Unknown Permission Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:unknown_permission"],
                  "consumes": ["echo:services"],
                  "permissions": ["filesystem.write"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.denied.UnknownPermissionEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(classPathEscapeModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-classpath-escape",
                  "name": "ECHO ABI Classpath Escape Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:classpath_escape"],
                  "consumes": ["echo:services"],
                  "classPath": [".."],
                  "entrypoint": "fixture.escape.ClassPathEscapeEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(incompatibleModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-incompatible-core",
                  "name": "ECHO ABI Incompatible Core Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[2.0.0,)"},
                  "optional": [],
                  "provides": ["echoabi:incompatible_core"],
                  "consumes": ["echo:services"],
                  "permissions": ["content.register"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.incompatible.IncompatibleEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(serviceProviderModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-service-provider",
                  "name": "ECHO ABI Service Provider Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:calibration_service"],
                  "consumes": ["echo:services"],
                  "permissions": ["services.export"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.service.ProviderEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(serviceConsumerModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-service-consumer",
                  "name": "ECHO ABI Service Consumer Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core", "echoabi-service-provider"],
                  "requiresVersions": {
                    "echo-core": "[1.0.0,2.0.0)",
                    "echoabi-service-provider": "[1.0.0,2.0.0)"
                  },
                  "optional": [],
                  "provides": ["echoabi:service_consumer"],
                  "consumes": ["echo:services", "echoabi:calibration_service"],
                  "permissions": ["services.import"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.service.ConsumerEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(optionalProviderModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-optional-provider",
                  "name": "ECHO ABI Optional Provider Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:optional_service"],
                  "consumes": ["echo:services"],
                  "permissions": ["services.export"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.optional.OptionalProviderEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(optionalConsumerModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-optional-consumer",
                  "name": "ECHO ABI Optional Consumer Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": ["echoabi-missing-optional", "echoabi-optional-provider"],
                  "optionalVersions": {"echoabi-optional-provider": "[1.0.0,2.0.0)"},
                  "provides": ["echoabi:optional_consumer"],
                  "consumes": ["echo:services", "echoabi:optional_service"],
                  "permissions": ["services.import"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.optional.OptionalConsumerEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(optionalWarningModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-optional-version-warning",
                  "name": "ECHO ABI Optional Version Warning Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": ["echoabi-optional-provider"],
                  "optionalVersions": {"echoabi-optional-provider": "[2.0.0,)"},
                  "provides": ["echoabi:optional_version_warning"],
                  "consumes": ["echo:services"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.optional.OptionalWarningEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(unloadCrashModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-unload-crash",
                  "name": "ECHO ABI Unload Crash Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:unload_crash", "echoabi:unload_crash_service"],
                  "consumes": ["echo:services"],
                  "permissions": ["content.register", "services.export"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.unload.UnloadCrashEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(deniedExportModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-denied-service-export",
                  "name": "ECHO ABI Denied Service Export Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:denied_service"],
                  "consumes": ["echo:services"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.service.DeniedExportEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(cycleAModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-cycle-a",
                  "name": "ECHO ABI Cycle A Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core", "echoabi-cycle-b"],
                  "requiresVersions": {
                    "echo-core": "[1.0.0,2.0.0)",
                    "echoabi-cycle-b": "[1.0.0,2.0.0)"
                  },
                  "optional": [],
                  "provides": ["echoabi:cycle_a"],
                  "consumes": ["echo:services"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.cycle.CycleAEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(cycleBModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-cycle-b",
                  "name": "ECHO ABI Cycle B Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core", "echoabi-cycle-a"],
                  "requiresVersions": {
                    "echo-core": "[1.0.0,2.0.0)",
                    "echoabi-cycle-a": "[1.0.0,2.0.0)"
                  },
                  "optional": [],
                  "provides": ["echoabi:cycle_b"],
                  "consumes": ["echo:services"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.cycle.CycleBEntrypoint",
                  "access": {"services": true}
                }
                """);
        writeDescriptor(adapterCoreModule.resolve("META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoabi-adaptercore-addon",
                  "name": "ECHO ABI AdapterCore Entrypoint Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "requiresVersions": {"echo-core": "[1.0.0,2.0.0)"},
                  "optional": [],
                  "provides": ["echoabi:adaptercore_content", "echoabi:adaptercore_service"],
                  "consumes": ["echo:services"],
                  "permissions": ["content.register", "services.export"],
                  "classPath": ["classes"],
                  "adapterCoreEntrypoint": "fixture.adaptercore.AdapterCoreEntrypoint",
                  "access": {"services": true}
                }
                """);

        compileSource(liveModule.resolve("classes"), "fixture/live/FieldGeneratorEntrypoint.java", """
                package fixture.live;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContentActivation;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleServiceExport;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ActivatedContent;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class FieldGeneratorEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        ModuleActivationLedger ledger = context.requireService(ModuleActivationLedger.class);
                        ledger.record(context.descriptor().id(), "load");
                        ledger.recordClassLoaderIsolation(
                                context.descriptor().id(),
                                getClass().getClassLoader() != ModuleActivationLedger.class.getClassLoader()
                        );
                        EchoRuntimeModuleContentActivation activation =
                                context.registerContent("block", "echoabi:field_generator");
                        ledger.activate(activation.contentId());
                        EchoRuntimeModuleServiceExport export = context.exportService(
                                "echoabi:activated_content",
                                new ActivatedContent(context.descriptor().id(), "echoabi:field_generator")
                        );
                        ledger.recordServiceExport(export.serviceId());
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "reload");
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "unload");
                    }
                }
                """);
        compileSource(crashModule.resolve("classes"), "fixture/crash/CrashEntrypoint.java", """
                package fixture.crash;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class CrashEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "load-attempt");
                        throw new IllegalStateException("intentional ABI crash fixture");
                    }
                }
                """);
        compileSource(dependentCrashModule.resolve("classes"), "fixture/crash/DependentOnCrashEntrypoint.java", """
                package fixture.crash;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class DependentOnCrashEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "should-not-load-after-required-dependency-failed");
                    }
                }
                """);
        compileSource(partialStateCrashModule.resolve("classes"), "fixture/crash/PartialStateCrashEntrypoint.java", """
                package fixture.crash;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContentActivation;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleServiceExport;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.OfferedService;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class PartialStateCrashEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        ModuleActivationLedger ledger = context.requireService(ModuleActivationLedger.class);
                        ledger.record(context.descriptor().id(), "load-attempt");
                        EchoRuntimeModuleContentActivation activation =
                                context.registerContent("block", "echoabi:partial_state_block");
                        ledger.activate(activation.contentId());
                        EchoRuntimeModuleServiceExport export = context.exportService(
                                "echoabi:partial_state_service",
                                new OfferedService(context.descriptor().id(), "echoabi:partial_state_service", "partial")
                        );
                        ledger.recordServiceExport(export.serviceId());
                        throw new IllegalStateException("partial state crash");
                    }
                }
                """);
        compileSource(reloadCrashModule.resolve("classes"), "fixture/reload/ReloadCrashEntrypoint.java", """
                package fixture.reload;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class ReloadCrashEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "load");
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "reload-attempt");
                        throw new IllegalStateException("intentional ABI reload crash fixture");
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "should-not-unload-after-reload-failure");
                    }
                }
                """);
        compileSource(deniedModule.resolve("classes"), "fixture/denied/DeniedContentEntrypoint.java", """
                package fixture.denied;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class DeniedContentEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "load-attempt");
                        context.registerContent("block", "echoabi:denied_content");
                    }
                }
                """);
        compileSource(deniedRegistryAccessModule.resolve("classes"), "fixture/denied/DeniedRegistryAccessEntrypoint.java", """
                package fixture.denied;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContentActivationRegistry;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class DeniedRegistryAccessEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "load-attempt");
                        context.requireService(EchoRuntimeModuleContentActivationRegistry.class)
                                .register(context.descriptor().id(), "block", "echoabi:raw_registry_bypass");
                    }
                }
                """);
        compileSource(unknownPermissionModule.resolve("classes"), "fixture/denied/UnknownPermissionEntrypoint.java", """
                package fixture.denied;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class UnknownPermissionEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "should-not-execute");
                    }
                }
                """);
        compileSource(classPathEscapeModule.resolve("classes"), "fixture/escape/ClassPathEscapeEntrypoint.java", """
                package fixture.escape;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class ClassPathEscapeEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "should-not-execute");
                    }
                }
                """);
        compileSource(incompatibleModule.resolve("classes"), "fixture/incompatible/IncompatibleEntrypoint.java", """
                package fixture.incompatible;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class IncompatibleEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "should-not-execute");
                    }
                }
                """);
        compileSource(serviceProviderModule.resolve("classes"), "fixture/service/ProviderEntrypoint.java", """
                package fixture.service;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleServiceExport;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.OfferedService;

                public final class ProviderEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        ModuleActivationLedger ledger = context.requireService(ModuleActivationLedger.class);
                        ledger.record(context.descriptor().id(), "load");
                        EchoRuntimeModuleServiceExport export = context.exportService(
                                "echoabi:calibration_service",
                                new OfferedService(context.descriptor().id(), "echoabi:calibration_service", "stable")
                        );
                        ledger.recordServiceExport(export.serviceId());
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "reload");
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "unload");
                    }
                }
                """);
        compileSource(serviceConsumerModule.resolve("classes"), "fixture/service/ConsumerEntrypoint.java", """
                package fixture.service;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.OfferedService;

                public final class ConsumerEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        ModuleActivationLedger ledger = context.requireService(ModuleActivationLedger.class);
                        ledger.record(context.descriptor().id(), "load");
                        OfferedService service = context.importService(
                                "echoabi:calibration_service",
                                OfferedService.class
                        ).orElseThrow();
                        ledger.recordServiceImport(context.descriptor().id(), service.serviceId());
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "reload");
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "unload");
                    }
                }
                """);
        compileSource(optionalProviderModule.resolve("classes"), "fixture/optional/OptionalProviderEntrypoint.java", """
                package fixture.optional;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleServiceExport;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.OfferedService;

                public final class OptionalProviderEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        ModuleActivationLedger ledger = context.requireService(ModuleActivationLedger.class);
                        ledger.record(context.descriptor().id(), "load");
                        EchoRuntimeModuleServiceExport export = context.exportService(
                                "echoabi:optional_service",
                                new OfferedService(context.descriptor().id(), "echoabi:optional_service", "optional")
                        );
                        ledger.recordServiceExport(export.serviceId());
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "reload");
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "unload");
                    }
                }
                """);
        compileSource(optionalConsumerModule.resolve("classes"), "fixture/optional/OptionalConsumerEntrypoint.java", """
                package fixture.optional;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.OfferedService;

                public final class OptionalConsumerEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        ModuleActivationLedger ledger = context.requireService(ModuleActivationLedger.class);
                        ledger.record(context.descriptor().id(), "load");
                        OfferedService service = context.importService(
                                "echoabi:optional_service",
                                OfferedService.class
                        ).orElseThrow();
                        ledger.recordServiceImport(context.descriptor().id(), service.serviceId());
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "reload");
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "unload");
                    }
                }
                """);
        compileSource(optionalWarningModule.resolve("classes"), "fixture/optional/OptionalWarningEntrypoint.java", """
                package fixture.optional;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class OptionalWarningEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "load");
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "reload");
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "unload");
                    }
                }
                """);
        compileSource(unloadCrashModule.resolve("classes"), "fixture/unload/UnloadCrashEntrypoint.java", """
                package fixture.unload;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContentActivation;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleServiceExport;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.OfferedService;

                public final class UnloadCrashEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        ModuleActivationLedger ledger = context.requireService(ModuleActivationLedger.class);
                        ledger.record(context.descriptor().id(), "load");
                        EchoRuntimeModuleContentActivation activation =
                                context.registerContent("block", "echoabi:unload_crash_block");
                        ledger.activate(activation.contentId());
                        EchoRuntimeModuleServiceExport export = context.exportService(
                                "echoabi:unload_crash_service",
                                new OfferedService(context.descriptor().id(), "echoabi:unload_crash_service", "unload")
                        );
                        ledger.recordServiceExport(export.serviceId());
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "reload");
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "unload-attempt");
                        throw new IllegalStateException("unload crash");
                    }
                }
                """);
        compileSource(deniedExportModule.resolve("classes"), "fixture/service/DeniedExportEntrypoint.java", """
                package fixture.service;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class DeniedExportEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "load-attempt");
                        context.exportService("echoabi:denied_service", "denied");
                    }
                }
                """);
        compileSource(cycleAModule.resolve("classes"), "fixture/cycle/CycleAEntrypoint.java", """
                package fixture.cycle;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class CycleAEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "should-not-execute");
                    }
                }
                """);
        compileSource(cycleBModule.resolve("classes"), "fixture/cycle/CycleBEntrypoint.java", """
                package fixture.cycle;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;

                public final class CycleBEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "should-not-execute");
                    }
                }
                """);
        compileSource(adapterCoreModule.resolve("classes"), "fixture/adaptercore/AdapterCoreEntrypoint.java", """
                package fixture.adaptercore;

                import dev.echo.standalone.runtime.modules.EchoRuntimeAdapterCoreEntrypoint;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContentActivation;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleServiceExport;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.ModuleActivationLedger;
                import dev.echo.standalone.runtime.testkit.EchoRuntimeNativeLoaderAbiSmokeHarness.OfferedService;

                public final class AdapterCoreEntrypoint implements EchoRuntimeAdapterCoreEntrypoint {
                    @Override
                    public void activate(EchoRuntimeModuleContext context) {
                        ModuleActivationLedger ledger = context.requireService(ModuleActivationLedger.class);
                        ledger.record(context.descriptor().id(), "adaptercore-activate");
                        EchoRuntimeModuleContentActivation activation =
                                context.registerContent("adaptercore_block", "echoabi:adaptercore_marker");
                        ledger.activate(activation.contentId());
                        EchoRuntimeModuleServiceExport export = context.exportService(
                                "echoabi:adaptercore_service",
                                new OfferedService(context.descriptor().id(), "echoabi:adaptercore_service", "adaptercore")
                        );
                        ledger.recordServiceExport(export.serviceId());
                    }

                    @Override
                    public void reloadData(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "adaptercore-reload");
                    }

                    @Override
                    public void deactivate(EchoRuntimeModuleContext context) {
                        context.requireService(ModuleActivationLedger.class)
                                .record(context.descriptor().id(), "adaptercore-deactivate");
                    }
                }
                """);

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoRuntimeLogBridge diagnostics = new EchoRuntimeLogBridge();
        ModuleActivationLedger ledger = new ModuleActivationLedger();
        EchoRuntimeModuleLifecycleBus lifecycleBus = new EchoRuntimeModuleLifecycleBus();
        lifecycleBus.subscribe(ledger::recordLifecycleBusEvent);
        services.register(EchoRuntimeDiagnosticSink.class, diagnostics);
        services.register(ModuleActivationLedger.class, ledger);
        services.register(EchoRuntimeModuleLifecycleBus.class, lifecycleBus);

        EchoRuntimeModuleManager manager = EchoRuntimeModuleManager.executableAbiV1();
        EchoRuntimeModuleRuntimeResult result = manager.run(List.of(fixtureRoot), services);
        EchoRuntimeModuleRegistry registry = result.registry();

        require(EchoRuntimeModuleDescriptorSchema.SCHEMA_ID.equals("echo.runtime.module.v1"),
                "descriptor schema id should remain locked to echo.runtime.module.v1");
        require(EchoRuntimeModuleDescriptorSchema.coversExecutableAbiV1(),
                "descriptor schema should cover executable ABI v1 fields");
        require(EchoRuntimeModuleDescriptorSchema.allFields().containsAll(List.of(
                        "classPath",
                        "entrypoint",
                        "adapterCoreEntrypoint",
                        "requiresVersions",
                        "optionalVersions",
                        "permissions"
                )),
                "descriptor schema should expose all executable ABI v1 fields");
        require(registry.find("echoabi-adaptercore-addon")
                        .orElseThrow()
                        .adapterCoreEntrypoint()
                        .equals("fixture.adaptercore.AdapterCoreEntrypoint"),
                "descriptor parser should bind adapterCoreEntrypoint");
        require(registry.find("echoabi-service-consumer")
                        .orElseThrow()
                        .requiresVersions()
                        .get("echoabi-service-provider")
                        .equals("[1.0.0,2.0.0)"),
                "descriptor parser should bind required dependency version ranges");
        require(registry.find("echoabi-field-generator").orElseThrow().classPath().equals(List.of("classes")),
                "descriptor parser should bind classPath entries");
        require(registry.find("echoabi-field-generator")
                        .orElseThrow()
                        .permissions()
                        .equals(List.of("content.register", "services.export")),
                "descriptor parser should bind ABI permissions");
        require(registry.lifecycle("echoabi-field-generator") == EchoRuntimeModuleLifecycle.READY,
                "live non-core addon should reach READY");
        require(registry.lifecycle("echoabi-reload-crash") == EchoRuntimeModuleLifecycle.READY,
                "reload crash addon should reach READY before data reload");
        require(registry.lifecycle("echoabi-crash-fixture") == EchoRuntimeModuleLifecycle.FAILED,
                "crashing addon should fail without crashing the loader");
        require(registry.lifecycle("echoabi-dependent-on-crash") == EchoRuntimeModuleLifecycle.FAILED,
                "dependent addon should fail when its required dependency failed during load");
        require(registry.lifecycle("echoabi-partial-state-crash") == EchoRuntimeModuleLifecycle.FAILED,
                "partial-state crashing addon should fail without leaving runtime state registered");
        require(registry.lifecycle("echoabi-denied-content") == EchoRuntimeModuleLifecycle.FAILED,
                "permission-denied addon should fail without crashing the loader");
        require(registry.lifecycle("echoabi-denied-registry-access") == EchoRuntimeModuleLifecycle.FAILED,
                "raw registry access addon should fail without crashing the loader");
        require(registry.lifecycle("echoabi-unknown-permission") == EchoRuntimeModuleLifecycle.FAILED,
                "unknown-permission addon should fail before execution");
        require(registry.lifecycle("echoabi-classpath-escape") == EchoRuntimeModuleLifecycle.FAILED,
                "classpath escape addon should fail before classloader activation");
        require(registry.lifecycle("echoabi-incompatible-core") == EchoRuntimeModuleLifecycle.FAILED,
                "incompatible dependency addon should fail during dependency resolution");
        require(registry.lifecycle("echoabi-service-provider") == EchoRuntimeModuleLifecycle.READY,
                "service provider addon should reach READY");
        require(registry.lifecycle("echoabi-service-consumer") == EchoRuntimeModuleLifecycle.READY,
                "service consumer addon should reach READY");
        require(registry.lifecycle("echoabi-optional-provider") == EchoRuntimeModuleLifecycle.READY,
                "optional provider addon should reach READY");
        require(registry.lifecycle("echoabi-optional-consumer") == EchoRuntimeModuleLifecycle.READY,
                "optional consumer addon should reach READY with present and missing optional dependencies");
        require(registry.lifecycle("echoabi-optional-version-warning") == EchoRuntimeModuleLifecycle.READY,
                "optional version warning addon should remain READY");
        require(registry.lifecycle("echoabi-unload-crash") == EchoRuntimeModuleLifecycle.READY,
                "unload crash addon should reach READY before unload");
        require(registry.lifecycle("echoabi-adaptercore-addon") == EchoRuntimeModuleLifecycle.READY,
                "AdapterCore entrypoint addon should reach READY");
        require(registry.lifecycle("echoabi-denied-service-export") == EchoRuntimeModuleLifecycle.FAILED,
                "permission-denied service exporter should fail without crashing the loader");
        require(registry.lifecycle("echoabi-cycle-a") == EchoRuntimeModuleLifecycle.FAILED,
                "cyclic dependency module A should fail during dependency resolution");
        require(registry.lifecycle("echoabi-cycle-b") == EchoRuntimeModuleLifecycle.FAILED,
                "cyclic dependency module B should fail during dependency resolution");
        require(lifecycleBus.events("echoabi-field-generator").stream()
                        .map(EchoRuntimeModuleLifecycleEvent::lifecycle)
                        .toList()
                        .containsAll(List.of(
                                EchoRuntimeModuleLifecycle.DISCOVERED,
                                EchoRuntimeModuleLifecycle.DESCRIPTOR_VALIDATED,
                                EchoRuntimeModuleLifecycle.DEPENDENCIES_RESOLVED,
                                EchoRuntimeModuleLifecycle.LOADED,
                                EchoRuntimeModuleLifecycle.READY
                        )),
                "lifecycle bus should publish live module discovery through ready events");
        require(lifecycleBus.events("echoabi-cycle-a").stream()
                        .map(EchoRuntimeModuleLifecycleEvent::lifecycle)
                        .toList()
                        .contains(EchoRuntimeModuleLifecycle.FAILED),
                "lifecycle bus should publish graph-failed modules");
        require(ledger.events("echoabi-field-generator").equals(List.of("load")),
                "live addon should execute load once");
        require(ledger.events("echoabi-reload-crash").equals(List.of("load")),
                "reload crash fixture should execute load once before reload");
        require(ledger.events("echoabi-crash-fixture").equals(List.of("load-attempt")),
                "crash fixture should execute only its failing load attempt");
        require(ledger.events("echoabi-dependent-on-crash").isEmpty(),
                "dependent addon should not execute after its required dependency failed");
        require(ledger.events("echoabi-partial-state-crash").equals(List.of("load-attempt")),
                "partial-state crash fixture should execute only its failing load attempt");
        require(ledger.events("echoabi-denied-content").equals(List.of("load-attempt")),
                "permission-denied fixture should execute only its denied load attempt");
        require(ledger.events("echoabi-denied-registry-access").equals(List.of("load-attempt")),
                "raw registry access fixture should execute only its denied load attempt");
        require(ledger.events("echoabi-unknown-permission").isEmpty(),
                "unknown-permission fixture should not execute its entrypoint");
        require(ledger.events("echoabi-classpath-escape").isEmpty(),
                "classpath escape fixture should not execute its entrypoint");
        require(ledger.events("echoabi-incompatible-core").isEmpty(),
                "incompatible dependency fixture should not execute its entrypoint");
        require(ledger.events("echoabi-service-provider").equals(List.of("load")),
                "service provider should execute load once");
        require(ledger.events("echoabi-service-consumer").equals(List.of("load")),
                "service consumer should execute load once");
        require(ledger.events("echoabi-optional-provider").equals(List.of("load")),
                "optional provider should execute load once");
        require(ledger.events("echoabi-optional-consumer").equals(List.of("load")),
                "optional consumer should execute load once");
        require(ledger.events("echoabi-optional-version-warning").equals(List.of("load")),
                "optional version warning addon should execute load once");
        require(ledger.events("echoabi-unload-crash").equals(List.of("load")),
                "unload crash addon should execute load once before unload");
        require(ledger.events("echoabi-adaptercore-addon").equals(List.of("adaptercore-activate")),
                "AdapterCore entrypoint should execute activate once");
        require(ledger.events("echoabi-denied-service-export").equals(List.of("load-attempt")),
                "permission-denied service exporter should execute only its denied load attempt");
        require(ledger.events("echoabi-cycle-a").isEmpty(),
                "cyclic dependency module A should not execute its entrypoint");
        require(ledger.events("echoabi-cycle-b").isEmpty(),
                "cyclic dependency module B should not execute its entrypoint");
        require(ledger.activatedContent().contains("echoabi:field_generator"),
                "live addon should activate content through the runtime service contract");
        EchoRuntimeModuleContentActivationRegistry contentRegistry =
                services.require(EchoRuntimeModuleContentActivationRegistry.class);
        EchoRuntimeModuleServiceExportRegistry serviceExportRegistry =
                services.require(EchoRuntimeModuleServiceExportRegistry.class);
        require(contentRegistry.activations("echoabi-field-generator").size() == 1,
                "live addon should record one permission-checked content activation");
        require(contentRegistry.activations("echoabi-adaptercore-addon").size() == 1,
                "AdapterCore entrypoint should record one permission-checked content activation");
        require(contentRegistry.activations("echoabi-denied-content").isEmpty(),
                "permission-denied addon should not activate content");
        require(contentRegistry.activations("echoabi-denied-registry-access").isEmpty(),
                "raw registry access addon should not bypass permission-checked content activation");
        require(contentRegistry.activations("echoabi-partial-state-crash").isEmpty(),
                "partial-state crash addon should have content activations revoked after load failure");
        require(serviceExportRegistry.findService("echoabi:calibration_service", OfferedService.class).isPresent(),
                "service provider should export a permission-checked service");
        require(serviceExportRegistry.findService("echoabi:adaptercore_service", OfferedService.class).isPresent(),
                "AdapterCore entrypoint should export a permission-checked service");
        require(serviceExportRegistry.findExport("echoabi:denied_service").isEmpty(),
                "permission-denied service exporter should not publish a service");
        require(serviceExportRegistry.findExport("echoabi:partial_state_service").isEmpty(),
                "partial-state crash addon should have service exports revoked after load failure");
        require(serviceExportRegistry.findService("echoabi:unload_crash_service", OfferedService.class).isPresent(),
                "unload crash addon should export a permission-checked service before unload");
        require(serviceExportRegistry.findService("echoabi:optional_service", OfferedService.class).isPresent(),
                "optional provider should export a permission-checked service");
        require(ledger.exportedServices().stream().sorted().toList().equals(List.of(
                        "echoabi:activated_content",
                        "echoabi:adaptercore_service",
                        "echoabi:calibration_service",
                        "echoabi:optional_service",
                        "echoabi:partial_state_service",
                        "echoabi:unload_crash_service"
                )),
                "service export attempts should be recorded exactly once per exporting module");
        require(ledger.importedServices("echoabi-service-consumer").equals(List.of("echoabi:calibration_service")),
                "service consumer should import the provider service exactly once");
        require(ledger.importedServices("echoabi-optional-consumer").equals(List.of("echoabi:optional_service")),
                "optional consumer should import the optional provider service exactly once");
        require(ledger.eventIndex("echoabi-service-provider", "load")
                        < ledger.eventIndex("echoabi-service-consumer", "load"),
                "dependency graph should load provider before consumer even when consumer id sorts first");
        require(ledger.eventIndex("echoabi-optional-provider", "load")
                        < ledger.eventIndex("echoabi-optional-consumer", "load"),
                "dependency graph should load present optional provider before optional consumer");
        require(ledger.isolatedClassLoaders().get("echoabi-field-generator") == Boolean.TRUE,
                "live addon should execute from an isolated module classloader");
        ActivatedContent activatedContent = serviceExportRegistry.findService(
                "echoabi:activated_content",
                ActivatedContent.class
        ).orElseThrow();
        require(activatedContent.contentId().equals("echoabi:field_generator"),
                "live addon should export activated content through the permission-checked service API");
        require(!diagnostics.diagnostics().isEmpty(),
                "crashing addon should emit a diagnostic");
        require(diagnostics.diagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.code().equals("echo.runtime.module.required_dependency_failed")
                                && "echoabi-dependent-on-crash".equals(diagnostic.attributes().get("moduleId"))),
                "dependent addon should emit a required dependency failure diagnostic");
        require(result.moduleGraph().issues().stream()
                        .anyMatch(issue -> issue.code().equals("ECHO-STANDALONE-MODULE-DEPENDENCY-VERSION-MISMATCH")
                                && "echoabi-incompatible-core".equals(issue.moduleId())),
                "incompatible dependency should produce a version mismatch graph issue");
        require(result.moduleGraph().issues().stream()
                        .filter(issue -> issue.code().equals("ECHO-STANDALONE-MODULE-DEPENDENCY-CYCLE"))
                        .map(issue -> issue.moduleId() == null ? "" : issue.moduleId())
                        .collect(java.util.stream.Collectors.toSet())
                        .containsAll(List.of("echoabi-cycle-a", "echoabi-cycle-b")),
                "cyclic dependencies should produce dependency cycle graph issues for both modules");
        require(result.moduleGraph().issues().stream()
                        .anyMatch(issue -> issue.code().equals("ECHO-STANDALONE-MODULE-OPTIONAL-DEPENDENCY-VERSION-MISMATCH")
                                && "echoabi-optional-version-warning".equals(issue.moduleId())),
                "optional dependency version mismatch should produce a non-blocking warning");
        require(result.moduleGraph().issues().stream()
                        .anyMatch(issue -> issue.code().equals("ECHO-STANDALONE-MODULE-PERMISSION-UNKNOWN")
                                && "echoabi-unknown-permission".equals(issue.moduleId())),
                "unknown descriptor permissions should produce a graph validation error");

        manager.reloadData(result, services);
        require(registry.trace("echoabi-field-generator").contains(EchoRuntimeModuleLifecycle.DATA_RELOADED),
                "live addon should receive the data reload lifecycle hook");
        require(lifecycleBus.events("echoabi-field-generator").stream()
                        .map(EchoRuntimeModuleLifecycleEvent::lifecycle)
                        .toList()
                        .contains(EchoRuntimeModuleLifecycle.DATA_RELOADED),
                "lifecycle bus should publish data reload events");
        require(ledger.events("echoabi-field-generator").equals(List.of("load", "reload")),
                "live addon should record load then reload");
        require(ledger.events("echoabi-service-provider").equals(List.of("load", "reload")),
                "service provider should record load then reload");
        require(ledger.events("echoabi-service-consumer").equals(List.of("load", "reload")),
                "service consumer should record load then reload");
        require(ledger.events("echoabi-optional-provider").equals(List.of("load", "reload")),
                "optional provider should record load then reload");
        require(ledger.events("echoabi-optional-consumer").equals(List.of("load", "reload")),
                "optional consumer should record load then reload");
        require(ledger.events("echoabi-optional-version-warning").equals(List.of("load", "reload")),
                "optional version warning addon should record load then reload");
        require(ledger.events("echoabi-unload-crash").equals(List.of("load", "reload")),
                "unload crash addon should record load then reload");
        require(ledger.events("echoabi-adaptercore-addon").equals(List.of("adaptercore-activate", "adaptercore-reload")),
                "AdapterCore entrypoint should record activate then reload");
        require(registry.lifecycle("echoabi-reload-crash") == EchoRuntimeModuleLifecycle.FAILED,
                "reload crash addon should fail during data reload");
        require(ledger.events("echoabi-reload-crash").equals(List.of("load", "reload-attempt")),
                "reload crash fixture should record load and the failing reload attempt only");
        require(lifecycleBus.events("echoabi-reload-crash").stream()
                        .map(EchoRuntimeModuleLifecycleEvent::lifecycle)
                        .toList()
                        .contains(EchoRuntimeModuleLifecycle.FAILED),
                "lifecycle bus should publish reload failure event");

        int preUnloadServiceExportCount = serviceExportRegistry.snapshot().size();
        int preUnloadContentActivationCount = contentActivationCount(contentRegistry);
        int preUnloadLiveActivationCount = contentRegistry.activations("echoabi-field-generator").size();
        int preUnloadAdapterCoreActivationCount = contentRegistry.activations("echoabi-adaptercore-addon").size();
        int preUnloadUnloadCrashActivationCount = contentRegistry.activations("echoabi-unload-crash").size();
        String exportedServiceId = serviceExportRegistry.findExport("echoabi:calibration_service")
                .map(export -> export.serviceId())
                .orElse("");
        String adapterCoreExportedServiceId = serviceExportRegistry.findExport("echoabi:adaptercore_service")
                .map(export -> export.serviceId())
                .orElse("");
        String unloadCrashExportedServiceId = serviceExportRegistry.findExport("echoabi:unload_crash_service")
                .map(export -> export.serviceId())
                .orElse("");
        require(preUnloadServiceExportCount == 5,
                "successful modules should have five active service exports before unload");
        require(preUnloadContentActivationCount == 3,
                "successful modules should have three active content activations before unload");
        require(preUnloadUnloadCrashActivationCount == 1,
                "unload crash addon should have one active content activation before unload");

        manager.unload(result, services);
        require(registry.lifecycle("echoabi-field-generator") == EchoRuntimeModuleLifecycle.UNLOADED,
                "live addon should unload cleanly");
        require(registry.lifecycle("echoabi-service-provider") == EchoRuntimeModuleLifecycle.UNLOADED,
                "service provider should unload cleanly");
        require(registry.lifecycle("echoabi-service-consumer") == EchoRuntimeModuleLifecycle.UNLOADED,
                "service consumer should unload cleanly");
        require(registry.lifecycle("echoabi-optional-provider") == EchoRuntimeModuleLifecycle.UNLOADED,
                "optional provider should unload cleanly");
        require(registry.lifecycle("echoabi-optional-consumer") == EchoRuntimeModuleLifecycle.UNLOADED,
                "optional consumer should unload cleanly");
        require(registry.lifecycle("echoabi-optional-version-warning") == EchoRuntimeModuleLifecycle.UNLOADED,
                "optional version warning addon should unload cleanly");
        require(registry.lifecycle("echoabi-unload-crash") == EchoRuntimeModuleLifecycle.FAILED,
                "unload crash addon should fail safely during unload");
        require(registry.lifecycle("echoabi-adaptercore-addon") == EchoRuntimeModuleLifecycle.UNLOADED,
                "AdapterCore entrypoint should unload cleanly");
        require(lifecycleBus.events("echoabi-adaptercore-addon").stream()
                        .map(EchoRuntimeModuleLifecycleEvent::lifecycle)
                        .toList()
                        .contains(EchoRuntimeModuleLifecycle.UNLOADED),
                "lifecycle bus should publish AdapterCore unload events");
        require(ledger.lifecycleBusEventCount() == lifecycleBus.events().size(),
                "pre-registered lifecycle bus observer should receive every lifecycle event");
        require(ledger.events("echoabi-field-generator").equals(List.of("load", "reload", "unload")),
                "live addon should record load, reload, and unload");
        require(ledger.events("echoabi-reload-crash").equals(List.of("load", "reload-attempt")),
                "reload crash fixture should be detached and not receive unload");
        require(ledger.events("echoabi-service-provider").equals(List.of("load", "reload", "unload")),
                "service provider should record load, reload, and unload");
        require(ledger.events("echoabi-service-consumer").equals(List.of("load", "reload", "unload")),
                "service consumer should record load, reload, and unload");
        require(ledger.events("echoabi-optional-provider").equals(List.of("load", "reload", "unload")),
                "optional provider should record load, reload, and unload");
        require(ledger.events("echoabi-optional-consumer").equals(List.of("load", "reload", "unload")),
                "optional consumer should record load, reload, and unload");
        require(ledger.events("echoabi-optional-version-warning").equals(List.of("load", "reload", "unload")),
                "optional version warning addon should record load, reload, and unload");
        require(ledger.events("echoabi-unload-crash").equals(List.of("load", "reload", "unload-attempt")),
                "unload crash addon should record load, reload, and failing unload attempt");
        require(ledger.eventIndex("echoabi-service-consumer", "unload")
                        < ledger.eventIndex("echoabi-service-provider", "unload"),
                "reverse dependency unload should unload consumer before provider");
        require(ledger.eventIndex("echoabi-optional-consumer", "unload")
                        < ledger.eventIndex("echoabi-optional-provider", "unload"),
                "reverse dependency unload should unload optional consumer before optional provider");
        require(ledger.events("echoabi-adaptercore-addon").equals(List.of(
                        "adaptercore-activate",
                        "adaptercore-reload",
                        "adaptercore-deactivate"
                )),
                "AdapterCore entrypoint should record activate, reload, and deactivate");
        require(serviceExportRegistry.snapshot().isEmpty(),
                "all module service exports should be revoked after unload");
        require(contentActivationCount(contentRegistry) == 0,
                "all module content activations should be deactivated after unload");
        require(diagnostics.diagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.code().equals("echo.runtime.module.execution_failed")
                                && "echoabi-unload-crash".equals(diagnostic.attributes().get("moduleId"))
                                && diagnostic.summary().equals("module unload failed")),
                "unload crash addon should emit an unload failure diagnostic");

        EchoRuntimeModuleSandboxPolicy sandboxPolicy = services.require(EchoRuntimeModuleSandboxPolicy.class);
        require(!sandboxPolicy.descriptorOnly(), "ABI v1 smoke should not run in descriptor-only mode");
        require(sandboxPolicy.classloaderCreationAllowed(), "ABI v1 smoke should allow classloader creation");
        require(sandboxPolicy.moduleCodeExecutionAllowed(), "ABI v1 smoke should allow module code execution");

        writeReport(
                registry,
                result,
                diagnostics,
                ledger,
                activatedContent,
                contentRegistry,
                serviceExportRegistry,
                preUnloadServiceExportCount,
                preUnloadContentActivationCount,
                preUnloadLiveActivationCount,
                preUnloadAdapterCoreActivationCount,
                preUnloadUnloadCrashActivationCount,
                exportedServiceId,
                adapterCoreExportedServiceId,
                unloadCrashExportedServiceId,
                lifecycleBus
        );
        System.out.println("native-loader-abi-v1 smoke PASS loaded=9 failed=13 reload=8 unload=7");
    }

    private static void writeReport(
            EchoRuntimeModuleRegistry registry,
            EchoRuntimeModuleRuntimeResult result,
            EchoRuntimeLogBridge diagnostics,
            ModuleActivationLedger ledger,
            ActivatedContent activatedContent,
            EchoRuntimeModuleContentActivationRegistry contentRegistry,
            EchoRuntimeModuleServiceExportRegistry serviceExportRegistry,
            int preUnloadServiceExportCount,
            int preUnloadContentActivationCount,
            int preUnloadLiveActivationCount,
            int preUnloadAdapterCoreActivationCount,
            int preUnloadUnloadCrashActivationCount,
            String exportedServiceId,
            String adapterCoreExportedServiceId,
            String unloadCrashExportedServiceId,
            EchoRuntimeModuleLifecycleBus lifecycleBus
    ) throws IOException {
        Path reportPath = Path.of("reports/echo/standalone/native-loader-abi-v1-smoke.json");
        Files.createDirectories(reportPath.getParent());
        String moduleId = "echoabi-field-generator";
        String reloadCrashId = "echoabi-reload-crash";
        String crashId = "echoabi-crash-fixture";
        String dependentCrashId = "echoabi-dependent-on-crash";
        String partialStateCrashId = "echoabi-partial-state-crash";
        String deniedId = "echoabi-denied-content";
        String deniedRegistryAccessId = "echoabi-denied-registry-access";
        String unknownPermissionId = "echoabi-unknown-permission";
        String classPathEscapeId = "echoabi-classpath-escape";
        String incompatibleId = "echoabi-incompatible-core";
        String serviceProviderId = "echoabi-service-provider";
        String serviceConsumerId = "echoabi-service-consumer";
        String optionalProviderId = "echoabi-optional-provider";
        String optionalConsumerId = "echoabi-optional-consumer";
        String optionalWarningId = "echoabi-optional-version-warning";
        String unloadCrashId = "echoabi-unload-crash";
        String adapterCoreId = "echoabi-adaptercore-addon";
        String deniedExportId = "echoabi-denied-service-export";
        String cycleAId = "echoabi-cycle-a";
        String cycleBId = "echoabi-cycle-b";
        long versionMismatchIssues = result.moduleGraph().issues().stream()
                .filter(issue -> issue.code().equals("ECHO-STANDALONE-MODULE-DEPENDENCY-VERSION-MISMATCH"))
                .count();
        long dependencyCycleIssues = result.moduleGraph().issues().stream()
                .filter(issue -> issue.code().equals("ECHO-STANDALONE-MODULE-DEPENDENCY-CYCLE"))
                .count();
        long optionalVersionMismatchIssues = result.moduleGraph().issues().stream()
                .filter(issue -> issue.code().equals("ECHO-STANDALONE-MODULE-OPTIONAL-DEPENDENCY-VERSION-MISMATCH"))
                .count();
        long unknownPermissionIssues = result.moduleGraph().issues().stream()
                .filter(issue -> issue.code().equals("ECHO-STANDALONE-MODULE-PERMISSION-UNKNOWN"))
                .count();
        String json = """
                {
                  "schema": "echo.standalone.native_loader_abi_v1_smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "summary": "Executable Native Loader ABI v1 smoke loaded raw and AdapterCore entrypoint addons in isolated classloaders, confined module classpaths to module roots, enforced dependency version ranges, dependency cycles, required-dependency load-failure propagation, and descriptor permission catalog validation, published lifecycle bus events, activated content through permission-checked runtime APIs, exported and imported module-scoped services through permission-checked APIs, revoked module state after partial load failure and unload, blocked raw registry bypass attempts, denied unknown/unpermitted activation/export safely, failed load and reload crashes safely, detached reload-failed modules before unload, reloaded data, and unloaded in reverse dependency order.",
                  "descriptorSchemaId": "%s",
                  "descriptorSchemaSources": %s,
                  "descriptorSchemaFields": %s,
                  "descriptorExecutableAbiV1Fields": %s,
                  "descriptorSchemaCoversExecutableAbiV1": %s,
                  "descriptorFieldTypes": %s,
                  "loadedModule": "%s",
                  "reloadCrashModule": "%s",
                  "adapterCoreEntrypointModule": "%s",
                  "serviceProviderModule": "%s",
                  "serviceConsumerModule": "%s",
                  "optionalProviderModule": "%s",
                  "optionalConsumerModule": "%s",
                  "optionalVersionWarningModule": "%s",
                  "unloadCrashModule": "%s",
                  "failedModule": "%s",
                  "dependentOnFailedDependencyModule": "%s",
                  "partialStateCrashModule": "%s",
                  "permissionDeniedModule": "%s",
                  "permissionDeniedRegistryAccessModule": "%s",
                  "unknownPermissionModule": "%s",
                  "classpathEscapeModule": "%s",
                  "permissionDeniedServiceExportModule": "%s",
                  "incompatibleDependencyModule": "%s",
                  "cyclicDependencyModules": %s,
                  "liveLifecycle": %s,
                  "reloadCrashLifecycle": %s,
                  "adapterCoreEntrypointLifecycle": %s,
                  "serviceProviderLifecycle": %s,
                  "serviceConsumerLifecycle": %s,
                  "optionalProviderLifecycle": %s,
                  "optionalConsumerLifecycle": %s,
                  "optionalVersionWarningLifecycle": %s,
                  "unloadCrashLifecycle": %s,
                  "liveLifecycleBus": %s,
                  "adapterCoreLifecycleBus": %s,
                  "cyclicDependencyLifecycleBus": %s,
                  "liveTrace": %s,
                  "failedLifecycle": "%s",
                  "dependentOnFailedDependencyLifecycle": "%s",
                  "partialStateCrashLifecycle": "%s",
                  "unloadCrashFinalLifecycle": "%s",
                  "reloadCrashFinalLifecycle": "%s",
                  "permissionDeniedLifecycle": "%s",
                  "permissionDeniedRegistryAccessLifecycle": "%s",
                  "unknownPermissionLifecycle": "%s",
                  "classpathEscapeLifecycle": "%s",
                  "permissionDeniedServiceExportLifecycle": "%s",
                  "incompatibleDependencyLifecycle": "%s",
                  "optionalProviderFinalLifecycle": "%s",
                  "optionalConsumerFinalLifecycle": "%s",
                  "optionalVersionWarningFinalLifecycle": "%s",
                  "cyclicDependencyLifecycles": %s,
                  "incompatibleDependencyExecuted": %s,
                  "dependentOnFailedDependencyExecuted": %s,
                  "partialStateCrashExecuted": %s,
                  "partialStateCrashContentRevoked": %s,
                  "partialStateCrashServiceRevoked": %s,
                  "unloadCrashContentRevoked": %s,
                  "unloadCrashServiceRevoked": %s,
                  "reloadCrashUnloaded": %s,
                  "classpathEscapeExecuted": %s,
                  "unknownPermissionExecuted": %s,
                  "cyclicDependencyExecuted": %s,
                  "dependencyVersionMismatchIssues": %d,
                  "optionalDependencyVersionMismatchIssues": %d,
                  "unknownPermissionIssues": %d,
                  "dependencyCycleIssues": %d,
                  "classloaderIsolated": %s,
                  "activatedContent": "%s",
                  "adapterCoreActivatedContent": "%s",
                  "serviceExportedContent": "%s",
                  "permissionCheckedActivationCount": %d,
                  "adapterCoreActivationCount": %d,
                  "deniedActivationCount": %d,
                  "deniedRegistryAccessActivationCount": %d,
                  "permissionCheckedServiceExportCount": %d,
                  "permissionCheckedServiceImportCount": %d,
                  "adapterCoreServiceExportCount": %d,
                  "exportedServiceId": "%s",
                  "adapterCoreExportedServiceId": "%s",
                  "unloadCrashExportedServiceId": "%s",
                  "importedServiceIds": %s,
                  "optionalImportedServiceIds": %s,
                  "dependencyOrderedServiceLoad": %s,
                  "optionalDependencyOrderedLoad": %s,
                  "missingOptionalDependencyAllowed": %s,
                  "optionalVersionWarningNonBlocking": %s,
                  "reverseDependencyUnload": %s,
                  "reverseOptionalDependencyUnload": %s,
                  "deniedServiceExportCount": %d,
                  "requiredDependencyFailureDiagnostic": %s,
                  "unloadCrashDiagnostic": %s,
                  "preUnloadServiceExportCount": %d,
                  "preUnloadContentActivationCount": %d,
                  "preUnloadUnloadCrashActivationCount": %d,
                  "postUnloadServiceExportCount": %d,
                  "postUnloadContentActivationCount": %d,
                  "lifecycleBusEventCount": %d,
                  "lifecycleBusObserverEventCount": %d,
                  "diagnosticCount": %d
                }
                """.formatted(
                EchoRuntimeModuleDescriptorSchema.SCHEMA_ID,
                jsonArray(EchoRuntimeModuleDescriptorSchema.DESCRIPTOR_SOURCES),
                jsonArray(EchoRuntimeModuleDescriptorSchema.allFields()),
                jsonArray(EchoRuntimeModuleDescriptorSchema.EXECUTABLE_ABI_V1_FIELDS),
                EchoRuntimeModuleDescriptorSchema.coversExecutableAbiV1(),
                jsonFieldTypes(EchoRuntimeModuleDescriptorSchema.FIELD_TYPES),
                moduleId,
                reloadCrashId,
                adapterCoreId,
                serviceProviderId,
                serviceConsumerId,
                optionalProviderId,
                optionalConsumerId,
                optionalWarningId,
                unloadCrashId,
                crashId,
                dependentCrashId,
                partialStateCrashId,
                deniedId,
                deniedRegistryAccessId,
                unknownPermissionId,
                classPathEscapeId,
                deniedExportId,
                incompatibleId,
                jsonArray(List.of(cycleAId, cycleBId)),
                jsonArray(ledger.events(moduleId)),
                jsonArray(ledger.events(reloadCrashId)),
                jsonArray(ledger.events(adapterCoreId)),
                jsonArray(ledger.events(serviceProviderId)),
                jsonArray(ledger.events(serviceConsumerId)),
                jsonArray(ledger.events(optionalProviderId)),
                jsonArray(ledger.events(optionalConsumerId)),
                jsonArray(ledger.events(optionalWarningId)),
                jsonArray(ledger.events(unloadCrashId)),
                jsonArray(lifecycleNames(lifecycleBus, moduleId)),
                jsonArray(lifecycleNames(lifecycleBus, adapterCoreId)),
                jsonObject(Map.of(
                        cycleAId, String.join(",", lifecycleNames(lifecycleBus, cycleAId)),
                        cycleBId, String.join(",", lifecycleNames(lifecycleBus, cycleBId))
                )),
                jsonArray(registry.trace(moduleId).stream().map(Enum::name).toList()),
                registry.lifecycle(crashId).name(),
                registry.lifecycle(dependentCrashId).name(),
                registry.lifecycle(partialStateCrashId).name(),
                registry.lifecycle(unloadCrashId).name(),
                registry.lifecycle(reloadCrashId).name(),
                registry.lifecycle(deniedId).name(),
                registry.lifecycle(deniedRegistryAccessId).name(),
                registry.lifecycle(unknownPermissionId).name(),
                registry.lifecycle(classPathEscapeId).name(),
                registry.lifecycle(deniedExportId).name(),
                registry.lifecycle(incompatibleId).name(),
                registry.lifecycle(optionalProviderId).name(),
                registry.lifecycle(optionalConsumerId).name(),
                registry.lifecycle(optionalWarningId).name(),
                jsonObject(Map.of(cycleAId, registry.lifecycle(cycleAId).name(), cycleBId, registry.lifecycle(cycleBId).name())),
                !ledger.events(incompatibleId).isEmpty(),
                !ledger.events(dependentCrashId).isEmpty(),
                !ledger.events(partialStateCrashId).isEmpty(),
                contentRegistry.activations(partialStateCrashId).isEmpty(),
                serviceExportRegistry.findExport("echoabi:partial_state_service").isEmpty(),
                contentRegistry.activations(unloadCrashId).isEmpty(),
                serviceExportRegistry.findExport("echoabi:unload_crash_service").isEmpty(),
                ledger.events(reloadCrashId).contains("should-not-unload-after-reload-failure"),
                !ledger.events(classPathEscapeId).isEmpty(),
                !ledger.events(unknownPermissionId).isEmpty(),
                !ledger.events(cycleAId).isEmpty() || !ledger.events(cycleBId).isEmpty(),
                versionMismatchIssues,
                optionalVersionMismatchIssues,
                unknownPermissionIssues,
                dependencyCycleIssues,
                ledger.isolatedClassLoaders().get(moduleId),
                ledger.activatedContent().contains(activatedContent.contentId()) ? activatedContent.contentId() : "",
                ledger.activatedContent().contains("echoabi:adaptercore_marker") ? "echoabi:adaptercore_marker" : "",
                activatedContent.contentId(),
                preUnloadLiveActivationCount,
                preUnloadAdapterCoreActivationCount,
                contentRegistry.activations(deniedId).size(),
                contentRegistry.activations(deniedRegistryAccessId).size(),
                preUnloadServiceExportCount,
                ledger.importedServices(serviceConsumerId).size() + ledger.importedServices(optionalConsumerId).size(),
                adapterCoreExportedServiceId.isBlank() ? 0 : 1,
                exportedServiceId,
                adapterCoreExportedServiceId,
                unloadCrashExportedServiceId,
                jsonArray(ledger.importedServices(serviceConsumerId)),
                jsonArray(ledger.importedServices(optionalConsumerId)),
                ledger.eventIndex(serviceProviderId, "load") < ledger.eventIndex(serviceConsumerId, "load"),
                ledger.eventIndex(optionalProviderId, "load") < ledger.eventIndex(optionalConsumerId, "load"),
                registry.lifecycle(optionalConsumerId) == EchoRuntimeModuleLifecycle.UNLOADED,
                registry.lifecycle(optionalWarningId) == EchoRuntimeModuleLifecycle.UNLOADED
                        && optionalVersionMismatchIssues == 1,
                ledger.eventIndex(serviceConsumerId, "unload") < ledger.eventIndex(serviceProviderId, "unload"),
                ledger.eventIndex(optionalConsumerId, "unload") < ledger.eventIndex(optionalProviderId, "unload"),
                serviceExportRegistry.findExport("echoabi:denied_service").isPresent() ? 1 : 0,
                diagnostics.diagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.code().equals("echo.runtime.module.required_dependency_failed")
                                && dependentCrashId.equals(diagnostic.attributes().get("moduleId"))),
                diagnostics.diagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.code().equals("echo.runtime.module.execution_failed")
                                && unloadCrashId.equals(diagnostic.attributes().get("moduleId"))
                                && diagnostic.summary().equals("module unload failed")),
                preUnloadServiceExportCount,
                preUnloadContentActivationCount,
                preUnloadUnloadCrashActivationCount,
                serviceExportRegistry.snapshot().size(),
                contentActivationCount(contentRegistry),
                lifecycleBus.events().size(),
                ledger.lifecycleBusEventCount(),
                diagnostics.diagnostics().size()
        );
        Files.writeString(reportPath, json, StandardCharsets.UTF_8);
    }

    private static List<String> lifecycleNames(EchoRuntimeModuleLifecycleBus lifecycleBus, String moduleId) {
        return lifecycleBus.events(moduleId).stream()
                .map(event -> event.lifecycle().name())
                .toList();
    }

    private static int contentActivationCount(EchoRuntimeModuleContentActivationRegistry registry) {
        return registry.snapshot().values().stream()
                .mapToInt(List::size)
                .sum();
    }

    private static String jsonArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String jsonObject(Map<String, String> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "\"" + escapeJson(entry.getKey()) + "\": \"" + escapeJson(entry.getValue()) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "{", "}"));
    }

    private static String jsonFieldTypes(Map<String, String> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "{\"field\": \"" + escapeJson(entry.getKey())
                        + "\", \"type\": \"" + escapeJson(entry.getValue()) + "\"}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void compileSource(Path classesRoot, String relativeSourcePath, String source) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JDK compiler is required for native loader ABI smoke");
        }
        Path sourcePath = classesRoot.resolve(relativeSourcePath);
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, source);
        int exitCode = compiler.run(
                null,
                null,
                null,
                "-classpath",
                System.getProperty("java.class.path"),
                "-d",
                classesRoot.toString(),
                sourcePath.toString()
        );
        if (exitCode != 0) {
            throw new IllegalStateException("Fixture compilation failed: " + sourcePath);
        }
    }

    private static void writeDescriptor(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public record ActivatedContent(String moduleId, String contentId) {
    }

    public record OfferedService(String moduleId, String serviceId, String value) {
    }

    public static final class ModuleActivationLedger {
        private final Map<String, List<String>> events = new LinkedHashMap<>();
        private final Map<String, Boolean> isolatedClassLoaders = new LinkedHashMap<>();
        private final Map<String, List<String>> importedServices = new LinkedHashMap<>();
        private final List<String> activatedContent = new ArrayList<>();
        private final List<String> exportedServices = new ArrayList<>();
        private final List<String> eventOrder = new ArrayList<>();
        private final List<EchoRuntimeModuleLifecycleEvent> lifecycleBusEvents = new ArrayList<>();

        public synchronized void record(String moduleId, String event) {
            events.computeIfAbsent(moduleId, ignored -> new ArrayList<>()).add(event);
            eventOrder.add(moduleId + ":" + event);
        }

        public synchronized void recordClassLoaderIsolation(String moduleId, boolean isolated) {
            isolatedClassLoaders.put(moduleId, isolated);
        }

        public synchronized void activate(String contentId) {
            activatedContent.add(contentId);
        }

        public synchronized void recordServiceExport(String serviceId) {
            exportedServices.add(serviceId);
        }

        public synchronized void recordServiceImport(String moduleId, String serviceId) {
            importedServices.computeIfAbsent(moduleId, ignored -> new ArrayList<>()).add(serviceId);
        }

        public synchronized void recordLifecycleBusEvent(EchoRuntimeModuleLifecycleEvent event) {
            lifecycleBusEvents.add(event);
        }

        public synchronized List<String> events(String moduleId) {
            return List.copyOf(events.getOrDefault(moduleId, List.of()));
        }

        public synchronized int eventIndex(String moduleId, String event) {
            return eventOrder.indexOf(moduleId + ":" + event);
        }

        public synchronized Map<String, Boolean> isolatedClassLoaders() {
            return Map.copyOf(isolatedClassLoaders);
        }

        public synchronized List<String> activatedContent() {
            return List.copyOf(activatedContent);
        }

        public synchronized List<String> exportedServices() {
            return List.copyOf(exportedServices);
        }

        public synchronized List<String> importedServices(String moduleId) {
            return List.copyOf(importedServices.getOrDefault(moduleId, List.of()));
        }

        public synchronized int lifecycleBusEventCount() {
            return lifecycleBusEvents.size();
        }
    }
}
