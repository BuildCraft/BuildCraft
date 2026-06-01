package buildcraft.lib.client.render.font;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class DelegateFontRenderer extends TextRenderer {
    public final TextRenderer delegate;

    public DelegateFontRenderer(TextRenderer delegate) {
        super(MinecraftClient.getInstance().options, new Identifier("textures/font/ascii.png"),
            MinecraftClient.getInstance().renderEngine, delegate.getUnicodeFlag());
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

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void reload(ResourceManager resourceManager) {
        delegate.onResourceManagerReload(resourceManager);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public final int drawStringWithShadow(String text, float x, float y, int color) {
        return drawString(text, x, y, color, true);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public final int drawString(String text, int x, int y, int color) {
        return drawString(text, x, y, color, false);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        return delegate.drawString(text, x, y, color, dropShadow);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getStringWidth(String text) {
        return delegate.getWidth(text);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getCharWidth(char character) {
        return delegate.getCharWidth(character);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String trimStringToWidth(String text, int width) {
        return delegate.trimStringToWidth(text, width);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String trimStringToWidth(String text, int width, boolean reverse) {
        return delegate.trimStringToWidth(text, width, reverse);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        delegate.drawSplitString(str, x, y, wrapWidth, textColor);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getWordWrappedHeight(String str, int maxLength) {
        return delegate.getWordWrappedHeight(str, maxLength);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void setUnicodeFlag(boolean unicodeFlagIn) {
        delegate.setUnicodeFlag(unicodeFlagIn);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean getUnicodeFlag() {
        return delegate.getUnicodeFlag();
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void setBidiFlag(boolean bidiFlagIn) {
        delegate.setBidiFlag(bidiFlagIn);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return delegate.listFormattedStringToWidth(str, wrapWidth);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean getBidiFlag() {
        return delegate.getBidiFlag();
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getColorCode(char character) {
        return delegate.getColorCode(character);
    }
}
