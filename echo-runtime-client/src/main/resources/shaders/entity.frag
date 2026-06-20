#version 330 core

in vec2 vTexCoord;
in vec3 vNormal;
in vec4 vColor;
in float vDepth;

out vec4 FragColor;

uniform sampler2D uAtlas;
uniform vec3 uLightDir;
uniform vec3 uFogColor;
uniform float uFogDensity;

void main() {
    vec4 tex = texture(uAtlas, vTexCoord);
    if (tex.a < 0.1) discard;

    vec3 light = normalize(uLightDir);
    float diff = max(dot(normalize(vNormal), light), 0.0);
    float ambient = 0.48;
    float lit = ambient + diff * (1.0 - ambient);
    vec3 color = tex.rgb * vColor.rgb * lit;

    float fogFactor = exp(-uFogDensity * uFogDensity * vDepth * vDepth);
    fogFactor = clamp(fogFactor, 0.0, 1.0);
    color = mix(uFogColor, color, fogFactor);

    FragColor = vec4(color, tex.a * vColor.a);
}
