#requires -Version 5.1
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("packaged-opengl-30m", "packaged-opengl-60m")]
    [string] $SessionId,

    [string] $PackagedExePath = "build/jpackage-opengl-client/EchoStandaloneRuntime/EchoStandaloneRuntime.exe",
    [string] $PortableZipPath = "build/distributions/EchoStandaloneRuntime-portable-opengl-client.zip",
    [string] $OutputPath = "reports/echo/standalone/manual-playtest-report.json",
    [string] $ExpectedPackagedExeSha256 = "",
    [string] $ExpectedPortableZipSha256 = "",
    [string] $CaptureScriptSha256 = "",

    [string] $Tester = $env:USERNAME,
    [string] $IssuesNote = "",
    [string] $HumanTesterAttestation = "",
    [string] $InteractionCoverageNote = "",
    [string] $SaveLoadCheckpointNote = "",
    [string] $CrashFreeExitNote = "",

    [switch] $SaveLoadCheckpointPassed,
    [switch] $CrashFreeExitPassed,
    [switch] $SurvivalRouteCoveragePassed,
    [switch] $DeathRecoveryCoveragePassed,
    [switch] $NoIssues,
    [switch] $NonInteractive,
    [switch] $SkipLaunch
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

function Read-BooleanEvidence {
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

function Get-WindowTitleForProcess {
    param([int] $ProcessId)
    try {
        $process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
        if ($null -eq $process) {
            return ""
        }
        return [string] $process.MainWindowTitle
    } catch {
        return ""
    }
}

function Read-ExistingManualReport {
    param([string] $Path)
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        return $null
    }
    try {
        return Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json
    } catch {
        return $null
    }
}

function Convert-SessionObject {
    param([object] $Session)
    $ordered = [ordered]@{}
    if ($null -ne $Session) {
        foreach ($property in $Session.PSObject.Properties) {
            $ordered[$property.Name] = $property.Value
        }
    }
    return $ordered
}

function New-PendingSession {
    param(
        [string] $Id,
        [int] $DurationMinutes
    )
    $requiredEvidence = @(
        "tester identity",
        "start and end wall-clock timestamps",
        "packaged EXE path and SHA-256",
        "human tester attestation",
        "interaction coverage note",
        "save/load checkpoint result",
        "save/load checkpoint note",
        "crash-free exit result",
        "crash-free exit note",
        "issues found or explicit no-issues note"
    )
    if ($Id -eq "packaged-opengl-60m") {
        $requiredEvidence = @(
            "tester identity",
            "start and end wall-clock timestamps",
            "packaged EXE path and SHA-256",
            "human tester attestation",
            "interaction coverage note",
            "survival route coverage",
            "death/recovery coverage",
            "save/load checkpoint result",
            "save/load checkpoint note",
            "crash-free exit result",
            "crash-free exit note",
            "issues found or explicit no-issues note"
        )
    }
    return [ordered]@{
        id = $Id
        status = "PENDING"
        durationMinutes = $DurationMinutes
        requiredEvidence = $requiredEvidence
    }
}

$requiredMinutesBySession = @{
    "packaged-opengl-30m" = 30
    "packaged-opengl-60m" = 60
}
$requiredMinutes = [int] $requiredMinutesBySession[$SessionId]
$requiredSeconds = $requiredMinutes * 60
$sampleIntervalSeconds = 15
$scriptPath = if ($PSCommandPath) { $PSCommandPath } else { $MyInvocation.MyCommand.Path }
$actualCaptureScriptSha256 = Get-FileSha256OrEmpty $scriptPath
$exeFullPath = [System.IO.Path]::GetFullPath($PackagedExePath)
$portableZipFullPath = [System.IO.Path]::GetFullPath($PortableZipPath)
$packagedExeSha256 = Get-FileSha256OrEmpty $exeFullPath
$portableZipSha256 = Get-FileSha256OrEmpty $portableZipFullPath

if (-not (Test-Path -LiteralPath $exeFullPath -PathType Leaf)) {
    throw "Packaged EXE not found: $exeFullPath"
}
if (-not (Test-Path -LiteralPath $portableZipFullPath -PathType Leaf)) {
    throw "Portable ZIP not found: $portableZipFullPath"
}
if (-not [string]::IsNullOrWhiteSpace($ExpectedPackagedExeSha256) -and $packagedExeSha256 -ine $ExpectedPackagedExeSha256) {
    throw "Packaged EXE SHA-256 mismatch. actual=$packagedExeSha256 expected=$ExpectedPackagedExeSha256"
}
if (-not [string]::IsNullOrWhiteSpace($ExpectedPortableZipSha256) -and $portableZipSha256 -ine $ExpectedPortableZipSha256) {
    throw "Portable ZIP SHA-256 mismatch. actual=$portableZipSha256 expected=$ExpectedPortableZipSha256"
}
if (-not [string]::IsNullOrWhiteSpace($CaptureScriptSha256) -and $actualCaptureScriptSha256 -ine $CaptureScriptSha256) {
    throw "Capture script SHA-256 mismatch. actual=$actualCaptureScriptSha256 expected=$CaptureScriptSha256"
}

$startedAt = Get-UtcTimestamp
$process = $null
$samples = @()
$processSamples = 0
$visibleSamples = 0
$missingProcessSamples = 0
$firstVisibleSecond = 0
$windowTitle = ""
$exitCode = $null
$aliveAtEnd = $false

if (-not $SkipLaunch) {
    Write-Host "Starting packaged OpenGL EXE for $SessionId ($requiredMinutes minutes): $exeFullPath"
    $process = Start-Process -FilePath $exeFullPath -PassThru
    $deadline = (Get-Date).ToUniversalTime().AddSeconds($requiredSeconds)
    while ((Get-Date).ToUniversalTime() -lt $deadline) {
        Start-Sleep -Seconds $sampleIntervalSeconds
        $elapsedSeconds = [int] [Math]::Min([int]::MaxValue, ((Get-Date).ToUniversalTime() - [DateTime]::Parse($startedAt).ToUniversalTime()).TotalSeconds)
        $alive = $false
        $title = ""
        try {
            $current = Get-Process -Id $process.Id -ErrorAction SilentlyContinue
            $alive = $null -ne $current
            if ($alive) {
                $title = [string] $current.MainWindowTitle
            }
        } catch {
            $alive = $false
        }
        if ($alive) {
            $processSamples++
        } else {
            $missingProcessSamples++
        }
        $visible = -not [string]::IsNullOrWhiteSpace($title)
        if ($visible) {
            $visibleSamples++
            if ($firstVisibleSecond -eq 0) {
                $firstVisibleSecond = $elapsedSeconds
            }
            if ([string]::IsNullOrWhiteSpace($windowTitle)) {
                $windowTitle = $title
            }
        }
        $samples += [ordered]@{
            elapsedSeconds = $elapsedSeconds
            pidAlive = $alive
            visible = $visible
            windowTitle = $title
        }
        if (-not $alive) {
            break
        }
    }
    try {
        $aliveAtEnd = $null -ne (Get-Process -Id $process.Id -ErrorAction SilentlyContinue)
        if ($aliveAtEnd) {
            if (-not $NonInteractive) {
                Read-Host "Finish the session, save/quit from the game if needed, then press Enter to close the process" | Out-Null
            }
            Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 2
        }
        if ($process.HasExited) {
            $exitCode = $process.ExitCode
        }
    } catch {
        $exitCode = $null
    }
} else {
    Write-Warning "Skipping launch; evidence will not pass unless external session details are also valid."
}

$endedAt = Get-UtcTimestamp
$completedSeconds = [int] [Math]::Min([int]::MaxValue, ([DateTime]::Parse($endedAt).ToUniversalTime() - [DateTime]::Parse($startedAt).ToUniversalTime()).TotalSeconds)
$completedMinutes = [int] [Math]::Floor($completedSeconds / 60)
$saveLoadPassed = Read-BooleanEvidence "Did save/quit/continue restore objective, inventory, terminal notes, and HUD state" ([bool] $SaveLoadCheckpointPassed)
$crashFreePassed = Read-BooleanEvidence "Did the session exit without crash or softlock" ([bool] $CrashFreeExitPassed)
$humanTesterAttestationText = Read-TextEvidence "Attest that a human tester actively played this full wall-clock session" $HumanTesterAttestation
$interactionCoverageText = Read-TextEvidence "Describe inventory, terminal, pause/resume, and alt-tab interaction coverage" $InteractionCoverageNote
$saveLoadCheckpointText = Read-TextEvidence "Describe the save/quit/continue checkpoint result" $SaveLoadCheckpointNote
$crashFreeExitText = Read-TextEvidence "Describe the crash-free exit or softlock result" $CrashFreeExitNote
$survivalRoutePassed = $true
$deathRecoveryPassed = $true
if ($SessionId -eq "packaged-opengl-60m") {
    $survivalRoutePassed = Read-BooleanEvidence "Did this session cover the survival route objectives" ([bool] $SurvivalRouteCoveragePassed)
    $deathRecoveryPassed = Read-BooleanEvidence "Did this session cover death or recovery behavior" ([bool] $DeathRecoveryCoveragePassed)
}
if ($NoIssues -and [string]::IsNullOrWhiteSpace($IssuesNote)) {
    $IssuesNote = "No issues found."
}
$issuesText = Read-TextEvidence "Issues found, or explicit no-issues note" $IssuesNote
$humanSessionAccepted = $completedMinutes -ge $requiredMinutes `
    -and $processSamples -gt 0 `
    -and $missingProcessSamples -eq 0 `
    -and $visibleSamples -gt 0 `
    -and $saveLoadPassed `
    -and $crashFreePassed `
    -and -not [string]::IsNullOrWhiteSpace($humanTesterAttestationText) `
    -and -not [string]::IsNullOrWhiteSpace($interactionCoverageText) `
    -and -not [string]::IsNullOrWhiteSpace($saveLoadCheckpointText) `
    -and -not [string]::IsNullOrWhiteSpace($crashFreeExitText) `
    -and $survivalRoutePassed `
    -and $deathRecoveryPassed `
    -and -not [string]::IsNullOrWhiteSpace($issuesText)

$sessionEvidence = [ordered]@{
    id = $SessionId
    status = if ($humanSessionAccepted) { "PASS" } else { "FAILED" }
    durationMinutes = $completedMinutes
    requiredMinutes = $requiredMinutes
    tester = $Tester
    startedAt = $startedAt
    endedAt = $endedAt
    packagedExePath = Convert-PathForJson $exeFullPath
    packagedExeSha256 = $packagedExeSha256
    portableZipPath = Convert-PathForJson $portableZipFullPath
    portableZipSha256 = $portableZipSha256
    captureScriptPath = Convert-PathForJson $scriptPath
    captureScriptSha256 = $actualCaptureScriptSha256
    humanTesterAttestation = $humanTesterAttestationText
    interactionCoverageNote = $interactionCoverageText
    saveLoadCheckpointResult = if ($saveLoadPassed) { "PASS" } else { "FAILED" }
    saveLoadCheckpointNote = $saveLoadCheckpointText
    crashFreeExitResult = if ($crashFreePassed) { "PASS" } else { "FAILED" }
    crashFreeExitNote = $crashFreeExitText
    survivalRouteCoverage = if ($survivalRoutePassed) { "PASS" } else { "FAILED" }
    deathRecoveryCoverage = if ($deathRecoveryPassed) { "PASS" } else { "FAILED" }
    issuesNote = $issuesText
    processSamples = $processSamples
    visibleSamples = $visibleSamples
    missingProcessSamples = $missingProcessSamples
    firstVisibleSecond = $firstVisibleSecond
    windowTitle = $windowTitle
    exitCodeAfterCleanup = $exitCode
    replacesHumanPlaytest = $false
    nativeModLoaderCommandUsed = $false
    samples = @($samples)
}

$outputFullPath = [System.IO.Path]::GetFullPath($OutputPath)
$existing = Read-ExistingManualReport $outputFullPath
$sessions = @()
if ($null -ne $existing -and $null -ne $existing.requiredSessions) {
    foreach ($session in @($existing.requiredSessions)) {
        if ([string] $session.id -ne $SessionId) {
            $sessions += Convert-SessionObject $session
        }
    }
}
if (-not (@($sessions | ForEach-Object { $_.id }) -contains "packaged-opengl-30m") -and $SessionId -ne "packaged-opengl-30m") {
    $sessions += New-PendingSession "packaged-opengl-30m" 30
}
if (-not (@($sessions | ForEach-Object { $_.id }) -contains "packaged-opengl-60m") -and $SessionId -ne "packaged-opengl-60m") {
    $sessions += New-PendingSession "packaged-opengl-60m" 60
}
$sessions += $sessionEvidence
$sessions = @($sessions | Sort-Object @{ Expression = { if ($_.id -eq "packaged-opengl-30m") { 0 } else { 1 } } })
$complete = @($sessions | Where-Object { $_.status -eq "PASS" }).Count -eq 2

$report = [ordered]@{
    schema = "echo.standalone.manual_playtest_report.v1"
    generatedAt = Get-UtcTimestamp
    generator = "scripts/capture-manual-wallclock-playtest.ps1"
    status = if ($complete) { "PASS" } else { "PENDING" }
    summary = if ($complete) {
        "Manual wall-clock playtest evidence is complete for packaged OpenGL sessions."
    } else {
        "Manual wall-clock playtest evidence is partially captured; remaining session evidence is still required."
    }
    manualEvidenceComplete = $complete
    nativeModLoaderCommandUsed = $false
    replacesHumanPlaytest = $false
    captureScriptPath = Convert-PathForJson $scriptPath
    captureScriptSha256 = $actualCaptureScriptSha256
    requiredSessions = @($sessions)
    automatedEvidence = @(
        "reports/echo/standalone/packaged-exe-wallclock-smoke.json",
        "reports/echo/standalone/packaged-exe-wallclock-strict-rehearsal.json",
        "reports/echo/standalone/packaged-exe-wallclock-strict-30m.json",
        "reports/echo/standalone/beta-readiness-playable-qa.json"
    )
}

$outputParent = Split-Path -Parent $outputFullPath
if (-not [string]::IsNullOrWhiteSpace($outputParent)) {
    New-Item -ItemType Directory -Force -Path $outputParent | Out-Null
}
$report | ConvertTo-Json -Depth 16 | Set-Content -LiteralPath $outputFullPath -Encoding UTF8
Write-Host "Wrote manual wall-clock playtest evidence: $outputFullPath"
Write-Host "Session status: $($sessionEvidence.status)"
Write-Host "Report status: $($report.status)"

if ($sessionEvidence.status -ne "PASS") {
    exit 2
}
