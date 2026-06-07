package dev.echo.standalone.runtime.ui;

import java.util.List;

public record EchoListView(
        String id,
        List<String> rows,
        int selectedIndex
) {
    public EchoListView {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        rows = List.copyOf(rows);
        if (selectedIndex < 0 || selectedIndex >= rows.size()) {
            selectedIndex = rows.isEmpty() ? 0 : rows.size() - 1;
        }
    }

    public String selectedRow() {
        return rows.isEmpty() ? "" : rows.get(selectedIndex);
    }
}
