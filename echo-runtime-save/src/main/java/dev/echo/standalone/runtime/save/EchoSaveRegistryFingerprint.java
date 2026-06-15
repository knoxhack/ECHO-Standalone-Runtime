package dev.echo.standalone.runtime.save;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Computes a deterministic SHA-256 fingerprint of an ordered registry ID list.
 */
public final class EchoSaveRegistryFingerprint {

    public static final String ALGORITHM = "sha256:echo.standalone.registry_fingerprint.v1";

    private EchoSaveRegistryFingerprint() {
    }

    public static String compute(List<String> orderedIds) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
        for (String id : orderedIds) {
            digest.update(id.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
        }
        return bytesToHex(digest.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }
}
