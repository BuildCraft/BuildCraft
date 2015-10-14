// The opengl shader language version, this requires opengl 3.0 or later
#version 150

layout (points) in;
layout (quads) out;

layout (location = 0) in vec3 min;
layout (location = 1) in vec3 max;
layout (location = 2) in vec3 point;
layout (location = 3) in vec3 direction;


void main() {
	for (int i = 0; i < 8; i++) {
		gl_Position = vec4(0, 0, 0, 0);
		EmitVertex();
	}
	EndPrimitive();
}
