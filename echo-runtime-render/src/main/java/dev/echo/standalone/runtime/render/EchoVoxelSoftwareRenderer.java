package dev.echo.standalone.runtime.render;

import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class EchoVoxelSoftwareRenderer {
    private static final double NEAR = 0.12D;
    private static final int SKY_TOP = 0xFF07151A;
    private static final int SKY_BOTTOM = 0xFF132423;

    private static final double MAX_RENDER_DISTANCE = 56.0D;
    private static final double MAX_RENDER_DISTANCE_SQUARED = MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE;

    public EchoVoxelFramebuffer render(EchoVoxelWorld world, EchoVoxelCamera camera, int width, int height) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(camera, "camera");
        int[] pixels = new int[width * height];
        drawBackground(pixels, width, height);
        double yaw = Math.toRadians(camera.yawDegrees());
        double pitch = Math.toRadians(camera.pitchDegrees());
        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);
        double sinPitch = Math.sin(pitch);
        double cosPitch = Math.cos(pitch);
        double focal = width / (2.0D * Math.tan(Math.toRadians(70.0D) / 2.0D));
        EchoVoxelRenderPacket renderPacket = new EchoVoxelChunkMesher().buildPacket(world, camera);
        int chunkSize = world.chunkSize();
        double halfChunk = chunkSize / 2.0D;
        ArrayList<Face> faces = new ArrayList<>();
        for (EchoVoxelChunkMesh chunkMesh : renderPacket.chunkMeshes()) {
            double chunkCenterX = chunkMesh.chunkId().x() * chunkSize + halfChunk;
            double chunkCenterY = chunkMesh.chunkId().y() * chunkSize + halfChunk;
            double chunkCenterZ = chunkMesh.chunkId().z() * chunkSize + halfChunk;
            double dx = chunkCenterX - camera.x();
            double dy = chunkCenterY - camera.y();
            double dz = chunkCenterZ - camera.z();
            if (dx * dx + dy * dy + dz * dz > MAX_RENDER_DISTANCE_SQUARED) {
                continue;
            }
            for (EchoVoxelMeshFace meshFace : chunkMesh.faces()) {
                addFace(camera, meshFace, faces, sinYaw, cosYaw, sinPitch, cosPitch);
            }
        }
        // Stable, deterministic back-to-front order: farthest first, with
        // tiebreakers on block coordinates + direction so the ordering never
        // flips frame-to-frame (which is what caused the flicker before).
        faces.sort(Comparator.comparingDouble(Face::depth).reversed()
                .thenComparingInt(Face::blockX)
                .thenComparingInt(Face::blockY)
                .thenComparingInt(Face::blockZ)
                .thenComparing(f -> f.direction().ordinal()));
        int drawn = 0;
        for (Face face : faces) {
            if (drawFace(pixels, width, height, face, focal)) {
                drawn += 1;
            }
        }
        drawCrosshair(pixels, width, height);
        return new EchoVoxelFramebuffer(width, height, pixels, renderPacket.sourceBlockCount(), drawn, checksum(pixels));
    }

    private static void drawBackground(int[] pixels, int width, int height) {
        for (int y = 0; y < height; y++) {
            double t = height <= 1 ? 0.0D : (double) y / (double) (height - 1);
            int color = mix(SKY_TOP, SKY_BOTTOM, t);
            Arrays.fill(pixels, y * width, (y + 1) * width, color);
        }
    }

    private static void addFace(EchoVoxelCamera camera, EchoVoxelMeshFace meshFace, List<Face> faces,
                                double sinYaw, double cosYaw, double sinPitch, double cosPitch) {
        int x = meshFace.x();
        int y = meshFace.y();
        int z = meshFace.z();
        if (!faceVisibleFrom(camera, meshFace)) {
            return;
        }
        double[] offs = cornerOffsets(meshFace);
        addProjectedFace(camera, meshFace, x, y, z, offs, faces, sinYaw, cosYaw, sinPitch, cosPitch);
    }

    private static boolean faceVisibleFrom(EchoVoxelCamera camera, EchoVoxelMeshFace face) {
        return switch (face.direction()) {
            case UP -> camera.y() > face.y() + face.maxY();
            case DOWN -> camera.y() < face.y() + face.minY();
            case EAST -> camera.x() > face.x() + face.maxX();
            case WEST -> camera.x() < face.x() + face.minX();
            case SOUTH -> camera.z() > face.z() + face.maxZ();
            case NORTH -> camera.z() < face.z() + face.minZ();
        };
    }

    private static double[] cornerOffsets(EchoVoxelMeshFace face) {
        double minX = face.minX();
        double minY = face.minY();
        double minZ = face.minZ();
        double maxX = face.maxX();
        double maxY = face.maxY();
        double maxZ = face.maxZ();
        return switch (face.direction()) {
            case UP -> new double[]{minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ};
            case EAST -> new double[]{maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ};
            case WEST -> new double[]{minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ};
            case SOUTH -> new double[]{minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ};
            case NORTH -> new double[]{maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ};
            case DOWN -> new double[]{minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ};
        };
    }

    private static void addProjectedFace(
            EchoVoxelCamera camera,
            EchoVoxelMeshFace meshFace,
            int bx, int by, int bz,
            double[] offs,
            List<Face> faces,
            double sinYaw, double cosYaw, double sinPitch, double cosPitch
    ) {
        Projected[] projected = new Projected[4];
        double depth = 0.0D;
        for (int i = 0; i < 4; i++) {
            Projected point = project(camera, bx + offs[i * 3], by + offs[i * 3 + 1], bz + offs[i * 3 + 2],
                    sinYaw, cosYaw, sinPitch, cosPitch);
            if (point == null) {
                return;
            }
            projected[i] = point;
            depth += point.depth();
        }
        double avgDepth = depth / 4.0D;
        int baseColor = shade(meshFace.material().argb(), meshFace.direction().shade());
        int detailColor = shade(meshFace.material().detailArgb(), meshFace.direction().shade());
        int color = computeFaceColor(meshFace, baseColor, detailColor, avgDepth);
        faces.add(new Face(
                projected,
                meshFace.material(),
                meshFace.direction(),
                meshFace.x(),
                meshFace.y(),
                meshFace.z(),
                baseColor,
                detailColor,
                avgDepth,
                color
        ));
    }

    private static int computeFaceColor(EchoVoxelMeshFace meshFace, int baseColor, int detailColor, double depth) {
        int hash = meshFace.material().atlasKey().hashCode();
        hash = hash * 31 + meshFace.x();
        hash = hash * 31 + meshFace.y();
        hash = hash * 31 + meshFace.z();
        hash = hash * 31 + meshFace.direction().ordinal();
        hash ^= hash >>> 16;
        hash = hash & 0x7FFFFFFF;
        int patternColor = switch (meshFace.material().pattern()) {
            case ASH_GRAIN, WASTELAND_GRASS, BERRY_BUSH -> ashGrain(baseColor, detailColor, hash);
            case BASALT_CRACKS, RUBBLE_PILE -> basaltCracks(baseColor, detailColor, hash);
            case RUST_PATCHES, TWISTED_METAL, ORE_VEIN -> rustPatches(baseColor, detailColor, hash);
            case TERMINAL_GRID -> terminalGrid(baseColor, detailColor, hash);
            case CACHE_PANEL -> cachePanel(baseColor, detailColor, hash);
            case POWER_NODE -> powerNode(baseColor, detailColor, hash);
            case HAZARD_STRIPES -> hazardStripes(baseColor, detailColor, meshFace.x(), meshFace.z());
            case MARKER_GRID -> markerGrid(baseColor, detailColor, hash);
            case WATER_RATION, TOXIC_PUDDLE -> waterRation(baseColor, detailColor, hash);
            case FLAT -> baseColor;
        };
        double fogFactor = clamp((depth - FOG_START) / (FOG_END - FOG_START), 0.0D, 1.0D);
        return mix(patternColor, FOG_COLOR, fogFactor);
    }

    private static Projected project(EchoVoxelCamera camera, double px, double py, double pz,
                                     double sinYaw, double cosYaw, double sinPitch, double cosPitch) {
        double dx = px - camera.x();
        double dy = py - camera.y();
        double dz = pz - camera.z();
        double camX = cosYaw * dx - sinYaw * dz;
        double yawZ = sinYaw * dx + cosYaw * dz;
        double camY = cosPitch * dy - sinPitch * yawZ;
        double camZ = sinPitch * dy + cosPitch * yawZ;
        if (camZ <= NEAR) {
            return null;
        }
        return new Projected(camX, camY, camZ);
    }

    private static boolean drawFace(int[] pixels, int width, int height, Face face, double focal) {
        ScreenPoint[] points = new ScreenPoint[face.points().length];
        for (int index = 0; index < face.points().length; index++) {
            Projected point = face.points()[index];
            points[index] = new ScreenPoint(
                    width / 2.0D + point.x() * focal / point.depth(),
                    height / 2.0D - point.y() * focal / point.depth(),
                    point.depth()
            );
        }
        if (Math.abs(area(points)) < 0.01D) {
            return false;
        }
        // Rasterize the whole convex quad in one scanline pass (no internal
        // diagonal seam) so the solid face has no visible triangulation.
        fillConvexPolygon(pixels, width, height, points, face.color());
        return true;
    }

    private static void fillConvexPolygon(int[] pixels, int width, int height, ScreenPoint[] poly, int color) {
        double minYd = Double.MAX_VALUE;
        double maxYd = -Double.MAX_VALUE;
        for (ScreenPoint p : poly) {
            minYd = Math.min(minYd, p.y());
            maxYd = Math.max(maxYd, p.y());
        }
        int minY = clamp((int) Math.floor(minYd), 0, height - 1);
        int maxY = clamp((int) Math.ceil(maxYd), 0, height - 1);
        int n = poly.length;
        for (int y = minY; y <= maxY; y++) {
            double scanY = y + 0.5D;
            double xLeft = Double.MAX_VALUE;
            double xRight = -Double.MAX_VALUE;
            // Find the span by intersecting the scanline with every polygon edge.
            for (int i = 0; i < n; i++) {
                ScreenPoint a = poly[i];
                ScreenPoint b = poly[(i + 1) % n];
                double ay = a.y();
                double by = b.y();
                if ((scanY >= ay && scanY < by) || (scanY >= by && scanY < ay)) {
                    double t = (scanY - ay) / (by - ay);
                    double x = a.x() + t * (b.x() - a.x());
                    if (x < xLeft) {
                        xLeft = x;
                    }
                    if (x > xRight) {
                        xRight = x;
                    }
                }
            }
            if (xRight < xLeft) {
                continue;
            }
            int startX = clamp((int) Math.round(xLeft), 0, width - 1);
            int endX = clamp((int) Math.round(xRight), 0, width - 1);
            int rowBase = y * width;
            for (int x = startX; x <= endX; x++) {
                pixels[rowBase + x] = color;
            }
        }
    }

    private static final double FOG_START = 48.0D;
    private static final double FOG_END = 92.0D;
    private static final int FOG_COLOR = 0xFF0D1A1F;

    private static int ashGrain(int baseColor, int detailColor, int hash) {
        int grain = hash & 15;
        if (grain < 3) {
            return mix(baseColor, detailColor, 0.34D);
        }
        if (grain == 15) {
            return shade(baseColor, 0.72D);
        }
        return baseColor;
    }

    private static int basaltCracks(int baseColor, int detailColor, int hash) {
        if (Math.floorMod(hash, 41) < 2 || Math.floorMod(hash * 3, 53) == 0) {
            return shade(baseColor, 0.48D);
        }
        return (hash & 31) == 0 ? mix(baseColor, detailColor, 0.18D) : baseColor;
    }

    private static int rustPatches(int baseColor, int detailColor, int hash) {
        int cell = Math.floorMod(hash * 17, 29);
        if (cell < 6) {
            return mix(baseColor, detailColor, 0.46D);
        }
        if (cell == 28) {
            return shade(baseColor, 0.62D);
        }
        return baseColor;
    }

    private static int terminalGrid(int baseColor, int detailColor, int hash) {
        if (Math.floorMod(hash, 13) == 0 || Math.floorMod(hash, 11) == 0) {
            return mix(baseColor, detailColor, 0.52D);
        }
        if ((hash & 63) == 0) {
            return detailColor;
        }
        return baseColor;
    }

    private static int cachePanel(int baseColor, int detailColor, int hash) {
        if (Math.floorMod(hash, 24) < 2 || Math.floorMod(hash, 18) < 2) {
            return shade(baseColor, 0.56D);
        }
        return (hash & 23) == 0 ? mix(baseColor, detailColor, 0.40D) : baseColor;
    }

    private static int powerNode(int baseColor, int detailColor, int hash) {
        int pulse = Math.floorMod(hash, 17);
        if (pulse < 2) {
            return detailColor;
        }
        if (Math.floorMod(hash, 19) == 0) {
            return shade(baseColor, 0.58D);
        }
        return baseColor;
    }

    private static int hazardStripes(int baseColor, int detailColor, int blockX, int blockZ) {
        int stripe = Math.floorMod(blockX * 5 + blockZ * 3, 22);
        return stripe < 9 ? baseColor : detailColor;
    }

    private static int markerGrid(int baseColor, int detailColor, int hash) {
        if (Math.floorMod(hash, 10) < 2 || Math.floorMod(hash, 10) < 2) {
            return mix(baseColor, detailColor, 0.58D);
        }
        return (hash & 31) == 1 ? detailColor : baseColor;
    }

    private static int waterRation(int baseColor, int detailColor, int hash) {
        if (Math.floorMod(hash, 9) < 2) {
            return mix(baseColor, detailColor, 0.42D);
        }
        return (hash & 15) == 0 ? detailColor : baseColor;
    }

    private static double area(ScreenPoint[] points) {
        double value = 0.0D;
        for (int index = 0; index < points.length; index++) {
            ScreenPoint a = points[index];
            ScreenPoint b = points[(index + 1) % points.length];
            value += a.x() * b.y() - b.x() * a.y();
        }
        return value * 0.5D;
    }

    private static void drawLine(int[] pixels, int width, int height, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int error = dx + dy;
        int x = x0;
        int y = y0;
        while (true) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                pixels[y * width + x] = color;
            }
            if (x == x1 && y == y1) {
                break;
            }
            int twice = 2 * error;
            if (twice >= dy) {
                error += dy;
                x += sx;
            }
            if (twice <= dx) {
                error += dx;
                y += sy;
            }
        }
    }

    private static void drawCrosshair(int[] pixels, int width, int height) {
        int x = width / 2;
        int y = height / 2;
        int color = 0xCCECF8F1;
        drawLine(pixels, width, height, x - 8, y, x - 2, y, color);
        drawLine(pixels, width, height, x + 2, y, x + 8, y, color);
        drawLine(pixels, width, height, x, y - 8, x, y - 2, color);
        drawLine(pixels, width, height, x, y + 2, x, y + 8, color);
    }

    private static int shade(int color, double factor) {
        int alpha = (color >>> 24) & 0xFF;
        int red = clamp((int) Math.round(((color >>> 16) & 0xFF) * factor), 0, 255);
        int green = clamp((int) Math.round(((color >>> 8) & 0xFF) * factor), 0, 255);
        int blue = clamp((int) Math.round((color & 0xFF) * factor), 0, 255);
        return argb(alpha, red, green, blue);
    }

    private static int mix(int a, int b, double t) {
        int alpha = (int) Math.round(((a >>> 24) & 0xFF) * (1.0D - t) + ((b >>> 24) & 0xFF) * t);
        int red = (int) Math.round(((a >>> 16) & 0xFF) * (1.0D - t) + ((b >>> 16) & 0xFF) * t);
        int green = (int) Math.round(((a >>> 8) & 0xFF) * (1.0D - t) + ((b >>> 8) & 0xFF) * t);
        int blue = (int) Math.round((a & 0xFF) * (1.0D - t) + (b & 0xFF) * t);
        return argb(alpha, red, green, blue);
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (clamp(alpha, 0, 255) << 24)
                | (clamp(red, 0, 255) << 16)
                | (clamp(green, 0, 255) << 8)
                | clamp(blue, 0, 255);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static long checksum(int[] pixels) {
        long hash = 0xcbf29ce484222325L;
        for (int pixel : pixels) {
            hash ^= pixel & 0xFFFFFFFFL;
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private record Vec3(double x, double y, double z) {
    }

    private record Projected(double x, double y, double depth) {
    }

    private record ScreenPoint(double x, double y, double depth) {
    }

    private record Face(
            Projected[] points,
            EchoVoxelMeshMaterial material,
            EchoVoxelMeshDirection direction,
            int blockX,
            int blockY,
            int blockZ,
            int baseColor,
            int detailColor,
            double depth,
            int color
    ) {
    }
}
