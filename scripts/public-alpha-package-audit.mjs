import fs from 'node:fs'
import path from 'node:path'

const root = process.cwd()
const errors = []

function read(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), 'utf8')
}

function requireIncludes(filePath, needle, label) {
  if (!read(filePath).includes(needle)) errors.push(`${filePath} missing ${label}`)
}

requireIncludes(
  'build.gradle',
  "dependsOn tasks.named('refreshStandalonePackagedRuntimeImage')",
  'packaged runtime image dependency for public alpha staging'
)
requireIncludes(
  'build.gradle',
  "releaseArchiveName = 'echo-standalone-runtime-0.1.0-alpha.zip'",
  'Release Index archive filename'
)
requireIncludes(
  'build.gradle',
  'rename { releaseArchiveName }',
  'public alpha archive rename'
)
requireIncludes(
  'build.gradle',
  'throw new GradleException("Missing packaged runtime archive:',
  'missing runtime archive failure'
)
requireIncludes(
  '.github/workflows/build.yml',
  'packagePublicAlphaRelease',
  'public alpha packaging workflow step'
)
requireIncludes(
  '.github/workflows/build.yml',
  'actions/upload-artifact@v4',
  'public alpha artifact upload'
)
requireIncludes(
  '.github/workflows/release-public-alpha.yml',
  'actions/attest@v4',
  'public alpha checksum attestation'
)
requireIncludes(
  '.github/workflows/release-public-alpha.yml',
  'gh release upload $env:RELEASE_TAG build/public-alpha/*',
  'public alpha GitHub release upload'
)
requireIncludes(
  '.github/workflows/release-public-alpha.yml',
  'v0.1.0-standalone-runtime-alpha',
  'Release Index tag default'
)
requireIncludes(
  'README.md',
  'packagePublicAlphaRelease',
  'public alpha packaging command documentation'
)
requireIncludes(
  'README.md',
  'Release Public Alpha',
  'public alpha release workflow documentation'
)

if (errors.length) {
  console.error(errors.join('\n'))
  process.exit(1)
}

console.log('Public alpha package audit passed.')
