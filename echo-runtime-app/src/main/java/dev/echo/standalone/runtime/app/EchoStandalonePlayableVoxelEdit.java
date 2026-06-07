package dev.echo.standalone.runtime.app;

record EchoStandalonePlayableVoxelEdit(
        int x,
        int y,
        int z,
        String beforeBlockId,
        String afterBlockId
) {
    EchoStandalonePlayableVoxelEdit {
        beforeBlockId = EchoAppText.requireText(beforeBlockId, "beforeBlockId");
        afterBlockId = EchoAppText.requireText(afterBlockId, "afterBlockId");
    }
}
