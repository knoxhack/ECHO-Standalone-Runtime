package dev.echo.standalone.runtime.modules;

import java.util.List;
import java.util.Objects;

public record EchoRuntimeModuleScanResult(
        List<EchoRuntimeModuleDescriptor> descriptors,
        List<EchoRuntimeModuleIssue> issues
) {
    public EchoRuntimeModuleScanResult {
        Objects.requireNonNull(descriptors, "descriptors");
        Objects.requireNonNull(issues, "issues");
        descriptors = List.copyOf(descriptors);
        issues = List.copyOf(issues);
    }
}
