# ECHO Standalone Final Tester Script

Package under test:

- Portable zip: `build/distributions/EchoStandaloneRuntime-portable-opengl-client.zip`
- EXE: `build/jpackage-opengl-client/EchoStandaloneRuntime/EchoStandaloneRuntime.exe`
- Support bundle: `build/support/EchoStandaloneSupportBundle.zip`

## Script

1. Install or extract the package, then open `EchoStandaloneRuntime.exe`.
2. Confirm the window title is `ECHO Ashfall Standalone`.
3. Start a new game.
4. Find shelter and record time to first shelter.
5. Consume water and food, then record time to first water use.
6. Enter ash exposure long enough to see the hazard warning, then recover.
7. Use the terminal and record time to terminal online.
8. Recover the crash cache.
9. Repair power and record time to power restored.
10. Trigger extraction and record extraction time.
11. Save manually, quit, continue, and verify objective, inventory, terminal notes, and HUD state.
12. Export the support bundle and confirm the zip exists.
13. Record deaths, confusing UI moments, HUD readability notes, inventory flow notes, terminal usefulness notes, and audio cue notes.

## Stability Add-On

- Run one 30-minute packaged EXE session.
- Run one 60-minute packaged EXE session.
- During each session, spam inventory open/close, terminal open/close, pause/resume, and alt-tab.
- Corrupt a copied save slot and confirm the warning/backup recovery path is understandable.

## Exit Criteria

- No blank, white, or flickering frames.
- No softlock after pause, terminal, inventory, or alt-tab.
- Continue restores expected objective and inventory state.
- Tester can explain extraction conditions in their own words.
- Support bundle export is available for bug reports.
