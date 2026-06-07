package dev.echo.standalone.runtime.contracts;

public interface EchoRuntimeDiagnosticSink {
    void emit(EchoRuntimeDiagnostic diagnostic);

    default void info(String code, String layer, String summary) {
        emit(EchoRuntimeDiagnostic.of(code, EchoRuntimeDiagnosticSeverity.INFO, layer, summary));
    }

    default void warning(String code, String layer, String summary) {
        emit(EchoRuntimeDiagnostic.of(code, EchoRuntimeDiagnosticSeverity.WARNING, layer, summary));
    }

    static EchoRuntimeDiagnosticSink noop() {
        return diagnostic -> {
        };
    }
}
