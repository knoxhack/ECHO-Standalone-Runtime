package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class StandaloneRuntimeHostFactory {
    private final EchoAdapterCoreStandaloneContentBridge bridge;

    public StandaloneRuntimeHostFactory(EchoAdapterCoreStandaloneContentBridge bridge) {
        this.bridge = Objects.requireNonNull(bridge, "bridge");
    }

    public static StandaloneRuntimeHostFactory ashfallLive() {
        return new StandaloneRuntimeHostFactory(EchoAdapterCoreStandaloneContentBridge.ashfallLive());
    }

    public StandaloneEchoRuntimeHost create(Path saveRoot) throws IOException {
        StandaloneRuntimeHostContext context = StandaloneRuntimeHostContext.ashfall(bridge, saveRoot);
        StandaloneEchoRuntimeHost host = new StandaloneEchoRuntimeHost(context, new StandaloneRuntimeMutationLedgerSink());
        StandaloneAdapterCoreNativeHostBridge.registerIfAdapterCorePresent(host);
        return host;
    }
}
