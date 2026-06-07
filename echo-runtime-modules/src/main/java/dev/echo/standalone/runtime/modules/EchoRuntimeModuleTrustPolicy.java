package dev.echo.standalone.runtime.modules;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public record EchoRuntimeModuleTrustPolicy(Set<String> allowedTrustLevels) {
    public EchoRuntimeModuleTrustPolicy {
        Objects.requireNonNull(allowedTrustLevels, "allowedTrustLevels");
        allowedTrustLevels = Set.copyOf(new TreeSet<>(allowedTrustLevels));
    }

    public static EchoRuntimeModuleTrustPolicy sandboxed() {
        return new EchoRuntimeModuleTrustPolicy(Set.of("sandboxed", "trusted", "official"));
    }

    public List<EchoRuntimeModuleIssue> validate(List<EchoRuntimeModuleDescriptor> descriptors) {
        java.util.ArrayList<EchoRuntimeModuleIssue> issues = new java.util.ArrayList<>();
        for (EchoRuntimeModuleDescriptor descriptor : descriptors) {
            if (!allowedTrustLevels.contains(descriptor.trust())) {
                issues.add(EchoRuntimeModuleIssue.error(
                        "ECHO-STANDALONE-MODULE-TRUST-DENIED",
                        descriptor.id(),
                        "Runtime module trust level is not allowed: " + descriptor.trust()
                ));
            }
            List<String> unknownPermissions = EchoRuntimeModulePermissionCatalog.unknownPermissions(descriptor);
            if (!unknownPermissions.isEmpty()) {
                issues.add(EchoRuntimeModuleIssue.error(
                        "ECHO-STANDALONE-MODULE-PERMISSION-UNKNOWN",
                        descriptor.id(),
                        "Runtime module declares unknown permissions: " + unknownPermissions
                ));
            }
        }
        return List.copyOf(issues);
    }
}
