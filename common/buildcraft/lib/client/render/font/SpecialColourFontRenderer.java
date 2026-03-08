package buildcraft.lib.client.render.font;

import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.ColourUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class SpecialColourFontRenderer extends Font {
    public static final SpecialColourFontRenderer INSTANCE = new SpecialColourFontRenderer();

    private SpecialColourFontRenderer() {
        // TODO Calen textureManager?
        super((resourceLocation -> new FontSet(Minecraft.getInstance().textureManager, new ResourceLocation("textures/font/ascii.png"))));
//        super(Minecraft.getInstance().gameSettings, new ResourceLocation("textures/font/ascii.png"),
//            Minecraft.getInstance().renderEngine, false);
    }

    // TODO Calen dropShadow???
    @Override
    public int draw(PoseStack poseStack, String text, float x, float y, int color) {

        int next = text.indexOf(ColourUtil.COLOUR_SPECIAL_START);
        int taken = 0;

        if (next > 0) {
            // Render some of it normally
            // TODO Calen dropShadow???
            x = getRealRenderer().draw(poseStack, text.substring(0, next), x, y, color);
            taken = next;
        }

        while (next != -1) {

            int end = text.indexOf(ChatFormatting.RESET.toString());
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

            // TODO Calen dropShadow???
            // 似乎drawShaw是文字+shadow 不是纯shadow
//            x = getRealRenderer().draw(sub, x, y, thisColour, dropShadow);
            x = getRealRenderer().draw(poseStack, sub, x, y, thisColour);

            next = text.indexOf(ColourUtil.COLOUR_SPECIAL_START, end);
        }

        if (taken < text.length()) {
//            x = getRealRenderer().drawString(text.substring(taken), x, y, color, dropShadow);
            x = getRealRenderer().draw(poseStack, text.substring(taken), x, y, color);
        }

        return (int) x;
    }

    private static Font getRealRenderer() {
        return Minecraft.getInstance().font;
    }

    // TODO Calen ???
    // Delegate methods (To ensure we have the exact same behaviour as the normal font renderer)

//    @Override
//    public void onResourceManagerReload(ResourceManager resourceManager)
//    {
//        // NO-OP
//    }

    @Override
//    public int getStringWidth(String text)
    public int width(String text) {
//        return getRealRenderer().getStringWidth(text);
        return getRealRenderer().width(text);
    }

//    @Override
//    public int getCharWidth(char character)
//    {
//        return getRealRenderer().getCharWidth(character);
//    }

    @Override
//    public String trimStringToWidth(String text, int width)
    public String plainSubstrByWidth(String text, int width) {
//        return getRealRenderer().trimStringToWidth(text, width);
        return getRealRenderer().plainSubstrByWidth(text, width);
    }

    @Override
//    public String trimStringToWidth(String text, int width, boolean reverse)
    public String plainSubstrByWidth(String text, int width, boolean reverse) {
//        return getRealRenderer().trimStringToWidth(text, width, reverse);
        return getRealRenderer().plainSubstrByWidth(text, width, reverse);
    }

    @Override
//    public int getWordWrappedHeight(String str, int maxLength)
    public int wordWrapHeight(String str, int maxLength) {
//        return getRealRenderer().getWordWrappedHeight(str, maxLength);
        return getRealRenderer().wordWrapHeight(str, maxLength);
    }

    // TODO Calen ???
//    @Override
//    public void setUnicodeFlag(boolean unicodeFlagIn)
//    {
//        getRealRenderer().setUnicodeFlag(unicodeFlagIn);
//    }

    // TODO Calen ???
//    @Override
//    public boolean getUnicodeFlag()
//    {
//        return getRealRenderer().getUnicodeFlag();
//    }

//    @Override
//    public void setBidiFlag(boolean bidiFlagIn)
//    {
//        getRealRenderer().setBidiFlag(bidiFlagIn);
//    }

    @Override
//    public List<String> listFormattedStringToWidth(String str, int wrapWidth)
    public FormattedText substrByWidth(FormattedText str, int wrapWidth) {
//        return getRealRenderer().listFormattedStringToWidth(str, wrapWidth);
        return getRealRenderer().substrByWidth(str, wrapWidth);
    }

    @Override
//    public boolean getBidiFlag()
    public boolean isBidirectional() {
//        return getRealRenderer().getBidiFlag();
        return getRealRenderer().isBidirectional();
    }

    // TODO Calen ???
//    @Override
//    public int getColorCode(char character)
//    {
//        return getRealRenderer().getColorCode(character);
//    }
}
