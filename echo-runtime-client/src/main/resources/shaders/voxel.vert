#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec3 aNormal;
layout(location = 3) in vec4 aColor;

out vec2 vTexCoord;
out vec3 vNormal;
out vec4 vColor;
out float vDepth;

uniform mat4 uProjection;
uniform mat4 uView;

void main() {
    vec4 worldPos = vec4(aPosition, 1.0);
    vec4 clipPos = uProjection * uView * worldPos;
    gl_Position = clipPos;
    vTexCoord = aTexCoord;
    vNormal = aNormal;
    vColor = aColor;
    vDepth = length((uView * worldPos).xyz);
}
