package dev.echo.standalone.runtime.render;

public enum EchoRenderLayer {
    BACKGROUND(0),
    WORLD(100),
    ENTITY(200),
    PARTICLE(300),
    UI(400),
    DIAGNOSTIC(500);

    private final int order;

    EchoRenderLayer(int order) {
        this.order = order;
    }

    public int order() {
        return order;
    }
}
