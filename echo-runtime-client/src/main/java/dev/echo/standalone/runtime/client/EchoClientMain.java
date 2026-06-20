package dev.echo.standalone.runtime.client;

import org.lwjgl.Version;

/**
 * Entry point for the standalone LWJGL/OpenGL client (Path B).
 */
public final class EchoClientMain {
    public static void main(String[] args) {
        EchoClientLaunchContext launchContext = EchoClientLaunchContext.parse(args);
        launchContext.applySystemProperties();
        System.out.println("[echo-client] starting ECHO standalone client (LWJGL " + Version.getVersion() + ")");
        System.out.println("[echo-client] launch context " + launchContext.summaryLine());
        System.out.println("[echo-client] packRoot=" + launchContext.packRoot()
                + " modulesRoot=" + launchContext.modulesRoot());
        EchoClientEngine engine = new EchoClientEngine(launchContext);
        try {
            engine.start();
        } finally {
            engine.close();
        }
        System.out.println("[echo-client] shut down cleanly");
    }
}
