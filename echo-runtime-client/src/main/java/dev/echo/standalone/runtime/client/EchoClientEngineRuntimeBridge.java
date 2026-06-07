package dev.echo.standalone.runtime.client;

final class EchoClientEngineRuntimeBridge {
    private static final InputSource NO_INPUT = new InputSource() {
        @Override
        public boolean consumeScreenshot() {
            return false;
        }

        @Override
        public void unlockCursor() {
        }

        @Override
        public void updatePointer() {
        }

        @Override
        public boolean consumeSlotGridClose() {
            return false;
        }

        @Override
        public boolean consumeUiPrimaryClick() {
            return false;
        }

        @Override
        public boolean consumeUiSecondaryClick() {
            return false;
        }

        @Override
        public boolean uiPrimaryDown() {
            return false;
        }

        @Override
        public boolean uiSecondaryDown() {
            return false;
        }

        @Override
        public int consumeHotbarSlotKeyPress() {
            return -1;
        }

        @Override
        public boolean consumeInventoryDrop() {
            return false;
        }

        @Override
        public boolean shiftDown() {
            return false;
        }

        @Override
        public boolean controlDown() {
            return false;
        }

        @Override
        public double pointerX() {
            return 0.0D;
        }

        @Override
        public double pointerY() {
            return 0.0D;
        }

        @Override
        public void clearGameplayTriggers() {
        }
    };

    private final EchoClientRenderRuntimeController renderRuntime;
    private final EchoClientShellRuntimeController shellRuntime;
    private final EchoClientSlotGridRuntimeController slotGridRuntime;
    private final CloseTarget closeTarget;
    private InputSource input = NO_INPUT;

    private final EchoClientScreenshotRuntimeController.InputGate screenshotInputGate;
    private final EchoClientShellRuntimeController.InputGate shellInputGate;
    private final EchoClientShellRuntimeController.Host shellRuntimeHost;
    private final EchoClientSlotGridRuntimeController.InputGate slotGridInputGate;
    private final EchoClientGameplayRuntimeController.Host gameplayRuntimeHost;
    private final EchoClientCommandController.Host commandHost;

    EchoClientEngineRuntimeBridge(
            EchoClientRenderRuntimeController renderRuntime,
            EchoClientShellRuntimeController shellRuntime,
            EchoClientSlotGridRuntimeController slotGridRuntime,
            EchoGlfwWindow window
    ) {
        this(renderRuntime, shellRuntime, slotGridRuntime, window::requestClose);
    }

    EchoClientEngineRuntimeBridge(
            EchoClientRenderRuntimeController renderRuntime,
            EchoClientShellRuntimeController shellRuntime,
            EchoClientSlotGridRuntimeController slotGridRuntime,
            CloseTarget closeTarget
    ) {
        if (renderRuntime == null) {
            throw new IllegalArgumentException("renderRuntime must not be null");
        }
        if (shellRuntime == null) {
            throw new IllegalArgumentException("shellRuntime must not be null");
        }
        if (slotGridRuntime == null) {
            throw new IllegalArgumentException("slotGridRuntime must not be null");
        }
        if (closeTarget == null) {
            throw new IllegalArgumentException("closeTarget must not be null");
        }
        this.renderRuntime = renderRuntime;
        this.shellRuntime = shellRuntime;
        this.slotGridRuntime = slotGridRuntime;
        this.closeTarget = closeTarget;
        screenshotInputGate = () -> input.consumeScreenshot();
        shellInputGate = new EchoClientShellRuntimeController.InputGate() {
            @Override
            public void unlockCursor() {
                input.unlockCursor();
            }

            @Override
            public void clearGameplayTriggers() {
                input.clearGameplayTriggers();
            }
        };
        shellRuntimeHost = () -> this.renderRuntime.attachActiveSession();
        slotGridInputGate = new EchoClientSlotGridRuntimeController.InputGate() {
            @Override
            public void unlockCursor() {
                input.unlockCursor();
            }

            @Override
            public void updatePointer() {
                input.updatePointer();
            }

            @Override
            public boolean closeRequested() {
                return input.consumeSlotGridClose();
            }

            @Override
            public boolean primaryClick() {
                return input.consumeUiPrimaryClick();
            }

            @Override
            public boolean secondaryClick() {
                return input.consumeUiSecondaryClick();
            }

            @Override
            public boolean primaryDown() {
                return input.uiPrimaryDown();
            }

            @Override
            public boolean secondaryDown() {
                return input.uiSecondaryDown();
            }

            @Override
            public int hotbarSlotKey() {
                return input.consumeHotbarSlotKeyPress();
            }

            @Override
            public boolean dropRequested() {
                return input.consumeInventoryDrop();
            }

            @Override
            public boolean dropStackRequested() {
                return input.shiftDown() || input.controlDown();
            }

            @Override
            public boolean shiftDown() {
                return input.shiftDown();
            }

            @Override
            public double pointerX() {
                return input.pointerX();
            }

            @Override
            public double pointerY() {
                return input.pointerY();
            }

            @Override
            public void clearGameplayTriggers() {
                input.clearGameplayTriggers();
            }
        };
        gameplayRuntimeHost = new EchoClientGameplayRuntimeController.Host() {
            @Override
            public void clearInventoryDrag() {
                EchoClientEngineRuntimeBridge.this.slotGridRuntime.clearDrag();
            }

            @Override
            public void refreshWorldStreamingAndMeshes() {
                EchoClientEngineRuntimeBridge.this.renderRuntime.refreshWorldStreamingAndMeshes();
            }

            @Override
            public void attachSession() {
                EchoClientEngineRuntimeBridge.this.renderRuntime.attachActiveSession();
            }
        };
        commandHost = new EchoClientCommandController.Host() {
            @Override
            public void attachSession() {
                EchoClientEngineRuntimeBridge.this.renderRuntime.attachActiveSession();
            }

            @Override
            public void beginSaving() {
                EchoClientEngineRuntimeBridge.this.shellRuntime.beginSaving();
            }

            @Override
            public void unlockCursor() {
                input.unlockCursor();
            }

            @Override
            public void requestClose() {
                EchoClientEngineRuntimeBridge.this.closeTarget.requestClose();
            }

            @Override
            public void reloadMinecraftAssets(boolean rebuildAtlas) {
                EchoClientEngineRuntimeBridge.this.renderRuntime.reloadMinecraftAssets(rebuildAtlas);
            }
        };
    }

    void attachInput(EchoClientInput input) {
        this.input = input == null
                ? NO_INPUT
                : new InputSource() {
                    @Override
                    public boolean consumeScreenshot() {
                        return input.consumeScreenshot();
                    }

                    @Override
                    public void unlockCursor() {
                        input.setCursorLocked(false);
                    }

                    @Override
                    public void updatePointer() {
                        input.updatePointer();
                    }

                    @Override
                    public boolean consumeSlotGridClose() {
                        return input.consumeSlotGridClose();
                    }

                    @Override
                    public boolean consumeUiPrimaryClick() {
                        return input.consumeUiPrimaryClick();
                    }

                    @Override
                    public boolean consumeUiSecondaryClick() {
                        return input.consumeUiSecondaryClick();
                    }

                    @Override
                    public boolean uiPrimaryDown() {
                        return input.uiPrimaryDown();
                    }

                    @Override
                    public boolean uiSecondaryDown() {
                        return input.uiSecondaryDown();
                    }

                    @Override
                    public int consumeHotbarSlotKeyPress() {
                        return input.consumeHotbarSlotKeyPress();
                    }

                    @Override
                    public boolean consumeInventoryDrop() {
                        return input.consumeDropItem();
                    }

                    @Override
                    public boolean shiftDown() {
                        return input.shiftDown();
                    }

                    @Override
                    public boolean controlDown() {
                        return input.controlDown();
                    }

                    @Override
                    public double pointerX() {
                        return input.pointerX();
                    }

                    @Override
                    public double pointerY() {
                        return input.pointerY();
                    }

                    @Override
                    public void clearGameplayTriggers() {
                        input.clearGameplayTriggers();
                    }
                };
    }

    void attachInputSource(InputSource input) {
        this.input = input == null ? NO_INPUT : input;
    }

    EchoClientScreenshotRuntimeController.InputGate screenshotInputGate() {
        return screenshotInputGate;
    }

    EchoClientShellRuntimeController.InputGate shellInputGate() {
        return shellInputGate;
    }

    EchoClientShellRuntimeController.Host shellRuntimeHost() {
        return shellRuntimeHost;
    }

    EchoClientSlotGridRuntimeController.InputGate slotGridInputGate() {
        return slotGridInputGate;
    }

    EchoClientGameplayRuntimeController.Host gameplayRuntimeHost() {
        return gameplayRuntimeHost;
    }

    EchoClientCommandController.Host commandHost() {
        return commandHost;
    }

    interface InputSource {
        boolean consumeScreenshot();

        void unlockCursor();

        void updatePointer();

        boolean consumeSlotGridClose();

        boolean consumeUiPrimaryClick();

        boolean consumeUiSecondaryClick();

        boolean uiPrimaryDown();

        boolean uiSecondaryDown();

        int consumeHotbarSlotKeyPress();

        boolean consumeInventoryDrop();

        boolean shiftDown();

        boolean controlDown();

        double pointerX();

        double pointerY();

        void clearGameplayTriggers();
    }

    interface CloseTarget {
        void requestClose();
    }
}
