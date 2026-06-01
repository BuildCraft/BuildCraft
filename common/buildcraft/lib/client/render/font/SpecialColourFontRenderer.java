package buildcraft.lib.client.render.font;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;

import buildcraft.api.core.BCLog;

import buildcraft.lib.misc.ColourUtil;

public class SpecialColourFontRenderer extends TextRenderer {
    public static final SpecialColourFontRenderer INSTANCE = new SpecialColourFontRenderer();

    private SpecialColourFontRenderer() {
        super(MinecraftClient.getInstance().options, new Identifier("textures/font/ascii.png"),
            MinecraftClient.getInstance().renderEngine, false);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {

        int next = text.indexOf(ColourUtil.COLOUR_SPECIAL_START);
        int taken = 0;

        if (next > 0) {
            // Render some of it normally
            x = getRealRenderer().drawString(text.substring(0, next), x, y, color, dropShadow);
            taken = next;
        }

        while (next != -1) {

            int end = text.indexOf(Formatting.RESET.toString());
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
                thisColour = ColourUtil.getLightHex(DyeColor.byId(ord));
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

    private static TextRenderer getRealRenderer() {
        return MinecraftClient.getInstance().fontRenderer;
    }

    // Delegate methods (To ensure we have the exact same behaviour as the normal font renderer)

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void reload(ResourceManager resourceManager) {
        // NO-OP
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getStringWidth(String text) {
        return getRealRenderer().getWidth(text);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getCharWidth(char character) {
        return getRealRenderer().getCharWidth(character);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String trimStringToWidth(String text, int width) {
        return getRealRenderer().trimStringToWidth(text, width);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String trimStringToWidth(String text, int width, boolean reverse) {
        return getRealRenderer().trimStringToWidth(text, width, reverse);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getWordWrappedHeight(String str, int maxLength) {
        return getRealRenderer().getWordWrappedHeight(str, maxLength);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void setUnicodeFlag(boolean unicodeFlagIn) {
        getRealRenderer().setUnicodeFlag(unicodeFlagIn);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean getUnicodeFlag() {
        return getRealRenderer().getUnicodeFlag();
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void setBidiFlag(boolean bidiFlagIn) {
        getRealRenderer().setBidiFlag(bidiFlagIn);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return getRealRenderer().listFormattedStringToWidth(str, wrapWidth);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean getBidiFlag() {
        return getRealRenderer().getBidiFlag();
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getColorCode(char character) {
        return getRealRenderer().getColorCode(character);
    }
}
