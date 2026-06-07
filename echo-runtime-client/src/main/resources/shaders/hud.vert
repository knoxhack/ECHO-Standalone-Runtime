#version 330 core

layout(location = 0) in vec2 aPosition;
layout(location = 1) in vec4 aColor;

out vec4 vColor;

uniform vec3 uScreen;

void main() {
    vec2 pos = aPosition / uScreen.xy * 2.0 - 1.0;
    gl_Position = vec4(pos.x, -pos.y, 0.0, 1.0);
    vColor = aColor;
}
