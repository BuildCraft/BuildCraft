/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.utils;

import java.text.DecimalFormat;
import com.google.common.base.Splitter;

import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

public final class BCStringUtils {

    public static final Splitter newLineSplitter = Splitter.on("\\n");

    private static final DecimalFormat displayDecimalFormat = new DecimalFormat("#####0.00");

    /** Deactivate constructor */
    private BCStringUtils() {}

    public static String localize(String key) {
        return StatCollector.translateToLocal(key);
    }

    public static boolean canLocalize(String key) {
        return StatCollector.canTranslate(key);
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
    public static String vec3ToDispString(Vec3 vec) {
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
