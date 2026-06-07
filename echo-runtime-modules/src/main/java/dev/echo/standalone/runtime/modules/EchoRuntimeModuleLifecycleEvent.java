package dev.echo.standalone.runtime.modules;

public record EchoRuntimeModuleLifecycleEvent(
        long sequence,
        String moduleId,
        EchoRuntimeModuleLifecycle lifecycle,
        String source
) {
    public EchoRuntimeModuleLifecycleEvent {
        if (sequence < 0) {
            throw new IllegalArgumentException("sequence must not be negative");
        }
        if (moduleId == null || moduleId.isBlank()) {
            throw new IllegalArgumentException("moduleId must not be blank");
        }
        if (lifecycle == null) {
            throw new IllegalArgumentException("lifecycle must not be null");
        }
        source = source == null ? "" : source;
    }
}
