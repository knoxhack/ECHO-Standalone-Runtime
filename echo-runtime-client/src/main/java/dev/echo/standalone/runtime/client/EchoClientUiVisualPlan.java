package dev.echo.standalone.runtime.client;

record EchoClientUiVisualPlan(
        boolean mainMenuPanorama,
        int panoramaLayerCount,
        int panoramaTerrainLayers,
        int panoramaTerrainSteps,
        int panoramaAtmosphericStreaks,
        int panoramaLineBudget,
        int panoramaSeed,
        String loadingTipKey,
        String loadingTip,
        String screenCoreRouteId
) {
    EchoClientUiVisualPlan {
        panoramaLayerCount = Math.max(0, panoramaLayerCount);
        panoramaTerrainLayers = Math.max(0, panoramaTerrainLayers);
        panoramaTerrainSteps = Math.max(0, panoramaTerrainSteps);
        panoramaAtmosphericStreaks = Math.max(0, panoramaAtmosphericStreaks);
        panoramaLineBudget = Math.max(0, panoramaLineBudget);
        loadingTipKey = loadingTipKey == null ? "" : loadingTipKey;
        loadingTip = loadingTip == null ? "" : loadingTip;
        screenCoreRouteId = screenCoreRouteId == null ? "" : screenCoreRouteId;
    }

    boolean loadingTipVisible() {
        return !loadingTip.isBlank();
    }
}
