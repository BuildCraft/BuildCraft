/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Formatting;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.client.render.font.SpecialColourFontRenderer;

public class ColourUtil {
    public static final char MINECRAFT_FORMAT_CHAR;
    public static final String COLOUR_SPECIAL_START;

    public static final Function<Formatting, Formatting> getTextFormatForBlack =
        ColourUtil::getTextFormatForBlack;
    public static final Function<Formatting, Formatting> getTextFormatForWhite =
        ColourUtil::getTextFormatForWhite;

    public static final DyeColor[] COLOURS = DyeColor.values();

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

    private static final Formatting[] FORMATTING_VALUES = Formatting.values();

    private static final Formatting[] COLOUR_TO_FORMAT = new Formatting[16];
    private static final Formatting[] REPLACE_FOR_WHITE = new Formatting[16];
    private static final Formatting[] REPLACE_FOR_BLACK = new Formatting[16];
    private static final Formatting[] REPLACE_FOR_WHITE_HIGH_CONTRAST = new Formatting[16];
    private static final Formatting[] REPLACE_FOR_BLACK_HIGH_CONTRAST = new Formatting[16];
    private static final Formatting[] FACE_TO_FORMAT = new Formatting[6];

    private static final Pattern ALL_FORMAT_MATCHER = Pattern.compile("(?i)\u00a7[0-9A-Za-z]");

    static {
        MINECRAFT_FORMAT_CHAR = '\u00a7';
        COLOUR_SPECIAL_START = MINECRAFT_FORMAT_CHAR + "z" + MINECRAFT_FORMAT_CHAR;
        for (int i = 0; i < 16; i++) {
            DYES[i] = "dye" + NAMES[i];
            REPLACE_FOR_WHITE[i] = REPLACE_FOR_WHITE_HIGH_CONTRAST[i] = FORMATTING_VALUES[i];
            REPLACE_FOR_BLACK[i] = REPLACE_FOR_BLACK_HIGH_CONTRAST[i] = FORMATTING_VALUES[i];
        }

        replaceColourForWhite(Formatting.WHITE, Formatting.GRAY);
        replaceColourForWhite(Formatting.YELLOW, Formatting.GOLD);
        replaceColourForWhite(Formatting.AQUA, Formatting.BLUE);
        replaceColourForWhite(Formatting.GREEN, Formatting.DARK_GREEN);

        replaceColourForBlack(Formatting.BLACK, Formatting.GRAY);
        replaceColourForBlack(Formatting.DARK_GRAY, Formatting.GRAY);
        replaceColourForBlack(Formatting.DARK_BLUE, Formatting.BLUE, Formatting.AQUA);
        replaceColourForBlack(Formatting.BLUE, Formatting.BLUE, Formatting.AQUA);
        replaceColourForBlack(Formatting.DARK_PURPLE, Formatting.LIGHT_PURPLE);
        replaceColourForBlack(Formatting.DARK_RED, Formatting.RED);
        replaceColourForBlack(Formatting.DARK_GREEN, Formatting.GREEN);

        COLOUR_TO_FORMAT[DyeColor.BLACK.ordinal()] = Formatting.BLACK;
        COLOUR_TO_FORMAT[DyeColor.GRAY.ordinal()] = Formatting.DARK_GRAY;
        COLOUR_TO_FORMAT[DyeColor.SILVER.ordinal()] = Formatting.GRAY;
        COLOUR_TO_FORMAT[DyeColor.WHITE.ordinal()] = Formatting.WHITE;

        COLOUR_TO_FORMAT[DyeColor.RED.ordinal()] = Formatting.DARK_RED;
        COLOUR_TO_FORMAT[DyeColor.BLUE.ordinal()] = Formatting.BLUE;
        COLOUR_TO_FORMAT[DyeColor.CYAN.ordinal()] = Formatting.DARK_AQUA;
        COLOUR_TO_FORMAT[DyeColor.LIGHT_BLUE.ordinal()] = Formatting.AQUA;

        COLOUR_TO_FORMAT[DyeColor.GREEN.ordinal()] = Formatting.DARK_GREEN;
        COLOUR_TO_FORMAT[DyeColor.LIME.ordinal()] = Formatting.GREEN;
        COLOUR_TO_FORMAT[DyeColor.BROWN.ordinal()] = Formatting.GOLD;
        COLOUR_TO_FORMAT[DyeColor.YELLOW.ordinal()] = Formatting.YELLOW;

        COLOUR_TO_FORMAT[DyeColor.ORANGE.ordinal()] = Formatting.GOLD;
        COLOUR_TO_FORMAT[DyeColor.PURPLE.ordinal()] = Formatting.DARK_PURPLE;
        COLOUR_TO_FORMAT[DyeColor.MAGENTA.ordinal()] = Formatting.LIGHT_PURPLE;
        COLOUR_TO_FORMAT[DyeColor.PINK.ordinal()] = Formatting.LIGHT_PURPLE;

        FACE_TO_FORMAT[Direction.UP.ordinal()] = Formatting.WHITE;
        FACE_TO_FORMAT[Direction.DOWN.ordinal()] = Formatting.BLACK;
        FACE_TO_FORMAT[Direction.NORTH.ordinal()] = Formatting.RED;
        FACE_TO_FORMAT[Direction.SOUTH.ordinal()] = Formatting.BLUE;
        FACE_TO_FORMAT[Direction.EAST.ordinal()] = Formatting.YELLOW;
        FACE_TO_FORMAT[Direction.WEST.ordinal()] = Formatting.GREEN;

        ImmutableMap.Builder<String, DyeColor> builder = ImmutableMap.builder();
        for (DyeColor c : COLOURS) {
            builder.put(c.getName(), c);
        }
        nameToColourMap = builder.build();

        FACE_TO_COLOUR = new int[6];
        FACE_TO_COLOUR[Direction.DOWN.ordinal()] = 0xFF_33_33_33;
        FACE_TO_COLOUR[Direction.UP.ordinal()] = 0xFF_CC_CC_CC;
    }

    private static void replaceColourForBlack(Formatting colour, Formatting with) {
        replaceColourForBlack(colour, with, with);
    }

    private static void replaceColourForBlack(Formatting colour, Formatting normal,
        Formatting highContrast) {
        REPLACE_FOR_BLACK[colour.ordinal()] = normal;
        REPLACE_FOR_BLACK_HIGH_CONTRAST[colour.ordinal()] = highContrast;
    }

    private static void replaceColourForWhite(Formatting colour, Formatting with) {
        replaceColourForWhite(colour, with, with);
    }

    private static void replaceColourForWhite(Formatting colour, Formatting normal,
        Formatting highContrast) {
        REPLACE_FOR_WHITE[colour.ordinal()] = normal;
        REPLACE_FOR_WHITE_HIGH_CONTRAST[colour.ordinal()] = highContrast;
    }

    @Nullable
    public static DyeColor parseColourOrNull(String string) {
        return nameToColourMap.get(string);
    }

    public static String getDyeName(DyeColor colour) {
        return DYES[colour.getDyeDamage()];
    }

    public static String getName(DyeColor colour) {
        return NAMES[colour.getDyeDamage()];
    }

    public static int getDarkHex(DyeColor colour) {
        return DARK_HEX[colour.getDyeDamage()];
    }

    public static int getLightHex(DyeColor colour) {
        return LIGHT_HEX[colour.getDyeDamage()];
    }

    public static int getColourForSide(Direction face) {
        return FACE_TO_COLOUR[face.ordinal()];
    }

    public static String[] getNameArray() {
        return Arrays.copyOf(NAMES, NAMES.length);
    }

    /** Returns a string formatted for use in a tooltip (or anything else with a black background). If
     * {@link BCLibConfig#useColouredLabels} is true then this will make prefix the string with an appropriate
     * {@link Formatting} colour, and postfix with {@link Formatting#RESET} */
    public static String getTextFullTooltip(DyeColor colour) {
        if (BCLibConfig.useColouredLabels) {
            Formatting formatColour = convertColourToTextFormat(colour);
            return formatColour.toString() + getTextFormatForBlack(formatColour) + LocaleUtil.localizeColour(colour)
                + Formatting.RESET;
        } else {
            return LocaleUtil.localizeColour(colour);
        }
    }

    /** Similar to {@link #getTextFullTooltip(DyeColor)}, but outputs a string specifically designed for
     * {@link SpecialColourFontRenderer}. MUST be the first string used! */
    public static String getTextFullTooltipSpecial(DyeColor colour) {
        if (colour == DyeColor.BLACK || colour == DyeColor.BLUE) {
            return getTextFullTooltip(colour);
        }
        if (BCLibConfig.useColouredLabels) {
            Formatting formatColour = convertColourToTextFormat(colour);
            return COLOUR_SPECIAL_START + Integer.toHexString(colour.getId())//
                + getTextFormatForBlack(formatColour) + LocaleUtil.localizeColour(colour) + Formatting.RESET;
        }
        return LocaleUtil.localizeColour(colour);
    }

    /** Returns a string formatted for use in a tooltip (or anything else with a black background). If
     * {@link BCLibConfig#useColouredLabels} is true then this will make prefix the string with an appropriate
     * {@link Formatting} colour, and postfixed with {@link Formatting#RESET} */
    public static String getTextFullTooltip(Direction face) {
        if (BCLibConfig.useColouredLabels) {
            Formatting formatColour = convertFaceToTextFormat(face);
            return formatColour.toString() + getTextFormatForBlack(formatColour) + LocaleUtil.localizeFacing(face)
                + Formatting.RESET;
        } else {
            return LocaleUtil.localizeFacing(face);
        }
    }

    /** Returns a {@link Formatting} colour that will display correctly on a black background, so it won't use any
     * of the darker colours (as they will be difficult to see). */
    public static Formatting getTextFormatForBlack(Formatting in) {
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

    /** Returns a {@link Formatting} colour that will display correctly on a white background, so it won't use any
     * of the lighter colours (as they will be difficult to see). */
    public static Formatting getTextFormatForWhite(Formatting in) {
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

    /** Converts an {@link DyeColor} into an equivalent {@link Formatting} for display. */
    public static Formatting convertColourToTextFormat(DyeColor colour) {
        return COLOUR_TO_FORMAT[colour.ordinal()];
    }

    /** Converts an {@link Direction} into an equivalent {@link Formatting} for display. */
    public static Formatting convertFaceToTextFormat(Direction face) {
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

    /** Similar to {@link Formatting#getTextWithoutFormattingCodes(String)}, but also removes every special char
     * that {@link #getTextFullTooltipSpecial(DyeColor)} can add. */
    public static String stripAllFormatCodes(String string) {
        return ALL_FORMAT_MATCHER.matcher(string).replaceAll("");
    }
}
