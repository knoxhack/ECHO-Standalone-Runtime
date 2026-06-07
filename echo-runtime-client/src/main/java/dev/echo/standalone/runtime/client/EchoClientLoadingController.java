package dev.echo.standalone.runtime.client;

final class EchoClientLoadingController {
    private static final Stage[] SAVED_WORLD_STAGES = {
            new Stage(EchoClientGameState.MOD_SCAN, "Scanning AdapterCore modules", 0.25D),
            new Stage(EchoClientGameState.LOADING_ASSETS, "Loading resource packs", 0.35D),
            new Stage(EchoClientGameState.LOADING_DATA, "Validating save manifest", 0.35D),
            new Stage(EchoClientGameState.LOADING_WORLD, "Restoring chunks and player state", 0.45D)
    };

    private final EchoClientWorldPresentation presentation;
    private int index;
    private double elapsedInStage;
    private boolean active;
    private Stage[] stages;
    private String detail;

    EchoClientLoadingController() {
        this(EchoClientWorldTemplates.defaultTemplate().presentation());
    }

    EchoClientLoadingController(EchoClientWorldPresentation presentation) {
        this.presentation = presentation == null ? EchoClientWorldPresentation.generic() : presentation;
        this.stages = newWorldStages();
        this.detail = this.presentation.loadingInitialDetail();
    }

    void startNewWorld(String seedText) {
        String cleanSeed = seedText == null || seedText.isBlank() ? "42" : seedText.trim();
        start(newWorldStages(), presentation.newWorldDetail(cleanSeed));
    }

    void startNewWorld(String seedText, String worldName) {
        String cleanSeed = seedText == null || seedText.isBlank() ? "42" : seedText.trim();
        String cleanName = worldName == null || worldName.isBlank() ? "New World" : worldName.trim();
        start(newWorldStages(), cleanName + " | " + presentation.newWorldDetail(cleanSeed));
    }

    void startSavedWorld(String slotLabel) {
        String cleanSlot = slotLabel == null || slotLabel.isBlank() ? "Latest saved world" : slotLabel.trim();
        start(SAVED_WORLD_STAGES, cleanSlot);
    }

    private void start(Stage[] stages, String detail) {
        this.stages = stages == null || stages.length == 0 ? newWorldStages() : stages;
        this.detail = detail == null ? "" : detail;
        index = 0;
        elapsedInStage = 0.0D;
        active = true;
    }

    boolean active() {
        return active;
    }

    boolean update(double dt) {
        if (!active) {
            return false;
        }
        elapsedInStage += Math.max(0.0D, dt);
        if (elapsedInStage >= current().durationSeconds()) {
            index++;
            elapsedInStage = 0.0D;
            if (index >= stages.length) {
                active = false;
                return true;
            }
        }
        return false;
    }

    EchoClientGameState state() {
        return active ? current().state() : EchoClientGameState.IN_GAME;
    }

    String label() {
        return active ? current().label() : "Entering world";
    }

    String detail() {
        return detail;
    }

    String tip() {
        return switch (state()) {
            case MOD_SCAN -> "TIP ADAPTERCORE MODULES LOAD FIRST";
            case LOADING_ASSETS -> "TIP RESOURCE PACKS SHAPE MODELS";
            case LOADING_DATA -> "TIP REGISTRIES LOCK SAVE IDS";
            case LOADING_WORLD -> "TIP CHUNKS RESTORE CAMP STATE";
            default -> "TIP ASHFALL SAVES STAY LOCAL";
        };
    }

    String tipKey() {
        return switch (state()) {
            case MOD_SCAN -> "loading.mod_scan";
            case LOADING_ASSETS -> "loading.assets";
            case LOADING_DATA -> "loading.data";
            case LOADING_WORLD -> "loading.world";
            default -> "loading.complete";
        };
    }

    double progress() {
        if (!active) {
            return 1.0D;
        }
        double stageBase = index / (double) stages.length;
        double stagePart = Math.min(1.0D, elapsedInStage / current().durationSeconds()) / stages.length;
        return stageBase + stagePart;
    }

    private Stage current() {
        return stages[Math.min(index, stages.length - 1)];
    }

    private Stage[] newWorldStages() {
        return new Stage[] {
                new Stage(EchoClientGameState.MOD_SCAN, "Scanning AdapterCore modules", 0.35D),
                new Stage(EchoClientGameState.LOADING_ASSETS, "Loading resource packs", 0.45D),
                new Stage(EchoClientGameState.LOADING_DATA, "Building runtime registries", 0.45D),
                new Stage(EchoClientGameState.LOADING_WORLD, presentation.newWorldGenerationLabel(), 0.55D)
        };
    }

    private record Stage(EchoClientGameState state, String label, double durationSeconds) {
    }
}
