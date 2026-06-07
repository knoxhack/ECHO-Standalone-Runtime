#version 330 core

in vec3 vNormal;
in vec4 vColor;
in float vDepth;

out vec4 FragColor;

uniform vec3 uLightDir;
uniform vec3 uFogColor;
uniform float uFogDensity;

void main() {
    vec3 light = normalize(uLightDir);
    float diff = max(dot(normalize(vNormal), light), 0.0);
    float ambient = 0.48;
    float lit = ambient + diff * (1.0 - ambient);
    vec3 color = vColor.rgb * lit;

    float fogFactor = exp(-uFogDensity * uFogDensity * vDepth * vDepth);
    fogFactor = clamp(fogFactor, 0.0, 1.0);
    color = mix(uFogColor, color, fogFactor);

    FragColor = vec4(color, vColor.a);
}
