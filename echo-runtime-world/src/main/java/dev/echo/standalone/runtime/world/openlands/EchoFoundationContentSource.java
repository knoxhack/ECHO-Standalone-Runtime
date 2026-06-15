package dev.echo.standalone.runtime.world.openlands;

import java.nio.file.Path;

/**
 * Metadata describing a single Foundation payload file that was loaded.
 */
public record EchoFoundationContentSource(
        Path file,
        String schema,
        int movedOrder,
        String sourceNamespace,
        String canonicalOwner
) {
}
