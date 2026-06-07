package dev.echo.standalone.runtime.client;

record EchoClientUiVisualPlan(
        boolean mainMenuPanorama,
        int panoramaLayerCount,
        int panoramaSeed,
        String loadingTipKey,
        String loadingTip,
        String screenCoreRouteId
) {
    EchoClientUiVisualPlan {
        panoramaLayerCount = Math.max(0, panoramaLayerCount);
        loadingTipKey = loadingTipKey == null ? "" : loadingTipKey;
        loadingTip = loadingTip == null ? "" : loadingTip;
        screenCoreRouteId = screenCoreRouteId == null ? "" : screenCoreRouteId;
    }

    boolean loadingTipVisible() {
        return !loadingTip.isBlank();
    }
}
