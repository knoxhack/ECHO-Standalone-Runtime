package dev.echo.standalone.runtime.client;

record EchoClientSelectedItemUse(
        String action,
        String label
) {
    EchoClientSelectedItemUse {
        action = action == null ? "" : action.trim();
        label = label == null || label.isBlank() ? "Item" : label.trim();
    }

    static EchoClientSelectedItemUse none() {
        return new EchoClientSelectedItemUse("", "");
    }

    static EchoClientSelectedItemUse equipped(String label) {
        return new EchoClientSelectedItemUse("equip", label);
    }

    static EchoClientSelectedItemUse consumed(String label) {
        return new EchoClientSelectedItemUse("consume", label);
    }

    static EchoClientSelectedItemUse spawned(String label) {
        return new EchoClientSelectedItemUse("spawn", label);
    }

    static EchoClientSelectedItemUse interacted(String label) {
        return new EchoClientSelectedItemUse("interact", label);
    }

    static EchoClientSelectedItemUse bucketed(String label) {
        return new EchoClientSelectedItemUse("bucket", label);
    }

    boolean active() {
        return !action.isBlank();
    }

    String toastText() {
        if (!active()) {
            return "";
        }
        if (action.equals("equip")) {
            return "Equipped " + label;
        }
        if (action.equals("consume")) {
            return "Used " + label;
        }
        if (action.equals("spawn")) {
            return "Spawned " + label;
        }
        if (action.equals("interact")) {
            return "Interacted with " + label;
        }
        if (action.equals("bucket")) {
            return "Used " + label;
        }
        return label;
    }
}
