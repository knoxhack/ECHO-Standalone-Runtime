param(
    [Parameter(Mandatory = $true)]
    [string]$WorkspaceRoot,
    [switch]$Apply,
    [switch]$RunBuild
)

$ErrorActionPreference = "Stop"
$KitRoot = Split-Path -Parent $PSScriptRoot
$RuntimeRepo = Join-Path $WorkspaceRoot "ECHO-Standalone-Runtime"
$AshfallRepo = Join-Path $WorkspaceRoot "ECHO-Ashfall-Standalone-Edition"
$NeoForgeRepo = Join-Path $WorkspaceRoot "ECHO-Ashfall-NeoForge-Edition"

function Invoke-PatchCheck {
    param([string]$Repo, [string]$Patch)
    Push-Location $Repo
    try {
        git apply --check $Patch
        if ($Apply) {
            git apply $Patch
            Write-Host "Applied $Patch" -ForegroundColor Green
        } else {
            Write-Host "Patch check passed: $Patch" -ForegroundColor Green
        }
    }
    finally {
        Pop-Location
    }
}

$runtimePatches = @(
    (Join-Path $KitRoot "patches\runtime\0001-fix-double-frame-limiter.patch"),
    (Join-Path $KitRoot "patches\runtime\0002-fail-closed-evidence.patch"),
    (Join-Path $KitRoot "patches\runtime\0003-installed-pack-root-discovery.patch")
)
foreach ($patch in $runtimePatches) {
    Invoke-PatchCheck -Repo $RuntimeRepo -Patch $patch
}
Invoke-PatchCheck -Repo $AshfallRepo -Patch (Join-Path $KitRoot "patches\ashfall\0001-fix-standalone-launch-contract.patch")

node (Join-Path $KitRoot "scripts\verify-ashfall-standalone-manifest.mjs") `
    (Join-Path $AshfallRepo "release-manifest.template.json")
node (Join-Path $KitRoot "scripts\compare-ashfall-manifests.mjs") `
    (Join-Path $NeoForgeRepo "release-manifest.template.json") `
    (Join-Path $AshfallRepo "release-manifest.template.json")
node (Join-Path $KitRoot "scripts\verify-runtime-wiring.mjs") `
    $RuntimeRepo `
    (Join-Path $AshfallRepo "release-manifest.template.json")

if ($RunBuild) {
    Push-Location $RuntimeRepo
    try {
        .\gradlew.bat --no-daemon clean build
        .\gradlew.bat --no-daemon runStandaloneClientVSyncSmoke
        .\gradlew.bat --no-daemon runStandaloneRealModuleExecutionSmoke
        .\gradlew.bat --no-daemon runStandaloneBetaReadinessGate
        node (Join-Path $KitRoot "scripts\verify-standalone-evidence.mjs") $RuntimeRepo
    }
    finally {
        Pop-Location
    }
}
