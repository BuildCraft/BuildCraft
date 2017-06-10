/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import net.minecraft.block.material.Material;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.robotics.block.BlockZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;

public class BCRoboticsBlocks {
    public static BlockZonePlanner zonePlanner;

    public static void preInit() {
        zonePlanner = BlockBCBase_Neptune.register(new BlockZonePlanner(Material.ROCK, "block.zone_planner"));

        TileBC_Neptune.registerTile(TileZonePlanner.class, "tile.zone_planner");
    }
}
