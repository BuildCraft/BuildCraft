package buildcraft.lib.client.render.font;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import buildcraft.api.core.BCLog;

import buildcraft.lib.misc.ColourUtil;

public class SpecialColourFontRenderer extends FontRenderer {
    public static final SpecialColourFontRenderer INSTANCE = new SpecialColourFontRenderer();

    private SpecialColourFontRenderer() {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"),
            Minecraft.getMinecraft().renderEngine, false);
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {

        int next = text.indexOf(ColourUtil.COLOUR_SPECIAL_START);
        int taken = 0;

        if (next > 0) {
            // Render some of it normally
            x = getRealRenderer().drawString(text.substring(0, next), x, y, color, dropShadow);
            taken = next;
        }

        while (next != -1) {

            int end = text.indexOf(TextFormatting.RESET.toString());
            String sub;
            if (end > 0) {
                sub = text.substring(next, end);
                taken = end;
            } else {
                sub = text.substring(next);
                taken = text.length();
            }

            char c = text.charAt(next + 3);
            int thisColour = color;
            try {
                int ord = Integer.parseInt(Character.toString(c), 16);
                thisColour = ColourUtil.getLightHex(EnumDyeColor.byMetadata(ord));
            } catch (NumberFormatException nfe) {
                BCLog.logger
                    .warn("[lib.font] Invalid colour string for SpecialColourFontRenderer! " + nfe.getMessage());
            }

            x = getRealRenderer().drawString(sub, x, y, thisColour, dropShadow);

            next = text.indexOf(ColourUtil.COLOUR_SPECIAL_START, end);
        }

        if (taken < text.length()) {
            x = getRealRenderer().drawString(text.substring(taken), x, y, color, dropShadow);
        }

        return (int) x;
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
