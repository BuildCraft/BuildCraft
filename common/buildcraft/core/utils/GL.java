package buildcraft.core.utils;

import org.lwjgl.opengl.GL11;

public class GL {
	public static void color(int color) {
		float red = (float) (color >> 16 & 255) / 255.0F;
		float green = (float) (color >> 8 & 255) / 255.0F;
		float blue = (float) (color & 255) / 255.0F;
		GL11.glColor4f(red, green, blue, 1.0F);
	}
}
