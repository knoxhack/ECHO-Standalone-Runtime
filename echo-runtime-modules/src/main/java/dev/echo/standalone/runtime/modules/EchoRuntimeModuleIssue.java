package dev.echo.standalone.runtime.modules;

public record EchoRuntimeModuleIssue(String code, Severity severity, String moduleId, String summary) {
    public static EchoRuntimeModuleIssue error(String code, String moduleId, String summary) {
        return new EchoRuntimeModuleIssue(code, Severity.ERROR, moduleId, summary);
    }

    public static EchoRuntimeModuleIssue warning(String code, String moduleId, String summary) {
        return new EchoRuntimeModuleIssue(code, Severity.WARNING, moduleId, summary);
    }

    public enum Severity {
        WARNING,
        ERROR
    }
}
