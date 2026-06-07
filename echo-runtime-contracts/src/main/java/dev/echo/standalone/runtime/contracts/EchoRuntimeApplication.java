package dev.echo.standalone.runtime.contracts;

public interface EchoRuntimeApplication {
    String applicationId();

    default void configure(EchoRuntimeContext context) {
    }

    EchoRuntime createRuntime(EchoRuntimeContext context);
}
