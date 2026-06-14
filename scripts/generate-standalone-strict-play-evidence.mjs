#!/usr/bin/env node
import { promises as fs } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const DEFAULT_ROOT = process.cwd()

const INPUTS = {
  runtimeModuleStatus: 'reports/echo/standalone/runtime-module-status.json',
  realModuleExecution: 'reports/echo/standalone/real-module-execution-smoke.json',
  nativeLoaderAbi: 'reports/echo/standalone/native-loader-abi-v1-smoke.json',
  adapterCoreCoverage: 'reports/echo/standalone/runtime-adaptercore-module-coverage.json',
  uiParity: 'reports/echo/standalone/agent5-ui-parity-smoke.json',
  screenCatalog: 'reports/echo/standalone/client-screen-catalog-smoke.json',
  modsRuntimeContent: 'reports/echo/standalone/client-mods-runtime-content-smoke.json',
  worldInteraction: 'reports/echo/standalone/client-world-interaction-smoke.json',
  heldItemOverlay: 'reports/echo/standalone/client-held-item-overlay-smoke.json',
  saveContinue: 'reports/echo/standalone/client-save-continue.json',
  fullWorldgen: 'reports/echo/standalone/full-worldgen-dimensions-structures.json',
}

const OUTPUTS = {
  fullCatalog: 'reports/echo/standalone/full-catalog-play.json',
  uiSurfaces: 'reports/echo/standalone/client-ui-surfaces-play.json',
  voxelContent: 'reports/echo/standalone/voxel-content-play.json',
  blockActions: 'reports/echo/standalone/block-action-mutations.json',
  worldgen: 'reports/echo/standalone/worldgen-play.json',
  saveReload: 'reports/echo/standalone/save-reload-play.json',
}

export async function generateStandaloneStrictPlayEvidence({ root = DEFAULT_ROOT } = {}) {
  const repoRoot = path.resolve(root)
  const generatedAt = new Date().toISOString()
  const inputs = {}
  for (const [key, relativePath] of Object.entries(INPUTS)) {
    inputs[key] = await readReport(repoRoot, relativePath)
  }

  const outputs = {
    [OUTPUTS.fullCatalog]: fullCatalogReport({ generatedAt, repoRoot, inputs }),
    [OUTPUTS.uiSurfaces]: aggregateReport({
      generatedAt,
      repoRoot,
      schema: 'echo.standalone.strict_play.client_ui_surfaces.v1',
      evidenceKind: 'standalone-visible-client-ui-proof',
      requiredFor: ['ui'],
      inputs,
      include: ['uiParity', 'screenCatalog', 'heldItemOverlay'],
      partialIfAnyInputMissing: true,
      blockers: [],
    }),
    [OUTPUTS.voxelContent]: aggregateReport({
      generatedAt,
      repoRoot,
      schema: 'echo.standalone.strict_play.voxel_content.v1',
      evidenceKind: 'standalone-runtime-content-voxel-proof',
      requiredFor: ['content', 'blockItems'],
      inputs,
      include: ['modsRuntimeContent', 'worldInteraction', 'heldItemOverlay'],
      partialIfAnyInputMissing: true,
      blockers: [],
    }),
    [OUTPUTS.blockActions]: aggregateReport({
      generatedAt,
      repoRoot,
      schema: 'echo.standalone.strict_play.block_action_mutations.v1',
      evidenceKind: 'standalone-client-world-action-mutation-proof',
      requiredFor: ['actions', 'blockItems'],
      inputs,
      include: ['worldInteraction'],
      partialIfAnyInputMissing: true,
      blockers: [],
    }),
    [OUTPUTS.worldgen]: worldgenReport({ generatedAt, repoRoot, inputs }),
    [OUTPUTS.saveReload]: saveReloadReport({ generatedAt, repoRoot, inputs }),
  }

  const written = []
  for (const [relativePath, report] of Object.entries(outputs)) {
    const output = path.join(repoRoot, relativePath)
    await fs.mkdir(path.dirname(output), { recursive: true })
    await fs.writeFile(output, `${JSON.stringify(report, null, 2)}\n`, 'utf8')
    written.push({ path: normalizePath(output), status: report.status, moduleCount: report.moduleIds.length })
  }

  return { generatedAt, written }
}

function fullCatalogReport({ generatedAt, repoRoot, inputs }) {
  const statusReport = inputs.runtimeModuleStatus.report
  const executedReport = inputs.realModuleExecution.report
  const catalogReport = inputs.adapterCoreCoverage.report
  const moduleIds = unique([
    ...moduleIdsFrom(statusReport),
    ...moduleIdsFrom(executedReport),
  ]).filter((id) => !id.startsWith('broken-') && !id.includes('minecraft-only'))
  const catalogModuleIds = moduleIdsFrom(catalogReport)
  const missingModuleIds = catalogModuleIds.filter((moduleId) => !moduleIds.includes(moduleId))
  const executionPassed = inputs.realModuleExecution.status === 'PASS' && moduleIds.length > 0
  const statusPass = inputs.runtimeModuleStatus.status === 'PASS'
  const status = executionPassed ? 'PASS' : (statusPass ? 'PARTIAL' : 'FAIL')
  return {
    schema: 'echo.standalone.strict_play.full_catalog.v1',
    generatedAt,
    status,
    runtime: 'standalone',
    evidenceKind: executionPassed
      ? 'standalone-real-module-execution-play-proof'
      : 'standalone-runtime-status-not-full-catalog-play',
    repoRoot: normalizePath(repoRoot),
    requiredFor: ['lifecycle', 'content'],
    moduleIds,
    allModules: catalogModuleIds.length > 0 && missingModuleIds.length === 0,
    catalogModuleCount: catalogModuleIds.length,
    missingModuleIds,
    sourceReports: sourceReports(inputs, ['runtimeModuleStatus', 'realModuleExecution', 'adapterCoreCoverage']),
    trustedMutations: executionPassed ? standaloneExecutionMutations(executedReport) : [],
    visibleRoutes: [],
    saveEvidence: executionPassed && executedReport?.loadReloadUnloadExecuted
      ? ['Standalone real module execution smoke completed load/reload/unload lifecycle against executed module services.']
      : [],
    networkEvidence: [],
    coverageNotes: executionPassed && missingModuleIds.length > 0
      ? [`Standalone real module execution covers ${moduleIds.length}/${catalogModuleIds.length || 'unknown'} catalog module(s); uncovered modules remain strict-play coverage gaps.`]
      : [],
    blockers: executionPassed
      ? []
      : (statusPass
          ? [
              'Standalone runtime-module-status is passing, but executable real module proof is missing.',
              'Strict-play full catalog requires executable module ABI/content proof across the ECHO module catalog.',
            ]
          : missingBlockers(inputs.runtimeModuleStatus, 'Standalone runtime module status report is not PASS.')),
  }
}

function standaloneExecutionMutations(report) {
  if (!report) return []
  const mutations = []
  if (report.contentCoreReferenceRoundTripExecuted) {
    mutations.push('ContentCore reference round trip executed through the Standalone Native Loader ABI.')
  }
  if (report.assetCoreAssetRegistryRoundTripExecuted) {
    mutations.push('AssetCore asset registry round trip executed through the Standalone Native Loader ABI.')
  }
  if (report.assetCoreAssetValidationRoundTripExecuted) {
    mutations.push('AssetCore asset validation round trip executed through the Standalone Native Loader ABI.')
  }
  if (report.recipeCoreHostLoadedEntrypoint) {
    mutations.push('RecipeCore host loaded entrypoint executed through the Standalone Native Loader ABI.')
  }
  if (report.packCoreLoadPlanExecuted) {
    mutations.push('PackCore load plan executed through the Standalone Native Loader ABI.')
  }
  if (report.dataCoreRuntimeProfileExecuted) {
    mutations.push('DataCore runtime profile executed through the Standalone Native Loader ABI.')
  }
  if (report.worldCoreRegionCellSampleExecuted) {
    mutations.push('WorldCore region cell sample executed through the Standalone Native Loader ABI.')
  }
  if (report.missionCoreObjectiveProgressionExecuted) {
    mutations.push('MissionCore objective progression executed through the Standalone Native Loader ABI.')
  }
  if (report.playerCoreFeatureContractRoundTripExecuted) {
    mutations.push('PlayerCore feature contract round trip executed through the Standalone Native Loader ABI.')
  }
  if (report.blockworksBlockCatalogRoundTripExecuted) {
    mutations.push('Blockworks block catalog round trip executed through the Standalone Native Loader ABI.')
  }
  if (report.blockworksPaletteConversionRoundTripExecuted) {
    mutations.push('Blockworks palette conversion round trip executed through the Standalone Native Loader ABI.')
  }
  if (report.blockworksWorldgenSiteRoundTripExecuted) {
    mutations.push('Blockworks worldgen site round trip executed through the Standalone Native Loader ABI.')
  }
  return unique(mutations)
}

function saveReloadReport({ generatedAt, repoRoot, inputs }) {
  const save = inputs.saveContinue
  const realExecution = inputs.realModuleExecution
  const nativeLoaderAbi = inputs.nativeLoaderAbi
  const moduleIds = moduleIdsWithRuntimeEvidence(realExecution.report, /ABI v1 data reload hook executed/i)
  const hostSavePersistencePassed = nativeLoaderAbi.status === 'PASS'
    && nativeLoaderAbi.report?.moduleSaveDataPersistedAfterUnload === true
    && Number(nativeLoaderAbi.report?.permissionCheckedSaveDataCount ?? 0) > 0
  const statusPass = save.status === 'PASS'
    && realExecution.status === 'PASS'
    && hostSavePersistencePassed
    && moduleIds.length > 0
  return {
    schema: 'echo.standalone.strict_play.save_reload.v1',
    generatedAt,
    status: statusPass ? 'PASS' : (save.status === 'PASS' ? 'PARTIAL' : 'FAIL'),
    runtime: 'standalone',
    evidenceKind: statusPass
      ? 'standalone-module-data-reload-and-save-persistence-proof'
      : 'standalone-client-save-continue-not-module-specific-save-sync',
    repoRoot: normalizePath(repoRoot),
    requiredFor: ['saveNetwork'],
    moduleIds,
    allModules: false,
    sourceReports: sourceReports(inputs, ['saveContinue', 'realModuleExecution', 'nativeLoaderAbi']),
    trustedMutations: statusPass ? standaloneExecutionMutations(realExecution.report) : [],
    visibleRoutes: [],
    saveEvidence: statusPass
      ? [
          'Standalone client save/continue report passed.',
          'Standalone Native Loader ABI persisted permission-checked module save data after unload.',
          'Standalone real module execution report published per-module ABI data reload hooks for covered modules.',
        ]
      : (save.status === 'PASS' ? ['Standalone client save/continue report passed, but module-specific ABI save/reload evidence is incomplete.'] : []),
    networkEvidence: [],
    blockers: statusPass
      ? []
      : (save.status === 'PASS'
          ? [
              ...missingBlockers(realExecution, 'Standalone real module execution report is not PASS.'),
              ...missingBlockers(nativeLoaderAbi, 'Standalone Native Loader ABI smoke is not PASS.'),
              ...(hostSavePersistencePassed ? [] : ['Standalone Native Loader ABI smoke did not prove permission-checked module save data persisted after unload.']),
              ...(moduleIds.length > 0 ? [] : ['Standalone real module execution report did not publish per-module ABI data reload evidence.']),
            ]
          : missingBlockers(save, 'Standalone save/continue report is not PASS.')),
  }
}

function worldgenReport({ generatedAt, repoRoot, inputs }) {
  const fullWorldgen = inputs.fullWorldgen
  const worldInteraction = inputs.worldInteraction
  const realExecution = inputs.realModuleExecution
  const moduleIds = unique([
    ...moduleIdsFrom(worldInteraction.report),
    ...moduleIdsWithRuntimeEvidence(realExecution.report, /worldgen/i),
  ])
  const statusPass = fullWorldgen.status === 'PASS'
    && worldInteraction.status === 'PASS'
    && realExecution.status === 'PASS'
    && moduleIds.length > 0
  return {
    schema: 'echo.standalone.strict_play.worldgen.v1',
    generatedAt,
    status: statusPass ? 'PASS' : (moduleIds.length > 0 ? 'PARTIAL' : 'FAIL'),
    runtime: 'standalone',
    evidenceKind: statusPass
      ? 'standalone-full-worldgen-and-module-worldgen-execution-proof'
      : 'standalone-world-interaction-worldgen-proof',
    repoRoot: normalizePath(repoRoot),
    requiredFor: ['worldgen'],
    moduleIds,
    allModules: false,
    sourceReports: sourceReports(inputs, ['fullWorldgen', 'worldInteraction', 'realModuleExecution']),
    trustedMutations: statusPass ? standaloneExecutionMutations(realExecution.report) : [],
    visibleRoutes: unique(array(worldInteraction.report?.visibleRoutes)),
    saveEvidence: unique(array(fullWorldgen.report?.saveEvidence)),
    networkEvidence: [],
    blockers: statusPass
      ? []
      : [
          ...missingBlockers(fullWorldgen, 'Standalone full worldgen dimensions/structures report is not PASS.'),
          ...missingBlockers(worldInteraction, 'Standalone client world interaction report is not PASS.'),
          ...missingBlockers(realExecution, 'Standalone real module execution report is not PASS.'),
          ...(moduleIds.length > 0 ? [] : ['Standalone reports did not publish module-scoped worldgen execution evidence.']),
        ],
  }
}

function moduleIdsWithRuntimeEvidence(report, pattern) {
  if (!report || report.parseError) return []
  return unique([
    ...Object.entries(object(report.runtimeStatuses)),
    ...Object.entries(object(report.notes)),
    ...Object.entries(object(report.nativeServiceIdsBeforeUnload)),
  ]
    .filter(([, value]) => pattern.test(String(value)))
    .map(([moduleId]) => moduleId)
    .filter((moduleId) => moduleId && !moduleId.startsWith('broken-') && !moduleId.includes('minecraft-only')))
}

function aggregateReport({
  generatedAt,
  repoRoot,
  schema,
  evidenceKind,
  requiredFor,
  inputs,
  include,
  partialIfAnyInputMissing,
  blockers,
}) {
  const included = include.map((key) => inputs[key])
  const passing = included.filter((entry) => entry.status === 'PASS')
  const moduleIds = unique(passing.flatMap((entry) => moduleIdsFrom(entry.report)))
  const inputBlockers = included.flatMap((entry) => missingBlockers(entry, `${entry.relativePath} is not PASS.`))
  const status = moduleIds.length > 0 && (!partialIfAnyInputMissing || inputBlockers.length === 0) && blockers.length === 0
    ? 'PASS'
    : (moduleIds.length > 0 ? 'PARTIAL' : 'FAIL')
  return {
    schema,
    generatedAt,
    status,
    runtime: 'standalone',
    evidenceKind,
    repoRoot: normalizePath(repoRoot),
    requiredFor,
    moduleIds,
    allModules: false,
    sourceReports: sourceReports(inputs, include),
    trustedMutations: unique(passing.flatMap((entry) => array(entry.report?.trustedMutations))),
    visibleRoutes: unique(passing.flatMap((entry) => array(entry.report?.visibleRoutes))),
    saveEvidence: unique(passing.flatMap((entry) => array(entry.report?.saveEvidence))),
    networkEvidence: unique(passing.flatMap((entry) => array(entry.report?.networkEvidence))),
    blockers: unique([...blockers, ...inputBlockers]),
  }
}

function sourceReports(inputs, keys) {
  return keys.map((key) => ({
    key,
    path: inputs[key].relativePath,
    found: inputs[key].found,
    status: inputs[key].status,
    moduleCount: moduleIdsFrom(inputs[key].report).length,
  }))
}

function missingBlockers(input, message) {
  if (input.status === 'PASS') return []
  if (!input.found) return [`missing source report: ${input.relativePath}`]
  return [message, ...array(input.report?.blockers)]
}

async function readReport(root, relativePath) {
  const absolute = path.join(root, relativePath)
  try {
    const text = await fs.readFile(absolute, 'utf8')
    const report = JSON.parse(text.charCodeAt(0) === 0xfeff ? text.slice(1) : text)
    return {
      relativePath,
      found: true,
      report,
      status: reportStatus(report),
    }
  } catch (error) {
    if (error.code === 'ENOENT') {
      return { relativePath, found: false, report: null, status: 'MISSING' }
    }
    return { relativePath, found: true, report: { parseError: error.message }, status: 'PARSE_ERROR' }
  }
}

function reportStatus(report) {
  if (!report) return 'MISSING'
  if (report.parseError) return 'PARSE_ERROR'
  const value = string(report.status ?? report.result ?? report.summary?.status).trim().toUpperCase()
  if (['PASS', 'PASSED', 'SUCCESS', 'OK'].includes(value)) return 'PASS'
  if (['FAIL', 'FAILED', 'ERROR', 'SKIPPED'].includes(value)) return value
  if (['PARTIAL', 'WARN', 'WARNING'].includes(value)) return 'PARTIAL'
  return value || 'MISSING'
}

function moduleIdsFrom(report) {
  if (!report || report.parseError) return []
  return unique([
    ...array(report.moduleIds),
    ...array(report.modules).map((item) => typeof item === 'string' ? item : item?.moduleId ?? item?.id),
    ...Object.keys(object(report.runtimeStatuses)),
    ...Object.keys(object(report.lifecycles)),
    ...array(report.loadedModuleIds),
    ...array(report.lifecycleModuleIds),
  ].filter((value) => typeof value === 'string' && value.trim()))
}

function parseArgs(argv) {
  const options = { root: DEFAULT_ROOT, help: false }
  for (let index = 0; index < argv.length; index += 1) {
    const arg = argv[index]
    if (arg === '--root') options.root = argv[++index]
    else if (arg === '--help') options.help = true
    else throw new Error(`Unknown argument: ${arg}`)
  }
  return options
}

function array(value) {
  return Array.isArray(value) ? value : []
}

function object(value) {
  return value && typeof value === 'object' && !Array.isArray(value) ? value : {}
}

function string(value) {
  return typeof value === 'string' ? value : ''
}

function unique(values) {
  return [...new Set(values)].sort()
}

function normalizePath(value) {
  return value.replace(/\\/g, '/')
}

if (process.argv[1] && fileURLToPath(import.meta.url) === path.resolve(process.argv[1])) {
  try {
    const options = parseArgs(process.argv.slice(2))
    if (options.help) {
      console.log('Usage: node scripts/generate-standalone-strict-play-evidence.mjs [--root <path>]')
    } else {
      const { written } = await generateStandaloneStrictPlayEvidence(options)
      for (const entry of written) {
        console.log(`${entry.status} ${entry.moduleCount} module(s): ${entry.path}`)
      }
    }
  } catch (error) {
    console.error(error.message)
    process.exitCode = 1
  }
}
