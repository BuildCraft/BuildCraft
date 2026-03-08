/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import com.google.common.base.Splitter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class StringUtilBC {

    public static final Splitter newLineSplitter = Splitter.on("\\n");

    private static final DecimalFormat displayDecimalFormat = new DecimalFormat("#####0.00");

    /** Deactivate constructor */
    private StringUtilBC() {
    }

    public static List<ITextComponent> splitIntoLines(String string) {
        return newLineSplitter
                .splitToList(string.replaceAll("\\n", "\n"))
                .stream().map(s -> (ITextComponent) new StringTextComponent(s))
                .collect(Collectors.toList());
    }

    /** Formats a string to be displayed on a white background (for example a book background), replacing any
     * close-to-white colours with darker variants. Replaces instances of {@link TextFormatting} values. */
    public static String formatStringForWhite(String string) {
        return formatStringImpl(string, ColourUtil.getTextFormatForWhite);
    }

    /** Formats a string to be displayed on a black background (for example an item tooltip), replacing any
     * close-to-white colours with darker variants. Replaces instances of {@link TextFormatting} values. */
    public static String formatStringForBlack(String string) {
        return formatStringImpl(string, ColourUtil.getTextFormatForBlack);
    }

    private static String formatStringImpl(String string, Function<TextFormatting, TextFormatting> fn) {
        /*
         * FIXME: Normal usage (changing an item's text from onBlack to onWhite) has the disadvantage that colours will
         * be changed to onBlack FIRST, and then onWhite, which means that BLACK will be changed to GRAY despite BLACK
         * being absolutely fine on white. One possible fix could be to emit ORIGINAL_COLOUR + CHANGED_TO_COLOUR from
         * ColourUtil.convertColourToTextFormat as a pair, and then use the ORIGINAL_COLOUR, but change CHANGED_COLOUR
         * when calling this.
         */
        StringBuilder out = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == '\u00a7' & string.length() > i + 2) {// \u00a7 - § - the control char used by text formatting
                i++;
                char after = string.charAt(i);
                TextFormatting colour = null;
                if (after >= '0' & after <= '9') {
                    colour = TextFormatting.getById(after - '0');
                } else if (after >= 'a' & after <= 'f') {
                    colour = TextFormatting.getById(after - 'a' + 10);
                }
                if (colour == null) {
                    out.append(c);
                    out.append(after);
                } else {
                    out.append(fn.apply(colour).toString());
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String blockPosToString(BlockPos pos) {
        if (pos == null) {
            return "null";
        }
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    public static String blockPosAsSizeToString(BlockPos pos) {
        if (pos == null) {
            return "null";
        }
        return pos.getX() + "x" + pos.getY() + "x" + pos.getZ();
    }

    public static String fluidToString(FluidStack fluid) {
        if (fluid == null) {
            return "null";
        }
        return fluid.getAmount() + "mb " + fluid.getRawFluid().getRegistryName().getPath();
    }

    // Displaying objects
    public static String vec3ToDispString(Vector3d vec) {
        if (vec == null) {
            return "null";
        }
        return displayDecimalFormat.format(vec.x) + ", " + displayDecimalFormat.format(vec.y) + ", "
                + displayDecimalFormat.format(vec.z);
    }

    public static String vec3ToDispString(Vector3i vec) {
        if (vec == null) {
            return "null";
        }
        return vec.getX() + ", " + vec.getY() + ", " + vec.getZ();
    }

    /** @param keyExtractor An extractor that will map an object to a string.
     * @return A form of {@link #compareBasicReadable()} that operates on any object that can provide a string. */
    public static <T> Comparator<T> compareBasicReadable(Function<T, String> keyExtractor) {
        return Comparator.comparing(keyExtractor, compareBasicReadable());
    }

    /** @return A comparator that only compares the text that we can see - so this will remove any format codes, and
     *         ignore case when comparing. */
    public static Comparator<String> compareBasicReadable() {
        return BasicReadableStringComparator.INSTANCE;
    }

    enum BasicReadableStringComparator implements Comparator<String> {
        INSTANCE;

        @Override
        public int compare(String o1, String o2) {
            String __o1 = ColourUtil.stripAllFormatCodes(o1);
            String __o2 = ColourUtil.stripAllFormatCodes(o2);
            return __o1.compareToIgnoreCase(__o2);
        }
    }
}
