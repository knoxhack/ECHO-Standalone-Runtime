package dev.echo.standalone.runtime.modules;

public enum EchoRuntimeModuleLifecycle {
    DISCOVERED,
    DESCRIPTOR_VALIDATED,
    DEPENDENCIES_RESOLVED,
    FEATURES_RESOLVED,
    TRUST_VALIDATED,
    LOADED,
    SERVICES_BOUND,
    COMMON_INIT,
    CLIENT_INIT,
    SERVER_INIT,
    DATA_RELOADED,
    READY,
    UNLOADED,
    DISABLED,
    FAILED
}
