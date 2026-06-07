package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.List;
import java.util.Locale;

record EchoClientWorldInteractionCatalog(
        List<Rule> rules
) {
    EchoClientWorldInteractionCatalog {
        rules = List.copyOf(rules == null ? List.of() : rules);
    }

    static EchoClientWorldInteractionCatalog empty() {
        return new EchoClientWorldInteractionCatalog(List.of());
    }

    EchoClientScreenRouteRequest routeFor(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return EchoClientScreenRouteRequest.NONE;
        }
        String text = (block.id() + " " + block.displayName()).toLowerCase(Locale.ROOT);
        for (Rule rule : rules) {
            if (rule.matches(text)) {
                return rule.route();
            }
        }
        return EchoClientScreenRouteRequest.NONE;
    }

    record Rule(
            List<String> matchTokens,
            EchoClientScreenCommand command,
            String targetId
    ) {
        Rule {
            matchTokens = (matchTokens == null ? List.<String>of() : matchTokens).stream()
                    .filter(token -> token != null && !token.isBlank())
                    .map(token -> token.trim().toLowerCase(Locale.ROOT))
                    .distinct()
                    .toList();
            if (matchTokens.isEmpty()) {
                throw new IllegalArgumentException("matchTokens must not be empty");
            }
            command = command == null ? EchoClientScreenCommand.NONE : command;
            targetId = targetId == null ? "" : targetId.trim();
        }

        boolean matches(String blockText) {
            for (String token : matchTokens) {
                if (blockText.contains(token)) {
                    return true;
                }
            }
            return false;
        }

        EchoClientScreenRouteRequest route() {
            if (command == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN && !targetId.isBlank()) {
                return new EchoClientScreenRouteRequest(command, targetId);
            }
            return new EchoClientScreenRouteRequest(command);
        }
    }
}
