/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.client.render.font.SpecialColourFontRenderer;
import com.google.common.collect.ImmutableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ColourUtil {
    public static final char MINECRAFT_FORMAT_CHAR;
    public static final String COLOUR_SPECIAL_START;

    public static final Function<ChatFormatting, ChatFormatting> getTextFormatForBlack =
            ColourUtil::getTextFormatForBlack;
    public static final Function<ChatFormatting, ChatFormatting> getTextFormatForWhite =
            ColourUtil::getTextFormatForWhite;

    public static final DyeColor[] COLOURS = DyeColor.values();

    // Calen: damage = 15 - id !!!
    private static final String[] NAMES = { //
            "Black", "Red", "Green", "Brown", //
            "Blue", "Purple", "Cyan", "LightGray", //
            "Gray", "Pink", "Lime", "Yellow", //
            "LightBlue", "Magenta", "Orange", "White"//
    };
    private static final int[] DARK_HEX = { //
            0x2D2D2D, 0xA33835, 0x394C1E, 0x5C3A24, //
            0x3441A2, 0x843FBF, 0x36809E, 0x888888, //
            0x444444, 0xE585A0, 0x3FAA36, 0xCFC231, //
            0x7F9AD1, 0xFF64FF, 0xFF6A00, 0xFFFFFF //
    };
    private static final int[] LIGHT_HEX = { //
            0x181414, 0xBE2B27, 0x007F0E, 0x89502D, //
            0x253193, 0x7e34bf, 0x299799, 0xa0a7a7, //
            0x7A7A7A, 0xD97199, 0x39D52E, 0xFFD91C, //
            0x66AAFF, 0xD943C6, 0xEA7835, 0xe4e4e4 //
    };
    private static final String[] DYES = new String[16];
    private static final Map<String, DyeColor> nameToColourMap;
    private static final int[] FACE_TO_COLOUR;

    private static final ChatFormatting[] FORMATTING_VALUES = ChatFormatting.values();

    private static final ChatFormatting[] COLOUR_TO_FORMAT = new ChatFormatting[16];
    private static final ChatFormatting[] REPLACE_FOR_WHITE = new ChatFormatting[16];
    private static final ChatFormatting[] REPLACE_FOR_BLACK = new ChatFormatting[16];
    private static final ChatFormatting[] REPLACE_FOR_WHITE_HIGH_CONTRAST = new ChatFormatting[16];
    private static final ChatFormatting[] REPLACE_FOR_BLACK_HIGH_CONTRAST = new ChatFormatting[16];
    private static final ChatFormatting[] FACE_TO_FORMAT = new ChatFormatting[6];

    private static final Pattern ALL_FORMAT_MATCHER = Pattern.compile("(?i)\u00a7[0-9A-Za-z]");

    static {
        MINECRAFT_FORMAT_CHAR = '\u00a7';
        COLOUR_SPECIAL_START = MINECRAFT_FORMAT_CHAR + "z" + MINECRAFT_FORMAT_CHAR;
        for (int i = 0; i < 16; i++) {
            DYES[i] = "dye" + NAMES[i];
            REPLACE_FOR_WHITE[i] = REPLACE_FOR_WHITE_HIGH_CONTRAST[i] = FORMATTING_VALUES[i];
            REPLACE_FOR_BLACK[i] = REPLACE_FOR_BLACK_HIGH_CONTRAST[i] = FORMATTING_VALUES[i];
        }

        replaceColourForWhite(ChatFormatting.WHITE, ChatFormatting.GRAY);
        replaceColourForWhite(ChatFormatting.YELLOW, ChatFormatting.GOLD);
        replaceColourForWhite(ChatFormatting.AQUA, ChatFormatting.BLUE);
        replaceColourForWhite(ChatFormatting.GREEN, ChatFormatting.DARK_GREEN);

        replaceColourForBlack(ChatFormatting.BLACK, ChatFormatting.GRAY);
        replaceColourForBlack(ChatFormatting.DARK_GRAY, ChatFormatting.GRAY);
        replaceColourForBlack(ChatFormatting.DARK_BLUE, ChatFormatting.BLUE, ChatFormatting.AQUA);
        replaceColourForBlack(ChatFormatting.BLUE, ChatFormatting.BLUE, ChatFormatting.AQUA);
        replaceColourForBlack(ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE);
        replaceColourForBlack(ChatFormatting.DARK_RED, ChatFormatting.RED);
        replaceColourForBlack(ChatFormatting.DARK_GREEN, ChatFormatting.GREEN);

        COLOUR_TO_FORMAT[DyeColor.BLACK.ordinal()] = ChatFormatting.BLACK;
        COLOUR_TO_FORMAT[DyeColor.GRAY.ordinal()] = ChatFormatting.DARK_GRAY;
        COLOUR_TO_FORMAT[DyeColor.LIGHT_GRAY.ordinal()] = ChatFormatting.GRAY;
        COLOUR_TO_FORMAT[DyeColor.WHITE.ordinal()] = ChatFormatting.WHITE;

        COLOUR_TO_FORMAT[DyeColor.RED.ordinal()] = ChatFormatting.DARK_RED;
        COLOUR_TO_FORMAT[DyeColor.BLUE.ordinal()] = ChatFormatting.BLUE;
        COLOUR_TO_FORMAT[DyeColor.CYAN.ordinal()] = ChatFormatting.DARK_AQUA;
        COLOUR_TO_FORMAT[DyeColor.LIGHT_BLUE.ordinal()] = ChatFormatting.AQUA;

        COLOUR_TO_FORMAT[DyeColor.GREEN.ordinal()] = ChatFormatting.DARK_GREEN;
        COLOUR_TO_FORMAT[DyeColor.LIME.ordinal()] = ChatFormatting.GREEN;
        COLOUR_TO_FORMAT[DyeColor.BROWN.ordinal()] = ChatFormatting.GOLD;
        COLOUR_TO_FORMAT[DyeColor.YELLOW.ordinal()] = ChatFormatting.YELLOW;

        COLOUR_TO_FORMAT[DyeColor.ORANGE.ordinal()] = ChatFormatting.GOLD;
        COLOUR_TO_FORMAT[DyeColor.PURPLE.ordinal()] = ChatFormatting.DARK_PURPLE;
        COLOUR_TO_FORMAT[DyeColor.MAGENTA.ordinal()] = ChatFormatting.LIGHT_PURPLE;
        COLOUR_TO_FORMAT[DyeColor.PINK.ordinal()] = ChatFormatting.LIGHT_PURPLE;

        FACE_TO_FORMAT[Direction.UP.ordinal()] = ChatFormatting.WHITE;
        FACE_TO_FORMAT[Direction.DOWN.ordinal()] = ChatFormatting.BLACK;
        FACE_TO_FORMAT[Direction.NORTH.ordinal()] = ChatFormatting.RED;
        FACE_TO_FORMAT[Direction.SOUTH.ordinal()] = ChatFormatting.BLUE;
        FACE_TO_FORMAT[Direction.EAST.ordinal()] = ChatFormatting.YELLOW;
        FACE_TO_FORMAT[Direction.WEST.ordinal()] = ChatFormatting.GREEN;

        ImmutableMap.Builder<String, DyeColor> builder = ImmutableMap.builder();
        for (DyeColor c : COLOURS) {
            builder.put(c.getName(), c);
        }
        nameToColourMap = builder.build();

        FACE_TO_COLOUR = new int[6];
        FACE_TO_COLOUR[Direction.DOWN.ordinal()] = 0xFF_33_33_33;
        FACE_TO_COLOUR[Direction.UP.ordinal()] = 0xFF_CC_CC_CC;
    }

    private static void replaceColourForBlack(ChatFormatting colour, ChatFormatting with) {
        replaceColourForBlack(colour, with, with);
    }

    private static void replaceColourForBlack(ChatFormatting colour, ChatFormatting normal, ChatFormatting highContrast) {
        REPLACE_FOR_BLACK[colour.ordinal()] = normal;
        REPLACE_FOR_BLACK_HIGH_CONTRAST[colour.ordinal()] = highContrast;
    }

    private static void replaceColourForWhite(ChatFormatting colour, ChatFormatting with) {
        replaceColourForWhite(colour, with, with);
    }

    private static void replaceColourForWhite(ChatFormatting colour, ChatFormatting normal, ChatFormatting highContrast) {
        REPLACE_FOR_WHITE[colour.ordinal()] = normal;
        REPLACE_FOR_WHITE_HIGH_CONTRAST[colour.ordinal()] = highContrast;
    }

    @Nullable
    public static DyeColor parseColourOrNull(String string) {
        return nameToColourMap.get(string);
    }

    public static String getDyeName(DyeColor colour) {
//        return DYES[colour.getDyeDamage()];
        return DYES[15 - colour.getId()];
    }

    public static String getName(DyeColor colour) {
//        return NAMES[colour.getDyeDamage()];
        return NAMES[15 - colour.getId()];
    }

    public static int getDarkHex(DyeColor colour) {
//        return DARK_HEX[colour.getDyeDamage()];
        return DARK_HEX[15 - colour.getId()];
    }

    public static int getLightHex(DyeColor colour) {
//        return LIGHT_HEX[colour.getDyeDamage()];
        return LIGHT_HEX[15 - colour.getId()];
    }

    public static int getColourForSide(Direction face) {
        return FACE_TO_COLOUR[face.ordinal()];
    }

    public static String[] getNameArray() {
        return Arrays.copyOf(NAMES, NAMES.length);
    }

    /** Returns a string formatted for use in a tooltip (or anything else with a black background). If
     * {@link BCLibConfig#useColouredLabels} is true then this will make prefix the string with an appropriate
     * {@link ChatFormatting} colour, and postfix with {@link ChatFormatting#RESET} */
    public static String getTextFullTooltip(DyeColor colour) {
        if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
            return formatColour.toString() + getTextFormatForBlack(formatColour) + LocaleUtil.localizeColour(colour)
                    + ChatFormatting.RESET;
        } else {
            return LocaleUtil.localizeColour(colour);
        }
    }

    // Calen
    public static MutableComponent getTextFullTooltipComponent(MutableComponent base, @Nullable DyeColor colour) {
        if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
//            return base.append(Component.literal(formatColour.toString() + getTextFormatForBlack(formatColour))).append(Component.translatable(LocaleUtil.getColorTranslateKey(colour)))
//                    .append(Component.literal(ChatFormatting.RESET.toString()));
            if (formatColour == null) {
                return base.append(Component.translatable(LocaleUtil.getColorTranslateKey(colour)))
                        .append(Component.literal(ChatFormatting.RESET.toString()));
            } else {
                return base.append(Component.literal(formatColour.toString() + getTextFormatForBlack(formatColour)))
                        .append(Component.translatable(LocaleUtil.getColorTranslateKey(colour)))
                        .append(Component.literal(ChatFormatting.RESET.toString()));
            }
        } else {
            return base.append(Component.translatable(LocaleUtil.getColorTranslateKey(colour)));
        }
    }

    // Calen
    public static MutableComponent getTextFullTooltipComponent(String translationKey, @Nullable DyeColor colour) {
        MutableComponent v;
        if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
//            return base.append(Component.literal(formatColour.toString() + getTextFormatForBlack(formatColour))).append(Component.translatable(LocaleUtil.getColorTranslateKey(colour)))
//                    .append(Component.literal(ChatFormatting.RESET.toString()));
            if (formatColour == null) {
                v = Component.translatable(LocaleUtil.getColorTranslateKey(colour))
                        .append(Component.literal(ChatFormatting.RESET.toString()));
            } else {
                v = Component.literal(formatColour.toString() + getTextFormatForBlack(formatColour))
                        .append(Component.translatable(LocaleUtil.getColorTranslateKey(colour)))
                        .append(Component.literal(ChatFormatting.RESET.toString()));
            }
        } else {
            v = Component.translatable(LocaleUtil.getColorTranslateKey(colour));
        }
        return Component.translatable(translationKey, v);
    }

    // Calen
    public static String getTextFullTooltipString(String translationKey, @Nullable DyeColor colour) {
        String colorStr;
        if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
//            return base.append(Component.literal(formatColour.toString() + getTextFormatForBlack(formatColour))).append(Component.translatable(LocaleUtil.getColorTranslateKey(colour)))
//                    .append(Component.literal(ChatFormatting.RESET.toString()));
            if (formatColour == null) {
                colorStr = Component.translatable(LocaleUtil.getColorTranslateKey(colour)).getString()
                        + ChatFormatting.RESET.toString();
            } else {
                colorStr = formatColour.toString()
                        + getTextFormatForBlack(formatColour)
                        + Component.translatable(LocaleUtil.getColorTranslateKey(colour)).getString()
                        + ChatFormatting.RESET.toString();
            }
        } else {
            colorStr = Component.translatable(LocaleUtil.getColorTranslateKey(colour)).getString();
        }
        return Component.translatable(translationKey, colorStr).getString();
    }

    public static MutableComponent getTextFullTooltipComponent(DyeColor colour) {
        if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
            return Component.literal(formatColour.toString() + getTextFormatForBlack(formatColour)).append(Component.translatable(LocaleUtil.getColorTranslateKey(colour)))
                    .append(Component.literal(ChatFormatting.RESET.toString()));
        } else {
            return Component.translatable(LocaleUtil.getColorTranslateKey(colour));
        }
    }

    public static MutableComponent getTextFullTooltipComponent(@Nonnull DyeColor colour, MutableComponent after) {
        if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
            return Component.literal(formatColour.toString() + getTextFormatForBlack(formatColour) + " ").append(Component.translatable(LocaleUtil.getColorTranslateKey(colour)))
                    .append(Component.literal(ChatFormatting.RESET.toString())).append(after);
        } else {
            return Component.translatable(LocaleUtil.getColorTranslateKey(colour)).append(after);
        }
    }

    /** Similar to {@link #getTextFullTooltip(DyeColor)}, but outputs a string specifically designed for
     * {@link SpecialColourFontRenderer}. MUST be the first string used! */
    public static String getTextFullTooltipSpecial(DyeColor colour) {
        // Calen
        if (colour == null) {
            return "";
        }
        if (colour == DyeColor.BLACK || colour == DyeColor.BLUE) {
            return getTextFullTooltip(colour);
        } else if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
            return COLOUR_SPECIAL_START + Integer.toHexString(colour.getId())//
                    + getTextFormatForBlack(formatColour) + LocaleUtil.localizeColour(colour) + ChatFormatting.RESET;
        } else {
            return LocaleUtil.localizeColour(colour);
        }
    }

    public static MutableComponent getTextFullTooltipSpecialComponent(MutableComponent base, DyeColor colour) {
        if (colour == DyeColor.BLACK || colour == DyeColor.BLUE) {
            return getTextFullTooltipComponent(base, colour);
        } else if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
            return base.append(Component.literal(COLOUR_SPECIAL_START + Integer.toHexString(colour.getId())//
                    + getTextFormatForBlack(formatColour))).append(Component.translatable(LocaleUtil.getColorTranslateKey(colour))).append(Component.literal(ChatFormatting.RESET.toString()));
        } else {
            return base.append(Component.translatable(LocaleUtil.getColorTranslateKey(colour)));
        }
    }

    public static MutableComponent getTextFullTooltipSpecialComponent(DyeColor colour) {
        // Calen
        if (colour == null) {
            return Component.literal("");
        }
        if (colour == DyeColor.BLACK || colour == DyeColor.BLUE) {
            return getTextFullTooltipComponent(colour);
        } else if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
            return Component.literal(COLOUR_SPECIAL_START + Integer.toHexString(colour.getId()) + getTextFormatForBlack(formatColour))
                    .append(Component.translatable(LocaleUtil.getColorTranslateKey(colour))).append(Component.literal(ChatFormatting.RESET.toString()));
        } else {
            return Component.translatable(LocaleUtil.getColorTranslateKey(colour));
        }
    }

    public static MutableComponent getTextFullTooltipSpecialComponent(DyeColor colour, MutableComponent after) {
        if (colour == null) {
            return after;
        }
        if (colour == DyeColor.BLACK || colour == DyeColor.BLUE) {
            return getTextFullTooltipComponent(colour, after);
        } else if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertColourToTextFormat(colour);
            return Component.literal(COLOUR_SPECIAL_START + Integer.toHexString(colour.getId())//
                            + getTextFormatForBlack(formatColour))
                    .append(Component.translatable(LocaleUtil.getColorTranslateKey(colour)))
                    .append(Component.literal(" " + ChatFormatting.RESET.toString())).append(after);
        } else {
            return Component.translatable(LocaleUtil.getColorTranslateKey(colour)).append(after);
        }
    }

    /** Returns a string formatted for use in a tooltip (or anything else with a black background). If
     * {@link BCLibConfig#useColouredLabels} is true then this will make prefix the string with an appropriate
     * {@link ChatFormatting} colour, and postfixed with {@link ChatFormatting#RESET} */
    public static String getTextFullTooltip(Direction face) {
        if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertFaceToTextFormat(face);
            return formatColour.toString() + getTextFormatForBlack(formatColour) + LocaleUtil.localizeFacing(face)
                    + ChatFormatting.RESET;
        } else {
            return LocaleUtil.localizeFacing(face);
        }
    }

    // Calen
    public static Component getTextFullTooltipComponent(Direction face) {
        if (BCLibConfig.useColouredLabels) {
            ChatFormatting formatColour = convertFaceToTextFormat(face);
            return Component.literal(formatColour.toString() + getTextFormatForBlack(formatColour)).append(LocaleUtil.localizeFacingComponent(face))
                    .append(Component.literal(ChatFormatting.RESET.toString()));
        } else {
            return LocaleUtil.localizeFacingComponent(face);
        }
    }

    /** Returns a {@link ChatFormatting} colour that will display correctly on a black background, so it won't use any
     * of the darker colours (as they will be difficult to see). */
    public static ChatFormatting getTextFormatForBlack(ChatFormatting in) {
        if (in.isColor()) {
            if (BCLibConfig.useHighContrastLabelColours) {
                return REPLACE_FOR_BLACK_HIGH_CONTRAST[in.ordinal()];
            } else {
                return REPLACE_FOR_BLACK[in.ordinal()];
            }
        } else {
            return in;
        }
    }

    /** Returns a {@link ChatFormatting} colour that will display correctly on a white background, so it won't use any
     * of the lighter colours (as they will be difficult to see). */
    public static ChatFormatting getTextFormatForWhite(ChatFormatting in) {
        if (in.isColor()) {
            if (BCLibConfig.useHighContrastLabelColours) {
                return REPLACE_FOR_WHITE_HIGH_CONTRAST[in.ordinal()];
            } else {
                return REPLACE_FOR_WHITE[in.ordinal()];
            }
        } else {
            return in;
        }
    }

    /** Converts an {@link DyeColor} into an equivalent {@link ChatFormatting} for display. */
//    public static ChatFormatting convertColourToTextFormat(DyeColor colour)
    public static ChatFormatting convertColourToTextFormat(@Nullable DyeColor colour) {
//        return COLOUR_TO_FORMAT[colour.ordinal()];
        return colour == null ? null : COLOUR_TO_FORMAT[colour.ordinal()];
    }

    /** Converts an {@link Direction} into an equivalent {@link ChatFormatting} for display. */
    public static ChatFormatting convertFaceToTextFormat(Direction face) {
        return FACE_TO_FORMAT[face.ordinal()];
    }

    public static int swapArgbToAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = (argb >> 0) & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public static DyeColor getNext(DyeColor colour) {
        int ord = colour.ordinal() + 1;
        return COLOURS[ord & 15];
    }

    public static DyeColor getNextOrNull(@Nullable DyeColor colour) {
        if (colour == null) {
            return COLOURS[0];
        } else if (colour == COLOURS[COLOURS.length - 1]) {
            return null;
        } else {
            return getNext(colour);
        }
    }

    public static DyeColor getPrev(DyeColor colour) {
        int ord = colour.ordinal() + 16 - 1;
        return COLOURS[ord & 15];
    }

    public static DyeColor getPrevOrNull(@Nullable DyeColor colour) {
        if (colour == null) {
            return COLOURS[COLOURS.length - 1];
        } else if (colour == COLOURS[0]) {
            return null;
        } else {
            return getPrev(colour);
        }
    }

    /** Similar to {@link ChatFormatting#stripFormatting(String)}, but also removes every special char
     * that {@link #getTextFullTooltipSpecial(DyeColor)} can add. */
    public static String stripAllFormatCodes(String string) {
        return ALL_FORMAT_MATCHER.matcher(string).replaceAll("");
    }

    // Calen
    public static ItemStack addColourTagToStack(ItemStack stack, DyeColor colour) {
        CompoundTag tag = new CompoundTag();
        tag.putString("colour", colour.getName());
        stack.setTag(tag);
        return stack;
    }

    public static ItemStack addColourTagToStack(ItemStack stack, int colour) {
        if (colour >= 0 && colour < 16) {
            CompoundTag tag = new CompoundTag();
            tag.putString("colour", DyeColor.byId(colour).getName());
            stack.setTag(tag);
        }
        return stack;
    }

    // Colors: 0->no color 1-15->
    @Nullable
    public static DyeColor getStackColourFromTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("colour")) {
                DyeColor colour = DyeColor.byName(tag.getString("colour"), null);
                if (colour != null) {
                    return colour;
                } else {
                    throw new RuntimeException("Invalid colour!");
                }
            }
        }
        return null;
    }

    @Nonnull
    public static int getStackColourIdFromTag(ItemStack stack) {
        DyeColor colour = getStackColourFromTag(stack);
        return colour == null ? -1 : colour.getId();
    }
}
