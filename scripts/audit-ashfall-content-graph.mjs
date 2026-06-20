#!/usr/bin/env node
import { promises as fs } from 'node:fs'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const repoRoot = path.resolve(__dirname, '..')
const modulesRepoRoot = path.resolve(repoRoot, '..', 'ECHO-Modules')

const checklistPath = path.join(
  repoRoot,
  'echo-runtime-compat',
  'src',
  'main',
  'resources',
  'ashfall-standalone-parity-checklist.json'
)
const reportPath = path.join(repoRoot, 'build', 'reports', 'ashfall-content-graph-audit.json')

const ashfallRuntimeModules = [
  'echocore',
  'echoplatformcore',
  'echoadaptercore',
  'echonetcore',
  'echoscreencore',
  'echorendercore',
  'echoruntimeguard',
  'echolens',
  'echopresencelink',
  'echoterminal',
  'echoindex',
  'echoholomap',
  'echomissioncore',
  'echomachinecore',
  'echopowercore',
  'echopowergrid',
  'echoindustrialnexus',
  'echomultiblockcore',
  'echologisticscore',
  'echologisticsnetwork',
  'echobasegrid',
  'echoconvoyprotocol',
  'echovehiclecore',
  'echoeconomycore',
  'echolootcore',
  'echosocialcore',
  'echorecipecore',
  'echoblockworks',
  'echoashfallprotocol',
]

function parseArgs(argv) {
  const moduleIndex = argv.indexOf('--modules')
  const modulesPathIndex = argv.indexOf('--modules-repo-root')
  return {
    write: argv.includes('--write'),
    strict: argv.includes('--strict'),
    help: argv.includes('--help'),
    moduleIds: moduleIndex >= 0 && argv[moduleIndex + 1] ? argv[moduleIndex + 1].split(',') : ashfallRuntimeModules,
    modulesRepoRoot: modulesPathIndex >= 0 && argv[modulesPathIndex + 1] ? path.resolve(argv[modulesPathIndex + 1]) : modulesRepoRoot,
  }
}

const options = parseArgs(process.argv.slice(2))
if (options.help) {
  console.log('Usage: node scripts/audit-ashfall-content-graph.mjs [--modules id1,id2] [--modules-repo-root <path>] [--write] [--strict]')
  process.exit(0)
}

async function main() {
  const checklist = JSON.parse(await fs.readFile(checklistPath, 'utf8'))
  const requiredDomains = checklist.requiredDomains || []

  process.chdir(options.modulesRepoRoot)
  const { generateContentGraph } = await import(pathToFileURL(path.join(options.modulesRepoRoot, 'scripts', 'generate-content-graph.mjs')).href)
  const results = await generateContentGraph({ write: options.write, moduleIds: options.moduleIds })

  const allNodes = []
  const nodesByModule = new Map()
  const plansByModule = new Map()
  const standaloneStatuses = new Map()

  for (const result of results) {
    const moduleId = result.moduleId
    const nodes = result.graph?.nodes || []
    const edges = result.graph?.edges || []
    const plan = result.plans?.echo_runtime_standalone
    allNodes.push(...nodes)
    nodesByModule.set(moduleId, { nodes, edges })
    plansByModule.set(moduleId, plan)

    if (plan?.nodes) {
      for (const planNode of plan.nodes) {
        if (planNode.nodeId) {
          standaloneStatuses.set(planNode.nodeId, planNode.status)
        }
      }
    }
  }

  const nodesById = new Map()
  const nodesByKind = new Map()
  for (const node of allNodes) {
    if (node.id) nodesById.set(node.id, node)
    const kind = node.kind || 'unknown'
    if (!nodesByKind.has(kind)) nodesByKind.set(kind, [])
    nodesByKind.get(kind).push(node)
  }

  const domainReports = {}
  for (const domain of requiredDomains) {
    const kinds = domain.nodeKinds || []
    const domainNodes = kinds.flatMap((kind) => nodesByKind.get(kind) || [])
    const presentIds = new Set(domainNodes.map((n) => n.id).filter(Boolean))
    const requiredIdSet = new Set((checklist.domains.find((d) => d.id === domain.id)?.entries || [])
      .filter((e) => e.required !== false)
      .map((e) => e.id))

    const loaded = []
    const partial = []
    const missing = []
    const unsupported = []

    for (const node of domainNodes) {
      const status = standaloneStatuses.get(node.id)
      const entry = { id: node.id, status }
      if (status === 'blocked' || status === 'unsupported') {
        unsupported.push(entry)
      } else {
        const missingMeta = missingMetadata(domain.id, node)
        if (missingMeta.length > 0) {
          entry.missingMetadata = missingMeta
          partial.push(entry)
        } else {
          loaded.push(entry)
        }
      }
    }

    for (const requiredId of requiredIdSet) {
      if (!presentIds.has(requiredId)) {
        missing.push({ id: requiredId, reason: 'not found in content graph' })
      }
    }

    const kindCounts = {}
    for (const kind of kinds) {
      kindCounts[kind] = (nodesByKind.get(kind) || []).length
    }

    domainReports[domain.id] = {
      label: domain.label,
      minCount: domain.minCount,
      kindCounts,
      presentCount: presentIds.size,
      loaded: loaded.sort((a, b) => a.id.localeCompare(b.id)),
      partial: partial.sort((a, b) => a.id.localeCompare(b.id)),
      missing: missing.sort((a, b) => a.id.localeCompare(b.id)),
      unsupported: unsupported.sort((a, b) => a.id.localeCompare(b.id)),
      passed: missing.length === 0 && unsupported.length === 0 && partial.length === 0 && presentIds.size >= domain.minCount,
    }
  }

  const report = {
    schema: 'echo.standalone.content_graph_audit.v1',
    generatedAt: new Date().toISOString(),
    modulesRepoRoot: options.modulesRepoRoot,
    modulesAudited: options.moduleIds,
    moduleCount: results.length,
    totalNodes: allNodes.length,
    domains: domainReports,
    status: Object.values(domainReports).every((d) => d.passed) ? 'PASS' : 'BLOCKED',
  }

  await fs.mkdir(path.dirname(reportPath), { recursive: true })
  await fs.writeFile(reportPath, JSON.stringify(report, null, 2) + '\n', 'utf8')
  console.log(`Wrote ${reportPath}`)
  console.log(`Status: ${report.status}`)
  for (const [domainId, domainReport] of Object.entries(domainReports)) {
    const issues = domainReport.missing.length + domainReport.partial.length + domainReport.unsupported.length
    console.log(`  ${domainId}: ${domainReport.presentCount} present, ${domainReport.missing.length} missing, ${domainReport.partial.length} partial, ${domainReport.unsupported.length} unsupported${issues > 0 ? ' ⚠' : ''}`)
  }

  if (options.strict && report.status !== 'PASS') {
    process.exitCode = 1
  }
}

function missingMetadata(domainId, node) {
  const rules = {
    entities: ['entityVisuals', 'threatMetadata', 'spawnRules'],
    spawn_rules: ['spawnRules'],
    biomes: ['worldgenHints'],
    structures: ['worldgenHints'],
    features: ['worldgenHints'],
    ui_routes: ['uiRoutes'],
  }
  const ruleIds = rules[domainId] || []
  const missing = []
  const data = node.data || {}
  for (const ruleId of ruleIds) {
    if (!satisfiesMetadataRule(ruleId, data)) {
      missing.push(ruleId)
    }
  }
  return missing
}

function satisfiesMetadataRule(ruleId, data) {
  // Simplified check; full rules live in the checklist metadataRules.
  switch (ruleId) {
    case 'entityVisuals':
      return !!(data.texture || data.texturePath || data.model || data.modelPath || data.animation || data.animationPath)
    case 'threatMetadata':
      return !!(data.threat || data.threatClass || data.threatProfile || data.threatLevel || data.dangerLevel || data.hostility || data.hostilityLevel)
    case 'spawnRules':
      return !!(data.spawnRules || data.spawnBiomeTags)
    case 'worldgenHints':
      return !!(data.biomeTags || data.surfaceBlockId || data.generationBlockId || data.placementBlockId)
    case 'uiRoutes':
      return !!(data.surface || data.route)
    default:
      return true
  }
}

main().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
