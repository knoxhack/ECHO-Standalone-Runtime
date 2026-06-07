package dev.echo.standalone.runtime.render;

public enum EchoVoxelRenderBackendTarget {
    OPENGL("opengl");

    private final String id;

    EchoVoxelRenderBackendTarget(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
