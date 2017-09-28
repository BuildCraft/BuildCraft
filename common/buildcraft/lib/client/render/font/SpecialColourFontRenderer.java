package buildcraft.lib.client.render.font;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

import buildcraft.lib.misc.ColourUtil;

public class SpecialColourFontRenderer extends FontRenderer {
    public static final SpecialColourFontRenderer INSTANCE = new SpecialColourFontRenderer();

    private SpecialColourFontRenderer() {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().renderEngine, false);
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        int index = text.indexOf(ColourUtil.COLOUR_SPECIAL_START);
        // Items start with their rarity colour first - even if its white
        if (index == 0 || (text.startsWith("§") && index == 2)) {
            String c2 = text.substring(index + ColourUtil.COLOUR_SPECIAL_START.length());
            if (c2.length() > 3) {
                try {
                    int ord = Integer.parseInt(c2.substring(0, 1), 16);
                    text = c2.substring(3);
                    color = ColourUtil.getLightHex(EnumDyeColor.byMetadata(ord));
                } catch (NumberFormatException nfe) {
                    BCLog.logger.warn("[lib.font] Invalid colour string for SpecialColourFontRenderer! " + nfe.getMessage());
                }
            }
        }
        return getRealRenderer().drawString(text, x, y, color, dropShadow);
    }

    private static FontRenderer getRealRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    // Delegate methods (To ensure we have the exact same behaviour as the normal font renderer)

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        // NO-OP
    }

    @Override
    public int getStringWidth(String text) {
        return getRealRenderer().getStringWidth(text);
    }

    @Override
    public int getCharWidth(char character) {
        return getRealRenderer().getCharWidth(character);
    }

    @Override
    public String trimStringToWidth(String text, int width) {
        return getRealRenderer().trimStringToWidth(text, width);
    }

    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        return getRealRenderer().trimStringToWidth(text, width, reverse);
    }

    @Override
    public int getWordWrappedHeight(String str, int maxLength) {
        return getRealRenderer().getWordWrappedHeight(str, maxLength);
    }

    @Override
    public void setUnicodeFlag(boolean unicodeFlagIn) {
        getRealRenderer().setUnicodeFlag(unicodeFlagIn);
    }

    @Override
    public boolean getUnicodeFlag() {
        return getRealRenderer().getUnicodeFlag();
    }

    @Override
    public void setBidiFlag(boolean bidiFlagIn) {
        getRealRenderer().setBidiFlag(bidiFlagIn);
    }

    @Override
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return getRealRenderer().listFormattedStringToWidth(str, wrapWidth);
    }

    @Override
    public boolean getBidiFlag() {
        return getRealRenderer().getBidiFlag();
    }

    @Override
    public int getColorCode(char character) {
        return getRealRenderer().getColorCode(character);
    }
}
