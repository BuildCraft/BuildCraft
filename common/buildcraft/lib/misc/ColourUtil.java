/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.client.render.font.SpecialColourFontRenderer;

public class ColourUtil {
    public static final String COLOUR_SPECIAL_START = "§z§";

    public static final Function<TextFormatting, TextFormatting> getTextFormatForBlack = ColourUtil::getTextFormatForBlack;
    public static final Function<TextFormatting, TextFormatting> getTextFormatForWhite = ColourUtil::getTextFormatForWhite;

    public static final EnumDyeColor[] COLOURS = EnumDyeColor.values();

    private static final String[] NAMES = { //
        "Black", "Red", "Green", "Brown",//
        "Blue", "Purple", "Cyan", "LightGray", //
        "Gray", "Pink", "Lime", "Yellow", //
        "LightBlue", "Magenta", "Orange", "White"//
    };
    private static final int[] DARK_HEX = { //
        0x2D2D2D, 0xA33835, 0x394C1E, 0x5C3A24,//
        0x3441A2, 0x843FBF, 0x36809E, 0x888888,//
        0x444444, 0xE585A0, 0x3FAA36, 0xCFC231,//
        0x7F9AD1, 0xFF64FF, 0xFF6A00, 0xFFFFFF //
    };
    private static final int[] LIGHT_HEX = { //
        0x181414, 0xBE2B27, 0x007F0E, 0x89502D,//
        0x253193, 0x7e34bf, 0x299799, 0xa0a7a7,//
        0x7A7A7A, 0xD97199, 0x39D52E, 0xFFD91C,//
        0x66AAFF, 0xD943C6, 0xEA7835, 0xe4e4e4 //
    };
    private static final String[] DYES = new String[16];
    private static final Map<String, EnumDyeColor> nameToColourMap;
    private static final int[] FACE_TO_COLOUR;

    private static final TextFormatting[] FORMATTING_VALUES = TextFormatting.values();

    private static final TextFormatting[] COLOUR_TO_FORMAT = new TextFormatting[16];
    private static final TextFormatting[] REPLACE_FOR_WHITE = new TextFormatting[16];
    private static final TextFormatting[] REPLACE_FOR_BLACK = new TextFormatting[16];
    private static final TextFormatting[] REPLACE_FOR_WHITE_HIGH_CONTRAST = new TextFormatting[16];
    private static final TextFormatting[] REPLACE_FOR_BLACK_HIGH_CONTRAST = new TextFormatting[16];
    private static final TextFormatting[] FACE_TO_FORMAT = new TextFormatting[6];

    static {
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

        COLOUR_TO_FORMAT[EnumDyeColor.BLACK.ordinal()] = TextFormatting.BLACK;
        COLOUR_TO_FORMAT[EnumDyeColor.GRAY.ordinal()] = TextFormatting.DARK_GRAY;
        COLOUR_TO_FORMAT[EnumDyeColor.SILVER.ordinal()] = TextFormatting.GRAY;
        COLOUR_TO_FORMAT[EnumDyeColor.WHITE.ordinal()] = TextFormatting.WHITE;

        COLOUR_TO_FORMAT[EnumDyeColor.RED.ordinal()] = TextFormatting.DARK_RED;
        COLOUR_TO_FORMAT[EnumDyeColor.BLUE.ordinal()] = TextFormatting.BLUE;
        COLOUR_TO_FORMAT[EnumDyeColor.CYAN.ordinal()] = TextFormatting.DARK_AQUA;
        COLOUR_TO_FORMAT[EnumDyeColor.LIGHT_BLUE.ordinal()] = TextFormatting.AQUA;

        COLOUR_TO_FORMAT[EnumDyeColor.GREEN.ordinal()] = TextFormatting.DARK_GREEN;
        COLOUR_TO_FORMAT[EnumDyeColor.LIME.ordinal()] = TextFormatting.GREEN;
        COLOUR_TO_FORMAT[EnumDyeColor.BROWN.ordinal()] = TextFormatting.GOLD;
        COLOUR_TO_FORMAT[EnumDyeColor.YELLOW.ordinal()] = TextFormatting.YELLOW;

        COLOUR_TO_FORMAT[EnumDyeColor.ORANGE.ordinal()] = TextFormatting.GOLD;
        COLOUR_TO_FORMAT[EnumDyeColor.PURPLE.ordinal()] = TextFormatting.DARK_PURPLE;
        COLOUR_TO_FORMAT[EnumDyeColor.MAGENTA.ordinal()] = TextFormatting.LIGHT_PURPLE;
        COLOUR_TO_FORMAT[EnumDyeColor.PINK.ordinal()] = TextFormatting.LIGHT_PURPLE;

        FACE_TO_FORMAT[EnumFacing.UP.ordinal()] = TextFormatting.WHITE;
        FACE_TO_FORMAT[EnumFacing.DOWN.ordinal()] = TextFormatting.BLACK;
        FACE_TO_FORMAT[EnumFacing.NORTH.ordinal()] = TextFormatting.RED;
        FACE_TO_FORMAT[EnumFacing.SOUTH.ordinal()] = TextFormatting.BLUE;
        FACE_TO_FORMAT[EnumFacing.EAST.ordinal()] = TextFormatting.YELLOW;
        FACE_TO_FORMAT[EnumFacing.WEST.ordinal()] = TextFormatting.GREEN;

        ImmutableMap.Builder<String, EnumDyeColor> builder = ImmutableMap.builder();
        for (EnumDyeColor c : COLOURS) {
            builder.put(c.getName(), c);
        }
        nameToColourMap = builder.build();

        FACE_TO_COLOUR = new int[6];
        FACE_TO_COLOUR[EnumFacing.DOWN.ordinal()] = 0xFF_33_33_33;
        FACE_TO_COLOUR[EnumFacing.UP.ordinal()] = 0xFF_CC_CC_CC;
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
    public static EnumDyeColor parseColourOrNull(String string) {
        return nameToColourMap.get(string);
    }

    public static String getDyeName(EnumDyeColor colour) {
        return DYES[colour.getDyeDamage()];
    }

    public static String getName(EnumDyeColor colour) {
        return NAMES[colour.getDyeDamage()];
    }

    public static int getDarkHex(EnumDyeColor colour) {
        return DARK_HEX[colour.getDyeDamage()];
    }

    public static int getLightHex(EnumDyeColor colour) {
        return LIGHT_HEX[colour.getDyeDamage()];
    }

    public static int getColourForSide(EnumFacing face) {
        return FACE_TO_COLOUR[face.ordinal()];
    }

    public static String[] getNameArray() {
        return Arrays.copyOf(NAMES, NAMES.length);
    }

    /** Returns a string formatted for use in a tooltip (or anything else with a black background). If
     * {@link BCLibConfig#useColouredLabels} is true then this will make prefix the string with an appropriate
     * {@link TextFormatting} colour, and postfix with {@link TextFormatting#RESET} */
    public static String getTextFullTooltip(EnumDyeColor colour) {
        if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertColourToTextFormat(colour);
            return formatColour.toString() + getTextFormatForBlack(formatColour) + LocaleUtil.localizeColour(colour) + TextFormatting.RESET;
        } else {
            return LocaleUtil.localizeColour(colour);
        }
    }

    /** Similar to {@link #getTextFullTooltip(EnumDyeColor)}, but outputs a string specifically designed for
     * {@link SpecialColourFontRenderer}. MUST be the first string used! */
    public static String getTextFullTooltipSpecial(EnumDyeColor colour) {
        if (colour == EnumDyeColor.BLACK || colour == EnumDyeColor.BLUE) {
            return getTextFullTooltip(colour);
        }
        if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertColourToTextFormat(colour);
            return COLOUR_SPECIAL_START + Integer.toHexString(colour.getMetadata())//
                + getTextFormatForBlack(formatColour) + LocaleUtil.localizeColour(colour) + TextFormatting.WHITE;
        }
        return LocaleUtil.localizeColour(colour);
    }

    /** Returns a string formatted for use in a tooltip (or anything else with a black background). If
     * {@link BCLibConfig#useColouredLabels} is true then this will make prefix the string with an appropriate
     * {@link TextFormatting} colour, and postfix with {@link TextFormatting#RESET} */
    public static String getTextFullTooltip(EnumFacing face) {
        if (BCLibConfig.useColouredLabels) {
            TextFormatting formatColour = convertFaceToTextFormat(face);
            return formatColour.toString() + getTextFormatForBlack(formatColour) + LocaleUtil.localizeFacing(face) + TextFormatting.RESET;
        } else {
            return LocaleUtil.localizeFacing(face);
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

    /** Converts an {@link EnumDyeColor} into an equivalent {@link TextFormatting} for display. */
    public static TextFormatting convertColourToTextFormat(EnumDyeColor colour) {
        return COLOUR_TO_FORMAT[colour.ordinal()];
    }

    /** Converts an {@link EnumFacing} into an equivalent {@link TextFormatting} for display. */
    public static TextFormatting convertFaceToTextFormat(EnumFacing face) {
        return FACE_TO_FORMAT[face.ordinal()];
    }

    public static int swapArgbToAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = (argb >> 0) & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public static EnumDyeColor getNext(EnumDyeColor colour) {
        int ord = colour.ordinal() + 1;
        return COLOURS[ord & 15];
    }

    public static EnumDyeColor getNextOrNull(@Nullable EnumDyeColor colour) {
        if (colour == null) {
            return COLOURS[0];
        } else if (colour == COLOURS[COLOURS.length - 1]) {
            return null;
        } else {
            return getNext(colour);
        }
    }

    public static EnumDyeColor getPrev(EnumDyeColor colour) {
        int ord = colour.ordinal() + 16 - 1;
        return COLOURS[ord & 15];
    }

    public static EnumDyeColor getPrevOrNull(@Nullable EnumDyeColor colour) {
        if (colour == null) {
            return COLOURS[COLOURS.length - 1];
        } else if (colour == COLOURS[0]) {
            return null;
        } else {
            return getPrev(colour);
        }
    }
}
