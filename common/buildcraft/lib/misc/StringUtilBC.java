/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.misc;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Splitter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

public final class StringUtilBC {

    public static final Splitter newLineSplitter = Splitter.on("\\n");

    private static final DecimalFormat displayDecimalFormat = new DecimalFormat("#####0.00");

    /** Deactivate constructor */
    private StringUtilBC() {}

    public static String localize(String key) {
        return I18n.translateToLocal(key);
    }

    public static boolean canLocalize(String key) {
        return I18n.canTranslate(key);
    }

    public static List<String> splitIntoLines(String string) {
        return newLineSplitter.splitToList(string.replaceAll("\\n", "\n"));
    }

    /** Formats a string to be displayed on a white background (for example a book background), replacing any
     * close-to-white colours with darker variants. Replaces instances of {@link TextFormatting} values. */
    public static String formatStringForWhite(String string) {
        /* FIXME: Normal usage (changing an item's text from onBlack to onWhite) has the disadvantage that colours will
         * be changed to onBlack FIRST, and then onWhite, which means that BLACK will be changed to GRAY despite BLACK
         * being absolutely fine on white. One possible fix could be to emit ORIGINAL_COLOUR + CHANGED_TO_COLOUR from
         * ColourUtil.convertColourToTextFormat as a pair, and then use the ORIGINAL_COLOUR, but change CHANGED_COLOUR
         * when calling this. */
        return formatStringImpl(string, ColourUtil.getTextFormatForWhite);
    }

    /** Formats a string to be displayed on a black background (for example an item tooltip), replacing any
     * close-to-white colours with darker variants. Replaces instances of {@link TextFormatting} values. */
    public static String formatStringForBlack(String string) {
        return formatStringImpl(string, ColourUtil.getTextFormatForBlack);
    }

    private static String formatStringImpl(String string, Function<TextFormatting, TextFormatting> fn) {
        StringBuilder out = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == '\u00a7' & string.length() > i + 2) {// \u00a7 - § - the control char used by text formatting
                i++;
                char after = string.charAt(i);
                TextFormatting colour = null;
                if (after >= '0' & after <= '9') {
                    colour = TextFormatting.fromColorIndex(after - '0');
                } else if (after >= 'a' & after <= 'f') {
                    colour = TextFormatting.fromColorIndex(after - 'a' + 10);
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

    public static String blockPosToShortString(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public static BlockPos blockPosFromShortString(String string) {
        String[] s = string.split(",");
        try {
            return new BlockPos(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("The given string \"" + string + "\" was invalid!", nfe);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            throw new IllegalArgumentException("The given string \"" + string + "\" was invalid!", aioobe);
        }
    }

    // Displaying objects
    public static String vec3ToDispString(Vec3d vec) {
        if (vec == null) return "null";
        StringBuilder builder = new StringBuilder();
        builder.append(displayDecimalFormat.format(vec.xCoord));
        builder.append(", ");
        builder.append(displayDecimalFormat.format(vec.yCoord));
        builder.append(", ");
        builder.append(displayDecimalFormat.format(vec.zCoord));
        return builder.toString();
    }

    public static String vec3ToDispString(Vec3i vec) {
        if (vec == null) return "null";
        StringBuilder builder = new StringBuilder();
        builder.append(vec.getX());
        builder.append(", ");
        builder.append(vec.getY());
        builder.append(", ");
        builder.append(vec.getZ());
        return builder.toString();
    }
}
