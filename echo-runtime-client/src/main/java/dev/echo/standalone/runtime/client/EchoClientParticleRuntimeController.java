package dev.echo.standalone.runtime.client;

import java.util.List;

final class EchoClientParticleRuntimeController {
    private final EchoClientRuntimeServices runtimeServices;
    private final EchoClientParticleRuntime particles = new EchoClientParticleRuntime();

    EchoClientParticleRuntimeController(EchoClientRuntimeServices runtimeServices) {
        if (runtimeServices == null) {
            throw new IllegalArgumentException("runtimeServices must not be null");
        }
        this.runtimeServices = runtimeServices;
    }

    void updateFromGameplay(double dt) {
        if (!runtimeServices.hasActiveWorld()) {
            particles.clear();
            runtimeServices.gameplay().consumeFeedbackEvents();
            return;
        }
        particles.emitAll(runtimeServices.gameplay().consumeFeedbackEvents());
        particles.tick(dt);
    }

    int activeParticleCount() {
        return particles.count();
    }

    List<EchoClientParticle> particles() {
        return particles.particles();
    }
}
