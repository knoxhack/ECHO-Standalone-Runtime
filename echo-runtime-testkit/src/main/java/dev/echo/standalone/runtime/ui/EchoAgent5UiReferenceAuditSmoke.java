package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5UiReferenceAuditSmoke {
    private static final List<String> REQUIRED_BEHAVIORS = List.of(
            "custom_main_menu",
            "terminal",
            "index",
            "lens_scanner",
            "hud",
            "mission_log",
            "notifications",
            "holomap",
            "wiki",
            "settings",
            "pause_flow",
            "death_recovery_screen"
    );

    private EchoAgent5UiReferenceAuditSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        List<Map<String, Object>> records = EchoAgent5UiReferenceAudit.records();
        List<String> screenIds = EchoAgent5UiReference.screenIds();
        List<String> checks = EchoAgent5UiReference.parityChecks();
        Map<String, Object> snapshot = source.snapshot();
        List<String> missingBehaviors = new ArrayList<>();
        List<String> missingScreens = new ArrayList<>();
        List<String> missingDataSources = new ArrayList<>();
        List<String> missingAcceptanceFeatures = new ArrayList<>();
        for (Map<String, Object> record : records) {
            String behavior = String.valueOf(record.get("behavior"));
            if (!REQUIRED_BEHAVIORS.contains(behavior)) {
                missingBehaviors.add(behavior);
            }
            if (!screenIds.contains(String.valueOf(record.get("screenId")))) {
                missingScreens.add(behavior);
            }
            if (!snapshot.containsKey(String.valueOf(record.get("dataSource")))) {
                missingDataSources.add(behavior);
            }
            if (!checks.contains(String.valueOf(record.get("acceptanceFeature")))) {
                missingAcceptanceFeatures.add(behavior);
            }
        }
        boolean behaviorCoverage = records.stream()
                .map(record -> String.valueOf(record.get("behavior")))
                .toList()
                .equals(REQUIRED_BEHAVIORS);
        boolean passed = behaviorCoverage
                && missingBehaviors.isEmpty()
                && missingScreens.isEmpty()
                && missingDataSources.isEmpty()
                && missingAcceptanceFeatures.isEmpty();

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("uiReferenceAuditSmokeClass", EchoAgent5UiReferenceAuditSmoke.class.getSimpleName());
        smoke.put("behaviors", REQUIRED_BEHAVIORS);
        smoke.put("records", records);
        smoke.put("behaviorCount", records.size());
        smoke.put("missingBehaviors", missingBehaviors);
        smoke.put("missingScreens", missingScreens);
        smoke.put("missingDataSources", missingDataSources);
        smoke.put("missingAcceptanceFeatures", missingAcceptanceFeatures);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
