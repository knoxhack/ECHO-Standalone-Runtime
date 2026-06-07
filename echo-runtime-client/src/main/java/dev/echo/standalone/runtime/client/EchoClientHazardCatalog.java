package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.world.EchoVoxelBiome;

import java.util.List;

record EchoClientHazardCatalog(
        List<Rule> rules
) {
    EchoClientHazardCatalog {
        rules = List.copyOf(rules == null ? List.of() : rules);
    }

    static EchoClientHazardCatalog empty() {
        return new EchoClientHazardCatalog(List.of());
    }

    HazardProfile profileForBiome(EchoVoxelBiome biome) {
        if (biome == null) {
            return HazardProfile.none();
        }
        for (Rule rule : rules) {
            if (rule.matches(biome)) {
                return rule.profile();
            }
        }
        return HazardProfile.none();
    }

    static Rule hazardWhenAnyTag(HazardProfile profile, String... tags) {
        return new Rule(List.of(tags), profile);
    }

    static HazardProfile hazard(String hazardId, String label, double exposurePerSecond, int damage) {
        return new HazardProfile(hazardId, label, exposurePerSecond, damage);
    }

    record Rule(
            List<String> biomeTags,
            HazardProfile profile
    ) {
        Rule {
            biomeTags = List.copyOf(biomeTags == null ? List.of() : biomeTags);
            profile = profile == null ? HazardProfile.none() : profile;
        }

        boolean matches(EchoVoxelBiome biome) {
            for (String tag : biomeTags) {
                if (tag == null || tag.isBlank()) {
                    continue;
                }
                String normalized = tag.trim();
                if (biome.id().equals(normalized)
                        || biome.path().equals(normalized)
                        || biome.hasTag(normalized)) {
                    return true;
                }
            }
            return false;
        }
    }

    record HazardProfile(
            String hazardId,
            String label,
            double exposurePerSecond,
            int damage
    ) {
        HazardProfile {
            hazardId = hazardId == null || hazardId.isBlank() ? "echo:none" : hazardId.trim();
            label = label == null || label.isBlank() ? "None" : label.trim();
            exposurePerSecond = Math.max(0.0D, exposurePerSecond);
            damage = Math.max(0, damage);
        }

        static HazardProfile none() {
            return new HazardProfile("echo:none", "None", 0.0D, 0);
        }

        boolean inactive() {
            return damage <= 0 || exposurePerSecond <= 0.0D || hazardId.equals("echo:none");
        }
    }
}
