package dev.echo.standalone.runtime.gameplay;

public record EchoFactionStanding(String factionId, String displayName, int reputation) {
    public EchoFactionStanding {
        factionId = EchoGameplayText.requireText(factionId, "factionId");
        displayName = EchoGameplayText.requireText(displayName, "displayName");
    }

    public boolean hostile() {
        return reputation < 0;
    }

    public EchoFactionStanding adjust(int delta) {
        return new EchoFactionStanding(factionId, displayName, reputation + delta);
    }
}
