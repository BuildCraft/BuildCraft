package buildcraft.lib.client.render.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// TODO Calen
public class DelegateFontRenderer extends Font {
    public final Font delegate;

    public DelegateFontRenderer(Font delegate) {
        // TODO Calen creat Font Intance? where is fontset???  When soved, GuidePageContents#<init>:new ConfigurableFontRenderer(Minecraft.getInstance().font).disableShadow();
//        super(
//                Minecraft.getInstance().options,
//                new ResourceLocation("textures/font/ascii.png"),
//                Minecraft.getInstance().textureManager,
//                delegate.getUnicodeFlag()
//        );
        // 参考 FontManager#public Font createFont()
        // FontManager 匿名内部类 protected void apply(Map<ResourceLocation, List<GlyphProvider>> p_95036_, ResourceManager p_95037_, ProfilerFiller p_95038_)
        super((p_95014_) ->
                {
                    try {
                        Field f_fontManager = Minecraft.class.getDeclaredField("fontManager");
                        f_fontManager.setAccessible(true);
                        FontManager mcFontManager = (FontManager) f_fontManager.get(Minecraft.getInstance());
                        Field f_textureManager = FontManager.class.getDeclaredField("textureManager");
                        f_textureManager.setAccessible(true);
                        TextureManager mcFontManagerTextureManager = (TextureManager) f_textureManager.get(mcFontManager);
                        FontSet fontset = new FontSet(mcFontManagerTextureManager, new ResourceLocation("textures/font/ascii.png"));
                        Field f_fontSets = FontManager.class.getDeclaredField("fontSets");
                        f_fontSets.setAccessible(true);
                        Map<ResourceLocation, FontSet> mcFontManager_fontSets = (Map) f_fontSets.get(mcFontManager);
                        mcFontManager_fontSets.put(p_95014_, fontset);
//                new FontSet(Minecraft.getInstance().textureManager,new ResourceLocation("textures/font/ascii.png"))
                        return fontset;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                , false);
        this.delegate = delegate;

        // copy from 1.12.2

        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i >> 0 & 1) * 170 + j;

            if (i == 6) {
                k += 85;
            }

            // Calen anaglyph???
//            if (Minecraft.getInstance().options.anaglyph)
//            {
//                int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
//                int k1 = (k * 30 + l * 70) / 100;
//                int l1 = (k * 30 + i1 * 70) / 100;
//                k = j1;
//                l = k1;
//                i1 = l1;
//            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

//    @Override
//    public void onResourceManagerReload(IResourceManager resourceManager)
//    {
//        delegate.onResourceManagerReload(resourceManager);
//    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    //    @Override
    public final int drawStringWithShadow(GuiGraphics guiGraphics, String text, float x, float y, int color) {
        return drawString(guiGraphics, text, x, y, color, true);
    }

    //    @Override
    public final int drawString(GuiGraphics guiGraphics, String text, int x, int y, int color) {
        return drawString(guiGraphics, text, x, y, color, false);
    }

    //    @Override
    public int drawString(GuiGraphics guiGraphics, String text, float x, float y, int color, boolean dropShadow) {
//        return delegate.drawString(text, x, y, color, dropShadow);
        return guiGraphics.drawString(delegate, text, x, y, color, dropShadow);
//        int ret;
//        if(dropShadow)
//        {
//            int shadowColor = (color & 16579836) >> 2 | color & -16777216;
//            int shadow_i = delegate.drawShadow(poseStack,text, x, y, shadowColor);
//            int foreText_i = delegate.draw(poseStack,text, x, y, color);
//            ret = Math.max(shadow_i,foreText_i);
//        }
//        else
//        {
//            ret = delegate.draw(poseStack,text, x, y, color);
//        }
//        return ret;
//        // 返回值 1.12.2 FontRenderer:334 文本和shadow取大
//        // i = Math.max(i, this.renderString(text, x, y, color, false));
//        // dropShadow -> 调用2次renderString 分别渲染shadow和上层文本
    }

    @Override
    public int width(String text) {
        return delegate.width(text);
    }

    //    @Override
    public int getCharWidth(char character) {
        return delegate.width(String.valueOf(character));
    }

    //
    @Override
    public String plainSubstrByWidth(String text, int width) {
        return delegate.plainSubstrByWidth(text, width);
    }

    public String trimStringToWidth(String text, int width) {
        return delegate.plainSubstrByWidth(text, width);
    }

    //    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        return delegate.plainSubstrByWidth(text, width, reverse);
    }

    @Override
    public String plainSubstrByWidth(String text, int width, boolean reverse) {
        return delegate.plainSubstrByWidth(text, width, reverse);
    }

    //    @Override
    public void drawSplitString(GuiGraphics guiGraphics, String str, int x, int y, int wrapWidth, int textColor) {
        str = this.trimStringNewline(str);
        str = delegate.plainSubstrByWidth(str, wrapWidth);
        guiGraphics.drawString(delegate, str, x, y, textColor);
    }

    /**
     * Remove all newline characters from the end of the string.
     * From 1.12.2
     */
    private String trimStringNewline(String text) {
        while (text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    public int getWordWrappedHeight(String str, int maxLength) {
        return delegate.wordWrapHeight(str, maxLength);
    }

    @Override
    public int wordWrapHeight(String str, int maxLength) {
        return delegate.wordWrapHeight(str, maxLength);
    }

    //    @Override
//    public void setUnicodeFlag(boolean unicodeFlagIn)
//    {
//        delegate.setUnicodeFlag(unicodeFlagIn);
//    }
//
//    @Override
//    public boolean getUnicodeFlag()
//    {
//        return delegate.getUnicodeFlag();
//    }
//
//    @Override
//    public void setBidiFlag(boolean bidiFlagIn)
//    {
//        delegate.setBidiFlag(bidiFlagIn);
//    }
//
//    @Override
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return Arrays.stream(delegate.plainSubstrByWidth(str, wrapWidth).split("\n")).toList();
    }
//
//    @Override
//    public boolean getBidiFlag()
//    {
//        return delegate.getBidiFlag();
//    }
//
//    @Override
//    public int getColorCode(char character)
//    {
//        return delegate(character);
//    }
    /**
     * Array of RGB triplets defining the 16 standard chat colors followed by 16 darker version of the same colors for
     * drop shadows.
     */
    private final int[] colorCode = new int[32];

    public int getColorCode(char character) {
        int i = "0123456789abcdef".indexOf(character);
        return i >= 0 && i < this.colorCode.length ? this.colorCode[i] : -1;
    }
}
