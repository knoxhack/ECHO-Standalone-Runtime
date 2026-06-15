package dev.echo.standalone.runtime.save.anvil;

import java.util.List;
import java.util.Map;

/**
 * Raw data decoded from one Anvil chunk column.
 *
 * @param chunkX chunk X coordinate in region
 * @param chunkZ chunk Z coordinate in region
 * @param sections map from section Y to raw section data
 * @param blockEntities list of block entity NBT compounds as raw strings (placeholder)
 * @param entities list of entity NBT compounds as raw strings (placeholder)
 */
public record EchoAnvilChunkData(
        int chunkX,
        int chunkZ,
        Map<Integer, EchoAnvilSectionData> sections,
        List<String> blockEntities,
        List<String> entities
) {

    public boolean hasSection(int sectionY) {
        return sections.containsKey(sectionY);
    }

    public EchoAnvilSectionData section(int sectionY) {
        return sections.get(sectionY);
    }
}
