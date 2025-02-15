/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib;

import buildcraft.lib.chunkload.IChunkLoadingTile;
import buildcraft.lib.chunkload.IChunkLoadingTile.LoadType;
import buildcraft.lib.client.sprite.AtlasSpriteSwappable;
import buildcraft.lib.client.sprite.AtlasSpriteVariants;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Configuration file for lib. In order to keep lib as close to being just a library mod as possible, these are not set
 * by a config file, but instead by BC Core. Feel free to set them yourself, from your own configs, if you do not depend
 * on BC Core itself, and it might not be loaded in the mod environment. */
public class BCLibConfig {

    // public static File guiConfigFile = null;
    private static File guiConfigFile = null;

    // Calen

    public static synchronized File getGuiConfigFileAndEnsureCreated() {
        if (guiConfigFile == null) {
            creatLibConfigFile();
        }
        return guiConfigFile;
    }

    private static synchronized void creatLibConfigFile() {
        File forgeConfigFolder = FMLPaths.CONFIGDIR.get().toFile();
        File buildCraftConfigFolder = new File(forgeConfigFolder, "buildcraft");

        guiConfigFile = new File(buildCraftConfigFolder, "gui.json");
    }

    /** If true then items and blocks will display the colour of an item (one of {@link DyeColor}) with the correct
     * {@link ChatFormatting} colour value.<br>
     * This changes the behaviour of {@link ColourUtil#convertColourToTextFormat(DyeColor)}. */
    public static boolean useColouredLabels = true;

    /** If this and {@link #useColouredLabels} is true then only colours which strongly contrast with the base colour
     * will be used. Useful if you can't read dark-gray on black (for example) */
    public static boolean useHighContrastLabelColours = false;

    /** If true then applicable visual elements will be displayed in more colourblind friendly way. */
    public static boolean colourBlindMode = false;

    /** The lifespan (in seconds) that spawned items will have, when dropped by a quarry or builder (etc) */
    public static int itemLifespan = 60;

    /** If true then fluidstacks will localize with something similar to "4B Water" rather than "4000mB of Water" when
     * calling {@link LocaleUtil#localizeFluidStaticAmount(int)} */
    public static boolean useBucketsStatic = true;

    /** If true then fluidstacks will localize with something similar to "4B/s" rather than "4000mB/t" when calling
     * {@link LocaleUtil#localizeFluidFlow(int)} */
    public static boolean useBucketsFlow = true;

    /** If true then fluidstacks and Mj will be localized with longer names (for example "1.2 Buckets per second" rather
     * than "60mB/t") */
    public static boolean useLongLocalizedName = false;

    /** If true then {@link AtlasSpriteVariants#createForConfig(ResourceLocation)} will retun
     * {@link AtlasSpriteSwappable}, allowing for instant reloads when switching between colourblind modes and other
     * changable things. If false it will return a normal {@link TextureAtlasSprite}. Disabling this might help if you
     * get sprite issues with mods like optifine. */
    public static boolean useSwappableSprites = true;

    /** If false then {@link AtlasSpriteVariants#updateAnimation()} will never update the animation for wrapped
     * sprites. */
    public static boolean enableAnimatedSprites = true;

    /** The maximum number of results to display in the guide contents page for the search bar. */
    public static int maxGuideSearchCount = 1200;

    public static TimeGap displayTimeGap = TimeGap.SECONDS;

    /** If true then ItemRenderUtil.renderItemStack will use the facing parameter to rotate the item */
    public static RenderRotation rotateTravelingItems = RenderRotation.ENABLED;

    public static ChunkLoaderType chunkLoadingType = ChunkLoaderType.AUTO;

    public static ChunkLoaderLevel chunkLoadingLevel = ChunkLoaderLevel.SELF_TILES;

    public static boolean guideShowDetail = false;

    /** The maximum number of items that the guide book will index. */
    public static int guideItemSearchLimit = 10_000;

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
            return ticks * ticksInGap;
        }

        public long convertTicksToGap(long ticks) {
            return ticks * ticksInGap;
        }

        public float convertTicksToGap(float ticks) {
            return ticks * ticksInGap;
        }

        public double convertTicksToGap(double ticks) {
            return ticks * ticksInGap;
        }
    }

    public enum RenderRotation {
        DISABLED {
            @Override
            public Direction changeFacing(Direction dir) {
                return Direction.EAST;
            }
        },
        HORIZONTALS_ONLY {
            @Override
            public Direction changeFacing(Direction dir) {
                return dir.getAxis() == Axis.Y ? Direction.EAST : dir;
            }
        },
        ENABLED {
            @Override
            public Direction changeFacing(Direction dir) {
                return dir;
            }
        };

        public abstract Direction changeFacing(Direction dir);
    }

    public enum ChunkLoaderType {
        /** Automatic chunkloading is ENABLED. */
        ON,

        /** Automatic chunkloading is ENABLED when using the integrated server (singleplayer + LAN), and DISABLED when
         * using a dedicated server. Currently NOT implemented */
        AUTO,

        /** Automatic chunkloading is DISABLED. Even for strict tiles (like the quarry) */
        OFF
    }

    public enum ChunkLoaderLevel {
        /** No automatic chunkloading is done. */
        NONE,

        /** {@link BlockEntity}'s that implement the {@link IChunkLoadingTile} interface will be loaded, provided they
         * return {@link LoadType#HARD} */
        STRICT_TILES,

        /** {@link BlockEntity}'s that implement the {@link IChunkLoadingTile} interface will be loaded, provided they
         * DON'T return null. */
        SELF_TILES,

        /** All {@link BlockEntity}'s in the world. */
        ALL_TILES;

        public boolean canLoad(LoadType loadType) {
            switch (this) {
                case NONE:
                    return false;
                case STRICT_TILES:
                    return loadType == LoadType.HARD;
                case SELF_TILES:
                case ALL_TILES:
                    return true;
                default:
                    throw new IllegalStateException("Unknown ChunkLoaderLevel " + this);
            }
        }
    }
}
