/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Splitter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public final class StringUtilBC {

    public static final Splitter newLineSplitter = Splitter.on("\\n");

    private static final DecimalFormat displayDecimalFormat = new DecimalFormat("#####0.00");

    /** Deactivate constructor */
    private StringUtilBC() {}

    public static List<String> splitIntoLines(String string) {
        return newLineSplitter.splitToList(string.replaceAll("\\n", "\n"));
    }

    // STUB(R.Chen): formatStringForWhite/Black + compareBasicReadable removed — they depend on the
    // unmigrated ColourUtil and Forge TextFormatting. fluidToString removed — depends on Forge FluidStack.
    // Restore once ColourUtil and the Transfer-API fluid layer are migrated.

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

    // Displaying objects
    public static String vec3ToDispString(Vec3d vec) {
        if (vec == null) {
            return "null";
        }
        return displayDecimalFormat.format(vec.x) + ", " + displayDecimalFormat.format(vec.y) + ", "
            + displayDecimalFormat.format(vec.z);
    }

    public static String vec3ToDispString(Vec3i vec) {
        if (vec == null) {
            return "null";
        }
        return vec.getX() + ", " + vec.getY() + ", " + vec.getZ();
    }

    /** A direct replacement for {@link String#format(String, Object...)} which returns a descriptive string if the
     * given format is invalid. */
    public static String formatSafe(String format, Object... args) {
        if (true || Boolean.getBoolean("buildcraft.lib.misc.StringUtilBC.debugFormatSafe")) {
            return formatDirect(format, args);
        }
        try {
            return String.format(format, args);
        } catch (IllegalFormatException error) {
            return "![" + error.getMessage() + "]! for '" + format + "' " + Arrays.toString(args);
        }
    }

    /** A direct replacement for {@link String#format(String, Object...)} which includes the full format argument if
     * {@link String#format(String, Object...)} throws an {@link IllegalFormatException} */
    public static String formatDirect(String format, Object... args) {
        try {
            return String.format(format, args);
        } catch (IllegalFormatException error) {
            throw new IllegalArgumentException("Invalid format: '" + format + "'", error);
        }
    }

    public static <T> Comparator<T> compareByString(Function<T, String> keyExtractor) {
        return Comparator.comparing(keyExtractor, String::compareToIgnoreCase);
    }
}
