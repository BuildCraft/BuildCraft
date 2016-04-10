package buildcraft.core.guide;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;
import buildcraft.core.guide.parts.GuideImage;
import buildcraft.core.guide.parts.GuideImageFactory;
import buildcraft.core.guide.parts.GuidePartFactory;

public class ImageLoader extends LocationLoader {
    public static GuidePartFactory<GuideImage> loadImage(ResourceLocation location, int width, int height) {
        SimpleTexture texture = new SimpleTexture(location);
        if (Minecraft.getMinecraft().renderEngine.loadTexture(location, texture)) {
            Minecraft.getMinecraft().renderEngine.bindTexture(location);
            int texWidth = GL11.glGetInteger(GL11.GL_TEXTURE_WIDTH);
            int texHeight = GL11.glGetInteger(GL11.GL_TEXTURE_HEIGHT);
            return new GuideImageFactory(location, texWidth, texHeight, width, height);
        } else {
            BCLog.logger.warn("Could not load the image file " + location);
        }
        return null;
    }
}
