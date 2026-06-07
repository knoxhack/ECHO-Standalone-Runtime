package dev.echo.standalone.runtime.modules;

public record EchoRuntimeModuleSandboxPolicy(
        boolean descriptorOnly,
        boolean classloaderCreationAllowed,
        boolean moduleCodeExecutionAllowed,
        boolean filesystemMutationAllowed
) {
    public static EchoRuntimeModuleSandboxPolicy descriptorOnlyPolicy() {
        return new EchoRuntimeModuleSandboxPolicy(true, false, false, false);
    }

    public static EchoRuntimeModuleSandboxPolicy executableAbiV1Policy() {
        return new EchoRuntimeModuleSandboxPolicy(false, true, true, false);
    }
}
