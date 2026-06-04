/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.compat;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;

/**
 * Compatibility shim replacing {@code net.minecraft.block.Material} which was
 * removed in Minecraft 1.20.
 *
 * Provides the most-used Forge Material constants as {@link AbstractBlock.Settings}
 * instances so unmigrated block constructors can compile without change.
 *
 * TODO(R.Chen): replace all MaterialBC.XYZ usages with inline AbstractBlock.Settings
 *               chains once per-block settings (hardness, resistance, sounds) are set.
 */
public final class MaterialBC {

    private MaterialBC() {}

    public static final AbstractBlock.Settings IRON =
        AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY);

    public static final AbstractBlock.Settings ROCK =
        AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY);

    public static final AbstractBlock.Settings WOOD =
        AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN);

    public static final AbstractBlock.Settings GLASS =
        AbstractBlock.Settings.create().mapColor(MapColor.CLEAR);

    public static final AbstractBlock.Settings GROUND =
        AbstractBlock.Settings.create().mapColor(MapColor.DIRT_BROWN);

    public static final AbstractBlock.Settings GRASS =
        AbstractBlock.Settings.create().mapColor(MapColor.PALE_GREEN);

    public static final AbstractBlock.Settings SAND =
        AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW);

    public static final AbstractBlock.Settings WATER =
        AbstractBlock.Settings.create().mapColor(MapColor.WATER_BLUE);

    public static final AbstractBlock.Settings LAVA =
        AbstractBlock.Settings.create().mapColor(MapColor.ORANGE);

    public static final AbstractBlock.Settings CLAY =
        AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE_GRAY);

    public static final AbstractBlock.Settings FIRE =
        AbstractBlock.Settings.create().mapColor(MapColor.BRIGHT_RED);

    public static final AbstractBlock.Settings AIR =
        AbstractBlock.Settings.create().mapColor(MapColor.CLEAR).noCollision().noBlockBreakParticles();

    // Forge Material.CIRCUITS was used for redstone components; closest 1.20 map color is BRIGHT_RED
    public static final AbstractBlock.Settings CIRCUITS =
        AbstractBlock.Settings.create().mapColor(MapColor.BRIGHT_RED);
}
