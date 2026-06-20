#!/usr/bin/env node
import { promises as fs } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const matrixPath = path.join(__dirname, '..', 'docs', 'echo', 'standalone', 'ASHFALL_PARITY_MATRIX.md')
const resourcesOutputPath = path.join(
  __dirname,
  '..',
  'echo-runtime-compat',
  'src',
  'main',
  'resources',
  'ashfall-standalone-parity-checklist.json'
)
const docsOutputPath = path.join(__dirname, '..', 'docs', 'ashfall-standalone-parity-checklist.json')

const matrixText = await fs.readFile(matrixPath, 'utf8')

const domainNamespaceMap = {
  blocks: ['echoashfallprotocol', 'echoterminal'],
  items: ['echoashfallprotocol', 'echoterminal'],
  entities: ['echoashfallprotocol'],
  recipes: ['echoashfallprotocol'],
  loot: ['echoashfallprotocol'],
  structures: ['echoashfallprotocol'],
  biomes: ['echoashfallprotocol'],
  features: ['echoashfallprotocol'],
  sounds: ['echoashfallprotocol', 'echoterminal'],
  missions: ['echoashfallprotocol'],
  objectives: ['echoashfallprotocol'],
  effects: ['echoashfallprotocol'],
  regions: ['echoashfallprotocol'],
  hazards: ['echoashfallprotocol'],
}

const knownDomains = new Set(Object.keys(domainNamespaceMap))

function parseInventoryRows(text) {
  const rows = []
  const regex = /^\| `([^`]+:([^`:]+):([^`]+))` \| `([^`]+)` \| `([^`]+)` \|/gm
  let match
  while ((match = regex.exec(text)) !== null) {
    const rawId = match[1]
    const domain = match[2]
    const namespace = match[3]
    const localId = match[4]
    const status = match[5]
    rows.push({ rawId, domain, namespace, localId, status })
  }
  return rows
}

function parseAdapterCoreRows(text) {
  // First table: AdapterCore-backed feature rows with binding like `registry.blocks.toxic_ash_block`
  const rows = []
  const regex = /^\| ([^|]+) \| `ADAPTERCORE_BACKED` \| `([^`]+)` \| `([^`]+)` \|/gm
  let match
  while ((match = regex.exec(text)) !== null) {
    const feature = match[1].trim()
    const binding = match[2]
    const neoforgeSource = match[3]
    rows.push({ feature, binding, neoforgeSource })
  }
  return rows
}

const inventoryRows = parseInventoryRows(matrixText)
const adapterCoreRows = parseAdapterCoreRows(matrixText)

const byDomain = {}
for (const row of inventoryRows) {
  if (!knownDomains.has(row.domain)) continue
  if (!byDomain[row.domain]) byDomain[row.domain] = []
  const canonicalId = `${row.namespace}:${row.localId}`
  byDomain[row.domain].push({
    id: canonicalId,
    rawId: row.rawId,
    namespace: row.namespace,
    localId: row.localId,
    source: 'ASHFALL_PARITY_MATRIX inventory scan',
    required: true,
  })
}

// Deduplicate by canonical id within each domain
for (const domain of Object.keys(byDomain)) {
  const seen = new Set()
  byDomain[domain] = byDomain[domain].filter((entry) => {
    if (seen.has(entry.id)) return false
    seen.add(entry.id)
    return true
  })
}

const requiredDomains = [
  { id: 'blocks', label: 'Blocks', nodeKinds: ['echo:block'], minCount: 1 },
  { id: 'items', label: 'Items', nodeKinds: ['echo:item'], minCount: 1 },
  { id: 'creative_tabs', label: 'Creative Tabs', nodeKinds: ['echo:creative_tab'], minCount: 1 },
  { id: 'recipes', label: 'Recipes', nodeKinds: ['echo:recipe'], minCount: 1 },
  { id: 'loot', label: 'Loot Tables', nodeKinds: ['echo:loot_table', 'echo:loot'], minCount: 1 },
  { id: 'entities', label: 'Entities/Mobs', nodeKinds: ['echo:entity', 'echo:npc'], minCount: 1 },
  { id: 'spawn_rules', label: 'Spawn Rules', nodeKinds: ['echo:spawn_rule'], minCount: 1 },
  { id: 'biomes', label: 'Biomes', nodeKinds: ['echo:biome', 'echo:region'], minCount: 1 },
  { id: 'structures', label: 'Structures', nodeKinds: ['echo:structure'], minCount: 1 },
  { id: 'features', label: 'World Features', nodeKinds: ['echo:feature'], minCount: 1 },
  { id: 'sounds', label: 'Sound Events', nodeKinds: ['echo:sound_event', 'echo:sound'], minCount: 1 },
  { id: 'missions', label: 'Missions', nodeKinds: ['echo:mission'], minCount: 1 },
  { id: 'objectives', label: 'Objectives', nodeKinds: ['echo:objective'], minCount: 1 },
  { id: 'ui_routes', label: 'UI Routes (Terminal/Lens/Index/HUD/ScreenCore)', nodeKinds: ['echo:ui_intent'], minCount: 1 },
]

const checklist = {
  schema: 'echo.standalone.parity_checklist.v1',
  generatedAt: new Date().toISOString(),
  generatedFrom: 'docs/echo/standalone/ASHFALL_PARITY_MATRIX.md',
  title: 'Ashfall Standalone Parity Checklist',
  description:
    'Machine-readable list of Ashfall content IDs and domains required for standalone parity. ' +
    'The runtime uses this checklist to report exactly what is missing by ID/domain.',
  requiredNamespaces: ['echoashfallprotocol', 'echoterminal'],
  requiredDomains,
  domains: Object.entries(byDomain).map(([domain, entries]) => ({
    id: domain,
    label: requiredDomains.find((d) => d.id === domain)?.label ?? domain,
    nodeKinds: requiredDomains.find((d) => d.id === domain)?.nodeKinds ?? [],
    minCount: requiredDomains.find((d) => d.id === domain)?.minCount ?? 1,
    entries,
  })),
  metadataRules: {
    entityVisuals: {
      requiredFields: ['model', 'modelPath', 'texture', 'texturePath', 'animation', 'animationPath'],
      atLeastOneOf: ['texture', 'texturePath', 'model', 'modelPath'],
    },
    threatMetadata: {
      requiredFields: [],
      atLeastOneOf: ['threat', 'threatClass', 'threatProfile', 'threatLevel', 'dangerLevel', 'hostility', 'hostilityLevel'],
    },
    spawnRules: {
      requiredFields: [],
      atLeastOneOf: ['spawnRules', 'spawnBiomeTags'],
    },
    worldgenHints: {
      requiredFields: [],
      atLeastOneOf: ['biomeTags', 'surfaceBlockId', 'generationBlockId', 'placementBlockId'],
    },
    uiRoutes: {
      requiredFields: ['surface', 'route'],
      atLeastOneOf: ['surface'],
    },
  },
}

const jsonText = JSON.stringify(checklist, null, 2) + '\n'

await fs.mkdir(path.dirname(resourcesOutputPath), { recursive: true })
await fs.writeFile(resourcesOutputPath, jsonText, 'utf8')
console.log(`Wrote ${resourcesOutputPath}`)

await fs.mkdir(path.dirname(docsOutputPath), { recursive: true })
await fs.writeFile(docsOutputPath, jsonText, 'utf8')
console.log(`Wrote ${docsOutputPath}`)
console.log(`Domains:`)
for (const domain of checklist.domains) {
  console.log(`  ${domain.id}: ${domain.entries.length} entries`)
}
console.log(`AdapterCore-backed feature rows parsed: ${adapterCoreRows.length}`)
