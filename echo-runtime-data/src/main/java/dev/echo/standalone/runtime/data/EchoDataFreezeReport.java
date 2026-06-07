package dev.echo.standalone.runtime.data;

public record EchoDataFreezeReport(
        EchoDataFreezePolicy policy,
        boolean frozen,
        int frozenRegistries,
        int frozenSupportingRegistries
) {
    public EchoDataFreezeReport {
        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }
        if (frozenRegistries < 0 || frozenSupportingRegistries < 0) {
            throw new IllegalArgumentException("frozen registry counts must not be negative");
        }
    }
}
