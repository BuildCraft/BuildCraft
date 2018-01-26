/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import net.minecraft.block.material.Material;

import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.robotics.block.BlockZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;

public class BCRoboticsBlocks {
    public static BlockZonePlanner zonePlanner;

    public static void preInit() {
        zonePlanner = RegistrationHelper.addBlockAndItem(new BlockZonePlanner(Material.IRON, "block.zone_planner"));

        RegistrationHelper.registerTile(TileZonePlanner.class, "tile.zone_planner");
    }
}
