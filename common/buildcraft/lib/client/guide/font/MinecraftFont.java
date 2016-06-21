package buildcraft.lib.client.guide.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import buildcraft.core.lib.client.render.RenderUtils;

/** Implements a font that delegates to Minecraft's own {@link FontRenderer} */
public enum MinecraftFont implements IFontRenderer {
    INSTANCE;

    private static FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRendererObj;
    }

    @Override
    public int getStringWidth(String text) {
        return getFontRenderer().getStringWidth(text);
    }

    @Override
    public int getFontHeight() {
        return getFontRenderer().FONT_HEIGHT;
    }

    @Override
    public int drawString(String text, int x, int y, int colour) {
        int v = getFontRenderer().drawString(text, x, y, colour);
        RenderUtils.setGLColorFromInt(-1);
        return v;
    }
}
