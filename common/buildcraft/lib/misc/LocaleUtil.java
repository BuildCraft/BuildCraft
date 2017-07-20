/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.BCLibConfig.TimeGap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** The central class for localizing objects. */
public class LocaleUtil {

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.locale");
    private static final Set<String> failedStrings = new HashSet<>();

    private static final NumberFormat FORMAT_FLUID = NumberFormat.getNumberInstance();

    private static String localeKeyFluidStatic, localeKeyFluidFlow;
    private static String localeKeyFluidStaticCap, localeKeyFluidStaticEmpty, localeKeyFluidStaticFull;
    private static String localeKeyMjStatic, localeKeyMjFlow;

    static {
        onConfigChanged();
    }

    /** Should be called whenever any of the {@link BCLibConfig} options are changed that affect any of the methods in
     * this class. */
    public static void onConfigChanged() {
        boolean bucketStatic = BCLibConfig.useBucketsStatic;
        boolean bucketFlow = BCLibConfig.useBucketsFlow;
        String longName = BCLibConfig.useLongLocalizedName ? "long" : "short";
        String timeGap = BCLibConfig.displayTimeGap == TimeGap.SECONDS ? "seconds." : "";
        localeKeyFluidStatic = "buildcraft.fluid.static." + (bucketStatic ? "bucket." : "milli.") + longName;
        localeKeyFluidFlow = "buildcraft.fluid.flow." + (bucketFlow ? "bucket." : "milli.") + longName;
        localeKeyFluidStaticCap = "buildcraft.fluid.static.cap." + (bucketStatic ? "bucket." : "milli.") + longName;
        localeKeyFluidStaticEmpty = "buildcraft.fluid.empty." + (bucketFlow ? "bucket." : "milli.") + longName;
        localeKeyFluidStaticFull = "buildcraft.fluid.full." + (bucketFlow ? "bucket." : "milli.") + longName;
        localeKeyMjStatic = "buildcraft.mj.static." + longName;
        localeKeyMjFlow = "buildcraft.mj.flow." + timeGap + longName;
    }

    /** Localizes the give key to the current locale.
     * 
     * @param key The key to localize
     * @return The localized key, or the input key if no localization was found. */
    public static String localize(String key) {
        String localized = I18n.translateToLocal(key);
        if (localized == key) {
            if (DEBUG && failedStrings.add(localized)) {
                BCLog.logger.warn("[lib.locale] Attempted to localize '" + key + "' but no localization existed!");
            }
            return key;
        }
        return localized;
    }

    /** Localizes the given key, and performs {@link String#format(String, Object...)} with the localized value and the
     * arguments given.
     * 
     * @param key The key to localize
     * @param args The arguments to put into the localized key
     * @return The localized string. */
    public static String localize(String key, Object... args) {
        String localized = I18n.translateToLocal(key);
        if (localized == key) {
            if (DEBUG && failedStrings.add(localized)) {
                BCLog.logger.warn("[lib.locale] Attempted to localize '" + key + "' but no localization existed!");
            }
            return key + " " + Arrays.toString(args);
        }
        return String.format(localized, args);
    }

    /** Checks to see if the given key can be localized.
     * 
     * @param key The key to check
     * @return True if the key could be localized, false if not. */
    public static boolean canLocalize(String key) {
        return I18n.canTranslate(key);
    }

    /** @param colour The {@link EnumDyeColor} to localize.
     * @return a localised name for the given colour. */
    public static String localizeColour(EnumDyeColor colour) {
        return localize("item.fireworksCharge." + colour.getUnlocalizedName());
    }

    /** @param face The {@link EnumFacing} to localize.
     * @return a localised name for the given face. */
    public static String localizeFacing(@Nullable EnumFacing face) {
        return localize("direction." + (face == null ? "center" : face.getName()));
    }

    public static String localizeFluidStatic(IFluidTank tank) {
        return localizeFluidStatic(tank.getFluid(), tank.getCapacity());
    }

    public static String localizeFluidStatic(FluidStack fluidStack) {
        return localizeFluidStatic(fluidStack, -1);
    }

    /** Localizes the given fluid stack, out of a given capacity
     * 
     * @param fluidStack
     * @param capacity
     * @return */
    public static String localizeFluidStatic(FluidStack fluidStack, int capacity) {
        if (fluidStack == null || fluidStack.amount <= 0) {
            if (capacity > 0) {
                String cap;
                if (BCLibConfig.useBucketsStatic) {
                    cap = FORMAT_FLUID.format(capacity / 1000.0);
                } else {
                    cap = FORMAT_FLUID.format(capacity);
                }
                return localize(localeKeyFluidStaticEmpty, cap);
            }
            return localize("buildcraft.fluid.empty");
        } else {
            String fluid = fluidStack.getLocalizedName();
            String amount;
            String cap;
            if (BCLibConfig.useBucketsStatic) {
                amount = FORMAT_FLUID.format(fluidStack.amount / 1000.0);
                cap = FORMAT_FLUID.format(capacity / 1000.0);
            } else {
                amount = FORMAT_FLUID.format(fluidStack.amount);
                cap = FORMAT_FLUID.format(capacity);
            }
            if (capacity == fluidStack.amount) {
                return localize(localeKeyFluidStaticFull, amount, fluid);
            }
            return localize(capacity > 0 ? localeKeyFluidStaticCap : localeKeyFluidStatic, amount, fluid, cap);
        }
    }

    public static String localizeFluidFlow(int milliBucketsPerTick) {
        String amount;
        if (BCLibConfig.useBucketsFlow) {
            amount = FORMAT_FLUID.format(milliBucketsPerTick / 50.0);
        } else {
            amount = FORMAT_FLUID.format(milliBucketsPerTick);
        }
        return localize(localeKeyFluidFlow, amount);
    }

    public static String localizeMj(long mj) {
        return localize(localeKeyMjStatic, MjAPI.formatMj(mj));
    }

    public static String localizeMjFlow(long mj) {
        mj = BCLibConfig.displayTimeGap.convertTicksToGap(mj);
        return localize(localeKeyMjFlow, MjAPI.formatMj(mj));
    }

    public static String localizeHeat(double heat) {
        // if (BCLibConfig.useLongLocalizedName) {
        // return localize("buildcraft.heat.long", heat);
        // } else {
        return String.format("%.2f \u00B0C", heat);
        // }
    }
}
