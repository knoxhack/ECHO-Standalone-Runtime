#!/usr/bin/env node
import { promises as fs } from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import assert from 'node:assert/strict'
import { generateStandaloneStrictPlayEvidence } from './generate-standalone-strict-play-evidence.mjs'

async function writeJson(root, relativePath, value) {
  const target = path.join(root, relativePath)
  await fs.mkdir(path.dirname(target), { recursive: true })
  await fs.writeFile(target, `${JSON.stringify(value, null, 2)}\n`, 'utf8')
}

async function readJson(root, relativePath) {
  return JSON.parse(await fs.readFile(path.join(root, relativePath), 'utf8'))
}

const root = await fs.mkdtemp(path.join(os.tmpdir(), 'echo-standalone-strict-play-'))

try {
  await writeJson(root, 'reports/echo/standalone/runtime-module-status.json', {
    schema: 'echo.standalone.runtime_module_status.v2',
    status: 'PASS',
    runtimeStatuses: {
      'broken-addon': 'runtime-disabled-with-reason',
      echocore: 'runtime-active',
      echoindex: 'runtime-active',
      'minecraft-only': 'runtime-disabled-with-reason',
    },
    lifecycles: {
      echocore: 'READY',
      echoindex: 'READY',
    },
  })
  await writeJson(root, 'reports/echo/standalone/real-module-execution-smoke.json', {
    schema: 'echo.standalone.real_module_execution_smoke.v16',
    status: 'PASS',
    moduleIds: ['echocore', 'echoindex', 'echocontentcore'],
    runtimeStatuses: {
      echocore: 'runtime-active: ABI v1 data reload hook executed; ABI v1 unload hook executed',
      echoindex: 'runtime-active: ABI v1 data reload hook executed; ABI v1 unload hook executed',
      echocontentcore: 'runtime-active: ABI v1 data reload hook executed; ABI v1 unload hook executed',
    },
    notes: {
      echocontentcore: 'runtime-active: worldgen execution surface registered',
    },
    contentCoreReferenceRoundTripExecuted: true,
    assetCoreAssetRegistryRoundTripExecuted: true,
    loadReloadUnloadExecuted: true,
  })
  await writeJson(root, 'reports/echo/standalone/native-loader-abi-v1-smoke.json', {
    schema: 'echo.standalone.native_loader_abi_v1_smoke.v1',
    status: 'PASS',
    permissionCheckedSaveDataCount: 2,
    moduleSaveDataPersistedAfterUnload: true,
  })
  await writeJson(root, 'reports/echo/standalone/runtime-adaptercore-module-coverage.json', {
    schema: 'echo.standalone.adaptercore_module_coverage.v2',
    status: 'PASS',
    modules: [
      { moduleId: 'echocore' },
      { moduleId: 'echoindex' },
      { moduleId: 'echocontentcore' },
      { moduleId: 'echomissingstandaloneproof' },
    ],
  })
  await writeJson(root, 'reports/echo/standalone/agent5-ui-parity-smoke.json', {
    schema: 'echo.standalone.agent5.ui_parity_smoke.v1',
    status: 'PASS',
    moduleIds: ['echoindex', 'echohudcore'],
    visibleRoutes: ['echoindex:index'],
  })
  await writeJson(root, 'reports/echo/standalone/client-screen-catalog-smoke.json', {
    schema: 'echo.standalone.client_smoke.client-screen-catalog-smoke.v1',
    status: 'PASS',
    moduleIds: ['echoscreencore'],
  })
  await writeJson(root, 'reports/echo/standalone/client-mods-runtime-content-smoke.json', {
    schema: 'echo.standalone.client_smoke.client-mods-runtime-content-smoke.v1',
    status: 'PASS',
    moduleIds: ['echocontentcore'],
  })
  await writeJson(root, 'reports/echo/standalone/client-world-interaction-smoke.json', {
    schema: 'echo.standalone.client_smoke.client-world-interaction-smoke.v1',
    status: 'PASS',
    moduleIds: ['echoashfallprotocol', 'echoterminal'],
  })
  await writeJson(root, 'reports/echo/standalone/client-held-item-overlay-smoke.json', {
    schema: 'echo.standalone.client_smoke.client-held-item-overlay-smoke.v1',
    status: 'PASS',
    moduleIds: ['echohudcore'],
  })
  await writeJson(root, 'reports/echo/standalone/client-save-continue.json', {
    schema: 'echo.standalone.client_save_continue.v1',
    status: 'PASS',
  })
  await writeJson(root, 'reports/echo/standalone/full-worldgen-dimensions-structures.json', {
    schema: 'echo.standalone.full_worldgen_dimensions_structures.v1',
    status: 'PASS',
  })

  const { written } = await generateStandaloneStrictPlayEvidence({ root })
  assert.equal(written.length, 6)

  const fullCatalog = await readJson(root, 'reports/echo/standalone/full-catalog-play.json')
  assert.equal(fullCatalog.status, 'PASS')
  assert.deepEqual(fullCatalog.moduleIds, ['echocontentcore', 'echocore', 'echoindex'])
  assert.equal(fullCatalog.allModules, false)
  assert.deepEqual(fullCatalog.missingModuleIds, ['echomissingstandaloneproof'])
  assert.ok(fullCatalog.trustedMutations.some((mutation) => mutation.includes('ContentCore reference round trip')))

  const ui = await readJson(root, 'reports/echo/standalone/client-ui-surfaces-play.json')
  assert.equal(ui.status, 'PASS')
  assert.deepEqual(ui.moduleIds, ['echohudcore', 'echoindex', 'echoscreencore'])

  const voxel = await readJson(root, 'reports/echo/standalone/voxel-content-play.json')
  assert.equal(voxel.status, 'PASS')
  assert.deepEqual(voxel.moduleIds, ['echoashfallprotocol', 'echocontentcore', 'echohudcore', 'echoterminal'])

  const actions = await readJson(root, 'reports/echo/standalone/block-action-mutations.json')
  assert.equal(actions.status, 'PASS')
  assert.deepEqual(actions.moduleIds, ['echoashfallprotocol', 'echoterminal'])

  const worldgen = await readJson(root, 'reports/echo/standalone/worldgen-play.json')
  assert.equal(worldgen.status, 'PASS')
  assert.deepEqual(worldgen.moduleIds, ['echoashfallprotocol', 'echocontentcore', 'echoterminal'])

  const save = await readJson(root, 'reports/echo/standalone/save-reload-play.json')
  assert.equal(save.status, 'PASS')
  assert.deepEqual(save.moduleIds, ['echocontentcore', 'echocore', 'echoindex'])
  assert.ok(save.saveEvidence.some((entry) => entry.includes('module save data after unload')))
} finally {
  await fs.rm(root, { recursive: true, force: true })
}

console.log('generate-standalone-strict-play-evidence tests passed')
