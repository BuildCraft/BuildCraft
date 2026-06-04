/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.misc;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.fluid.Tank;

// STUB(R.Chen): Forge-dependent methods (localizeColour, localizeFacing, localizeFluid*, localizeRf*,
// localizeRfFlow, formatRf, localize(key, args)) stripped. BCLibConfig / I18n / IFluidTank / DyeColor
// deps removed. Only MJ-related and heat helpers remain for TileEngineBase_BC8 / debug info rendering.
// TODO(R.Chen): restore full localization once I18n is ported (net.minecraft.client.resource.language.I18n).
public class LocaleUtil {

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.locale");
    private static final Set<String> failedStrings = new HashSet<>();

    private static final NumberFormat FORMAT_RF = NumberFormat.getIntegerInstance();

    public static String localizeMj(long mj) {
        return MjAPI.MJ_DISPLAY_FORMAT.format(mj / (double) MjAPI.MJ) + " MJ";
    }

    public static String localizeMjFlow(long mj) {
        return MjAPI.MJ_DISPLAY_FORMAT.format(mj / (double) MjAPI.MJ) + " MJ/t";
    }

    public static String localizeHeat(double heat) {
        return StringUtilBC.formatSafe("%.2f °C", heat);
    }

    /** Localize a translation key. Returns the raw key if no translation is found. */
    public static String localize(String key) {
        // TODO(R.Chen): replace with net.minecraft.client.resource.language.I18n.translate(key) once client I18n ported.
        return key;
    }

    /** Formats the given key with String.format-style args. */
    public static String localize(String key, Object... args) {
        String base = localize(key);
        try {
            return StringUtilBC.formatSafe(base, args);
        } catch (IllegalFormatException ife) {
            return "Bad Format: " + ife.getMessage();
        }
    }

    public static boolean canLocalize(String key) {
        return false;
    }

    // STUB(R.Chen): colour/facing/fluid/RF localizers — return placeholder strings until I18n is ported.
    public static String localizeColour(DyeColor colour) { return colour.getName(); }
    public static String localizeFacing(Direction dir) { return dir.getName(); }
    public static String localizeFluidStaticAmount(int amount, int capacity) { return amount + "/" + capacity + " mB"; }
    public static String localizeFluidStaticAmount(long amount, int capacity) { return amount + "/" + capacity + " mB"; }
    public static String localizeFluidStaticAmount(Tank tank) {
        int amount = tank.getFluidAmount();
        return localizeFluidStaticAmount(amount, tank.getCapacity());
    }
    public static String localizeRf(int rf) { return FORMAT_RF.format(rf) + " RF"; }
    public static String localizeRfFlow(int rf) { return FORMAT_RF.format(rf) + " RF/t"; }
    public static String preTranslated(String prefix, int value, String suffix) { return prefix + value + suffix; }
    public static Direction getFacingFromAxis(AxisDirection axisDir, Axis axis) {
        for (Direction d : Direction.values()) {
            if (d.getAxis() == axis && d.getDirection() == axisDir) return d;
        }
        return Direction.UP;
    }
}
