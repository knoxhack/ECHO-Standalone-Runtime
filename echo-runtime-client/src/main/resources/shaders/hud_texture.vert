#version 330 core

layout(location = 0) in vec2 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aTint;

out vec2 vTexCoord;
out vec4 vTint;

uniform vec3 uScreen;

void main() {
    vec2 pos = aPosition / uScreen.xy * 2.0 - 1.0;
    gl_Position = vec4(pos.x, -pos.y, 0.0, 1.0);
    vTexCoord = aTexCoord;
    vTint = aTint;
}
