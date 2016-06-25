package buildcraft.lib.client.resource;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuideImage;
import buildcraft.lib.client.guide.parts.GuidePartFactory;

public class TextureResourceHolder extends ResourceHolder implements GuidePartFactory {
    private SimpleTexture texture;
    private boolean langFallback;
    private final int dispWidth, dispHeight;
    private int texWidth, texHeight;

    public TextureResourceHolder(ResourceLocation location) {
        this(location, -1, -1);
    }

    public TextureResourceHolder(ResourceLocation location, int width, int height) {
        super(location);
        dispWidth = width;
        dispHeight = height;
    }

    @Override
    protected byte[] load(IResourceManager resourceManager) {
        if (createFrom(getLocationForLang(false))) {
            langFallback = false;
        } else if (createFrom(getLocationForLang(true))) {
            langFallback = true;
        } else {
            texture = null;
        }
        return null;
    }

    private boolean createFrom(ResourceLocation locationBase) {
        texture = new SimpleTexture(locationBase);
        if (Minecraft.getMinecraft().renderEngine.loadTexture(locationBase, texture)) {
            Minecraft.getMinecraft().renderEngine.bindTexture(locationBase);
            texWidth = GL11.glGetInteger(GL11.GL_TEXTURE_WIDTH);
            texHeight = GL11.glGetInteger(GL11.GL_TEXTURE_HEIGHT);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onLoad(byte[] data) {}

    @Override
    public GuideImage createNew(GuiGuide gui) {
        return new GuideImage(gui, getLocationForLang(langFallback), texWidth, texHeight, dispWidth, dispHeight);
    }

}
