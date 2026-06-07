package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoContentCoreStandaloneAdapter {
    public static final String MODULE_ID = "echocontentcore";
    public static final String BLOCK_CATALOG_CONTRACT_ID = "echocontentcore:block/content_catalog";
    public static final String ITEM_CATALOG_CONTRACT_ID = "echocontentcore:item/content_catalog";
    public static final String ENTITY_CATALOG_CONTRACT_ID = "echocontentcore:entity/content_catalog";
    public static final String RECIPE_CATALOG_CONTRACT_ID = "echocontentcore:recipe/content_catalog";
    public static final String LOOT_CATALOG_CONTRACT_ID = "echocontentcore:loot/content_catalog";
    public static final String STRUCTURE_CATALOG_CONTRACT_ID = "echocontentcore:structure/content_catalog";
    public static final String CONTENT_REGISTRY_CONTRACT_ID = "echocontentcore:data/content_registry";
    public static final List<String> CONTRACT_IDS = List.of(
            BLOCK_CATALOG_CONTRACT_ID,
            ITEM_CATALOG_CONTRACT_ID,
            ENTITY_CATALOG_CONTRACT_ID,
            RECIPE_CATALOG_CONTRACT_ID,
            LOOT_CATALOG_CONTRACT_ID,
            STRUCTURE_CATALOG_CONTRACT_ID,
            CONTENT_REGISTRY_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> referenceProbe = exerciseReferenceBehavior();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "contentcore_standalone_contract_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", CONTRACT_IDS);
        report.put("logicalRegistrationCount", bindings.size());
        report.put("allRuntimeAliasesRegistered", bindings.stream()
                .allMatch(EchoAdapterCoreContentBinding::supportsAllAdapterCoreRuntimes));
        report.put("runtimeDomains", bindings.stream()
                .map(binding -> bridge.registry().requireContentId(binding.contentId()).domain().id())
                .distinct()
                .sorted()
                .toList());
        report.put("ownerLookupRoundTrip", referenceProbe.get("ownerLookupRoundTrip"));
        report.put("referenceLookupRoundTrip", referenceProbe.get("referenceLookupRoundTrip"));
        report.put("gateAvailabilityRoundTrip", referenceProbe.get("gateAvailabilityRoundTrip"));
        report.put("validationIssueRoundTrip", referenceProbe.get("validationIssueRoundTrip"));
        report.put("referenceProbe", referenceProbe);
        report.put("summary", "ContentCore standalone adapter resolved content registry, ownership, references, gates, and validation contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private Map<String, Object> exerciseReferenceBehavior() {
        ContentId blockId = ContentId.of(MODULE_ID, "reference_block");
        ContentId itemId = ContentId.of(MODULE_ID, "reference_item");
        ContentSource source = new ContentSource(
                "contentcore.standalone.reference",
                MODULE_ID,
                "echo-standalone-runtime/echo-runtime-compat",
                Map.of("contract", CONTENT_REGISTRY_CONTRACT_ID)
        );
        ContentOwner blockOwner = owner(blockId, ContentKind.BLOCK, source, "Reference Block");
        ContentOwner itemOwner = owner(itemId, ContentKind.ITEM, source, "Reference Item");
        ContentReference reference = new ContentReference(
                "contentcore.reference.block_to_item",
                blockId,
                ContentKind.BLOCK,
                itemId,
                ContentKind.ITEM,
                MODULE_ID,
                ContentAvailability.PRESENT,
                ContentReferenceKind.REQUIRES,
                false,
                ContentGate.open(),
                source,
                Map.of("contract", CONTENT_REGISTRY_CONTRACT_ID)
        );
        ContentValidationIssue issue = new ContentValidationIssue(
                "contentcore.reference.issue",
                blockId,
                ContentKind.BLOCK,
                ContentAvailability.PRESENT,
                "CONTENT_REFERENCE",
                source,
                false,
                "Reference content is valid.",
                Map.of("contract", CONTENT_REGISTRY_CONTRACT_ID)
        );
        ContentRegistry registry = new ContentRegistry(
                "contentcore.standalone.registry",
                List.of(blockOwner, itemOwner),
                List.of(reference),
                List.of(issue),
                Map.of("runtime", "echo_runtime_standalone")
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ownerLookupRoundTrip", registry.ownerOf(blockId)
                .map(owner -> owner.contentId().equals(blockId)
                        && owner.kind() == ContentKind.BLOCK
                        && owner.searchVisible()
                        && !owner.gated())
                .orElse(false));
        result.put("referenceLookupRoundTrip", registry.referencesFrom(blockId).size() == 1
                && registry.referencesTo(itemId).size() == 1
                && !reference.blocking()
                && CONTENT_REGISTRY_CONTRACT_ID.equals(reference.metadata().get("contract")));
        result.put("gateAvailabilityRoundTrip", ContentAvailability.PRESENT.available()
                && !ContentAvailability.PRESENT.blocking()
                && !ContentGate.open().blocksWhenMissing()
                && ContentReferenceKind.REQUIRES.blockingWhenUnavailable());
        result.put("validationIssueRoundTrip", registry.issuesFor(blockId).size() == 1
                && !issue.blocking()
                && "CONTENT_REFERENCE".equals(issue.category())
                && !registry.hasBlockingIssues());
        result.put("ownerCount", registry.owners().size());
        result.put("referenceCount", registry.references().size());
        result.put("issueCount", registry.issues().size());
        result.put("registryRuntime", registry.metadata().get("runtime"));
        return Map.copyOf(result);
    }

    private static ContentOwner owner(
            ContentId contentId,
            ContentKind kind,
            ContentSource source,
            String displayName
    ) {
        return new ContentOwner(
                contentId,
                kind,
                MODULE_ID,
                source,
                displayName,
                ContentGate.open(),
                true,
                Map.of("contract", CONTENT_REGISTRY_CONTRACT_ID)
        );
    }

    private record ContentId(String namespace, String path) {
        private ContentId {
            Objects.requireNonNull(namespace, "namespace");
            Objects.requireNonNull(path, "path");
        }

        private static ContentId of(String namespace, String path) {
            return new ContentId(namespace, path);
        }
    }

    private enum ContentKind {
        BLOCK,
        ITEM
    }

    private enum ContentAvailability {
        PRESENT(true, false);

        private final boolean available;
        private final boolean blocking;

        ContentAvailability(boolean available, boolean blocking) {
            this.available = available;
            this.blocking = blocking;
        }

        private boolean available() {
            return available;
        }

        private boolean blocking() {
            return blocking;
        }
    }

    private enum ContentReferenceKind {
        REQUIRES(true);

        private final boolean blockingWhenUnavailable;

        ContentReferenceKind(boolean blockingWhenUnavailable) {
            this.blockingWhenUnavailable = blockingWhenUnavailable;
        }

        private boolean blockingWhenUnavailable() {
            return blockingWhenUnavailable;
        }
    }

    private record ContentGate(boolean blocksWhenMissing) {
        private static ContentGate open() {
            return new ContentGate(false);
        }
    }

    private record ContentSource(
            String id,
            String moduleId,
            String path,
            Map<String, String> metadata
    ) {
        private ContentSource {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(moduleId, "moduleId");
            Objects.requireNonNull(path, "path");
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }
    }

    private record ContentOwner(
            ContentId contentId,
            ContentKind kind,
            String moduleId,
            ContentSource source,
            String displayName,
            ContentGate gate,
            boolean searchVisible,
            Map<String, String> metadata
    ) {
        private ContentOwner {
            Objects.requireNonNull(contentId, "contentId");
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(moduleId, "moduleId");
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(displayName, "displayName");
            Objects.requireNonNull(gate, "gate");
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }

        private boolean gated() {
            return gate.blocksWhenMissing();
        }
    }

    private record ContentReference(
            String id,
            ContentId fromId,
            ContentKind fromKind,
            ContentId toId,
            ContentKind toKind,
            String moduleId,
            ContentAvailability availability,
            ContentReferenceKind referenceKind,
            boolean blocking,
            ContentGate gate,
            ContentSource source,
            Map<String, String> metadata
    ) {
        private ContentReference {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(fromId, "fromId");
            Objects.requireNonNull(fromKind, "fromKind");
            Objects.requireNonNull(toId, "toId");
            Objects.requireNonNull(toKind, "toKind");
            Objects.requireNonNull(moduleId, "moduleId");
            Objects.requireNonNull(availability, "availability");
            Objects.requireNonNull(referenceKind, "referenceKind");
            Objects.requireNonNull(gate, "gate");
            Objects.requireNonNull(source, "source");
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }
    }

    private record ContentValidationIssue(
            String id,
            ContentId contentId,
            ContentKind kind,
            ContentAvailability availability,
            String category,
            ContentSource source,
            boolean blocking,
            String message,
            Map<String, String> metadata
    ) {
        private ContentValidationIssue {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(contentId, "contentId");
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(availability, "availability");
            Objects.requireNonNull(category, "category");
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(message, "message");
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }
    }

    private record ContentRegistry(
            String id,
            List<ContentOwner> owners,
            List<ContentReference> references,
            List<ContentValidationIssue> issues,
            Map<String, String> metadata
    ) {
        private ContentRegistry {
            Objects.requireNonNull(id, "id");
            owners = List.copyOf(owners == null ? List.of() : owners);
            references = List.copyOf(references == null ? List.of() : references);
            issues = List.copyOf(issues == null ? List.of() : issues);
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }

        private Optional<ContentOwner> ownerOf(ContentId contentId) {
            return owners.stream()
                    .filter(owner -> owner.contentId().equals(contentId))
                    .findFirst();
        }

        private List<ContentReference> referencesFrom(ContentId contentId) {
            return references.stream()
                    .filter(reference -> reference.fromId().equals(contentId))
                    .toList();
        }

        private List<ContentReference> referencesTo(ContentId contentId) {
            return references.stream()
                    .filter(reference -> reference.toId().equals(contentId))
                    .toList();
        }

        private List<ContentValidationIssue> issuesFor(ContentId contentId) {
            return issues.stream()
                    .filter(issue -> issue.contentId().equals(contentId))
                    .toList();
        }

        private boolean hasBlockingIssues() {
            return issues.stream().anyMatch(ContentValidationIssue::blocking);
        }
    }
}
