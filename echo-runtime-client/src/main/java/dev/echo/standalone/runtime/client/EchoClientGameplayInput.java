package dev.echo.standalone.runtime.client;

interface EchoClientGameplayInput {
    int selectedHotbarSlot(int current);

    boolean consumeBreak();

    boolean isCursorLocked();

    boolean consumePlace();
}
