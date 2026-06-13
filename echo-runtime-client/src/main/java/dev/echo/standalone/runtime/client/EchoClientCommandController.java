package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemCraftResult;

final class EchoClientCommandController {
    private final EchoClientRuntimeServices runtimeServices;
    private final EchoClientScreenController screens;
    private final EchoClientWorldSessionController worldSessions;
    private final EchoClientGameplayRuntimeController gameplayRuntime;
    private final Host host;

    EchoClientCommandController(
            EchoClientRuntimeServices runtimeServices,
            EchoClientScreenController screens,
            EchoClientWorldSessionController worldSessions,
            EchoClientGameplayRuntimeController gameplayRuntime,
            Host host
    ) {
        this.runtimeServices = runtimeServices;
        this.screens = screens;
        this.worldSessions = worldSessions;
        this.gameplayRuntime = gameplayRuntime;
        this.host = host;
    }

    boolean execute(EchoClientScreenCommand command) {
        if (screens.executeNavigationCommand(command, runtimeServices.hasContinuableSession())) {
            return true;
        }
        switch (command) {
            case START_NEW_GAME -> {
                return worldSessions.beginNewWorldLoad();
            }
            case CONTINUE_GAME -> {
                return worldSessions.beginContinueWorldLoad();
            }
            case RESUME_GAME -> {
                return worldSessions.resumeOrTitle();
            }
            case RESPAWN -> {
                if (worldSessions.respawn()) {
                    host.attachSession();
                    return true;
                }
                return false;
            }
            case SAVE_GAME -> {
                if (!runtimeServices.hasActiveWorld()) {
                    return false;
                }
                gameplayRuntime.captureMemorySave(host.captureSaveThumbnail());
                host.beginSaving();
                screens.showSaving();
                return true;
            }
            case QUIT_TO_TITLE -> {
                worldSessions.quitToTitle();
                host.unlockCursor();
                return true;
            }
            case QUIT_CLIENT -> {
                host.requestClose();
                return true;
            }
            case REFRESH_RESOURCE_PACKS -> {
                runtimeServices.refreshResourcePacks();
                host.reloadMinecraftAssets(runtimeServices.session() != null);
                screens.updateResourcePacks(runtimeServices.resourcePackSummaries(), runtimeServices.resourcePackError());
                screens.updateWorkbenchRecipes(
                        runtimeServices.workbenchRecipeSummaries(),
                        runtimeServices.workbenchRecipeError()
                );
                screens.showToast("Resource packs refreshed");
                return true;
            }
            case RELOAD_TEXTURE_ATLAS -> {
                host.reloadMinecraftAssets(runtimeServices.session() != null);
                screens.showToast("Texture atlas reloaded");
                return true;
            }
            case EXPORT_SUPPORT_BUNDLE -> {
                EchoClientSupportBundleResult result = runtimeServices.exportSupportBundle(
                        screens.snapshot(runtimeServices.hasContinuableSession()),
                        screens.clientSettings(),
                        screens.runtimeDiagnosticsSnapshot()
                );
                screens.updateSupportBundleResult(result);
                screens.showToast(result.toastLabel());
                return result.exported();
            }
            case BACKUP_SELECTED_WORLD -> {
                String slotId = screens.selectedManageSaveSlotId();
                String result = runtimeServices.backupAndPlanMigration(slotId);
                screens.updateSaveSlots(runtimeServices.saveSlotSummaries(), runtimeServices.saveSlotError());
                if (result.isBlank()) {
                    screens.showToast("Backup failed");
                } else {
                    screens.showToast("Backup ready: " + result);
                }
                return !result.isBlank();
            }
            case RENAME_SELECTED_WORLD -> {
                String slotId = screens.selectedManageSaveSlotId();
                String displayName = screens.saveSlotRenameText();
                boolean renamed = runtimeServices.renameSlot(slotId, displayName);
                screens.updateSaveSlots(runtimeServices.saveSlotSummaries(), runtimeServices.saveSlotError());
                if (renamed) {
                    screens.showToast("Renamed " + displayName);
                } else {
                    screens.showToast("Rename failed");
                }
                return renamed;
            }
            case DELETE_SELECTED_WORLD -> {
                String slotId = screens.selectedManageSaveSlotId();
                boolean deleted = runtimeServices.deleteSlot(slotId);
                screens.updateSaveSlots(runtimeServices.saveSlotSummaries(), runtimeServices.saveSlotError());
                if (deleted) {
                    screens.showToast("Deleted " + slotId);
                    if (!runtimeServices.hasContinuableSession()) {
                        screens.showMainMenu(false);
                    }
                } else {
                    screens.showToast("Delete failed");
                }
                return deleted;
            }
            case CRAFT_WORKBENCH_RECIPE -> {
                EchoItemCraftResult result = runtimeServices.craftWorkbenchRecipe(screens.selectedWorkbenchRecipeId());
                if (result != null && result.crafted()) {
                    runtimeServices.updateWorldSessionFromGameplay();
                    screens.updateWorkbenchRecipes(
                            runtimeServices.workbenchRecipeSummaries(),
                            runtimeServices.workbenchRecipeError()
                    );
                    screens.showToast("Crafted " + result.outputQuantity());
                    return true;
                }
                if (result != null) {
                    screens.showToast("Craft failed: " + result.reason());
                }
                return false;
            }
            case INSERT_MACHINE_INPUT -> {
                EchoClientMachineInputResult result =
                        runtimeServices.insertScrapIntoMachine(screens.selectedMachineInputTargetId());
                screens.updateTechSurfaceModel(runtimeServices.techSurfaceModel());
                if (result.success()) {
                    runtimeServices.updateWorldSessionFromGameplay();
                    screens.showToast("Inserted scrap into " + result.machineId());
                    return true;
                }
                screens.showToast("Machine insert failed: " + result.reason());
                return false;
            }
            case EXTRACT_MACHINE_OUTPUT -> {
                EchoClientMachineOutputResult result =
                        runtimeServices.extractCompressedScrapFromMachine(screens.selectedMachineOutputTargetId());
                screens.updateTechSurfaceModel(runtimeServices.techSurfaceModel());
                if (result.success()) {
                    runtimeServices.updateWorldSessionFromGameplay();
                    screens.showToast("Extracted scrap from " + result.machineId());
                    return true;
                }
                screens.showToast("Machine extract failed: " + result.reason());
                return false;
            }
            case SELECT_MACHINE_RECIPE -> {
                EchoClientMachineRecipeSelectionResult result =
                        runtimeServices.selectMachineRecipe(screens.selectedMachineRecipeTargetId());
                screens.updateTechSurfaceModel(runtimeServices.techSurfaceModel());
                if (result.success()) {
                    runtimeServices.updateWorldSessionFromGameplay();
                    screens.showToast("Selected recipe for " + result.machineId());
                    return true;
                }
                screens.showToast("Recipe select failed: " + result.reason());
                return false;
            }
            case NONE -> {
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    interface Host {
        void attachSession();

        void beginSaving();

        void unlockCursor();

        void requestClose();

        void reloadMinecraftAssets(boolean rebuildAtlas);

        default EchoClientSaveSlotThumbnailCapture captureSaveThumbnail() {
            return EchoClientSaveSlotThumbnailCapture.EMPTY;
        }
    }
}
