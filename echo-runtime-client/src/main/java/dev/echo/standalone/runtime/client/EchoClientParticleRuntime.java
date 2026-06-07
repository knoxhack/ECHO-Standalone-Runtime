package dev.echo.standalone.runtime.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

final class EchoClientParticleRuntime {
    static final int MAX_ACTIVE_PARTICLES = 160;
    static final int BLOCK_BREAK_PARTICLES = 10;
    static final int BLOCK_PLACE_PARTICLES = 6;

    private final ArrayList<EchoClientParticle> particles = new ArrayList<>();
    private final List<EchoClientParticle> particleView = Collections.unmodifiableList(particles);
    private long nextParticleSequence = 1L;

    void emitAll(List<EchoClientWorldFeedbackEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        for (EchoClientWorldFeedbackEvent event : events) {
            emit(event);
        }
    }

    void emit(EchoClientWorldFeedbackEvent event) {
        if (event == null) {
            return;
        }
        int count = event.kind() == EchoClientWorldFeedbackKind.BLOCK_PLACE
                ? BLOCK_PLACE_PARTICLES
                : BLOCK_BREAK_PARTICLES;
        for (int index = 0; index < count; index++) {
            particles.add(particle(event, index));
        }
        trim();
    }

    void tick(double dt) {
        if (particles.isEmpty()) {
            return;
        }
        ListIterator<EchoClientParticle> iterator = particles.listIterator();
        while (iterator.hasNext()) {
            EchoClientParticle particle = iterator.next();
            if (!particle.tickInPlace(dt)) {
                iterator.remove();
            }
        }
    }

    void clear() {
        particles.clear();
    }

    int count() {
        return particles.size();
    }

    List<EchoClientParticle> particles() {
        return particles.isEmpty() ? List.of() : particleView;
    }

    private EchoClientParticle particle(EchoClientWorldFeedbackEvent event, int index) {
        long sequence = nextParticleSequence++;
        int hash = event.sourceId().hashCode()
                ^ event.kind().ordinal() * 0x45D9F3B
                ^ index * 0x119DE1F3
                ^ (int) sequence;
        double angle = Math.floorMod(hash, 6283) / 1000.0D;
        double radial = 0.18D + Math.floorMod(hash >>> 7, 90) / 300.0D;
        double normalX = event.normalX();
        double normalY = event.normalY();
        double normalZ = event.normalZ();
        double normalLength = Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        if (normalLength <= 0.0001D) {
            normalX = 0.0D;
            normalY = 1.0D;
            normalZ = 0.0D;
        } else {
            normalX /= normalLength;
            normalY /= normalLength;
            normalZ /= normalLength;
        }

        double sideX = Math.cos(angle) * radial;
        double sideZ = Math.sin(angle) * radial;
        double lift = event.kind() == EchoClientWorldFeedbackKind.BLOCK_PLACE ? 0.38D : 0.52D;
        double lifetime = event.kind() == EchoClientWorldFeedbackKind.BLOCK_PLACE ? 0.42D : 0.58D;
        double size = event.kind() == EchoClientWorldFeedbackKind.BLOCK_PLACE ? 0.055D : 0.07D;
        double spawnOffset = event.kind() == EchoClientWorldFeedbackKind.BLOCK_PLACE ? 0.48D : 0.22D;
        return new EchoClientParticle(
                "particle-" + sequence,
                event.kind(),
                event.x() + normalX * spawnOffset + sideX * 0.08D,
                event.y() + normalY * spawnOffset,
                event.z() + normalZ * spawnOffset + sideZ * 0.08D,
                sideX + normalX * 0.28D,
                lift + normalY * 0.18D,
                sideZ + normalZ * 0.28D,
                0.0D,
                lifetime,
                size,
                event.argb()
        );
    }

    private void trim() {
        while (particles.size() > MAX_ACTIVE_PARTICLES) {
            particles.removeFirst();
        }
    }
}
