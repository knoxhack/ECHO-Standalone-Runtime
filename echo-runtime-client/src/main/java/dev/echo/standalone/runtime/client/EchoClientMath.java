package dev.echo.standalone.runtime.client;

/**
 * Minimal 4x4 matrix and vector math for the OpenGL renderer.
 */
final class EchoClientMath {
    private EchoClientMath() {}

    public static float[] identity() {
        float[] m = new float[16];
        m[0] = 1.0f; m[5] = 1.0f; m[10] = 1.0f; m[15] = 1.0f;
        return m;
    }

    public static float[] perspective(float fovDegrees, float aspect, float near, float far) {
        float fovRadians = (float) Math.toRadians(fovDegrees);
        float f = 1.0f / (float) Math.tan(fovRadians / 2.0f);
        float nf = 1.0f / (near - far);
        float[] m = new float[16];
        m[0] = f / aspect;
        m[5] = f;
        m[10] = (far + near) * nf;
        m[11] = -1.0f;
        m[14] = 2.0f * far * near * nf;
        return m;
    }

    public static float[] lookAt(float eyeX, float eyeY, float eyeZ,
                                  float centerX, float centerY, float centerZ,
                                  float upX, float upY, float upZ) {
        float fx = centerX - eyeX;
        float fy = centerY - eyeY;
        float fz = centerZ - eyeZ;
        float fl = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
        fx /= fl; fy /= fl; fz /= fl;

        float sx = fy * upZ - fz * upY;
        float sy = fz * upX - fx * upZ;
        float sz = fx * upY - fy * upX;
        float sl = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
        sx /= sl; sy /= sl; sz /= sl;

        float ux = sy * fz - sz * fy;
        float uy = sz * fx - sx * fz;
        float uz = sx * fy - sy * fx;

        float[] m = new float[16];
        m[0] = sx;  m[1] = ux;  m[2] = -fx;  m[3] = 0.0f;
        m[4] = sy;  m[5] = uy;  m[6] = -fy;  m[7] = 0.0f;
        m[8] = sz;  m[9] = uz;  m[10] = -fz; m[11] = 0.0f;
        m[12] = -(sx * eyeX + sy * eyeY + sz * eyeZ);
        m[13] = -(ux * eyeX + uy * eyeY + uz * eyeZ);
        m[14] = fx * eyeX + fy * eyeY + fz * eyeZ;
        m[15] = 1.0f;
        return m;
    }

    public static float[] multiply(float[] a, float[] b) {
        float[] r = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                r[i * 4 + j] =
                    a[i * 4 + 0] * b[0 * 4 + j] +
                    a[i * 4 + 1] * b[1 * 4 + j] +
                    a[i * 4 + 2] * b[2 * 4 + j] +
                    a[i * 4 + 3] * b[3 * 4 + j];
            }
        }
        return r;
    }

    public static float[] translation(float x, float y, float z) {
        float[] m = identity();
        m[12] = x; m[13] = y; m[14] = z;
        return m;
    }

    public static float[] rotationY(float degrees) {
        float rad = (float) Math.toRadians(degrees);
        float c = (float) Math.cos(rad);
        float s = (float) Math.sin(rad);
        float[] m = identity();
        m[0] = c;  m[2] = s;
        m[8] = -s; m[10] = c;
        return m;
    }
}
