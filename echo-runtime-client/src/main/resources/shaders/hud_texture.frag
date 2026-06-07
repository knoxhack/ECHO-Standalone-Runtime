#version 330 core

in vec2 vTexCoord;
in vec4 vTint;

out vec4 FragColor;

uniform sampler2D uTexture;

void main() {
    vec4 tex = texture(uTexture, vTexCoord);
    if (tex.a < 0.05) discard;
    FragColor = tex * vTint;
}
