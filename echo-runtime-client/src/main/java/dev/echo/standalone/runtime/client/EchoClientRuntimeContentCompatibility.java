package dev.echo.standalone.runtime.client;

import java.util.List;
import java.util.Map;

record EchoClientRuntimeContentCompatibility(
        boolean compatible,
        String detail,
        List<Map<String, Object>> savedRows
) {
    EchoClientRuntimeContentCompatibility {
        detail = detail == null ? "" : detail;
        savedRows = savedRows == null ? List.of() : List.copyOf(savedRows);
    }

    static EchoClientRuntimeContentCompatibility compatible(
            String detail,
            List<Map<String, Object>> savedRows
    ) {
        return new EchoClientRuntimeContentCompatibility(true, detail, savedRows);
    }

    static EchoClientRuntimeContentCompatibility incompatible(
            String detail,
            List<Map<String, Object>> savedRows
    ) {
        return new EchoClientRuntimeContentCompatibility(false, detail, savedRows);
    }
}
