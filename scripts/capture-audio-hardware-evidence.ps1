#requires -Version 5.1
[CmdletBinding()]
param(
    [string] $OutputPath = "reports/echo/standalone/audio-hardware-verification.json",
    [string] $ExpectedCaptureScriptSha256 = "",

    [string] $Tester = $env:USERNAME,
    [string] $MachineId = "$env:COMPUTERNAME\$env:USERNAME",
    [string] $OutputDeviceLabel = "",

    [ValidateSet("SPEAKERS", "HEADSET", "HDMI", "BLUETOOTH", "OTHER")]
    [string] $DeviceType = "SPEAKERS",

    [string] $WindowsAudioEndpointEvidence = "",
    [string] $PlaybackAppVersionEvidence = "",
    [string] $HardwareTesterAttestation = "",
    [string] $MusicCueNote = "",
    [string] $UiCueNote = "",
    [string] $SfxCueNote = "",
    [string] $AmbienceCueNote = "",
    [string] $VolumeMuteNote = "",
    [string] $FallbackDiagnosticsNote = "",
    [string] $IssuesNote = "",

    [switch] $HeardMusic,
    [switch] $HeardUi,
    [switch] $HeardSfx,
    [switch] $HeardAmbience,
    [switch] $VolumeMuteChecked,
    [switch] $AudioDeviceMatchedDiagnostics,
    [switch] $SupportBundleAudioDiagnosticsCaptured,
    [switch] $NoCrackleDropout,
    [switch] $NoIssues,
    [switch] $NonInteractive
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-UtcTimestamp {
    return (Get-Date).ToUniversalTime().ToString("o")
}

function Convert-PathForJson {
    param([string] $Path)
    if ([string]::IsNullOrWhiteSpace($Path)) {
        return ""
    }
    try {
        return ([System.IO.Path]::GetFullPath($Path)).Replace("\", "/")
    } catch {
        return $Path.Replace("\", "/")
    }
}

function Get-FileSha256OrEmpty {
    param([string] $Path)
    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        return ""
    }
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Read-Confirmation {
    param(
        [string] $Prompt,
        [bool] $DefaultValue
    )
    if ($DefaultValue -or $NonInteractive) {
        return $DefaultValue
    }
    $answer = Read-Host "$Prompt [y/N]"
    return $answer -imatch "^y(es)?$"
}

function Read-TextEvidence {
    param(
        [string] $Prompt,
        [string] $DefaultValue
    )
    if (-not [string]::IsNullOrWhiteSpace($DefaultValue) -or $NonInteractive) {
        return $DefaultValue
    }
    return Read-Host $Prompt
}

function Get-OsVersionText {
    try {
        $os = Get-CimInstance -ClassName Win32_OperatingSystem
        return "$($os.Caption) $($os.Version) build $($os.BuildNumber)"
    } catch {
        return [System.Environment]::OSVersion.VersionString
    }
}

function Get-AudioDeviceSnapshot {
    try {
        return @(Get-CimInstance -ClassName Win32_SoundDevice -ErrorAction Stop | ForEach-Object {
            [ordered]@{
                name = [string] $_.Name
                manufacturer = [string] $_.Manufacturer
                status = [string] $_.Status
                deviceId = [string] $_.DeviceID
            }
        })
    } catch {
        return @()
    }
}

$scriptPath = if ($PSCommandPath) { $PSCommandPath } else { $MyInvocation.MyCommand.Path }
$actualCaptureScriptSha256 = Get-FileSha256OrEmpty $scriptPath
if (-not [string]::IsNullOrWhiteSpace($ExpectedCaptureScriptSha256) -and
        $actualCaptureScriptSha256 -ine $ExpectedCaptureScriptSha256) {
    throw "Capture script SHA-256 mismatch. actual=$actualCaptureScriptSha256 expected=$ExpectedCaptureScriptSha256"
}

$runStartedAt = Get-UtcTimestamp
$OutputDeviceLabel = Read-TextEvidence "Physical speaker/headset output device label" $OutputDeviceLabel
$WindowsAudioEndpointEvidence = Read-TextEvidence "Windows audio endpoint evidence" $WindowsAudioEndpointEvidence
$PlaybackAppVersionEvidence = Read-TextEvidence "Packaged app version/hash evidence" $PlaybackAppVersionEvidence
$HardwareTesterAttestation = Read-TextEvidence "Hardware tester attestation" $HardwareTesterAttestation
$MusicCueNote = Read-TextEvidence "Music cue heard note" $MusicCueNote
$UiCueNote = Read-TextEvidence "UI cue heard note" $UiCueNote
$SfxCueNote = Read-TextEvidence "World SFX cue heard note" $SfxCueNote
$AmbienceCueNote = Read-TextEvidence "Ambience cue heard note" $AmbienceCueNote
$VolumeMuteNote = Read-TextEvidence "Volume/mute behavior note" $VolumeMuteNote
$FallbackDiagnosticsNote = Read-TextEvidence "Diagnostics/support-bundle audio row note" $FallbackDiagnosticsNote
$IssuesNote = Read-TextEvidence "Issues note, or explicit no-issues statement" $IssuesNote

$heardMusicValue = Read-Confirmation "Music cue heard on the physical output device?" ([bool] $HeardMusic)
$heardUiValue = Read-Confirmation "UI cue heard on the physical output device?" ([bool] $HeardUi)
$heardSfxValue = Read-Confirmation "World SFX cue heard on the physical output device?" ([bool] $HeardSfx)
$heardAmbienceValue = Read-Confirmation "Ambience cue heard on the physical output device?" ([bool] $HeardAmbience)
$volumeMuteCheckedValue = Read-Confirmation "Volume sliders and mute behavior checked?" ([bool] $VolumeMuteChecked)
$deviceMatchedDiagnosticsValue = Read-Confirmation "ScreenCore Diagnostics audio device/fallback row matched hardware expectation?" ([bool] $AudioDeviceMatchedDiagnostics)
$supportBundleDiagnosticsValue = Read-Confirmation "Support bundle captured audio diagnostics?" ([bool] $SupportBundleAudioDiagnosticsCaptured)
$noCrackleValue = Read-Confirmation "No crackle, dropout, routing, or latency issue observed?" ([bool] $NoCrackleDropout)
$noIssuesValue = Read-Confirmation "No audio issues found?" ([bool] $NoIssues)

$requiredTextPresent = -not [string]::IsNullOrWhiteSpace($Tester) -and
        -not [string]::IsNullOrWhiteSpace($MachineId) -and
        -not [string]::IsNullOrWhiteSpace($OutputDeviceLabel) -and
        -not [string]::IsNullOrWhiteSpace($WindowsAudioEndpointEvidence) -and
        -not [string]::IsNullOrWhiteSpace($PlaybackAppVersionEvidence) -and
        -not [string]::IsNullOrWhiteSpace($HardwareTesterAttestation) -and
        -not [string]::IsNullOrWhiteSpace($MusicCueNote) -and
        -not [string]::IsNullOrWhiteSpace($UiCueNote) -and
        -not [string]::IsNullOrWhiteSpace($SfxCueNote) -and
        -not [string]::IsNullOrWhiteSpace($AmbienceCueNote) -and
        -not [string]::IsNullOrWhiteSpace($VolumeMuteNote) -and
        -not [string]::IsNullOrWhiteSpace($FallbackDiagnosticsNote) -and
        ($noIssuesValue -or -not [string]::IsNullOrWhiteSpace($IssuesNote))

$hardwareAudioEvidenceComplete = $requiredTextPresent -and
        $heardMusicValue -and
        $heardUiValue -and
        $heardSfxValue -and
        $heardAmbienceValue -and
        $volumeMuteCheckedValue -and
        $deviceMatchedDiagnosticsValue -and
        $supportBundleDiagnosticsValue -and
        $noCrackleValue

$status = if ($hardwareAudioEvidenceComplete) { "PASS" } else { "PENDING_HARDWARE_AUDIO_RUN" }
$runEndedAt = Get-UtcTimestamp

$report = [ordered]@{
    schema = "echo.standalone.audio_hardware_verification.v1"
    generatedAt = "1970-01-01T00:00:00Z"
    generator = "scripts/capture-audio-hardware-evidence.ps1"
    status = $status
    summary = if ($hardwareAudioEvidenceComplete) {
        "Real speaker/headset audio hardware verification passed for the packaged OpenGL client."
    } else {
        "Real speaker/headset audio hardware verification is incomplete."
    }
    rendererTarget = "opengl"
    nativeModLoaderCommandUsed = $false
    hardwareAudioEvidenceComplete = $hardwareAudioEvidenceComplete
    replacesAutomatedAudioEvidence = $false
    tester = $Tester
    machineId = $MachineId
    osVersion = Get-OsVersionText
    runStartedAt = $runStartedAt
    runEndedAt = $runEndedAt
    captureScriptPath = Convert-PathForJson $scriptPath
    captureScriptSha256 = $actualCaptureScriptSha256
    outputDeviceLabel = $OutputDeviceLabel
    deviceType = $DeviceType
    windowsAudioEndpointEvidence = $WindowsAudioEndpointEvidence
    playbackAppVersionEvidence = $PlaybackAppVersionEvidence
    hardwareTesterAttestation = $HardwareTesterAttestation
    heardMusicCue = $heardMusicValue
    heardUiCue = $heardUiValue
    heardWorldSfxCue = $heardSfxValue
    heardAmbienceCue = $heardAmbienceValue
    volumeMuteBehaviorChecked = $volumeMuteCheckedValue
    screenCoreDiagnosticsChecked = $deviceMatchedDiagnosticsValue
    supportBundleAudioDiagnosticsCaptured = $supportBundleDiagnosticsValue
    noCrackleDropoutOrLatencyIssue = $noCrackleValue
    noIssues = $noIssuesValue
    musicCueNote = $MusicCueNote
    uiCueNote = $UiCueNote
    sfxCueNote = $SfxCueNote
    ambienceCueNote = $AmbienceCueNote
    volumeMuteNote = $VolumeMuteNote
    fallbackDiagnosticsNote = $FallbackDiagnosticsNote
    issuesNote = if ($noIssuesValue -and [string]::IsNullOrWhiteSpace($IssuesNote)) { "No audio issues found." } else { $IssuesNote }
    discoveredWindowsSoundDevices = Get-AudioDeviceSnapshot
    automatedEvidence = @(
        "reports/echo/standalone/runtime-audio-device.json",
        "reports/echo/standalone/audio-device-output.json",
        "reports/echo/standalone/audio-device-fallback.json",
        "reports/echo/standalone/audio-device-volume-controls.json",
        "reports/echo/standalone/client-machine-terminal-surfaces.json",
        "reports/echo/standalone/full-audio-particles-weather-ambience.json"
    )
    requiredEvidence = @(
        "tester identity",
        "machine and OS version",
        "physical output device label",
        "speaker/headset device type",
        "Windows audio endpoint evidence",
        "music cue heard",
        "UI cue heard",
        "world SFX cue heard",
        "ambience cue heard",
        "volume and mute behavior checked",
        "ScreenCore diagnostics device/fallback row checked",
        "support bundle audio diagnostics captured",
        "no crackle/dropout/latency issue or explicit issue note"
    )
}

$outputFullPath = [System.IO.Path]::GetFullPath($OutputPath)
$outputDirectory = [System.IO.Path]::GetDirectoryName($outputFullPath)
if (-not [string]::IsNullOrWhiteSpace($outputDirectory)) {
    New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
}
$json = $report | ConvertTo-Json -Depth 8
[System.IO.File]::WriteAllText($outputFullPath, $json + [System.Environment]::NewLine, [System.Text.UTF8Encoding]::new($false))

if ($hardwareAudioEvidenceComplete) {
    exit 0
}
exit 2
