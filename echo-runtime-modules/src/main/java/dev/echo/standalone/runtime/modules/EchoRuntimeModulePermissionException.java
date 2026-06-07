package dev.echo.standalone.runtime.modules;

public final class EchoRuntimeModulePermissionException extends RuntimeException {
    public EchoRuntimeModulePermissionException(String moduleId, String permission) {
        super("Module '" + moduleId + "' is missing required permission '" + permission + "'");
    }
}
