package buildcraft.lib.client.render.font;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class DelegateFontRenderer extends FontRenderer {
    public final FontRenderer delegate;

    public DelegateFontRenderer(FontRenderer delegate) {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"),
            Minecraft.getMinecraft().renderEngine, delegate.getUnicodeFlag());
        this.delegate = delegate;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        delegate.onResourceManagerReload(resourceManager);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public final int drawStringWithShadow(String text, float x, float y, int color) {
        return drawString(text, x, y, color, true);
    }

    @Override
    public final int drawString(String text, int x, int y, int color) {
        return drawString(text, x, y, color, false);
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        return delegate.drawString(text, x, y, color, dropShadow);
    }

    @Override
    public int getStringWidth(String text) {
        return delegate.getStringWidth(text);
    }

    @Override
    public int getCharWidth(char character) {
        return delegate.getCharWidth(character);
    }

    @Override
    public String trimStringToWidth(String text, int width) {
        return delegate.trimStringToWidth(text, width);
    }

    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        return delegate.trimStringToWidth(text, width, reverse);
    }

    @Override
    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        delegate.drawSplitString(str, x, y, wrapWidth, textColor);
    }

    @Override
    public int getWordWrappedHeight(String str, int maxLength) {
        return delegate.getWordWrappedHeight(str, maxLength);
    }

    @Override
    public void setUnicodeFlag(boolean unicodeFlagIn) {
        delegate.setUnicodeFlag(unicodeFlagIn);
    }

    @Override
    public boolean getUnicodeFlag() {
        return delegate.getUnicodeFlag();
    }

    @Override
    public void setBidiFlag(boolean bidiFlagIn) {
        delegate.setBidiFlag(bidiFlagIn);
    }

    @Override
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return delegate.listFormattedStringToWidth(str, wrapWidth);
    }

    @Override
    public boolean getBidiFlag() {
        return delegate.getBidiFlag();
    }

    @Override
    public int getColorCode(char character) {
        return delegate.getColorCode(character);
    }
}
