/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.lib.block.BlockPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.robotics.block.BlockRequester;
import buildcraft.robotics.block.BlockZonePlanner;
import buildcraft.robotics.tile.TileRequester;
import buildcraft.robotics.tile.TileZonePlanner;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;

public class BCRoboticsBlocks {
    private static final RegistrationHelper HELPER = new RegistrationHelper(BCRobotics.MODID);

    public static RegistryObject<BlockZonePlanner> zonePlanner;
    public static RegistryObject<BlockRequester> requester;
    public static RegistryObject<BlockEntityType<TileZonePlanner>> zonePlannerTile;
    public static RegistryObject<BlockEntityType<TileRequester>> requesterTile;

    public static void preInit() {
        zonePlanner = HELPER.addBlockAndItem("block.zone_planner", BlockPropertiesCreator.createDefaultProperties(Material.METAL), BlockZonePlanner::new);
        requester = HELPER.addBlockAndItem("block.requester", BlockPropertiesCreator.createDefaultProperties(Material.METAL), BlockRequester::new);

        zonePlannerTile = HELPER.registerTile("tile.zone_planner", TileZonePlanner::new, zonePlanner);
        requesterTile = HELPER.registerTile("tile.requester", TileRequester::new, requester);
    }
}
