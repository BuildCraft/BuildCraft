/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.text.TextFormatting;

import buildcraft.lib.chunkload.IChunkLoadingTile;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;

/** Configuration file for lib. In order to keep lib as close to being just a library mod as possible, these are not set
 * by a config file, but instead by BC Core. Feel free to set them yourself, from your own configs, if you do not depend
 * on BC COre itself, and it might not be loaded in the mod environment. */
public class BCLibConfig {
    /** If true then items and blocks will display the colour of an item (one of {@link EnumDyeColor}) with the correct
     * {@link TextFormatting} colour value.<br>
     * This changes the behaviour of {@link ColourUtil#convertColourToTextFormat(EnumDyeColor)}. */
    public static boolean useColouredLabels = true;

    /** If this and {@link #useColouredLabels} is true then only colours which strongly contrast with the base colour
     * will be used. Useful if you can't read dark-gray on black (for example) */
    public static boolean useHighContrastLabelColours = false;

    /** If true then applicable visual elements will be displayed in more colourblind friendly way. */
    public static boolean colourBlindMode = false;

    /** The lifespan (in seconds) that spawned items will have, when dropped by a quarry or builder (etc) */
    public static int itemLifespan = 60;

    /** If true then fluidstacks will localize with something similar to "4B Water" rather than "4000mB of Water" when
     * calling {@link LocaleUtil#localizeFluidStatic(net.minecraftforge.fluids.FluidStack)} */
    public static boolean useBucketsStatic = true;

    /** If true then fluidstacks will localize with something similar to "4B/s" rather than "4000mB/t" when calling
     * {@link LocaleUtil#localizeFluidStatic(net.minecraftforge.fluids.FluidStack)} */
    public static boolean useBucketsFlow = true;

    /** If true then fluidstacks and Mj will be localized with longer names (for example "1.2 Buckets per second" rather
     * than "60mB/t") */
    public static boolean useLongLocalizedName = false;

    public static TimeGap displayTimeGap = TimeGap.SECONDS;

    /** If true then ItemRenderUtil.renderItemStack will use the facing parameter to rotate the item */
    public static RenderRotation rotateTravelingItems = RenderRotation.ENABLED;

    public static ChunkLoaderType chunkLoadingType = ChunkLoaderType.AUTO;

    public static ChunkLoaderLevel chunkLoadingLevel = ChunkLoaderLevel.SELF_TILES;

    public static final List<Runnable> configChangeListeners = new ArrayList<>();

    /** Resets cached values across various BCLib classes that rely on these config options. */
    public static void refreshConfigs() {
        for (Runnable r : configChangeListeners) {
            r.run();
        }
    }
    
    public enum TimeGap {
        TICKS(1),
        SECONDS(20);

        private final int ticksInGap;

        TimeGap(int ticksInGap) {
            this.ticksInGap = ticksInGap;
        }

        public int convertTicksToGap(int ticks) {
            return ticks / ticksInGap;
        }

        public long convertTicksToGap(long ticks) {
            return ticks / ticksInGap;
        }

        public float convertTicksToGap(float ticks) {
            return ticks / ticksInGap;
        }

        public double convertTicksToGap(double ticks) {
            return ticks / ticksInGap;
        }
    }

    public enum RenderRotation {
        DISABLED {
            @Override
            public EnumFacing changeFacing(EnumFacing dir) {
                return EnumFacing.EAST;
            }
        },
        HORIZONTALS_ONLY {
            @Override
            public EnumFacing changeFacing(EnumFacing dir) {
                return dir.getAxis() == Axis.Y ? EnumFacing.EAST : dir;
            }
        },
        ENABLED {
            @Override
            public EnumFacing changeFacing(EnumFacing dir) {
                return dir;
            }
        };

        public abstract EnumFacing changeFacing(EnumFacing dir);
    }

    public enum ChunkLoaderType {
        /** Automatic chunkloading is ENABLED. */
        ON,
        /** Automatic chunkloading is ENABLED when using the integrated server (singleplayer + LAN), and DISABLED when
         * using a dedicated server. */
        AUTO,
        /** Automatic chunkloading is DISABLED. Even for strict tiles (like the quarry) */
        OFF
    }

    public enum ChunkLoaderLevel {
        /** No automatic chunkloading is done. */
        NONE,

        /** {@link TileEntity}'s that implement the {@link IChunkLoadingTile} interface will be loaded, provided they
         * return {@link buildcraft.lib.chunkload.IChunkLoadingTile.LoadType#HARD} */
        STRICT_TILES,

        /** {@link TileEntity}'s that implement the {@link IChunkLoadingTile} interface will be loaded, provided they
         * DON'T return null. */
        SELF_TILES,
    }

    static {
        configChangeListeners.add(LocaleUtil::onConfigChanged);
    }
}
