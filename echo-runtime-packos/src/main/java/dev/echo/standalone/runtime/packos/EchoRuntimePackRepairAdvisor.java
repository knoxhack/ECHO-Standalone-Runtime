package dev.echo.standalone.runtime.packos;

import java.util.ArrayList;
import java.util.List;

public final class EchoRuntimePackRepairAdvisor {
    public EchoRuntimePackRepairPlan advise(
            EchoRuntimePackIntegrityReport integrityReport,
            EchoRuntimePackCompatibilityReport compatibilityReport
    ) {
        List<String> actions = new ArrayList<>();
        for (String blocker : integrityReport.blockers()) {
            actions.add("Review integrity blocker: " + blocker);
        }
        for (String blocker : compatibilityReport.blockers()) {
            actions.add("Review compatibility blocker: " + blocker);
        }
        for (String warning : integrityReport.warnings()) {
            actions.add("Review integrity warning: " + warning);
        }
        for (String warning : compatibilityReport.warnings()) {
            actions.add("Review compatibility warning: " + warning);
        }
        return new EchoRuntimePackRepairPlan(actions.isEmpty(), false, actions);
    }
}
