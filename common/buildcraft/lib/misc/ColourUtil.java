/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.client.render.font.SpecialColourFontRenderer;
import com.google.common.collect.ImmutableMap;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ColourUtil {
    public static final char MINECRAFT_FORMAT_CHAR;
    public static final String COLOUR_SPECIAL_START;

    public static final Function<TextFormatting, TextFormatting> getTextFormatForBlack =
            ColourUtil::getTextFormatForBlack;
    public static final Function<TextFormatting, TextFormatting> getTextFormatForWhite =
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

    private static final TextFormatting[] FORMATTING_VALUES = TextFormatting.values();

    private static final TextFormatting[] COLOUR_TO_FORMAT = new TextFormatting[16];
    private static final TextFormatting[] REPLACE_FOR_WHITE = new TextFormatting[16];
    private static final TextFormatting[] REPLACE_FOR_BLACK = new TextFormatting[16];
    private static final TextFormatting[] REPLACE_FOR_WHITE_HIGH_CONTRAST = new TextFormatting[16];
    private static final TextFormatting[] REPLACE_FOR_BLACK_HIGH_CONTRAST = new TextFormatting[16];
    private static final TextFormatting[] FACE_TO_FORMAT = new TextFormatting[6];

    private static final Pattern ALL_FORMAT_MATCHER = Pattern.compile("(?i)\u00a7[0-9A-Za-z]");

    static {
        MINECRAFT_FORMAT_CHAR = '\u00a7';
        COLOUR_SPECIAL_START = MINECRAFT_FORMAT_CHAR + "z" + MINECRAFT_FORMAT_CHAR;
        for (int i = 0; i < 16; i++) {
            DYES[i] = "dye" + NAMES[i];
            REPLACE_FOR_WHITE[i] = REPLACE_FOR_WHITE_HIGH_CONTRAST[i] = FORMATTING_VALUES[i];
            REPLACE_FOR_BLACK[i] = REPLACE_FOR_BLACK_HIGH_CONTRAST[i] = FORMATTING_VALUES[i];
        }

        replaceColourForWhite(TextFormatting.WHITE, TextFormatting.GRAY);
        replaceColourForWhite(TextFormatting.YELLOW, TextFormatting.GOLD);
        replaceColourForWhite(TextFormatting.AQUA, TextFormatting.BLUE);
        replaceColourForWhite(TextFormatting.GREEN, TextFormatting.DARK_GREEN);

        replaceColourForBlack(TextFormatting.BLACK, TextFormatting.GRAY);
        replaceColourForBlack(TextFormatting.DARK_GRAY, TextFormatting.GRAY);
        replaceColourForBlack(TextFormatting.DARK_BLUE, TextFormatting.BLUE, TextFormatting.AQUA);
        replaceColourForBlack(TextFormatting.BLUE, TextFormatting.BLUE, TextFormatting.AQUA);
        replaceColourForBlack(TextFormatting.DARK_PURPLE, TextFormatting.LIGHT_PURPLE);
        replaceColourForBlack(TextFormatting.DARK_RED, TextFormatting.RED);
        replaceColourForBlack(TextFormatting.DARK_GREEN, TextFormatting.GREEN);

        COLOUR_TO_FORMAT[DyeColor.BLACK.ordinal()] = TextFormatting.BLACK;
        COLOUR_TO_FORMAT[DyeColor.GRAY.ordinal()] = TextFormatting.DARK_GRAY;
        COLOUR_TO_FORMAT[DyeColor.LIGHT_GRAY.ordinal()] = TextFormatting.GRAY;
        COLOUR_TO_FORMAT[DyeColor.WHITE.ordinal()] = TextFormatting.WHITE;

        COLOUR_TO_FORMAT[DyeColor.RED.ordinal()] = TextFormatting.DARK_RED;
        COLOUR_TO_FORMAT[DyeColor.BLUE.ordinal()] = TextFormatting.BLUE;
        COLOUR_TO_FORMAT[DyeColor.CYAN.ordinal()] = TextFormatting.DARK_AQUA;
        COLOUR_TO_FORMAT[DyeColor.LIGHT_BLUE.ordinal()] = TextFormatting.AQUA;

        COLOUR_TO_FORMAT[DyeColor.GREEN.ordinal()] = TextFormatting.DARK_GREEN;
        COLOUR_TO_FORMAT[DyeColor.LIME.ordinal()] = TextFormatting.GREEN;
        COLOUR_TO_FORMAT[DyeColor.BROWN.ordinal()] = TextFormatting.GOLD;
        COLOUR_TO_FORMAT[DyeColor.YELLOW.ordinal()] = TextFormatting.YELLOW;

        COLOUR_TO_FORMAT[DyeColor.ORANGE.ordinal()] = TextFormatting.GOLD;
        COLOUR_TO_FORMAT[DyeColor.PURPLE.ordinal()] = TextFormatting.DARK_PURPLE;
        COLOUR_TO_FORMAT[DyeColor.MAGENTA.ordinal()] = TextFormatting.LIGHT_PURPLE;
        COLOUR_TO_FORMAT[DyeColor.PINK.ordinal()] = TextFormatting.LIGHT_PURPLE;

        FACE_TO_FORMAT[Direction.UP.ordinal()] = TextFormatting.WHITE;
        FACE_TO_FORMAT[Direction.DOWN.ordinal()] = TextFormatting.BLACK;
        FACE_TO_FORMAT[Direction.NORTH.ordinal()] = TextFormatting.RED;
        FACE_TO_FORMAT[Direction.SOUTH.ordinal()] = TextFormatting.BLUE;
        FACE_TO_FORMAT[Direction.EAST.ordinal()] = TextFormatting.YELLOW;
        FACE_TO_FORMAT[Direction.WEST.ordinal()] = TextFormatting.GREEN;

        ImmutableMap.Builder<String, DyeColor> builder = ImmutableMap.builder();
        for (DyeColor c : COLOURS) {
            builder.put(c.getName(), c);
        }
        nameToColourMap = builder.build();

        FACE_TO_COLOUR = new int[6];
        FACE_TO_COLOUR[Direction.DOWN.ordinal()] = 0xFF_33_33_33;
        FACE_TO_COLOUR[Direction.UP.ordinal()] = 0xFF_CC_CC_CC;
    }

    private static void replaceColourForBlack(TextFormatting colour, TextFormatting with) {
        replaceColourForBlack(colour, with, with);
    }

    private static void replaceColourForBlack(TextFormatting colour, TextFormatting normal, TextFormatting highContrast) {
        REPLACE_FOR_BLACK[colour.ordinal()] = normal;
        REPLACE_FOR_BLACK_HIGH_CONTRAST[colour.ordinal()] = highContrast;
    }

    private static void replaceColourForWhite(TextFormatting colour, TextFormatting with) {
        replaceColourForWhite(colour, with, with);
    }

    private static void replaceColourForWhite(TextFormatting colour, TextFormatting normal, TextFormatting highContrast) {
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
     * {@link TextFormatting} colour, and postfix with {@link TextFormatting#RESET} */
    public static String getTextFullTooltip(DyeColor colour) {
        if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertColourToTextFormat(colour);
            return formatColour.toString() + getTextFormatForBlack(formatColour) + LocaleUtil.localizeColour(colour)
                    + TextFormatting.RESET;
        } else {
            return LocaleUtil.localizeColour(colour);
        }
    }

    // Calen
    public static IFormattableTextComponent getTextFullTooltipComponent(IFormattableTextComponent base, @Nullable DyeColor colour) {
        if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertColourToTextFormat(colour);
//            return base.append(new StringTextComponent(formatColour.toString() + getTextFormatForBlack(formatColour))).append(new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour)))
//                    .append(new StringTextComponent(TextFormatting.RESET.toString()));
            if (formatColour == null) {
                return base.append(new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour)))
                        .append(new StringTextComponent(TextFormatting.RESET.toString()));
            } else {
                return base.append(new StringTextComponent(formatColour.toString() + getTextFormatForBlack(formatColour))).append(new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour)))
                        .append(new StringTextComponent(TextFormatting.RESET.toString()));
            }
        } else {
            return base.append(new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour)));
        }
    }

    public static IFormattableTextComponent getTextFullTooltipComponent(DyeColor colour) {
        if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertColourToTextFormat(colour);
            return new StringTextComponent(formatColour.toString() + getTextFormatForBlack(formatColour)).append(new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour)))
                    .append(new StringTextComponent(TextFormatting.RESET.toString()));
        } else {
            return new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour));
        }
    }

    public static IFormattableTextComponent getTextFullTooltipComponent(@Nonnull DyeColor colour, IFormattableTextComponent after) {
        if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertColourToTextFormat(colour);
            return new StringTextComponent(formatColour.toString() + getTextFormatForBlack(formatColour) + " ").append(new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour)))
                    .append(new StringTextComponent(TextFormatting.RESET.toString())).append(after);
        } else {
            return new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour)).append(after);
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
            TextFormatting formatColour = convertColourToTextFormat(colour);
            return COLOUR_SPECIAL_START + Integer.toHexString(colour.getId())//
                    + getTextFormatForBlack(formatColour) + LocaleUtil.localizeColour(colour) + TextFormatting.RESET;
        } else {
            return LocaleUtil.localizeColour(colour);
        }
    }

    public static IFormattableTextComponent getTextFullTooltipSpecialComponent(IFormattableTextComponent base, DyeColor colour) {
        if (colour == DyeColor.BLACK || colour == DyeColor.BLUE) {
            return getTextFullTooltipComponent(base, colour);
        } else if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertColourToTextFormat(colour);
            return base.append(new StringTextComponent(COLOUR_SPECIAL_START + Integer.toHexString(colour.getId())//
                    + getTextFormatForBlack(formatColour))).append(new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour))).append(new StringTextComponent(TextFormatting.RESET.toString()));
        } else {
            return base.append(new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour)));
        }
    }

    public static IFormattableTextComponent getTextFullTooltipSpecialComponent(DyeColor colour) {
        // Calen
        if (colour == null) {
            return new StringTextComponent("");
        }
        if (colour == DyeColor.BLACK || colour == DyeColor.BLUE) {
            return getTextFullTooltipComponent(colour);
        } else if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertColourToTextFormat(colour);
            return new StringTextComponent(COLOUR_SPECIAL_START + Integer.toHexString(colour.getId()) + getTextFormatForBlack(formatColour))
                    .append(new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour))).append(new StringTextComponent(TextFormatting.RESET.toString()));
        } else {
            return new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour));
        }
    }

    public static IFormattableTextComponent getTextFullTooltipSpecialComponent(DyeColor colour, IFormattableTextComponent after) {
        if (colour == null) {
            return after;
        }
        if (colour == DyeColor.BLACK || colour == DyeColor.BLUE) {
            return getTextFullTooltipComponent(colour, after);
        } else if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertColourToTextFormat(colour);
            return new StringTextComponent(COLOUR_SPECIAL_START + Integer.toHexString(colour.getId())//
                    + getTextFormatForBlack(formatColour))
                    .append(new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour)))
                    .append(new StringTextComponent(" " + TextFormatting.RESET.toString())).append(after);
        } else {
            return new TranslationTextComponent(LocaleUtil.getColorTranslateKey(colour)).append(after);
        }
    }

    /** Returns a string formatted for use in a tooltip (or anything else with a black background). If
     * {@link BCLibConfig#useColouredLabels} is true then this will make prefix the string with an appropriate
     * {@link TextFormatting} colour, and postfixed with {@link TextFormatting#RESET} */
    public static String getTextFullTooltip(Direction face) {
        if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertFaceToTextFormat(face);
            return formatColour.toString() + getTextFormatForBlack(formatColour) + LocaleUtil.localizeFacing(face)
                    + TextFormatting.RESET;
        } else {
            return LocaleUtil.localizeFacing(face);
        }
    }

    // Calen
    public static ITextComponent getTextFullTooltipComponent(Direction face) {
        if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertFaceToTextFormat(face);
            return new StringTextComponent(formatColour.toString() + getTextFormatForBlack(formatColour)).append(LocaleUtil.localizeFacingComponent(face))
                    .append(new StringTextComponent(TextFormatting.RESET.toString()));
        } else {
            return LocaleUtil.localizeFacingComponent(face);
        }
    }

    /** Returns a {@link TextFormatting} colour that will display correctly on a black background, so it won't use any
     * of the darker colours (as they will be difficult to see). */
    public static TextFormatting getTextFormatForBlack(TextFormatting in) {
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

    /** Returns a {@link TextFormatting} colour that will display correctly on a white background, so it won't use any
     * of the lighter colours (as they will be difficult to see). */
    public static TextFormatting getTextFormatForWhite(TextFormatting in) {
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

    /** Converts an {@link DyeColor} into an equivalent {@link TextFormatting} for display. */
//    public static TextFormatting convertColourToTextFormat(DyeColor colour)
    public static TextFormatting convertColourToTextFormat(@Nullable DyeColor colour) {
//        return COLOUR_TO_FORMAT[colour.ordinal()];
        return colour == null ? null : COLOUR_TO_FORMAT[colour.ordinal()];
    }

    /** Converts an {@link Direction} into an equivalent {@link TextFormatting} for display. */
    public static TextFormatting convertFaceToTextFormat(Direction face) {
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

    /** Similar to {@link TextFormatting#stripFormatting(String)}, but also removes every special char
     * that {@link #getTextFullTooltipSpecial(DyeColor)} can add. */
    public static String stripAllFormatCodes(String string) {
        return ALL_FORMAT_MATCHER.matcher(string).replaceAll("");
    }

    // Calen
    public static ItemStack addColourTagToStack(ItemStack stack, DyeColor colour) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("colour", colour.getName());
        stack.setTag(tag);
        return stack;
    }

    public static ItemStack addColourTagToStack(ItemStack stack, int colour) {
        if (colour >= 0 && colour < 16) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("colour", DyeColor.byId(colour).getName());
            stack.setTag(tag);
        }
        return stack;
    }

    // Colors: 0->no color 1-15->
    @Nullable
    public static DyeColor getStackColourFromTag(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
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
