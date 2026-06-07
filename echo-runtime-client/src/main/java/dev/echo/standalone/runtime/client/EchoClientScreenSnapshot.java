package dev.echo.standalone.runtime.client;

import java.util.List;

record EchoClientScreenSnapshot(
        EchoClientGameState state,
        EchoClientScreenKind kind,
        String title,
        String subtitle,
        List<EchoClientScreenOption> options,
        int selectedIndex,
        int scrollOffset,
        boolean loading,
        double loadingProgress,
        String tooltip,
        EchoClientModalSnapshot modal,
        EchoClientToastSnapshot toast,
        String footer,
        EchoClientSaveSlotThumbnailSnapshot saveSlotThumbnail
) {
    EchoClientScreenSnapshot {
        if (state == null) {
            state = EchoClientGameState.BOOT;
        }
        if (kind == null) {
            kind = EchoClientScreenKind.MAIN_MENU;
        }
        title = title == null ? "" : title;
        subtitle = subtitle == null ? "" : subtitle;
        options = options == null ? List.of() : List.copyOf(options);
        if (selectedIndex < 0 || selectedIndex >= options.size()) {
            selectedIndex = options.isEmpty() ? -1 : 0;
        }
        int maxScroll = Math.max(0, options.size() - 1);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
        loadingProgress = Math.max(0.0D, Math.min(1.0D, loadingProgress));
        tooltip = tooltip == null ? "" : tooltip;
        modal = modal == null ? EchoClientModalSnapshot.EMPTY : modal;
        toast = toast == null ? EchoClientToastSnapshot.EMPTY : toast;
        footer = footer == null ? "" : footer;
        saveSlotThumbnail = saveSlotThumbnail == null
                ? EchoClientSaveSlotThumbnailSnapshot.EMPTY
                : saveSlotThumbnail;
    }
}
