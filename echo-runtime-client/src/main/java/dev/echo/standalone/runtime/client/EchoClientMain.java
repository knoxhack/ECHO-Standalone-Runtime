package dev.echo.standalone.runtime.client;

import org.lwjgl.Version;

/**
 * Entry point for the standalone LWJGL/OpenGL client (Path B).
 */
public final class EchoClientMain {
    public static void main(String[] args) {
        System.out.println("[echo-client] starting ECHO standalone client (LWJGL " + Version.getVersion() + ")");
        EchoClientEngine engine = new EchoClientEngine();
        try {
            engine.start();
        } finally {
            engine.close();
        }
        System.out.println("[echo-client] shut down cleanly");
    }
}
