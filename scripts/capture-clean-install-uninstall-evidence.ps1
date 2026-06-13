#requires -Version 5.1
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $SetupArtifactPath,

    [string] $ExpectedSetupSha256 = "",
    [string] $ExpectedLauncherExeSha256 = "",
    [string] $CaptureScriptSha256 = "",
    [string] $OutputPath = "reports/echo/standalone/manual-install-uninstall-evidence.json",

    [ValidateSet("WINDOWS_VM", "DISPOSABLE_WINDOWS_PROFILE", "DISPOSABLE_WINDOWS_USER", "CI_DISPOSABLE_WINDOWS_VM", "CLEAN_WINDOWS_PROFILE")]
    [string] $EnvironmentType = "WINDOWS_VM",

    [string] $Tester = $env:USERNAME,
    [string] $MachineId = "$env:COMPUTERNAME\$env:USERNAME",
    [string] $DisposableEnvironmentEvidence = "",
    [string] $FreshProfileOrVmSnapshotEvidence = "",
    [string] $PreInstallCleanStateEvidence = "",
    [string] $PostUninstallCleanStateEvidence = "",

    [switch] $DisposableEnvironmentConfirmed,
    [switch] $FreshProfileOrVmSnapshotConfirmed,
    [switch] $LauncherFirstRunConfirmed,
    [switch] $RuntimeDetectedConfirmed,
    [switch] $ResidualFilesOnlyAppDataConfirmed,

    [string] $SupportBundleArchivePath = "",

    [ValidateSet("PASS", "SKIPPED_NOT_APPLICABLE")]
    [string] $OldSpacedInstallDirMigrationStatus = "SKIPPED_NOT_APPLICABLE",

    [switch] $SkipInstall,
    [switch] $SkipUninstall,
    [switch] $RehearsalOnly,
    [switch] $NonInteractive
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ($RehearsalOnly) {
    $SkipInstall = $true
    $SkipUninstall = $true
    $NonInteractive = $true
}

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

function Get-AuthenticodeSnapshot {
    param([string] $Path)
    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        return [ordered]@{
            status = "MISSING"
            signerSubject = ""
            signerThumbprint = ""
        }
    }
    $signature = Get-AuthenticodeSignature -LiteralPath $Path
    return [ordered]@{
        status = [string] $signature.Status
        signerSubject = if ($signature.SignerCertificate) { $signature.SignerCertificate.Subject } else { "" }
        signerThumbprint = if ($signature.SignerCertificate) { $signature.SignerCertificate.Thumbprint } else { "" }
    }
}

function Get-WindowsVersionText {
    try {
        $os = Get-CimInstance -ClassName Win32_OperatingSystem
        return "$($os.Caption) $($os.Version) build $($os.BuildNumber)"
    } catch {
        return [System.Environment]::OSVersion.VersionString
    }
}

function Get-EchoProcessSnapshot {
    $names = @("ECHOLauncher", "EchoStandaloneRuntime", "ECHO Launcher", "ECHO")
    return @(Get-Process -ErrorAction SilentlyContinue | Where-Object {
        $name = $_.ProcessName
        $names -contains $name -or $name -like "Echo*" -or $name -like "ECHO*"
    } | Select-Object -Property Id, ProcessName)
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

function Find-EchoLauncherInstallDir {
    $candidates = @()
    if ($env:LOCALAPPDATA) {
        $candidates += (Join-Path $env:LOCALAPPDATA "Programs\ECHO Launcher")
    }
    if ($env:ProgramFiles) {
        $candidates += (Join-Path $env:ProgramFiles "ECHO Launcher")
    }
    ${env:ProgramFiles(x86)} | ForEach-Object {
        if (-not [string]::IsNullOrWhiteSpace($_)) {
            $candidates += (Join-Path $_ "ECHO Launcher")
        }
    }
    foreach ($candidate in ($candidates | Select-Object -Unique)) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }
    return $candidates[0]
}

function Find-EchoLauncherExe {
    param([string] $InstallDir)
    $candidates = @(
        (Join-Path $InstallDir "ECHOLauncher.exe"),
        (Join-Path $InstallDir "ECHO Launcher.exe")
    )
    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate -PathType Leaf) {
            return $candidate
        }
    }
    return $candidates[0]
}

function Find-EchoUninstallEntry {
    $roots = @(
        "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall",
        "HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall",
        "HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall"
    )
    foreach ($root in $roots) {
        if (-not (Test-Path -LiteralPath $root)) {
            continue
        }
        foreach ($key in Get-ChildItem -LiteralPath $root -ErrorAction SilentlyContinue) {
            $props = Get-ItemProperty -LiteralPath $key.PSPath -ErrorAction SilentlyContinue
            if ($null -eq $props) {
                continue
            }
            $displayNameProperty = $props.PSObject.Properties["DisplayName"]
            $displayName = if ($null -ne $displayNameProperty) { [string] $displayNameProperty.Value } else { "" }
            if ($displayName -eq "ECHO Launcher" -or $displayName -like "*ECHO*Launcher*") {
                $uninstallProperty = $props.PSObject.Properties["UninstallString"]
                $quietUninstallProperty = $props.PSObject.Properties["QuietUninstallString"]
                return [ordered]@{
                    keyPath = $key.Name
                    displayName = $displayName
                    uninstallString = if ($null -ne $uninstallProperty) { [string] $uninstallProperty.Value } else { "" }
                    quietUninstallString = if ($null -ne $quietUninstallProperty) { [string] $quietUninstallProperty.Value } else { "" }
                }
            }
        }
    }
    return $null
}

function Invoke-CommandLineProcess {
    param([string] $CommandLine)
    $trimmed = $CommandLine.Trim()
    if ([string]::IsNullOrWhiteSpace($trimmed)) {
        throw "Cannot start an empty command line."
    }
    if ($trimmed.StartsWith('"')) {
        $endQuote = $trimmed.IndexOf('"', 1)
        if ($endQuote -lt 1) {
            throw "Cannot parse command line: $CommandLine"
        }
        $filePath = $trimmed.Substring(1, $endQuote - 1)
        $arguments = $trimmed.Substring($endQuote + 1).Trim()
    } else {
        $firstSpace = $trimmed.IndexOf(" ")
        if ($firstSpace -lt 0) {
            $filePath = $trimmed
            $arguments = ""
        } else {
            $filePath = $trimmed.Substring(0, $firstSpace)
            $arguments = $trimmed.Substring($firstSpace + 1).Trim()
        }
    }
    if ([string]::IsNullOrWhiteSpace($arguments)) {
        return Start-Process -FilePath $filePath -Wait -PassThru
    }
    return Start-Process -FilePath $filePath -ArgumentList $arguments -Wait -PassThru
}

function Test-SupportBundleManifest {
    param([string] $ArchivePath)
    if ([string]::IsNullOrWhiteSpace($ArchivePath) -or -not (Test-Path -LiteralPath $ArchivePath -PathType Leaf)) {
        return $false
    }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    try {
        $zip = [System.IO.Compression.ZipFile]::OpenRead($ArchivePath)
    } catch {
        return $false
    }
    try {
        foreach ($entry in $zip.Entries) {
            $name = $entry.FullName.Replace("\", "/")
            if ($name -eq "support/manifest.txt" -or $name -like "*launcher-support-bundle.json") {
                return $true
            }
        }
        return $false
    } finally {
        $zip.Dispose()
    }
}

function Add-Check {
    param(
        [string] $Id,
        [bool] $Passed,
        [object] $Actual,
        [object] $Expected,
        [string] $Detail
    )
    $script:checks += [ordered]@{
        id = $Id
        passed = $Passed
        actual = $Actual
        expected = $Expected
        detail = $Detail
    }
}

$runStartedAt = Get-UtcTimestamp
$setupPath = [System.IO.Path]::GetFullPath($SetupArtifactPath)
$scriptPath = if ($PSCommandPath) { $PSCommandPath } else { $MyInvocation.MyCommand.Path }
$actualCaptureScriptSha256 = Get-FileSha256OrEmpty $scriptPath
$setupArtifactSha256 = Get-FileSha256OrEmpty $setupPath
$installDir = Find-EchoLauncherInstallDir
$preInstallLauncherPath = Find-EchoLauncherExe -InstallDir $installDir
$preInstallNoExistingEchoLauncher = -not (Test-Path -LiteralPath $installDir) -and -not (Test-Path -LiteralPath $preInstallLauncherPath)
$preInstallEchoProcesses = @(Get-EchoProcessSnapshot)
$preInstallNoRunningEchoProcesses = $preInstallEchoProcesses.Count -eq 0
$disposableEnvironment = Read-Confirmation "Confirm this is a disposable Windows VM/profile/user" ([bool] $DisposableEnvironmentConfirmed)
$freshProfileOrVmSnapshot = Read-Confirmation "Confirm this run started from a fresh profile or VM snapshot" ([bool] $FreshProfileOrVmSnapshotConfirmed)
$disposableEnvironmentEvidence = Read-TextEvidence "Describe the disposable environment proof (VM snapshot, disposable user/profile, or CI VM run id)" $DisposableEnvironmentEvidence
$freshProfileOrVmSnapshotEvidence = Read-TextEvidence "Describe the fresh profile or VM snapshot proof" $FreshProfileOrVmSnapshotEvidence
$preInstallCleanStateEvidence = Read-TextEvidence "Describe the pre-install clean-state proof" $PreInstallCleanStateEvidence

if (-not (Test-Path -LiteralPath $setupPath -PathType Leaf)) {
    throw "Setup artifact not found: $setupPath"
}
if (-not [string]::IsNullOrWhiteSpace($ExpectedSetupSha256) -and $setupArtifactSha256 -ine $ExpectedSetupSha256) {
    throw "Setup artifact SHA-256 mismatch. actual=$setupArtifactSha256 expected=$ExpectedSetupSha256"
}

$installerExitCode = $null
if (-not $SkipInstall) {
    Write-Host "Starting setup artifact: $setupPath"
    $installer = Start-Process -FilePath $setupPath -Wait -PassThru
    $installerExitCode = $installer.ExitCode
} else {
    Write-Warning "Skipping installer execution; evidence will not pass release audit unless install state already exists."
}

$installDir = Find-EchoLauncherInstallDir
$installedLauncherExePath = Find-EchoLauncherExe -InstallDir $installDir
$installedLauncherExeExists = Test-Path -LiteralPath $installedLauncherExePath -PathType Leaf
$installedLauncherExeSha256 = Get-FileSha256OrEmpty $installedLauncherExePath
$installedLauncherSignature = Get-AuthenticodeSnapshot $installedLauncherExePath

$startMenuShortcut = Join-Path $env:APPDATA "Microsoft\Windows\Start Menu\Programs\ECHO Launcher.lnk"
$desktopShortcut = Join-Path ([Environment]::GetFolderPath("Desktop")) "ECHO Launcher.lnk"
$startMenuShortcutPresent = Test-Path -LiteralPath $startMenuShortcut -PathType Leaf
$desktopShortcutPresent = Test-Path -LiteralPath $desktopShortcut -PathType Leaf
$uninstallEntry = Find-EchoUninstallEntry
$uninstallRegistryKeyPresent = $null -ne $uninstallEntry

if ($installedLauncherExeExists -and -not $RehearsalOnly) {
    Write-Host "Launching installed launcher: $installedLauncherExePath"
    Start-Process -FilePath $installedLauncherExePath | Out-Null
    Start-Sleep -Seconds 5
}
$launcherFirstRunOpened = Read-Confirmation "Did the installed ECHO Launcher open successfully" ([bool] $LauncherFirstRunConfirmed)
$runtimeDetected = Read-Confirmation "Did the launcher detect the standalone OpenGL runtime" ([bool] $RuntimeDetectedConfirmed)

if ([string]::IsNullOrWhiteSpace($SupportBundleArchivePath) -and -not $NonInteractive) {
    $SupportBundleArchivePath = Read-Host "Export a support bundle from the installed launcher, then paste the zip path"
}
$supportBundleExported = -not [string]::IsNullOrWhiteSpace($SupportBundleArchivePath) -and (Test-Path -LiteralPath $SupportBundleArchivePath -PathType Leaf)
$supportBundleArchiveSha256 = Get-FileSha256OrEmpty $SupportBundleArchivePath
$supportBundleManifestPresent = Test-SupportBundleManifest $SupportBundleArchivePath

if (-not $NonInteractive) {
    Read-Host "Close the ECHO Launcher, then press Enter to start uninstall" | Out-Null
}

$uninstallerExitCode = $null
if (-not $SkipUninstall) {
    $uninstallerCandidates = @(
        (Join-Path $installDir "Uninstall ECHO Launcher.exe"),
        (Join-Path $installDir "Uninstall ECHOLauncher.exe")
    )
    $uninstallerPath = $uninstallerCandidates | Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } | Select-Object -First 1
    if ($uninstallerPath) {
        Write-Host "Starting uninstaller: $uninstallerPath"
        $uninstaller = Start-Process -FilePath $uninstallerPath -Wait -PassThru
        $uninstallerExitCode = $uninstaller.ExitCode
    } elseif ($uninstallEntry -and -not [string]::IsNullOrWhiteSpace($uninstallEntry.uninstallString)) {
        Write-Host "Starting uninstall command from registry."
        $uninstaller = Invoke-CommandLineProcess -CommandLine $uninstallEntry.uninstallString
        $uninstallerExitCode = $uninstaller.ExitCode
    } else {
        Write-Warning "No ECHO Launcher uninstaller was found."
    }
} else {
    Write-Warning "Skipping uninstaller execution; evidence will not pass release audit unless uninstall state is already clean."
}

Start-Sleep -Seconds 3
$uninstallRemovedInstallDir = -not (Test-Path -LiteralPath $installDir)
$uninstallRemovedShortcuts = -not (Test-Path -LiteralPath $startMenuShortcut) -and -not (Test-Path -LiteralPath $desktopShortcut)
$postUninstallEchoProcesses = @(Get-EchoProcessSnapshot)
$postUninstallNoRunningEchoProcesses = $postUninstallEchoProcesses.Count -eq 0
$residualFilesOnlyAppData = Read-Confirmation "Confirm only expected AppData profile/cache residue remains after uninstall" ([bool] $ResidualFilesOnlyAppDataConfirmed)
$postUninstallCleanStateEvidence = Read-TextEvidence "Describe the post-uninstall cleanup proof" $PostUninstallCleanStateEvidence

$checks = @()
Add-Check "environment.disposable" $disposableEnvironment $disposableEnvironment $true "Evidence must be captured in a disposable Windows environment."
Add-Check "environment.disposableEvidence" (-not [string]::IsNullOrWhiteSpace($disposableEnvironmentEvidence)) $disposableEnvironmentEvidence "non-empty disposable environment proof" "Evidence must describe the disposable VM/profile/user or CI VM run used for capture."
Add-Check "environment.freshProfileOrVmSnapshot" $freshProfileOrVmSnapshot $freshProfileOrVmSnapshot $true "Evidence must start from a fresh profile or VM snapshot."
Add-Check "environment.freshProfileOrVmSnapshotEvidence" (-not [string]::IsNullOrWhiteSpace($freshProfileOrVmSnapshotEvidence)) $freshProfileOrVmSnapshotEvidence "non-empty fresh snapshot proof" "Evidence must describe the fresh profile or VM snapshot used for capture."
Add-Check "preinstall.noExistingLauncher" $preInstallNoExistingEchoLauncher $preInstallNoExistingEchoLauncher $true "No ECHO Launcher install may exist before install."
Add-Check "preinstall.noRunningEchoProcesses" $preInstallNoRunningEchoProcesses $preInstallNoRunningEchoProcesses $true "No ECHO launcher/runtime process may be running before install."
Add-Check "preinstall.cleanStateEvidence" (-not [string]::IsNullOrWhiteSpace($preInstallCleanStateEvidence)) $preInstallCleanStateEvidence "non-empty pre-install clean-state proof" "Evidence must describe how the pre-install clean state was verified."
Add-Check "setup.sha256" ([string]::IsNullOrWhiteSpace($ExpectedSetupSha256) -or $setupArtifactSha256 -ieq $ExpectedSetupSha256) $setupArtifactSha256 $ExpectedSetupSha256 "Setup artifact hash must match the expected release setup artifact."
Add-Check "captureScript.sha256" ([string]::IsNullOrWhiteSpace($CaptureScriptSha256) -or $actualCaptureScriptSha256 -ieq $CaptureScriptSha256) $actualCaptureScriptSha256 $CaptureScriptSha256 "Capture script hash must match the release evidence template."
Add-Check "installer.exitCode" ($installerExitCode -eq 0) $installerExitCode 0 "Installer must exit successfully."
Add-Check "installed.launcherExists" $installedLauncherExeExists $installedLauncherExeExists $true "Installed launcher executable must exist."
Add-Check "installed.launcherSha256" (-not [string]::IsNullOrWhiteSpace($installedLauncherExeSha256)) $installedLauncherExeSha256 "non-empty SHA-256" "Installed launcher executable hash must be recorded."
Add-Check "installed.launcherSha256MatchesCurrentArtifact" ([string]::IsNullOrWhiteSpace($ExpectedLauncherExeSha256) -or $installedLauncherExeSha256 -ieq $ExpectedLauncherExeSha256) $installedLauncherExeSha256 $ExpectedLauncherExeSha256 "Installed launcher hash must match the expected launcher artifact."
Add-Check "installed.launcherSignatureStatus" (-not [string]::IsNullOrWhiteSpace($installedLauncherSignature.status)) $installedLauncherSignature.status "recorded signature status" "Installed launcher signature status must be recorded."
Add-Check "shortcut.startMenu" $startMenuShortcutPresent $startMenuShortcutPresent $true "Start menu shortcut must exist after install."
Add-Check "registry.uninstallKey" $uninstallRegistryKeyPresent $uninstallRegistryKeyPresent $true "Uninstall registry entry must exist after install."
Add-Check "launcher.firstRun" $launcherFirstRunOpened $launcherFirstRunOpened $true "Installed launcher must open successfully."
Add-Check "launcher.runtimeDetected" $runtimeDetected $runtimeDetected $true "Launcher must detect the standalone OpenGL runtime."
Add-Check "launcher.supportBundleExported" $supportBundleExported $supportBundleExported $true "Installed launcher must export a support bundle archive."
Add-Check "launcher.supportBundleSha256" (-not [string]::IsNullOrWhiteSpace($supportBundleArchiveSha256)) $supportBundleArchiveSha256 "non-empty SHA-256" "Support bundle archive hash must be recorded."
Add-Check "launcher.supportBundleManifest" $supportBundleManifestPresent $supportBundleManifestPresent $true "Support bundle archive must include its manifest."
Add-Check "uninstaller.exitCode" ($uninstallerExitCode -eq 0) $uninstallerExitCode 0 "Uninstaller must exit successfully."
Add-Check "uninstall.removedInstallDir" $uninstallRemovedInstallDir $uninstallRemovedInstallDir $true "Uninstall must remove the install directory."
Add-Check "uninstall.removedShortcuts" $uninstallRemovedShortcuts $uninstallRemovedShortcuts $true "Uninstall must remove shortcuts."
Add-Check "uninstall.noRunningEchoProcesses" $postUninstallNoRunningEchoProcesses $postUninstallNoRunningEchoProcesses $true "Uninstall must leave no ECHO processes running."
Add-Check "uninstall.residualFiles" $residualFilesOnlyAppData $residualFilesOnlyAppData $true "Only expected AppData profile/cache residue may remain."
Add-Check "uninstall.cleanStateEvidence" (-not [string]::IsNullOrWhiteSpace($postUninstallCleanStateEvidence)) $postUninstallCleanStateEvidence "non-empty post-uninstall cleanup proof" "Evidence must describe the post-uninstall cleanup verification."

$failedChecks = @($checks | Where-Object { $_.passed -ne $true })
$status = if ($failedChecks.Count -eq 0) { "PASS" } else { "FAILED" }
$runEndedAt = Get-UtcTimestamp

$evidence = [ordered]@{
    schema = "echo.standalone.manual_install_uninstall_evidence.v1"
    status = $status
    rendererTarget = "opengl"
    nativeModLoaderCommandUsed = $false
    rehearsalOnly = [bool] $RehearsalOnly
    skipInstall = [bool] $SkipInstall
    skipUninstall = [bool] $SkipUninstall
    nonInteractive = [bool] $NonInteractive
    tester = $Tester
    runStartedAt = $runStartedAt
    runEndedAt = $runEndedAt
    machineId = $MachineId
    osVersion = Get-WindowsVersionText
    environmentType = $EnvironmentType
    disposableEnvironment = $disposableEnvironment
    disposableEnvironmentEvidence = $disposableEnvironmentEvidence
    freshProfileOrVmSnapshot = $freshProfileOrVmSnapshot
    freshProfileOrVmSnapshotEvidence = $freshProfileOrVmSnapshotEvidence
    captureScriptPath = Convert-PathForJson $scriptPath
    captureScriptSha256 = $actualCaptureScriptSha256
    preInstallNoExistingEchoLauncher = $preInstallNoExistingEchoLauncher
    preInstallNoRunningEchoProcesses = $preInstallNoRunningEchoProcesses
    preInstallCleanStateEvidence = $preInstallCleanStateEvidence
    preInstallEchoProcesses = @($preInstallEchoProcesses)
    setupArtifactPath = Convert-PathForJson $setupPath
    setupArtifactSha256 = $setupArtifactSha256
    expectedLauncherExeSha256 = $ExpectedLauncherExeSha256
    installerExitCode = $installerExitCode
    installDir = Convert-PathForJson $installDir
    installedLauncherExePath = Convert-PathForJson $installedLauncherExePath
    installedLauncherExeExists = $installedLauncherExeExists
    installedLauncherExeSha256 = $installedLauncherExeSha256
    installedLauncherExeSignatureStatus = $installedLauncherSignature.status
    installedLauncherExeSignatureSubject = $installedLauncherSignature.signerSubject
    startMenuShortcutPresent = $startMenuShortcutPresent
    desktopShortcutPresent = $desktopShortcutPresent
    uninstallRegistryKeyPresent = $uninstallRegistryKeyPresent
    aumid = ""
    launcherFirstRunOpened = $launcherFirstRunOpened
    runtimeDetected = $runtimeDetected
    supportBundleExported = $supportBundleExported
    supportBundleArchivePath = Convert-PathForJson $SupportBundleArchivePath
    supportBundleArchiveSha256 = $supportBundleArchiveSha256
    supportBundleManifestPresent = $supportBundleManifestPresent
    uninstallerExitCode = $uninstallerExitCode
    uninstallRemovedInstallDir = $uninstallRemovedInstallDir
    uninstallRemovedShortcuts = $uninstallRemovedShortcuts
    postUninstallNoRunningEchoProcesses = $postUninstallNoRunningEchoProcesses
    postUninstallEchoProcesses = @($postUninstallEchoProcesses)
    residualFilesOnlyAppData = $residualFilesOnlyAppData
    postUninstallCleanStateEvidence = $postUninstallCleanStateEvidence
    oldSpacedInstallDirMigrationStatus = $OldSpacedInstallDirMigrationStatus
    checks = @($checks)
    failedChecks = @($failedChecks | ForEach-Object { $_.id })
    notes = "Generated by scripts/capture-clean-install-uninstall-evidence.ps1 in a disposable Windows profile or VM."
}

$outputFullPath = [System.IO.Path]::GetFullPath($OutputPath)
$outputParent = Split-Path -Parent $outputFullPath
if (-not [string]::IsNullOrWhiteSpace($outputParent)) {
    New-Item -ItemType Directory -Force -Path $outputParent | Out-Null
}
$evidence | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath $outputFullPath -Encoding UTF8
Write-Host "Wrote clean install/uninstall evidence: $outputFullPath"
Write-Host "Evidence status: $status"

if ($status -ne "PASS") {
    Write-Warning ("Evidence is incomplete: " + (($failedChecks | ForEach-Object { $_.id }) -join ", "))
    exit 2
}
