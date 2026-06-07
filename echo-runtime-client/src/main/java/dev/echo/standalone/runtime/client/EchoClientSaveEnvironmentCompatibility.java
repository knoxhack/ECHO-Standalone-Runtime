package dev.echo.standalone.runtime.client;

record EchoClientSaveEnvironmentCompatibility(boolean compatible, String detail) {
    EchoClientSaveEnvironmentCompatibility {
        detail = detail == null ? "" : detail;
    }

    static EchoClientSaveEnvironmentCompatibility compatible(String detail) {
        return new EchoClientSaveEnvironmentCompatibility(true, detail);
    }

    static EchoClientSaveEnvironmentCompatibility incompatible(String detail) {
        return new EchoClientSaveEnvironmentCompatibility(false, detail);
    }
}
