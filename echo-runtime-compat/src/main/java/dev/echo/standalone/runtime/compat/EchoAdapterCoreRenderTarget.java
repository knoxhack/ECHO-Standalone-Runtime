package dev.echo.standalone.runtime.compat;

public enum EchoAdapterCoreRenderTarget {
    OPENGL("opengl");

    private final String adapterId;

    EchoAdapterCoreRenderTarget(String adapterId) {
        this.adapterId = adapterId;
    }

    public String adapterId() {
        return adapterId;
    }
}
