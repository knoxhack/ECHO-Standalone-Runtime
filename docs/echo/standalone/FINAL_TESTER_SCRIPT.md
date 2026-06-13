# ECHO Standalone Final Tester Script

Package under test:

- Portable zip: `build/distributions/EchoStandaloneRuntime-portable-opengl-client.zip`
- EXE: `build/jpackage-opengl-client/EchoStandaloneRuntime/EchoStandaloneRuntime.exe`
- Support bundle: `build/support/EchoStandaloneSupportBundle.zip`
- External evidence handoff: `reports/echo/standalone/public-release-external-evidence-handoff.json`
- Release signing helper: `scripts/sign-release-artifacts.ps1`
- Clean install evidence helper: `scripts/capture-clean-install-uninstall-evidence.ps1`
- Manual wall-clock helper: `scripts/capture-manual-wallclock-playtest.ps1`
- Hardware audio helper: `scripts/capture-audio-hardware-evidence.ps1`

## Script

1. Install or extract the package, then open `EchoStandaloneRuntime.exe`.
2. Confirm the window title is `ECHO Ashfall - Standalone Client`.
3. Start a new game.
4. Find shelter and record time to first shelter.
5. Consume water and food, then record time to first water use.
6. Enter ash exposure long enough to see the hazard warning, then recover.
7. Use the terminal and record time to terminal online.
8. Recover the crash cache.
9. Repair power and record time to power restored.
10. Trigger extraction and record extraction time.
11. Save manually, quit, continue, and verify objective, inventory, terminal notes, HUD state, and the save-slot thumbnail preview.
12. Open Options or Diagnostics, confirm the frame-pacing row is visible, choose `Export Support Bundle`, and confirm the client support zip exists.
13. If the packaged client enters `RUNTIME ERROR`, confirm the screen offers `Export Support Bundle`, `Open Diagnostics`, `Quit To Title`, and `Quit Client`.
14. Record deaths, confusing UI moments, HUD readability notes, inventory flow notes, terminal usefulness notes, fatal/error screen notes, and audio cue notes.

## Stability Add-On

Use `reports/echo/standalone/public-release-external-evidence-handoff.json` for the current helper paths, artifact hashes, and command templates for signing, clean install/uninstall, and manual wall-clock capture. It is a handoff manifest only; the commands below still need to be run in their required external environments.

Public release also requires `reports/echo/standalone/openlands-full-route-contract.json` to record final Openlands public art/content polish. A successful route playtest does not clear the beta-only placeholder asset blocker.

- Run one 30-minute packaged EXE session.
- Run one 60-minute packaged EXE session.
- During each session, spam inventory open/close, terminal open/close, pause/resume, and alt-tab.
- Corrupt a copied save slot and confirm the warning/backup recovery path is understandable.

Use the manual wall-clock helper for each session. The packaged EXE, portable zip, and capture script hashes are listed in `reports/echo/standalone/manual-wallclock-capture-rehearsal.json`.

Automated strict monitor evidence is already captured in `reports/echo/standalone/packaged-exe-wallclock-strict-30m.json` and `reports/echo/standalone/packaged-exe-wallclock-strict-60m.json`. These prove process/window survivability and do not replace the human sessions.

The no-launch helper rehearsal is already covered by `reports/echo/standalone/manual-wallclock-helper-rehearsal.json`. It proves the helper fails closed, but it does not replace either human session.

Use the hardware audio helper after checking the packaged OpenGL client through the real speaker/headset output path. This is separate from the deterministic Java Sound and fallback smokes.

```powershell
powershell -ExecutionPolicy Bypass -File scripts/capture-audio-hardware-evidence.ps1 `
  -ExpectedCaptureScriptSha256 "<audio-hardware-script-sha256>" `
  -OutputDeviceLabel "<physical speaker/headset device label>" `
  -WindowsAudioEndpointEvidence "<Windows sound output device proof>" `
  -PlaybackAppVersionEvidence "<packaged EXE version/hash or tester build id>" `
  -HardwareTesterAttestation "<attest real output playback was checked>" `
  -MusicCueNote "<main-menu or survival music cue heard>" `
  -UiCueNote "<button/menu UI cue heard>" `
  -SfxCueNote "<pickup/footstep/world action SFX heard>" `
  -AmbienceCueNote "<weather/biome ambience cue heard>" `
  -VolumeMuteNote "<volume slider and mute behavior result>" `
  -FallbackDiagnosticsNote "<Diagnostics/support-bundle backend/device/fallback row result>" `
  -HeardMusic -HeardUi -HeardSfx -HeardAmbience `
  -VolumeMuteChecked -AudioDeviceMatchedDiagnostics `
  -SupportBundleAudioDiagnosticsCaptured -NoCrackleDropout -NoIssues
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/capture-manual-wallclock-playtest.ps1 `
  -SessionId packaged-opengl-30m `
  -ExpectedPackagedExeSha256 "<packaged-exe-sha256>" `
  -ExpectedPortableZipSha256 "<portable-zip-sha256>" `
  -CaptureScriptSha256 "<manual-capture-script-sha256>" `
  -HumanTesterAttestation "<attest active human play for the full session>" `
  -InteractionCoverageNote "<inventory, terminal, pause/resume, and alt-tab coverage>" `
  -SaveLoadCheckpointNote "<save/quit/continue result>" `
  -CrashFreeExitNote "<crash-free exit or softlock result>"

powershell -ExecutionPolicy Bypass -File scripts/capture-manual-wallclock-playtest.ps1 `
  -SessionId packaged-opengl-60m `
  -ExpectedPackagedExeSha256 "<packaged-exe-sha256>" `
  -ExpectedPortableZipSha256 "<portable-zip-sha256>" `
  -CaptureScriptSha256 "<manual-capture-script-sha256>" `
  -HumanTesterAttestation "<attest active human play for the full session>" `
  -InteractionCoverageNote "<inventory, terminal, pause/resume, and alt-tab coverage>" `
  -SaveLoadCheckpointNote "<save/quit/continue result>" `
  -CrashFreeExitNote "<crash-free exit or softlock result>"
```

## Clean Install Add-On

Run this only inside a fresh Windows VM, disposable Windows profile, or disposable Windows user. Use the setup artifact and hashes from `reports/echo/standalone/distribution-install-uninstall-evidence-template.json`.

The no-side-effect helper rehearsal is already covered by `reports/echo/standalone/clean-install-capture-rehearsal.json`. It proves the helper fails closed, but it does not replace this fresh install/uninstall run.

```powershell
powershell -ExecutionPolicy Bypass -File scripts/capture-clean-install-uninstall-evidence.ps1 `
  -SetupArtifactPath "<path-to-ECHO-Launcher-Setup.exe>" `
  -ExpectedSetupSha256 "<setup-sha256>" `
  -ExpectedLauncherExeSha256 "<launcher-exe-sha256>" `
  -CaptureScriptSha256 "<capture-script-sha256>" `
  -DisposableEnvironmentEvidence "<VM snapshot, disposable user/profile, or CI VM run id>" `
  -FreshProfileOrVmSnapshotEvidence "<fresh snapshot/profile proof>" `
  -PreInstallCleanStateEvidence "<no existing ECHO install/process proof>" `
  -PostUninstallCleanStateEvidence "<post-uninstall cleanup proof>" `
  -OutputPath "reports/echo/standalone/manual-install-uninstall-evidence.json"
```

The helper installs the launcher, records the installed launcher executable hash/signature, prompts for first-run/runtime/support-bundle confirmation, runs uninstall, and writes the evidence JSON consumed by the public release gate. The VM/profile proof, fresh snapshot proof, pre-install clean-state proof, and post-uninstall cleanup proof fields are required for public release.

## Release Signing Add-On

Run this only on the release signing machine after the trusted code-signing certificate is available in the Windows certificate store. The helper runs in verify/plan mode unless `-Sign` is supplied.
The fail-closed rehearsal is already captured in `reports/echo/standalone/release-signing-failure-rehearsal.json`; it proves missing-certificate handling and does not replace this release signing step.

```powershell
powershell -ExecutionPolicy Bypass -File scripts/sign-release-artifacts.ps1 `
  -CertificateThumbprint "<release-certificate-thumbprint>" `
  -PublisherName "<publisher-name>" `
  -TimestampServer "http://timestamp.digicert.com" `
  -ExpectedRuntimeExeSha256 "<unsigned-runtime-exe-sha256>" `
  -ExpectedLauncherExeSha256 "<unsigned-launcher-exe-sha256>" `
  -ExpectedLauncherSetupExeSha256 "<unsigned-setup-exe-sha256>" `
  -SigningOperator "<operator name>" `
  -SigningRunEvidence "<signing machine or ticket/run id>" `
  -Sign
```

After signing, regenerate `reports/echo/standalone/distribution-signing-setup.json` with `runStandaloneDistributionSigningSetupAudit`.

## Exit Criteria

- No blank, white, or flickering frames.
- No softlock after pause, terminal, inventory, or alt-tab.
- No sustained high `FRAME` slow/streak counters in the debug overlay or Diagnostics route during menu, pause, terminal, inventory, or alt-tab loops.
- Fatal runtime errors route to the `RUNTIME ERROR` screen instead of a blank/frozen window, with support bundle export available.
- Continue restores expected objective and inventory state.
- Tester can explain extraction conditions in their own words.
- Support bundle export is available from the OpenGL client UI for bug reports.
