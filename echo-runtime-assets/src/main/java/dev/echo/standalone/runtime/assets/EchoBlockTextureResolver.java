package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EchoBlockTextureResolver {
    private static final int MAX_MODEL_PARENT_DEPTH = 32;
    private static final Pattern STRING_FIELD =
            Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");

    private final EchoMinecraftAssetResolver resolver;

    public EchoBlockTextureResolver(EchoMinecraftAssetResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public EchoBlockTextureResolution resolve(String blockId) throws IOException {
        return resolve(blockId, Map.of());
    }

    public EchoBlockTextureResolution resolve(String blockId, Map<String, String> stateProperties) throws IOException {
        String normalizedBlockId = normalizeId("minecraft", blockId);
        String[] blockParts = splitId(normalizedBlockId, "block id");
        Optional<String> blockstate = resolver.loadBlockstate(blockParts[0], blockParts[1]);
        if (blockstate.isEmpty()) {
            return EchoBlockTextureResolution.missing(normalizedBlockId, "missing blockstate");
        }

        Map<String, String> safeStateProperties = stateProperties == null ? Map.of() : Map.copyOf(stateProperties);
        List<EchoBlockstateModelSelector.EchoBlockstateModelSelection> blockstateModels =
                EchoBlockstateModelSelector.selectAll(blockstate.get(), safeStateProperties);
        if (blockstateModels.isEmpty()) {
            return EchoBlockTextureResolution.missing(normalizedBlockId, "blockstate has no model");
        }
        if (blockstateModels.size() > 1 || blockstateModels.stream().anyMatch(EchoBlockTextureResolver::multipartSelection)) {
            return resolveComposite(normalizedBlockId, blockParts[0], blockstateModels);
        }
        EchoBlockstateModelSelector.EchoBlockstateModelSelection selection = blockstateModels.get(0);
        return resolveSingleSelection(normalizedBlockId, blockParts[0], selection);
    }

    private EchoBlockTextureResolution resolveSingleSelection(
            String normalizedBlockId,
            String defaultNamespace,
            EchoBlockstateModelSelector.EchoBlockstateModelSelection selection
    ) throws IOException {
        String modelId = normalizeId(defaultNamespace, selection.modelId());
        ModelTextureScan scan = scanModelTextures(modelId);
        Optional<String> textureId = selectTexture(scan.textures(), scan.templateKind());
        Map<String, String> faceTextureIds =
                faceTextureIds(scan.textures(), scan.elementFaceTextures(), scan.templateKind());
        Map<String, Integer> faceUvRotations = faceUvRotations(scan.elementFaceUvRotations());
        Map<String, EchoBlockModelFaceUv> faceUvs = faceUvs(scan.elementFaceUvs());
        Map<String, Integer> faceTintIndices = faceTintIndices(scan.elementFaceTintIndices());
        List<EchoBlockModelElement> modelElementDefinitions =
                modelElementDefinitions(scan.rawModelElements(), scan.textures());
        Optional<EchoBlockModelBounds> modelBounds = scan.modelBounds()
                .or(() -> defaultBoundsForTemplate(scan.templateKind()))
                .or(() -> Optional.of(EchoBlockModelBounds.fullCube()));
        if (textureId.isPresent()) {
            return new EchoBlockTextureResolution(
                    normalizedBlockId,
                    Optional.of(modelId),
                    textureId,
                    scan.templateKind(),
                    scan.parentChain(),
                    modelBounds,
                    scan.modelElements(),
                    modelElementDefinitions,
                    faceTextureIds,
                    faceUvRotations,
                    faceUvs,
                    faceTintIndices,
                    selection.xRotationDegrees(),
                    selection.yRotationDegrees(),
                    selection.uvLock(),
                    Optional.empty()
            );
        }

        String reason = scan.missingReason()
                .orElse(scan.textures().isEmpty() ? "model has no textures" : "model textures only contain unresolved references");
        return new EchoBlockTextureResolution(
                normalizedBlockId,
                Optional.of(modelId),
                Optional.empty(),
                scan.templateKind(),
                scan.parentChain(),
                modelBounds,
                scan.modelElements(),
                modelElementDefinitions,
                faceTextureIds,
                faceUvRotations,
                faceUvs,
                faceTintIndices,
                selection.xRotationDegrees(),
                selection.yRotationDegrees(),
                selection.uvLock(),
                Optional.of(reason)
        );
    }

    private EchoBlockTextureResolution resolveComposite(
            String normalizedBlockId,
            String defaultNamespace,
            List<EchoBlockstateModelSelector.EchoBlockstateModelSelection> selections
    ) throws IOException {
        ArrayList<String> parentChain = new ArrayList<>();
        ArrayList<EchoBlockModelBounds> modelElements = new ArrayList<>();
        ArrayList<EchoBlockModelElement> modelElementDefinitions = new ArrayList<>();
        LinkedHashMap<String, String> faceTextureIds = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> faceUvRotations = new LinkedHashMap<>();
        LinkedHashMap<String, EchoBlockModelFaceUv> faceUvs = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> faceTintIndices = new LinkedHashMap<>();
        Optional<String> firstModelId = Optional.empty();
        Optional<String> firstTextureId = Optional.empty();
        Optional<String> missingReason = Optional.empty();

        for (EchoBlockstateModelSelector.EchoBlockstateModelSelection selection : selections) {
            String modelId = normalizeId(defaultNamespace, selection.modelId());
            firstModelId = firstModelId.or(() -> Optional.of(modelId));
            ModelTextureScan scan = scanModelTextures(modelId);
            parentChain.addAll(scan.parentChain());
            Optional<String> textureId = selectTexture(scan.textures(), scan.templateKind());
            firstTextureId = firstTextureId.or(() -> textureId);
            faceTextureIds(scan.textures(), scan.elementFaceTextures(), scan.templateKind())
                    .forEach(faceTextureIds::putIfAbsent);
            faceUvRotations(scan.elementFaceUvRotations()).forEach(faceUvRotations::putIfAbsent);
            faceUvs(scan.elementFaceUvs()).forEach(faceUvs::putIfAbsent);
            faceTintIndices(scan.elementFaceTintIndices()).forEach(faceTintIndices::putIfAbsent);
            List<EchoBlockModelElement> elements = modelElementDefinitions(scan.rawModelElements(), scan.textures());
            if (elements.isEmpty()) {
                elements = templateModelElementDefinitions(scan);
            }
            for (EchoBlockModelElement element : elements) {
                EchoBlockModelElement transformed = rotateElement(element, selection.xRotationDegrees(), selection.yRotationDegrees());
                modelElementDefinitions.add(transformed);
                modelElements.add(transformed.bounds());
            }
            if (textureId.isEmpty() && missingReason.isEmpty()) {
                missingReason = scan.missingReason()
                        .or(() -> Optional.of(scan.textures().isEmpty()
                                ? "model has no textures"
                                : "model textures only contain unresolved references"));
            }
        }

        if (modelElementDefinitions.isEmpty() && !selections.isEmpty()) {
            return resolveSingleSelection(normalizedBlockId, defaultNamespace, selections.get(0));
        }

        Optional<EchoBlockModelBounds> modelBounds = combinedBounds(modelElements)
                .or(() -> Optional.of(EchoBlockModelBounds.fullCube()));
        return new EchoBlockTextureResolution(
                normalizedBlockId,
                firstModelId,
                firstTextureId,
                Optional.of("multipart"),
                parentChain,
                modelBounds,
                modelElements,
                modelElementDefinitions,
                faceTextureIds,
                faceUvRotations,
                faceUvs,
                faceTintIndices,
                0,
                0,
                false,
                firstTextureId.isPresent() ? Optional.empty() : missingReason
        );
    }

    private static boolean multipartSelection(EchoBlockstateModelSelector.EchoBlockstateModelSelection selection) {
        return selection != null && selection.source().startsWith("multipart");
    }

    private ModelTextureScan scanModelTextures(String initialModelId) throws IOException {
        LinkedHashMap<String, TextureReference> textures = new LinkedHashMap<>();
        ArrayList<String> parentChain = new ArrayList<>();
        Optional<EchoBlockModelBounds> modelBounds = Optional.empty();
        List<EchoBlockModelBounds> modelElements = List.of();
        List<RawBlockModelElement> rawModelElements = List.of();
        LinkedHashMap<String, TextureReference> elementFaceTextures = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> elementFaceUvRotations = new LinkedHashMap<>();
        LinkedHashMap<String, EchoBlockModelFaceUv> elementFaceUvs = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> elementFaceTintIndices = new LinkedHashMap<>();
        String current = initialModelId;
        String defaultNamespace = splitId(initialModelId, "model id")[0];

        for (int depth = 0; depth < MAX_MODEL_PARENT_DEPTH; depth++) {
            String normalizedModelId = normalizeId(defaultNamespace, current);
            if (parentChain.contains(normalizedModelId)) {
                return new ModelTextureScan(
                        textures,
                        parentChain,
                        Optional.empty(),
                        modelBounds,
                        modelElements,
                        rawModelElements,
                        elementFaceTextures,
                        elementFaceUvRotations,
                        elementFaceUvs,
                        elementFaceTintIndices,
                        Optional.of("model parent cycle")
                );
            }
            parentChain.add(normalizedModelId);

            Optional<String> builtInTemplate = builtInTemplateKind(normalizedModelId);
            if (builtInTemplate.isPresent()) {
                return new ModelTextureScan(
                        textures,
                        parentChain,
                        builtInTemplate,
                        modelBounds,
                        modelElements,
                        rawModelElements,
                        elementFaceTextures,
                        elementFaceUvRotations,
                        elementFaceUvs,
                        elementFaceTintIndices,
                        Optional.empty()
                );
            }

            String[] modelParts = splitId(normalizedModelId, "model id");
            Optional<String> json = resolver.loadBlockModel(modelParts[0], modelParts[1]);
            if (json.isEmpty()) {
                return new ModelTextureScan(
                        textures,
                        parentChain,
                        Optional.empty(),
                        modelBounds,
                        modelElements,
                        rawModelElements,
                        elementFaceTextures,
                        elementFaceUvRotations,
                        elementFaceUvs,
                        elementFaceTintIndices,
                        Optional.of("missing model " + normalizedModelId)
                );
            }

            if (modelElements.isEmpty()) {
                modelElements = elementBounds(json.get());
                rawModelElements = rawModelElements(json.get(), modelParts[0]);
                modelBounds = combinedBounds(modelElements);
                elementFaceTextures.putAll(elementFaceTextures(json.get(), modelParts[0]));
                elementFaceUvRotations.putAll(elementFaceUvRotations(json.get()));
                elementFaceUvs.putAll(elementFaceUvs(json.get()));
                elementFaceTintIndices.putAll(elementFaceTintIndices(json.get()));
            }
            parseTextureObject(json.get(), modelParts[0]).forEach(textures::putIfAbsent);
            Optional<String> parentId = stringField(json.get(), "parent");
            if (parentId.isEmpty()) {
                return new ModelTextureScan(
                        textures,
                        parentChain,
                        Optional.empty(),
                        modelBounds,
                        modelElements,
                        rawModelElements,
                        elementFaceTextures,
                        elementFaceUvRotations,
                        elementFaceUvs,
                        elementFaceTintIndices,
                        Optional.empty()
                );
            }

            current = parentId.get();
            defaultNamespace = modelParts[0];
        }

        return new ModelTextureScan(
                textures,
                parentChain,
                Optional.empty(),
                modelBounds,
                modelElements,
                rawModelElements,
                elementFaceTextures,
                elementFaceUvRotations,
                elementFaceUvs,
                elementFaceTintIndices,
                Optional.of("model parent chain too deep")
        );
    }

    private static Map<String, TextureReference> parseTextureObject(String json, String namespace) {
        Optional<String> object = objectField(json, "textures");
        if (object.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, TextureReference> textures = new LinkedHashMap<>();
        Matcher matcher = STRING_FIELD.matcher(object.get());
        while (matcher.find()) {
            String key = unescapeJsonString(matcher.group(1));
            String value = unescapeJsonString(matcher.group(2));
            if (!key.isBlank() && !value.isBlank()) {
                textures.put(key, new TextureReference(namespace, value));
            }
        }
        return textures;
    }

    private static List<EchoBlockModelBounds> elementBounds(String json) {
        Optional<String> elements = valueField(json, "elements");
        if (elements.isEmpty()) {
            return List.of();
        }
        String value = elements.get().trim();
        if (!value.startsWith("[")) {
            return List.of();
        }
        int arrayEnd = matchingBracket(value, 0);
        if (arrayEnd < 0) {
            return List.of();
        }
        ArrayList<EchoBlockModelBounds> result = new ArrayList<>();
        int index = skipWhitespace(value, 1);
        while (index < arrayEnd) {
            char current = value.charAt(index);
            if (current == ',') {
                index = skipWhitespace(value, index + 1);
                continue;
            }
            if (current != '{') {
                index++;
                continue;
            }
            int objectEnd = matchingBrace(value, index);
            if (objectEnd < index) {
                break;
            }
            String element = value.substring(index, objectEnd + 1);
            Optional<double[]> from = numericArrayField(element, "from");
            Optional<double[]> to = numericArrayField(element, "to");
            if (from.isPresent() && to.isPresent()) {
                double[] f = from.get();
                double[] t = to.get();
                try {
                    result.add(new EchoBlockModelBounds(f[0], f[1], f[2], t[0], t[1], t[2]));
                } catch (IllegalArgumentException ignored) {
                    // Ignore degenerate element boxes; callers still get any other valid elements.
                }
            }
            index = skipWhitespace(value, objectEnd + 1);
        }
        return List.copyOf(result);
    }

    private static List<RawBlockModelElement> rawModelElements(String json, String namespace) {
        Optional<String> elements = valueField(json, "elements");
        if (elements.isEmpty()) {
            return List.of();
        }
        String value = elements.get().trim();
        if (!value.startsWith("[")) {
            return List.of();
        }
        int arrayEnd = matchingBracket(value, 0);
        if (arrayEnd < 0) {
            return List.of();
        }
        ArrayList<RawBlockModelElement> result = new ArrayList<>();
        int index = skipWhitespace(value, 1);
        while (index < arrayEnd) {
            char current = value.charAt(index);
            if (current == ',') {
                index = skipWhitespace(value, index + 1);
                continue;
            }
            if (current != '{') {
                index++;
                continue;
            }
            int objectEnd = matchingBrace(value, index);
            if (objectEnd < index) {
                break;
            }
            String element = value.substring(index, objectEnd + 1);
            Optional<double[]> from = numericArrayField(element, "from");
            Optional<double[]> to = numericArrayField(element, "to");
            if (from.isPresent() && to.isPresent()) {
                double[] f = from.get();
                double[] t = to.get();
                try {
                    result.add(new RawBlockModelElement(
                            new EchoBlockModelBounds(f[0], f[1], f[2], t[0], t[1], t[2]),
                            rawElementFaceTextures(element, namespace),
                            rawElementFaceUvRotations(element),
                            rawElementFaceUvs(element),
                            rawElementFaceTintIndices(element),
                            rawElementFaceCullFaces(element),
                            elementRotation(element)
                    ));
                } catch (IllegalArgumentException ignored) {
                    // Ignore degenerate element boxes; callers still get any other valid elements.
                }
            }
            index = skipWhitespace(value, objectEnd + 1);
        }
        return List.copyOf(result);
    }

    private static Map<String, TextureReference> rawElementFaceTextures(String element, String namespace) {
        Optional<String> faces = objectField(element, "faces");
        if (faces.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, TextureReference> result = new LinkedHashMap<>();
        for (String face : List.of("up", "down", "north", "south", "east", "west")) {
            objectField(faces.get(), face)
                    .flatMap(faceObject -> stringField(faceObject, "texture"))
                    .filter(texture -> !texture.isBlank())
                    .ifPresent(texture -> result.put(face, new TextureReference(namespace, texture)));
        }
        return Map.copyOf(result);
    }

    private static Map<String, Integer> rawElementFaceUvRotations(String element) {
        Optional<String> faces = objectField(element, "faces");
        if (faces.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (String face : List.of("up", "down", "north", "south", "east", "west")) {
            objectField(faces.get(), face)
                    .flatMap(faceObject -> intField(faceObject, "rotation"))
                    .map(EchoBlockTextureResolver::normalizeModelRotation)
                    .filter(rotation -> rotation != 0)
                    .ifPresent(rotation -> result.put(face, rotation));
        }
        return Map.copyOf(result);
    }

    private static Map<String, EchoBlockModelFaceUv> rawElementFaceUvs(String element) {
        Optional<String> faces = objectField(element, "faces");
        if (faces.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, EchoBlockModelFaceUv> result = new LinkedHashMap<>();
        for (String face : List.of("up", "down", "north", "south", "east", "west")) {
            objectField(faces.get(), face)
                    .flatMap(faceObject -> numericArrayField(faceObject, "uv", 4))
                    .flatMap(EchoBlockModelFaceUv::fromArray)
                    .ifPresent(uv -> result.put(face, uv));
        }
        return Map.copyOf(result);
    }

    private static Map<String, Integer> rawElementFaceTintIndices(String element) {
        Optional<String> faces = objectField(element, "faces");
        if (faces.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (String face : List.of("up", "down", "north", "south", "east", "west")) {
            objectField(faces.get(), face)
                    .flatMap(faceObject -> intField(faceObject, "tintindex"))
                    .filter(tintIndex -> tintIndex >= 0)
                    .ifPresent(tintIndex -> result.put(face, tintIndex));
        }
        return Map.copyOf(result);
    }

    private static Map<String, String> rawElementFaceCullFaces(String element) {
        Optional<String> faces = objectField(element, "faces");
        if (faces.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (String face : List.of("up", "down", "north", "south", "east", "west")) {
            objectField(faces.get(), face)
                    .flatMap(faceObject -> stringField(faceObject, "cullface"))
                    .flatMap(EchoBlockTextureResolver::normalizedModelFaceName)
                    .ifPresent(cullFace -> result.put(face, cullFace));
        }
        return Map.copyOf(result);
    }

    private static Optional<EchoBlockModelElementRotation> elementRotation(String element) {
        Optional<String> rotation = objectField(element, "rotation");
        if (rotation.isEmpty()) {
            return Optional.empty();
        }
        Optional<double[]> origin = numericArrayField(rotation.get(), "origin", 3);
        Optional<String> axis = stringField(rotation.get(), "axis");
        Optional<Double> angle = doubleField(rotation.get(), "angle");
        if (origin.isEmpty() || axis.isEmpty() || angle.isEmpty()) {
            return Optional.empty();
        }
        double[] o = origin.get();
        try {
            return Optional.of(new EchoBlockModelElementRotation(
                    o[0],
                    o[1],
                    o[2],
                    axis.orElseThrow(),
                    angle.orElseThrow(),
                    booleanField(rotation.get(), "rescale").orElse(false)
            )).filter(EchoBlockModelElementRotation::active);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static Map<String, TextureReference> elementFaceTextures(String json, String namespace) {
        Optional<String> elements = valueField(json, "elements");
        if (elements.isEmpty()) {
            return Map.of();
        }
        String value = elements.get().trim();
        if (!value.startsWith("[")) {
            return Map.of();
        }
        int arrayEnd = matchingBracket(value, 0);
        if (arrayEnd < 0) {
            return Map.of();
        }
        LinkedHashMap<String, TextureReference> result = new LinkedHashMap<>();
        int index = skipWhitespace(value, 1);
        while (index < arrayEnd) {
            char current = value.charAt(index);
            if (current == ',') {
                index = skipWhitespace(value, index + 1);
                continue;
            }
            if (current != '{') {
                index++;
                continue;
            }
            int objectEnd = matchingBrace(value, index);
            if (objectEnd < index) {
                break;
            }
            String element = value.substring(index, objectEnd + 1);
            Optional<String> faces = objectField(element, "faces");
            if (faces.isPresent()) {
                for (String face : List.of("up", "down", "north", "south", "east", "west")) {
                    if (result.containsKey(face)) {
                        continue;
                    }
                    objectField(faces.get(), face)
                            .flatMap(faceObject -> stringField(faceObject, "texture"))
                            .filter(texture -> !texture.isBlank())
                            .ifPresent(texture -> result.put(face, new TextureReference(namespace, texture)));
                }
            }
            index = skipWhitespace(value, objectEnd + 1);
        }
        return Map.copyOf(result);
    }

    private static Map<String, Integer> elementFaceUvRotations(String json) {
        Optional<String> elements = valueField(json, "elements");
        if (elements.isEmpty()) {
            return Map.of();
        }
        String value = elements.get().trim();
        if (!value.startsWith("[")) {
            return Map.of();
        }
        int arrayEnd = matchingBracket(value, 0);
        if (arrayEnd < 0) {
            return Map.of();
        }
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        int index = skipWhitespace(value, 1);
        while (index < arrayEnd) {
            char current = value.charAt(index);
            if (current == ',') {
                index = skipWhitespace(value, index + 1);
                continue;
            }
            if (current != '{') {
                index++;
                continue;
            }
            int objectEnd = matchingBrace(value, index);
            if (objectEnd < index) {
                break;
            }
            String element = value.substring(index, objectEnd + 1);
            Optional<String> faces = objectField(element, "faces");
            if (faces.isPresent()) {
                for (String face : List.of("up", "down", "north", "south", "east", "west")) {
                    if (result.containsKey(face)) {
                        continue;
                    }
                    objectField(faces.get(), face)
                            .flatMap(faceObject -> intField(faceObject, "rotation"))
                            .map(EchoBlockTextureResolver::normalizeModelRotation)
                            .filter(rotation -> rotation != 0)
                            .ifPresent(rotation -> result.put(face, rotation));
                }
            }
            index = skipWhitespace(value, objectEnd + 1);
        }
        return Map.copyOf(result);
    }

    private static Map<String, EchoBlockModelFaceUv> elementFaceUvs(String json) {
        Optional<String> elements = valueField(json, "elements");
        if (elements.isEmpty()) {
            return Map.of();
        }
        String value = elements.get().trim();
        if (!value.startsWith("[")) {
            return Map.of();
        }
        int arrayEnd = matchingBracket(value, 0);
        if (arrayEnd < 0) {
            return Map.of();
        }
        LinkedHashMap<String, EchoBlockModelFaceUv> result = new LinkedHashMap<>();
        int index = skipWhitespace(value, 1);
        while (index < arrayEnd) {
            char current = value.charAt(index);
            if (current == ',') {
                index = skipWhitespace(value, index + 1);
                continue;
            }
            if (current != '{') {
                index++;
                continue;
            }
            int objectEnd = matchingBrace(value, index);
            if (objectEnd < index) {
                break;
            }
            String element = value.substring(index, objectEnd + 1);
            Optional<String> faces = objectField(element, "faces");
            if (faces.isPresent()) {
                for (String face : List.of("up", "down", "north", "south", "east", "west")) {
                    if (result.containsKey(face)) {
                        continue;
                    }
                    objectField(faces.get(), face)
                            .flatMap(faceObject -> numericArrayField(faceObject, "uv", 4))
                            .flatMap(EchoBlockModelFaceUv::fromArray)
                            .ifPresent(uv -> result.put(face, uv));
                }
            }
            index = skipWhitespace(value, objectEnd + 1);
        }
        return Map.copyOf(result);
    }

    private static Map<String, Integer> elementFaceTintIndices(String json) {
        Optional<String> elements = valueField(json, "elements");
        if (elements.isEmpty()) {
            return Map.of();
        }
        String value = elements.get().trim();
        if (!value.startsWith("[")) {
            return Map.of();
        }
        int arrayEnd = matchingBracket(value, 0);
        if (arrayEnd < 0) {
            return Map.of();
        }
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        int index = skipWhitespace(value, 1);
        while (index < arrayEnd) {
            char current = value.charAt(index);
            if (current == ',') {
                index = skipWhitespace(value, index + 1);
                continue;
            }
            if (current != '{') {
                index++;
                continue;
            }
            int objectEnd = matchingBrace(value, index);
            if (objectEnd < index) {
                break;
            }
            String element = value.substring(index, objectEnd + 1);
            Optional<String> faces = objectField(element, "faces");
            if (faces.isPresent()) {
                for (String face : List.of("up", "down", "north", "south", "east", "west")) {
                    if (result.containsKey(face)) {
                        continue;
                    }
                    objectField(faces.get(), face)
                            .flatMap(faceObject -> intField(faceObject, "tintindex"))
                            .filter(tintIndex -> tintIndex >= 0)
                            .ifPresent(tintIndex -> result.put(face, tintIndex));
                }
            }
            index = skipWhitespace(value, objectEnd + 1);
        }
        return Map.copyOf(result);
    }

    private static Optional<EchoBlockModelBounds> combinedBounds(List<EchoBlockModelBounds> elements) {
        if (elements == null || elements.isEmpty()) {
            return Optional.empty();
        }
        double minX = 16.0D;
        double minY = 16.0D;
        double minZ = 16.0D;
        double maxX = 0.0D;
        double maxY = 0.0D;
        double maxZ = 0.0D;
        for (EchoBlockModelBounds element : elements) {
            minX = Math.min(minX, element.fromX());
            minY = Math.min(minY, element.fromY());
            minZ = Math.min(minZ, element.fromZ());
            maxX = Math.max(maxX, element.toX());
            maxY = Math.max(maxY, element.toY());
            maxZ = Math.max(maxZ, element.toZ());
        }
        return Optional.of(new EchoBlockModelBounds(minX, minY, minZ, maxX, maxY, maxZ));
    }

    private static Optional<double[]> numericArrayField(String json, String fieldName) {
        return numericArrayField(json, fieldName, 3);
    }

    private static Optional<double[]> numericArrayField(String json, String fieldName, int expectedLength) {
        Optional<String> value = valueField(json, fieldName);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        String array = value.get().trim();
        if (!array.startsWith("[")) {
            return Optional.empty();
        }
        int arrayEnd = matchingBracket(array, 0);
        if (arrayEnd < 0) {
            return Optional.empty();
        }
        String[] parts = array.substring(1, arrayEnd).split(",");
        int length = Math.max(1, expectedLength);
        if (parts.length < length) {
            return Optional.empty();
        }
        double[] result = new double[length];
        try {
            for (int index = 0; index < length; index++) {
                result[index] = Double.parseDouble(parts[index].trim());
            }
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    private static Optional<Integer> intField(String json, String fieldName) {
        Optional<String> value = valueField(json, fieldName);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value.get().trim()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private static Optional<Double> doubleField(String json, String fieldName) {
        Optional<String> value = valueField(json, fieldName);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(value.get().trim()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private static Optional<Boolean> booleanField(String json, String fieldName) {
        Optional<String> value = valueField(json, fieldName);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        String normalized = value.get().trim().toLowerCase(java.util.Locale.ROOT);
        if ("true".equals(normalized)) {
            return Optional.of(true);
        }
        if ("false".equals(normalized)) {
            return Optional.of(false);
        }
        return Optional.empty();
    }


    private static Optional<String> selectTexture(
            Map<String, TextureReference> textures,
            Optional<String> templateKind
    ) {
        String[] preferredKeys = preferredTextureKeys(templateKind.orElse(""));
        for (String key : preferredKeys) {
            Optional<String> textureId = resolveTextureKey(textures, key, new LinkedHashSet<>());
            if (textureId.isPresent()) {
                return textureId;
            }
        }
        for (String key : textures.keySet()) {
            Optional<String> textureId = resolveTextureKey(textures, key, new LinkedHashSet<>());
            if (textureId.isPresent()) {
                return textureId;
            }
        }
        return Optional.empty();
    }

    private static Map<String, String> faceTextureIds(
            Map<String, TextureReference> textures,
            Map<String, TextureReference> elementFaceTextures,
            Optional<String> templateKind
    ) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (String face : List.of("up", "down", "north", "south", "east", "west")) {
            resolveDirectFaceTexture(textures, face)
                    .or(() -> resolveElementFaceTexture(textures, elementFaceTextures, face))
                    .or(() -> resolveFaceTexture(textures, templateKind.orElse(""), face))
                    .ifPresent(textureId -> result.put(face, textureId));
        }
        return Map.copyOf(result);
    }

    private static List<EchoBlockModelElement> templateModelElementDefinitions(ModelTextureScan scan) {
        if (scan == null || scan.templateKind().isEmpty()) {
            return List.of();
        }
        Optional<EchoBlockModelBounds> bounds = defaultBoundsForTemplate(scan.templateKind())
                .or(() -> fullCubeCompositeTemplate(scan.templateKind())
                        ? Optional.of(EchoBlockModelBounds.fullCube())
                        : Optional.empty());
        if (bounds.isEmpty()) {
            return List.of();
        }
        Map<String, String> faceTextures =
                faceTextureIds(scan.textures(), scan.elementFaceTextures(), scan.templateKind());
        if (faceTextures.isEmpty()) {
            return List.of();
        }
        return List.of(new EchoBlockModelElement(
                bounds.orElseThrow(),
                faceTextures,
                faceUvRotations(scan.elementFaceUvRotations()),
                faceUvs(scan.elementFaceUvs()),
                faceTintIndices(scan.elementFaceTintIndices()),
                Map.of(),
                Optional.empty()
        ));
    }

    private static boolean fullCubeCompositeTemplate(Optional<String> templateKind) {
        return templateKind
                .map(kind -> switch (kind) {
                    case "cube_all", "cube", "cube_column", "orientable" -> true;
                    default -> false;
                })
                .orElse(false);
    }

    private static Map<String, Integer> faceUvRotations(Map<String, Integer> elementFaceUvRotations) {
        if (elementFaceUvRotations == null || elementFaceUvRotations.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : elementFaceUvRotations.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                continue;
            }
            int rotation = normalizeModelRotation(entry.getValue());
            if (rotation != 0) {
                result.put(entry.getKey().trim().toLowerCase(java.util.Locale.ROOT), rotation);
            }
        }
        return Map.copyOf(result);
    }

    private static Map<String, EchoBlockModelFaceUv> faceUvs(Map<String, EchoBlockModelFaceUv> elementFaceUvs) {
        if (elementFaceUvs == null || elementFaceUvs.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, EchoBlockModelFaceUv> result = new LinkedHashMap<>();
        for (Map.Entry<String, EchoBlockModelFaceUv> entry : elementFaceUvs.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                continue;
            }
            result.put(entry.getKey().trim().toLowerCase(java.util.Locale.ROOT), entry.getValue());
        }
        return Map.copyOf(result);
    }

    private static Map<String, Integer> faceTintIndices(Map<String, Integer> elementFaceTintIndices) {
        if (elementFaceTintIndices == null || elementFaceTintIndices.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : elementFaceTintIndices.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()
                    || entry.getValue() == null || entry.getValue() < 0) {
                continue;
            }
            result.put(entry.getKey().trim().toLowerCase(java.util.Locale.ROOT), entry.getValue());
        }
        return Map.copyOf(result);
    }

    private static List<EchoBlockModelElement> modelElementDefinitions(
            List<RawBlockModelElement> rawElements,
            Map<String, TextureReference> textures
    ) {
        if (rawElements == null || rawElements.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoBlockModelElement> result = new ArrayList<>();
        for (RawBlockModelElement raw : rawElements) {
            if (raw == null) {
                continue;
            }
            LinkedHashMap<String, String> textureIds = new LinkedHashMap<>();
            for (Map.Entry<String, TextureReference> entry : raw.faceTextures().entrySet()) {
                if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                    continue;
                }
                resolveTextureReference(textures, entry.getValue(), new LinkedHashSet<>())
                        .ifPresent(textureId -> textureIds.put(
                                entry.getKey().trim().toLowerCase(java.util.Locale.ROOT),
                                textureId
                        ));
            }
            result.add(new EchoBlockModelElement(
                    raw.bounds(),
                    textureIds,
                    raw.uvRotationsByFace(),
                    raw.uvRectsByFace(),
                    raw.tintIndicesByFace(),
                    raw.cullFacesByFace(),
                    raw.rotation()
            ));
        }
        return List.copyOf(result);
    }

    private static EchoBlockModelElement rotateElement(
            EchoBlockModelElement element,
            int xRotationDegrees,
            int yRotationDegrees
    ) {
        if (element == null) {
            return null;
        }
        EchoBlockModelBounds bounds = rotateBoundsY(
                rotateBoundsX(element.bounds(), xRotationDegrees),
                yRotationDegrees
        );
        return new EchoBlockModelElement(
                bounds,
                element.rotation().isPresent()
                        ? element.textureIdsByFace()
                        : rotateFaceKeyedMap(element.textureIdsByFace(), xRotationDegrees, yRotationDegrees),
                element.rotation().isPresent()
                        ? element.uvRotationsByFace()
                        : rotateFaceKeyedMap(element.uvRotationsByFace(), xRotationDegrees, yRotationDegrees),
                element.rotation().isPresent()
                        ? element.uvRectsByFace()
                        : rotateFaceKeyedMap(element.uvRectsByFace(), xRotationDegrees, yRotationDegrees),
                element.rotation().isPresent()
                        ? element.tintIndicesByFace()
                        : rotateFaceKeyedMap(element.tintIndicesByFace(), xRotationDegrees, yRotationDegrees),
                element.rotation().isPresent()
                        ? element.cullFacesByFace()
                        : rotateCullFaces(element.cullFacesByFace(), xRotationDegrees, yRotationDegrees),
                element.rotation()
        );
    }

    private static <T> Map<String, T> rotateFaceKeyedMap(Map<String, T> values, int xRotationDegrees, int yRotationDegrees) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        int xRotation = normalizeModelRotation(xRotationDegrees);
        int yRotation = normalizeModelRotation(yRotationDegrees);
        if (xRotation == 0 && yRotation == 0) {
            return Map.copyOf(values);
        }
        LinkedHashMap<String, T> result = new LinkedHashMap<>();
        for (Map.Entry<String, T> entry : values.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                continue;
            }
            result.put(rotatedFaceName(entry.getKey(), xRotation, yRotation), entry.getValue());
        }
        return Map.copyOf(result);
    }

    private static Map<String, String> rotateCullFaces(
            Map<String, String> values,
            int xRotationDegrees,
            int yRotationDegrees
    ) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        int xRotation = normalizeModelRotation(xRotationDegrees);
        int yRotation = normalizeModelRotation(yRotationDegrees);
        if (xRotation == 0 && yRotation == 0) {
            return Map.copyOf(values);
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()
                    || entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            result.put(
                    rotatedFaceName(entry.getKey(), xRotation, yRotation),
                    rotatedFaceName(entry.getValue(), xRotation, yRotation)
            );
        }
        return Map.copyOf(result);
    }

    private static String rotatedFaceName(String faceName, int xRotationDegrees, int yRotationDegrees) {
        Optional<String> normalized = normalizedModelFaceName(faceName);
        if (normalized.isEmpty()) {
            return faceName == null ? "" : faceName.trim().toLowerCase(java.util.Locale.ROOT);
        }
        return rotateFaceY(rotateFaceX(normalized.get(), xRotationDegrees), yRotationDegrees);
    }

    private static String rotateFaceY(String faceName, int degrees) {
        return switch (normalizeModelRotation(degrees)) {
            case 90 -> switch (faceName) {
                case "north" -> "east";
                case "east" -> "south";
                case "south" -> "west";
                case "west" -> "north";
                default -> faceName;
            };
            case 180 -> switch (faceName) {
                case "north" -> "south";
                case "south" -> "north";
                case "east" -> "west";
                case "west" -> "east";
                default -> faceName;
            };
            case 270 -> switch (faceName) {
                case "north" -> "west";
                case "west" -> "south";
                case "south" -> "east";
                case "east" -> "north";
                default -> faceName;
            };
            default -> faceName;
        };
    }

    private static String rotateFaceX(String faceName, int degrees) {
        return switch (normalizeModelRotation(degrees)) {
            case 90 -> switch (faceName) {
                case "up" -> "south";
                case "south" -> "down";
                case "down" -> "north";
                case "north" -> "up";
                default -> faceName;
            };
            case 180 -> switch (faceName) {
                case "up" -> "down";
                case "down" -> "up";
                case "north" -> "south";
                case "south" -> "north";
                default -> faceName;
            };
            case 270 -> switch (faceName) {
                case "up" -> "north";
                case "north" -> "down";
                case "down" -> "south";
                case "south" -> "up";
                default -> faceName;
            };
            default -> faceName;
        };
    }

    private static EchoBlockModelBounds rotateBoundsY(EchoBlockModelBounds bounds, int degrees) {
        EchoBlockModelBounds safeBounds = bounds == null ? EchoBlockModelBounds.fullCube() : bounds;
        int rotation = normalizeModelRotation(degrees);
        if (rotation == 0) {
            return safeBounds;
        }
        double[][] corners = {
                rotateXZ(safeBounds.fromX(), safeBounds.fromZ(), rotation),
                rotateXZ(safeBounds.toX(), safeBounds.fromZ(), rotation),
                rotateXZ(safeBounds.fromX(), safeBounds.toZ(), rotation),
                rotateXZ(safeBounds.toX(), safeBounds.toZ(), rotation)
        };
        double minX = 16.0D;
        double minZ = 16.0D;
        double maxX = 0.0D;
        double maxZ = 0.0D;
        for (double[] corner : corners) {
            minX = Math.min(minX, corner[0]);
            minZ = Math.min(minZ, corner[1]);
            maxX = Math.max(maxX, corner[0]);
            maxZ = Math.max(maxZ, corner[1]);
        }
        return new EchoBlockModelBounds(minX, safeBounds.fromY(), minZ, maxX, safeBounds.toY(), maxZ);
    }

    private static EchoBlockModelBounds rotateBoundsX(EchoBlockModelBounds bounds, int degrees) {
        EchoBlockModelBounds safeBounds = bounds == null ? EchoBlockModelBounds.fullCube() : bounds;
        int rotation = normalizeModelRotation(degrees);
        if (rotation == 0) {
            return safeBounds;
        }
        double[][] corners = {
                rotateYZ(safeBounds.fromY(), safeBounds.fromZ(), rotation),
                rotateYZ(safeBounds.toY(), safeBounds.fromZ(), rotation),
                rotateYZ(safeBounds.fromY(), safeBounds.toZ(), rotation),
                rotateYZ(safeBounds.toY(), safeBounds.toZ(), rotation)
        };
        double minY = 16.0D;
        double minZ = 16.0D;
        double maxY = 0.0D;
        double maxZ = 0.0D;
        for (double[] corner : corners) {
            minY = Math.min(minY, corner[0]);
            minZ = Math.min(minZ, corner[1]);
            maxY = Math.max(maxY, corner[0]);
            maxZ = Math.max(maxZ, corner[1]);
        }
        return new EchoBlockModelBounds(safeBounds.fromX(), minY, minZ, safeBounds.toX(), maxY, maxZ);
    }

    private static double[] rotateXZ(double x, double z, int degrees) {
        return switch (degrees) {
            case 90 -> new double[]{16.0D - z, x};
            case 180 -> new double[]{16.0D - x, 16.0D - z};
            case 270 -> new double[]{z, 16.0D - x};
            default -> new double[]{x, z};
        };
    }

    private static double[] rotateYZ(double y, double z, int degrees) {
        return switch (degrees) {
            case 90 -> new double[]{16.0D - z, y};
            case 180 -> new double[]{16.0D - y, 16.0D - z};
            case 270 -> new double[]{z, 16.0D - y};
            default -> new double[]{y, z};
        };
    }

    private static Map<String, String> faceCullFaces(Map<String, String> rawCullFaces) {
        if (rawCullFaces == null || rawCullFaces.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : rawCullFaces.entrySet()) {
            Optional<String> face = normalizedModelFaceName(entry.getKey());
            Optional<String> cullFace = normalizedModelFaceName(entry.getValue());
            if (face.isPresent() && cullFace.isPresent()) {
                result.put(face.get(), cullFace.get());
            }
        }
        return Map.copyOf(result);
    }

    private static Optional<String> normalizedModelFaceName(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (normalized) {
            case "up", "down", "north", "south", "east", "west" -> Optional.of(normalized);
            default -> Optional.empty();
        };
    }

    private static Optional<String> normalizedRotationAxis(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (normalized) {
            case "x", "y", "z" -> Optional.of(normalized);
            default -> Optional.empty();
        };
    }


    private static Optional<String> resolveDirectFaceTexture(
            Map<String, TextureReference> textures,
            String face
    ) {
        if (textures == null || textures.isEmpty() || face == null || face.isBlank()) {
            return Optional.empty();
        }
        return resolveTextureKey(textures, face.trim().toLowerCase(java.util.Locale.ROOT), new LinkedHashSet<>());
    }

    private static Optional<String> resolveElementFaceTexture(
            Map<String, TextureReference> textures,
            Map<String, TextureReference> elementFaceTextures,
            String face
    ) {
        if (elementFaceTextures == null || elementFaceTextures.isEmpty()) {
            return Optional.empty();
        }
        TextureReference reference = elementFaceTextures.get(face);
        if (reference == null) {
            return Optional.empty();
        }
        return resolveTextureReference(textures, reference, new LinkedHashSet<>());
    }

    private static Optional<String> resolveFaceTexture(
            Map<String, TextureReference> textures,
            String templateKind,
            String face
    ) {
        for (String key : preferredFaceTextureKeys(templateKind, face)) {
            Optional<String> textureId = resolveTextureKey(textures, key, new LinkedHashSet<>());
            if (textureId.isPresent()) {
                return textureId;
            }
        }
        return selectTexture(textures, Optional.ofNullable(templateKind));
    }

    private static List<String> preferredFaceTextureKeys(String templateKind, String face) {
        String normalizedFace = face == null ? "" : face.toLowerCase(java.util.Locale.ROOT);
        boolean vertical = "up".equals(normalizedFace) || "down".equals(normalizedFace);
        if (doorTemplateKind(templateKind)) {
            return doorFaceTextureKeys(templateKind, vertical, normalizedFace);
        }
        return switch (templateKind) {
            case "cross", "tinted_cross" -> List.of("cross", "all", "particle");
            case "fence_post", "fence_side", "fence_inventory" ->
                    List.of("texture", "all", "side", normalizedFace, "particle");
            case "pane_post", "pane_side", "pane_side_alt", "pane_noside", "pane_noside_alt" -> vertical
                    ? List.of("edge", "pane", "texture", "all", "side", "particle")
                    : List.of("pane", "texture", "all", "side", "edge", normalizedFace, "particle");
            case "trapdoor_bottom", "trapdoor_top", "trapdoor_open" ->
                    List.of("texture", "all", "side", normalizedFace, "particle");
            case "wall_post", "wall_side", "wall_side_tall", "wall_inventory" ->
                    List.of("wall", "all", "side", normalizedFace, "particle");
            case "slab", "slab_top", "stairs", "inner_stairs", "outer_stairs" -> vertical
                    ? List.of(faceAlias(normalizedFace), normalizedFace, "all", "side", "particle")
                    : List.of("side", normalizedFace, "all", "particle");
            case "cube_all" -> List.of("all", normalizedFace, "side", "particle");
            case "cube_column" -> vertical
                    ? List.of("end", normalizedFace, faceAlias(normalizedFace), "all", "side", "particle")
                    : List.of("side", normalizedFace, "all", "particle", "end");
            case "cube" -> vertical
                    ? List.of(normalizedFace, faceAlias(normalizedFace), "all", "side", "particle")
                    : List.of(normalizedFace, "side", "all", "particle");
            case "orientable" -> switch (normalizedFace) {
                case "up" -> List.of("top", "up", "all", "side", "particle");
                case "down" -> List.of("bottom", "down", "top", "all", "side", "particle");
                case "north" -> List.of("front", "north", "side", "all", "particle");
                default -> List.of("side", normalizedFace, "front", "all", "particle");
            };
            default -> vertical
                    ? List.of(normalizedFace, faceAlias(normalizedFace), "all", "side", "top", "particle")
                    : List.of(normalizedFace, "side", "front", "north", "all", "particle");
        };
    }

    private static String faceAlias(String face) {
        return switch (face) {
            case "up" -> "top";
            case "down" -> "bottom";
            default -> face;
        };
    }

    private static Optional<String> resolveTextureKey(
            Map<String, TextureReference> textures,
            String key,
            Set<String> seen
    ) {
        TextureReference reference = textures.get(key);
        if (reference == null) {
            return Optional.empty();
        }
        return resolveTextureReference(textures, reference, seen);
    }

    private static Optional<String> resolveTextureReference(
            Map<String, TextureReference> textures,
            TextureReference reference,
            Set<String> seen
    ) {
        String value = reference.value().trim();
        if (value.startsWith("#")) {
            String targetKey = value.substring(1);
            if (targetKey.isBlank() || !seen.add(targetKey)) {
                return Optional.empty();
            }
            return resolveTextureKey(textures, targetKey, seen);
        }
        return Optional.of(normalizeTextureId(reference.namespace(), value));
    }

    private static String[] preferredTextureKeys(String templateKind) {
        if (doorTemplateKind(templateKind)) {
            return doorTextureKeys(templateKind);
        }
        return switch (templateKind) {
            case "cross", "tinted_cross" -> new String[]{"cross", "all", "particle"};
            case "fence_post", "fence_side", "fence_inventory" ->
                    new String[]{"texture", "all", "side", "particle"};
            case "pane_post", "pane_side", "pane_side_alt", "pane_noside", "pane_noside_alt" ->
                    new String[]{"pane", "texture", "all", "side", "edge", "particle"};
            case "trapdoor_bottom", "trapdoor_top", "trapdoor_open" ->
                    new String[]{"texture", "all", "side", "particle"};
            case "wall_post", "wall_side", "wall_side_tall", "wall_inventory" ->
                    new String[]{"wall", "all", "side", "particle"};
            case "slab", "slab_top", "stairs", "inner_stairs", "outer_stairs" ->
                    new String[]{"all", "side", "top", "bottom", "particle"};
            case "cube_all" -> new String[]{"all", "side", "north", "particle", "up", "down", "south", "east", "west"};
            case "cube", "cube_column" -> new String[]{"all", "side", "north", "south", "east", "west", "up", "down", "end", "particle"};
            case "orientable" -> new String[]{"front", "side", "top", "all", "north", "particle"};
            default -> new String[]{"all", "side", "north", "front", "up", "top", "particle", "south", "east", "west", "down", "end"};
        };
    }

    private static Optional<String> builtInTemplateKind(String modelId) {
        String[] parts = splitId(modelId, "model id");
        if (!"minecraft".equals(parts[0])) {
            return Optional.empty();
        }
        if (parts[1].startsWith("block/door_")) {
            return Optional.of(parts[1].substring("block/".length()));
        }
        return switch (parts[1]) {
            case "block/cube_all" -> Optional.of("cube_all");
            case "block/cube" -> Optional.of("cube");
            case "block/cube_column" -> Optional.of("cube_column");
            case "block/orientable" -> Optional.of("orientable");
            case "block/cross" -> Optional.of("cross");
            case "block/tinted_cross" -> Optional.of("tinted_cross");
            case "block/slab" -> Optional.of("slab");
            case "block/slab_top" -> Optional.of("slab_top");
            case "block/stairs" -> Optional.of("stairs");
            case "block/inner_stairs" -> Optional.of("inner_stairs");
            case "block/outer_stairs" -> Optional.of("outer_stairs");
            case "block/fence_post" -> Optional.of("fence_post");
            case "block/fence_side" -> Optional.of("fence_side");
            case "block/fence_inventory" -> Optional.of("fence_inventory");
            case "block/template_glass_pane_post" -> Optional.of("pane_post");
            case "block/template_glass_pane_side" -> Optional.of("pane_side");
            case "block/template_glass_pane_side_alt" -> Optional.of("pane_side_alt");
            case "block/template_glass_pane_noside" -> Optional.of("pane_noside");
            case "block/template_glass_pane_noside_alt" -> Optional.of("pane_noside_alt");
            case "block/template_trapdoor_bottom", "block/template_orientable_trapdoor_bottom" ->
                    Optional.of("trapdoor_bottom");
            case "block/template_trapdoor_top", "block/template_orientable_trapdoor_top" ->
                    Optional.of("trapdoor_top");
            case "block/template_trapdoor_open", "block/template_orientable_trapdoor_open" ->
                    Optional.of("trapdoor_open");
            case "block/template_wall_post" -> Optional.of("wall_post");
            case "block/template_wall_side" -> Optional.of("wall_side");
            case "block/template_wall_side_tall" -> Optional.of("wall_side_tall");
            case "block/wall_inventory" -> Optional.of("wall_inventory");
            default -> Optional.empty();
        };
    }

    private static Optional<EchoBlockModelBounds> defaultBoundsForTemplate(Optional<String> templateKind) {
        String kind = templateKind.orElse("");
        if (doorTemplateKind(kind)) {
            return Optional.of(new EchoBlockModelBounds(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D));
        }
        return switch (kind) {
            case "slab" -> Optional.of(new EchoBlockModelBounds(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D));
            case "slab_top" -> Optional.of(new EchoBlockModelBounds(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D));
            case "trapdoor_bottom" -> Optional.of(new EchoBlockModelBounds(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D));
            case "trapdoor_top" -> Optional.of(new EchoBlockModelBounds(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D));
            default -> Optional.empty();
        };
    }

    private static boolean doorTemplateKind(String templateKind) {
        return templateKind != null && templateKind.startsWith("door_");
    }

    private static String[] doorTextureKeys(String templateKind) {
        String halfTexture = templateKind == null || !templateKind.startsWith("door_top") ? "bottom" : "top";
        return new String[]{halfTexture, "texture", "all", "side", "particle"};
    }

    private static List<String> doorFaceTextureKeys(String templateKind, boolean vertical, String normalizedFace) {
        String halfTexture = templateKind == null || !templateKind.startsWith("door_top") ? "bottom" : "top";
        return vertical
                ? List.of(halfTexture, "texture", "all", "side", "particle")
                : List.of(halfTexture, "texture", "all", "side", normalizedFace, "particle");
    }

    private static Optional<String> stringField(String json, String fieldName) {
        if (json == null || fieldName == null || fieldName.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = STRING_FIELD.matcher(json);
        while (matcher.find()) {
            if (fieldName.equals(unescapeJsonString(matcher.group(1)))) {
                return Optional.of(unescapeJsonString(matcher.group(2)));
            }
        }
        return Optional.empty();
    }

    private static Optional<String> objectField(String json, String fieldName) {
        if (json == null || fieldName == null || fieldName.isBlank()) {
            return Optional.empty();
        }
        Pattern keyPattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:");
        Matcher matcher = keyPattern.matcher(json);
        while (matcher.find()) {
            int index = skipWhitespace(json, matcher.end());
            if (index < json.length() && json.charAt(index) == '{') {
                int end = matchingBrace(json, index);
                if (end > index) {
                    return Optional.of(json.substring(index + 1, end));
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<String> valueField(String json, String fieldName) {
        if (json == null || fieldName == null || fieldName.isBlank()) {
            return Optional.empty();
        }
        Pattern keyPattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:");
        Matcher matcher = keyPattern.matcher(json);
        while (matcher.find()) {
            int start = skipWhitespace(json, matcher.end());
            int end = valueEnd(json, start);
            if (end > start) {
                return Optional.of(json.substring(start, end));
            }
        }
        return Optional.empty();
    }

    private static int valueEnd(String json, int start) {
        if (json == null || start < 0 || start >= json.length()) {
            return -1;
        }
        char current = json.charAt(start);
        if (current == '{') {
            int end = matchingBrace(json, start);
            return end < 0 ? -1 : end + 1;
        }
        if (current == '[') {
            int end = matchingBracket(json, start);
            return end < 0 ? -1 : end + 1;
        }
        if (current == '"') {
            int end = matchingString(json, start);
            return end < 0 ? -1 : end + 1;
        }
        int index = start;
        while (index < json.length()) {
            char ch = json.charAt(index);
            if (ch == ',' || ch == '}' || ch == ']') {
                break;
            }
            index++;
        }
        return index;
    }

    private static int matchingBrace(String json, int start) {
        return matchingDelimited(json, start, '{', '}');
    }

    private static int matchingBracket(String json, int start) {
        return matchingDelimited(json, start, '[', ']');
    }

    private static int matchingDelimited(String json, int start, char open, char close) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < json.length(); index++) {
            char current = json.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }
            if (current == '"') {
                inString = true;
            } else if (current == open) {
                depth++;
            } else if (current == close) {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        return -1;
    }

    private static int matchingString(String json, int start) {
        boolean escaped = false;
        for (int index = start + 1; index < json.length(); index++) {
            char current = json.charAt(index);
            if (escaped) {
                escaped = false;
            } else if (current == '\\') {
                escaped = true;
            } else if (current == '"') {
                return index;
            }
        }
        return -1;
    }

    private static int skipWhitespace(String value, int index) {
        int current = index;
        while (current < value.length() && Character.isWhitespace(value.charAt(current))) {
            current++;
        }
        return current;
    }

    private static String normalizeTextureId(String defaultNamespace, String textureId) {
        String normalized = requireText(textureId, "textureId").replace('\\', '/').trim();
        String namespace = requireText(defaultNamespace, "defaultNamespace");
        String path = normalized;
        int separator = normalized.indexOf(':');
        if (separator >= 0) {
            namespace = normalized.substring(0, separator);
            path = normalized.substring(separator + 1);
        }
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        return namespace + ":" + requireText(path, "texturePath");
    }

    private static String normalizeId(String defaultNamespace, String id) {
        String normalized = requireText(id, "id").replace('\\', '/').trim();
        int separator = normalized.indexOf(':');
        if (separator >= 0) {
            return requireText(normalized.substring(0, separator), "namespace")
                    + ":"
                    + requireText(normalized.substring(separator + 1), "path");
        }
        return requireText(defaultNamespace, "defaultNamespace") + ":" + normalized;
    }

    private static String[] splitId(String id, String label) {
        String normalized = requireText(id, label).replace('\\', '/').trim();
        int separator = normalized.indexOf(':');
        if (separator < 1 || separator == normalized.length() - 1) {
            throw new IllegalArgumentException("Invalid namespaced " + label + ": " + id);
        }
        return new String[]{normalized.substring(0, separator), normalized.substring(separator + 1)};
    }

    private static String unescapeJsonString(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean escaped = false;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (!escaped) {
                if (current == '\\') {
                    escaped = true;
                } else {
                    result.append(current);
                }
                continue;
            }
            switch (current) {
                case '"' -> result.append('"');
                case '\\' -> result.append('\\');
                case '/' -> result.append('/');
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                default -> result.append(current);
            }
            escaped = false;
        }
        if (escaped) {
            result.append('\\');
        }
        return result.toString();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private record TextureReference(String namespace, String value) {
        private TextureReference {
            namespace = requireText(namespace, "namespace");
            value = requireText(value, "value");
        }
    }

    private record ModelTextureScan(
            Map<String, TextureReference> textures,
            List<String> parentChain,
            Optional<String> templateKind,
            Optional<EchoBlockModelBounds> modelBounds,
            List<EchoBlockModelBounds> modelElements,
            List<RawBlockModelElement> rawModelElements,
            Map<String, TextureReference> elementFaceTextures,
            Map<String, Integer> elementFaceUvRotations,
            Map<String, EchoBlockModelFaceUv> elementFaceUvs,
            Map<String, Integer> elementFaceTintIndices,
            Optional<String> missingReason
    ) {
        private ModelTextureScan {
            textures = Map.copyOf(textures);
            parentChain = List.copyOf(parentChain);
            templateKind = templateKind == null ? Optional.empty() : templateKind;
            modelBounds = modelBounds == null ? Optional.empty() : modelBounds;
            modelElements = modelElements == null ? List.of() : List.copyOf(modelElements);
            rawModelElements = rawModelElements == null ? List.of() : List.copyOf(rawModelElements);
            elementFaceTextures = elementFaceTextures == null ? Map.of() : Map.copyOf(elementFaceTextures);
            elementFaceUvRotations = elementFaceUvRotations == null
                    ? Map.of()
                    : Map.copyOf(elementFaceUvRotations);
            elementFaceUvs = elementFaceUvs == null ? Map.of() : Map.copyOf(elementFaceUvs);
            elementFaceTintIndices = elementFaceTintIndices == null
                    ? Map.of()
                    : Map.copyOf(elementFaceTintIndices);
            missingReason = missingReason == null ? Optional.empty() : missingReason;
        }
    }

    private record RawBlockModelElement(
            EchoBlockModelBounds bounds,
            Map<String, TextureReference> faceTextures,
            Map<String, Integer> uvRotationsByFace,
            Map<String, EchoBlockModelFaceUv> uvRectsByFace,
            Map<String, Integer> tintIndicesByFace,
            Map<String, String> cullFacesByFace,
            Optional<EchoBlockModelElementRotation> rotation
    ) {
        private RawBlockModelElement {
            bounds = bounds == null ? EchoBlockModelBounds.fullCube() : bounds;
            faceTextures = faceTextures == null ? Map.of() : Map.copyOf(faceTextures);
            uvRotationsByFace = faceUvRotations(uvRotationsByFace);
            uvRectsByFace = faceUvs(uvRectsByFace);
            tintIndicesByFace = faceTintIndices(tintIndicesByFace);
            cullFacesByFace = faceCullFaces(cullFacesByFace);
            rotation = rotation == null ? Optional.empty() : rotation.filter(EchoBlockModelElementRotation::active);
        }
    }

    public record EchoBlockModelBounds(
            double fromX,
            double fromY,
            double fromZ,
            double toX,
            double toY,
            double toZ
    ) {
        public EchoBlockModelBounds {
            fromX = clampModelCoordinate(fromX);
            fromY = clampModelCoordinate(fromY);
            fromZ = clampModelCoordinate(fromZ);
            toX = clampModelCoordinate(toX);
            toY = clampModelCoordinate(toY);
            toZ = clampModelCoordinate(toZ);
            if (toX <= fromX || toY <= fromY || toZ <= fromZ) {
                throw new IllegalArgumentException("model bounds must have positive size");
            }
        }

        public static EchoBlockModelBounds fullCube() {
            return new EchoBlockModelBounds(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        }

        public boolean fullCubeBounds() {
            return fromX == 0.0D && fromY == 0.0D && fromZ == 0.0D
                    && toX == 16.0D && toY == 16.0D && toZ == 16.0D;
        }

        public float minXUnit() {
            return (float) (fromX / 16.0D);
        }

        public float minYUnit() {
            return (float) (fromY / 16.0D);
        }

        public float minZUnit() {
            return (float) (fromZ / 16.0D);
        }

        public float maxXUnit() {
            return (float) (toX / 16.0D);
        }

        public float maxYUnit() {
            return (float) (toY / 16.0D);
        }

        public float maxZUnit() {
            return (float) (toZ / 16.0D);
        }
    }

    public record EchoBlockModelFaceUv(
            double u1,
            double v1,
            double u2,
            double v2
    ) {
        public EchoBlockModelFaceUv {
            u1 = clampModelCoordinate(u1);
            v1 = clampModelCoordinate(v1);
            u2 = clampModelCoordinate(u2);
            v2 = clampModelCoordinate(v2);
            if (u1 == u2 || v1 == v2) {
                throw new IllegalArgumentException("model face uv must have positive area");
            }
        }

        private static Optional<EchoBlockModelFaceUv> fromArray(double[] values) {
            if (values == null || values.length < 4) {
                return Optional.empty();
            }
            try {
                return Optional.of(new EchoBlockModelFaceUv(values[0], values[1], values[2], values[3]));
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
        }
    }

    public record EchoBlockModelElement(
            EchoBlockModelBounds bounds,
            Map<String, String> textureIdsByFace,
            Map<String, Integer> uvRotationsByFace,
            Map<String, EchoBlockModelFaceUv> uvRectsByFace,
            Map<String, Integer> tintIndicesByFace,
            Map<String, String> cullFacesByFace,
            Optional<EchoBlockModelElementRotation> rotation
    ) {
        public EchoBlockModelElement {
            bounds = bounds == null ? EchoBlockModelBounds.fullCube() : bounds;
            textureIdsByFace = textureIdsByFace == null ? Map.of() : Map.copyOf(textureIdsByFace);
            uvRotationsByFace = faceUvRotations(uvRotationsByFace);
            uvRectsByFace = faceUvs(uvRectsByFace);
            tintIndicesByFace = faceTintIndices(tintIndicesByFace);
            cullFacesByFace = faceCullFaces(cullFacesByFace);
            rotation = rotation == null ? Optional.empty() : rotation.filter(EchoBlockModelElementRotation::active);
        }

        public Optional<String> textureIdForFace(String faceName) {
            if (faceName == null || faceName.isBlank()) {
                return Optional.empty();
            }
            return Optional.ofNullable(textureIdsByFace.get(faceName.trim().toLowerCase(java.util.Locale.ROOT)));
        }

        public int uvRotationDegreesForFace(String faceName) {
            if (faceName == null || faceName.isBlank()) {
                return 0;
            }
            return uvRotationsByFace.getOrDefault(faceName.trim().toLowerCase(java.util.Locale.ROOT), 0);
        }

        public Optional<EchoBlockModelFaceUv> uvRectForFace(String faceName) {
            if (faceName == null || faceName.isBlank()) {
                return Optional.empty();
            }
            return Optional.ofNullable(uvRectsByFace.get(faceName.trim().toLowerCase(java.util.Locale.ROOT)));
        }

        public Optional<Integer> tintIndexForFace(String faceName) {
            if (faceName == null || faceName.isBlank()) {
                return Optional.empty();
            }
            return Optional.ofNullable(tintIndicesByFace.get(faceName.trim().toLowerCase(java.util.Locale.ROOT)));
        }

        public Optional<String> cullFaceForFace(String faceName) {
            if (faceName == null || faceName.isBlank()) {
                return Optional.empty();
            }
            return Optional.ofNullable(cullFacesByFace.get(faceName.trim().toLowerCase(java.util.Locale.ROOT)));
        }
    }

    public record EchoBlockModelElementRotation(
            double originX,
            double originY,
            double originZ,
            String axis,
            double angleDegrees,
            boolean rescale
    ) {
        public EchoBlockModelElementRotation {
            if (!Double.isFinite(originX) || !Double.isFinite(originY) || !Double.isFinite(originZ)
                    || !Double.isFinite(angleDegrees)) {
                throw new IllegalArgumentException("model element rotation values must be finite");
            }
            axis = normalizedRotationAxis(axis).orElseThrow(
                    () -> new IllegalArgumentException("model element rotation axis must be x, y, or z"));
            if (Math.abs(angleDegrees) < 1.0E-6D) {
                angleDegrees = 0.0D;
            }
        }

        public boolean active() {
            return angleDegrees != 0.0D;
        }
    }

    public record EchoBlockTextureResolution(
            String blockId,
            Optional<String> modelId,
            Optional<String> textureId,
            Optional<String> templateKind,
            List<String> parentChain,
            Optional<EchoBlockModelBounds> modelBounds,
            List<EchoBlockModelBounds> modelElements,
            List<EchoBlockModelElement> modelElementDefinitions,
            Map<String, String> textureIdsByFace,
            Map<String, Integer> uvRotationsByFace,
            Map<String, EchoBlockModelFaceUv> uvRectsByFace,
            Map<String, Integer> tintIndicesByFace,
            int xRotationDegrees,
            int yRotationDegrees,
            boolean uvLock,
            Optional<String> missingReason
    ) {
        public EchoBlockTextureResolution {
            blockId = requireText(blockId, "blockId");
            modelId = modelId == null ? Optional.empty() : modelId;
            textureId = textureId == null ? Optional.empty() : textureId;
            templateKind = templateKind == null ? Optional.empty() : templateKind;
            parentChain = parentChain == null ? List.of() : List.copyOf(parentChain);
            modelBounds = modelBounds == null ? Optional.empty() : modelBounds;
            modelElements = modelElements == null ? List.of() : List.copyOf(modelElements);
            modelElementDefinitions = modelElementDefinitions == null
                    ? List.of()
                    : List.copyOf(modelElementDefinitions);
            textureIdsByFace = textureIdsByFace == null ? Map.of() : Map.copyOf(textureIdsByFace);
            uvRotationsByFace = faceUvRotations(uvRotationsByFace);
            uvRectsByFace = faceUvs(uvRectsByFace);
            tintIndicesByFace = faceTintIndices(tintIndicesByFace);
            xRotationDegrees = normalizeModelRotation(xRotationDegrees);
            yRotationDegrees = normalizeModelRotation(yRotationDegrees);
            missingReason = missingReason == null ? Optional.empty() : missingReason;
        }

        public static EchoBlockTextureResolution missing(String blockId, String reason) {
            return new EchoBlockTextureResolution(
                    blockId,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    List.of(),
                    Optional.empty(),
                    List.of(),
                    List.of(),
                    Map.of(),
                    Map.of(),
                    Map.of(),
                    Map.of(),
                    0,
                    0,
                    false,
                    Optional.of(reason)
            );
        }

        public boolean resolved() {
            return textureId.isPresent();
        }

        public Optional<String> textureNamespace() {
            return textureId.map(value -> splitId(value, "texture id")[0]);
        }

        public Optional<String> texturePath() {
            return textureId.map(value -> splitId(value, "texture id")[1]);
        }

        public Optional<String> textureIdForFace(String faceName) {
            if (faceName == null || faceName.isBlank()) {
                return textureId;
            }
            String normalized = faceName.trim().toLowerCase(java.util.Locale.ROOT);
            return Optional.ofNullable(textureIdsByFace.get(normalized)).or(() -> textureId);
        }

        public int uvRotationDegreesForFace(String faceName) {
            if (faceName == null || faceName.isBlank()) {
                return 0;
            }
            return uvRotationsByFace.getOrDefault(faceName.trim().toLowerCase(java.util.Locale.ROOT), 0);
        }

        public Optional<EchoBlockModelFaceUv> uvRectForFace(String faceName) {
            if (faceName == null || faceName.isBlank()) {
                return Optional.empty();
            }
            return Optional.ofNullable(uvRectsByFace.get(faceName.trim().toLowerCase(java.util.Locale.ROOT)));
        }

        public Optional<Integer> tintIndexForFace(String faceName) {
            if (faceName == null || faceName.isBlank()) {
                return Optional.empty();
            }
            return Optional.ofNullable(tintIndicesByFace.get(faceName.trim().toLowerCase(java.util.Locale.ROOT)));
        }

        public Optional<String> textureNamespaceForFace(String faceName) {
            return textureIdForFace(faceName).map(value -> splitId(value, "texture id")[0]);
        }

        public Optional<String> texturePathForFace(String faceName) {
            return textureIdForFace(faceName).map(value -> splitId(value, "texture id")[1]);
        }

        public EchoBlockModelBounds modelBoundsOrFullCube() {
            return modelBounds.orElse(EchoBlockModelBounds.fullCube());
        }
    }

    private static double clampModelCoordinate(double value) {
        if (!Double.isFinite(value)) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(16.0D, value));
    }

    private static int normalizeModelRotation(int degrees) {
        int normalized = Math.floorMod(degrees, 360);
        return normalized % 90 == 0 ? normalized : 0;
    }
}
