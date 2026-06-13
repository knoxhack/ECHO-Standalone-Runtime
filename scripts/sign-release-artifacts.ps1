#requires -Version 5.1
[CmdletBinding()]
param(
    [string] $RuntimeExePath = "build/jpackage-opengl-client/EchoStandaloneRuntime/EchoStandaloneRuntime.exe",
    [string] $LauncherExePath = "../ECHO-Launcher/installer-artifacts/win-unpacked/ECHOLauncher.exe",
    [string] $LauncherSetupExePath = "../ECHO-Launcher/installer-artifacts/ECHO-Launcher-1.0.4-Setup.exe",
    [string] $OutputPath = "reports/echo/standalone/release-signing-evidence.json",

    [string] $CertificateThumbprint = "",
    [string] $CertificateSubject = "",
    [string] $PublisherName = "",
    [string] $TimestampServer = "http://timestamp.digicert.com",
    [string] $HashAlgorithm = "SHA256",
    [string] $ExpectedRuntimeExeSha256 = "",
    [string] $ExpectedLauncherExeSha256 = "",
    [string] $ExpectedLauncherSetupExeSha256 = "",
    [string] $SigningOperator = $env:USERNAME,
    [string] $SigningRunEvidence = "",

    [switch] $Sign,
    [switch] $RequireTimestamp = $true
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

function Normalize-Thumbprint {
    param([string] $Thumbprint)
    if ([string]::IsNullOrWhiteSpace($Thumbprint)) {
        return ""
    }
    return ($Thumbprint -replace "[^A-Fa-f0-9]", "").ToUpperInvariant()
}

function Get-AuthenticodeSnapshot {
    param(
        [string] $ArtifactId,
        [string] $Path
    )
    $exists = -not [string]::IsNullOrWhiteSpace($Path) -and (Test-Path -LiteralPath $Path -PathType Leaf)
    if (-not $exists) {
        return [ordered]@{
            artifactId = $ArtifactId
            path = Convert-PathForJson $Path
            exists = $false
            bytes = 0
            sha256 = ""
            status = "MISSING"
            signed = $false
            signerSubject = ""
            signerIssuer = ""
            signerThumbprint = ""
            timestampSubject = ""
            timestampPresent = $false
            rawStatusMessage = "Artifact is missing."
        }
    }
    $signature = Get-AuthenticodeSignature -LiteralPath $Path
    $timestampSubject = if ($signature.TimeStamperCertificate) { [string] $signature.TimeStamperCertificate.Subject } else { "" }
    return [ordered]@{
        artifactId = $ArtifactId
        path = Convert-PathForJson $Path
        exists = $true
        bytes = (Get-Item -LiteralPath $Path).Length
        sha256 = Get-FileSha256OrEmpty $Path
        status = [string] $signature.Status
        signed = [string] $signature.Status -eq "Valid"
        signerSubject = if ($signature.SignerCertificate) { [string] $signature.SignerCertificate.Subject } else { "" }
        signerIssuer = if ($signature.SignerCertificate) { [string] $signature.SignerCertificate.Issuer } else { "" }
        signerThumbprint = if ($signature.SignerCertificate) { [string] $signature.SignerCertificate.Thumbprint } else { "" }
        timestampSubject = $timestampSubject
        timestampPresent = -not [string]::IsNullOrWhiteSpace($timestampSubject)
        rawStatusMessage = [string] $signature.StatusMessage
    }
}

function Find-CodeSigningCertificate {
    param(
        [string] $Thumbprint,
        [string] $Subject
    )
    $normalizedThumbprint = Normalize-Thumbprint $Thumbprint
    $stores = @("Cert:\CurrentUser\My", "Cert:\LocalMachine\My")
    foreach ($store in $stores) {
        if (-not (Test-Path -LiteralPath $store)) {
            continue
        }
        $candidates = @(Get-ChildItem -LiteralPath $store -CodeSigningCert -ErrorAction SilentlyContinue)
        foreach ($candidate in $candidates) {
            $candidateThumbprint = Normalize-Thumbprint $candidate.Thumbprint
            $thumbprintMatches = -not [string]::IsNullOrWhiteSpace($normalizedThumbprint) -and $candidateThumbprint -eq $normalizedThumbprint
            $subjectMatches = -not [string]::IsNullOrWhiteSpace($Subject) -and $candidate.Subject -like "*$Subject*"
            if ($thumbprintMatches -or $subjectMatches) {
                return $candidate
            }
        }
    }
    return $null
}

function Test-ReleaseIdentityMatch {
    param(
        [object] $Snapshot,
        [string] $Thumbprint,
        [string] $Subject,
        [string] $Publisher
    )
    $expectedThumbprint = Normalize-Thumbprint $Thumbprint
    $actualThumbprint = Normalize-Thumbprint ([string] $Snapshot.signerThumbprint)
    $actualSubject = [string] $Snapshot.signerSubject
    if (-not [string]::IsNullOrWhiteSpace($expectedThumbprint)) {
        return $actualThumbprint -eq $expectedThumbprint
    }
    if (-not [string]::IsNullOrWhiteSpace($Subject)) {
        return $actualSubject.ToLowerInvariant().Contains($Subject.ToLowerInvariant())
    }
    if (-not [string]::IsNullOrWhiteSpace($Publisher)) {
        return $actualSubject.ToLowerInvariant().Contains($Publisher.ToLowerInvariant())
    }
    return $false
}

function Invoke-Signing {
    param(
        [string] $Path,
        [System.Security.Cryptography.X509Certificates.X509Certificate2] $Certificate
    )
    $result = Set-AuthenticodeSignature -LiteralPath $Path -Certificate $Certificate -TimestampServer $TimestampServer -HashAlgorithm $HashAlgorithm
    return [ordered]@{
        path = Convert-PathForJson $Path
        status = [string] $result.Status
        statusMessage = [string] $result.StatusMessage
    }
}

$artifactInputs = @(
    [ordered]@{ artifactId = "runtime-exe"; path = [System.IO.Path]::GetFullPath($RuntimeExePath); expectedUnsignedSha256 = $ExpectedRuntimeExeSha256 },
    [ordered]@{ artifactId = "launcher-exe"; path = [System.IO.Path]::GetFullPath($LauncherExePath); expectedUnsignedSha256 = $ExpectedLauncherExeSha256 },
    [ordered]@{ artifactId = "launcher-setup-exe"; path = [System.IO.Path]::GetFullPath($LauncherSetupExePath); expectedUnsignedSha256 = $ExpectedLauncherSetupExeSha256 }
)
$scriptPath = if ($PSCommandPath) { $PSCommandPath } else { $MyInvocation.MyCommand.Path }
$scriptSha256 = Get-FileSha256OrEmpty $scriptPath
$certificate = Find-CodeSigningCertificate -Thumbprint $CertificateThumbprint -Subject $CertificateSubject
$signingResults = @()
$signingPreflightErrors = @()
$preSigningArtifacts = @($artifactInputs | ForEach-Object {
    $snapshot = Get-AuthenticodeSnapshot -ArtifactId $_.artifactId -Path $_.path
    $expectedSha256 = [string] $_.expectedUnsignedSha256
    $expectedConfigured = -not [string]::IsNullOrWhiteSpace($expectedSha256)
    [ordered]@{
        artifactId = $snapshot.artifactId
        path = $snapshot.path
        exists = $snapshot.exists
        bytes = $snapshot.bytes
        sha256 = $snapshot.sha256
        status = $snapshot.status
        signed = $snapshot.signed
        expectedUnsignedSha256 = $expectedSha256
        expectedUnsignedSha256Configured = $expectedConfigured
        expectedUnsignedSha256Matches = $expectedConfigured -and $snapshot.sha256 -ieq $expectedSha256
    }
})
$preSigningArtifactsById = @{}
foreach ($preSigningArtifact in $preSigningArtifacts) {
    $preSigningArtifactsById[[string] $preSigningArtifact.artifactId] = $preSigningArtifact
}

if ($Sign) {
    if ($null -eq $certificate) {
        $signingPreflightErrors += "No matching code signing certificate found in Cert:\CurrentUser\My or Cert:\LocalMachine\My."
    }
    if ([string]::IsNullOrWhiteSpace($TimestampServer)) {
        $signingPreflightErrors += "TimestampServer is required when signing public-release artifacts."
    }
    foreach ($artifact in $artifactInputs) {
        if (-not (Test-Path -LiteralPath $artifact.path -PathType Leaf)) {
            $signingPreflightErrors += "Cannot sign missing artifact: $($artifact.path)"
        }
        $preSigningArtifact = $preSigningArtifactsById[[string] $artifact.artifactId]
        if ([string]::IsNullOrWhiteSpace([string] $artifact.expectedUnsignedSha256)) {
            $signingPreflightErrors += "Expected unsigned SHA-256 is required for $($artifact.artifactId) when signing public-release artifacts."
        } elseif ($null -eq $preSigningArtifact -or $preSigningArtifact.expectedUnsignedSha256Matches -ne $true) {
            $signingPreflightErrors += "Expected unsigned SHA-256 did not match $($artifact.artifactId)."
        }
    }
    if ([string]::IsNullOrWhiteSpace($SigningOperator)) {
        $signingPreflightErrors += "SigningOperator is required when signing public-release artifacts."
    }
    if ([string]::IsNullOrWhiteSpace($SigningRunEvidence)) {
        $signingPreflightErrors += "SigningRunEvidence is required when signing public-release artifacts."
    }
    if ($signingPreflightErrors.Count -eq 0) {
        foreach ($artifact in $artifactInputs) {
            Write-Host "Signing $($artifact.artifactId): $($artifact.path)"
            try {
                $signingResults += Invoke-Signing -Path $artifact.path -Certificate $certificate
            } catch {
                $signingResults += [ordered]@{
                    path = Convert-PathForJson $artifact.path
                    status = "EXCEPTION"
                    statusMessage = $_.Exception.Message
                }
            }
        }
    }
}

$artifacts = @($artifactInputs | ForEach-Object {
    $snapshot = Get-AuthenticodeSnapshot -ArtifactId $_.artifactId -Path $_.path
    $preSigningArtifact = $preSigningArtifactsById[[string] $_.artifactId]
    $identityMatched = Test-ReleaseIdentityMatch -Snapshot $snapshot -Thumbprint $CertificateThumbprint -Subject $CertificateSubject -Publisher $PublisherName
    $publicReady = [bool] $snapshot.exists `
        -and [bool] $snapshot.signed `
        -and (-not $RequireTimestamp -or [bool] $snapshot.timestampPresent) `
        -and $identityMatched
    [ordered]@{
        artifactId = $snapshot.artifactId
        path = $snapshot.path
        exists = $snapshot.exists
        bytes = $snapshot.bytes
        sha256 = $snapshot.sha256
        preSigningSha256 = if ($null -ne $preSigningArtifact) { $preSigningArtifact.sha256 } else { "" }
        expectedUnsignedSha256 = [string] $_.expectedUnsignedSha256
        expectedUnsignedSha256Configured = if ($null -ne $preSigningArtifact) { $preSigningArtifact.expectedUnsignedSha256Configured } else { $false }
        expectedUnsignedSha256Matches = if ($null -ne $preSigningArtifact) { $preSigningArtifact.expectedUnsignedSha256Matches } else { $false }
        status = $snapshot.status
        signed = $snapshot.signed
        signerSubject = $snapshot.signerSubject
        signerIssuer = $snapshot.signerIssuer
        signerThumbprint = $snapshot.signerThumbprint
        timestampSubject = $snapshot.timestampSubject
        timestampPresent = $snapshot.timestampPresent
        releaseIdentityMatched = $identityMatched
        publicReleaseSignatureReady = $publicReady
        rawStatusMessage = $snapshot.rawStatusMessage
    }
})

$identityConfigured = -not [string]::IsNullOrWhiteSpace($CertificateThumbprint) `
    -or -not [string]::IsNullOrWhiteSpace($CertificateSubject) `
    -or -not [string]::IsNullOrWhiteSpace($PublisherName)
$expectedUnsignedHashesConfigured = @($preSigningArtifacts | Where-Object { -not $_.expectedUnsignedSha256Configured }).Count -eq 0
$expectedUnsignedHashesMatched = $expectedUnsignedHashesConfigured -and @($preSigningArtifacts | Where-Object { -not $_.expectedUnsignedSha256Matches }).Count -eq 0
$signingAttestationReady = -not [string]::IsNullOrWhiteSpace($SigningOperator) -and -not [string]::IsNullOrWhiteSpace($SigningRunEvidence)
$allReady = [bool] $Sign `
    -and $identityConfigured `
    -and $expectedUnsignedHashesConfigured `
    -and $expectedUnsignedHashesMatched `
    -and $signingAttestationReady `
    -and @($artifacts | Where-Object { -not $_.publicReleaseSignatureReady }).Count -eq 0
$report = [ordered]@{
    schema = "echo.standalone.release_signing_evidence.v1"
    generatedAt = Get-UtcTimestamp
    generator = "scripts/sign-release-artifacts.ps1"
    status = if ($allReady) { "PASS" } elseif ($Sign) { "FAILED" } else { "PENDING_SIGNING" }
    summary = if ($allReady) {
        "All public-release EXE artifacts are signed, timestamped, and matched to the configured release identity."
    } elseif ($Sign) {
        "Signing was requested, but one or more artifacts still failed public-release signature validation."
    } else {
        "Signing helper ran in verify/plan mode; pass -Sign with a configured certificate to sign release artifacts."
    }
    signRequested = [bool] $Sign
    timestampServer = $TimestampServer
    hashAlgorithm = $HashAlgorithm
    requireTimestamp = [bool] $RequireTimestamp
    identityConfigured = $identityConfigured
    certificateFound = $null -ne $certificate
    expectedCertificateThumbprint = Normalize-Thumbprint $CertificateThumbprint
    expectedCertificateSubject = $CertificateSubject
    expectedPublisherName = $PublisherName
    signingOperator = $SigningOperator
    signingRunEvidence = $SigningRunEvidence
    signingAttestationReady = $signingAttestationReady
    expectedUnsignedHashesConfigured = $expectedUnsignedHashesConfigured
    expectedUnsignedHashesMatched = $expectedUnsignedHashesMatched
    preSigningArtifacts = @($preSigningArtifacts)
    signingPreflightErrors = @($signingPreflightErrors)
    captureScriptPath = Convert-PathForJson $scriptPath
    captureScriptSha256 = $scriptSha256
    signingResults = @($signingResults)
    artifacts = @($artifacts)
    requiredNextActions = if ($allReady) { @() } else { @(
        "Import or select a trusted code-signing certificate in Cert:\CurrentUser\My or Cert:\LocalMachine\My.",
        "Run this helper with -Sign, -CertificateThumbprint or -CertificateSubject, expected unsigned artifact SHA-256 values, SigningOperator, SigningRunEvidence, and a timestamp server.",
        "Regenerate reports/echo/standalone/distribution-signing-setup.json with runStandaloneDistributionSigningSetupAudit."
    ) }
}

$outputFullPath = [System.IO.Path]::GetFullPath($OutputPath)
$outputParent = Split-Path -Parent $outputFullPath
if (-not [string]::IsNullOrWhiteSpace($outputParent)) {
    New-Item -ItemType Directory -Force -Path $outputParent | Out-Null
}
$report | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath $outputFullPath -Encoding UTF8
Write-Host "Wrote release signing evidence: $outputFullPath"
Write-Host "Signing evidence status: $($report.status)"

if (-not $allReady) {
    exit 2
}
