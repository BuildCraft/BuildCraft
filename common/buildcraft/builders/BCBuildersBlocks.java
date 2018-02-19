/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.block.*;
import buildcraft.builders.tile.*;
import buildcraft.lib.registry.RegistrationHelper;
import net.minecraft.block.material.Material;

public class BCBuildersBlocks {


    public static void fmlPreInit() {
        RegistrationHelper.addBlockAndItem(new BlockFiller(Material.IRON, "block.filler"));
        RegistrationHelper.addBlockAndItem(new BlockBuilder(Material.IRON, "block.builder"));
        RegistrationHelper.addBlockAndItem(new BlockArchitectTable(Material.IRON, "block.architect"));
        RegistrationHelper.addBlockAndItem(new BlockElectronicLibrary(Material.IRON, "block.library"));
        RegistrationHelper.addBlockAndItem(new BlockReplacer(Material.IRON, "block.replacer"));

        RegistrationHelper.addBlockAndItem(new BlockFrame(Material.IRON, "block.frame"));
        RegistrationHelper.addBlockAndItem(new BlockQuarry(Material.IRON, "block.quarry"));
    }

    public static void fmlInit() {
        RegistrationHelper.registerTile(TileFiller.class, "tile.filler");
        RegistrationHelper.registerTile(TileBuilder.class, "tile.builder");
        RegistrationHelper.registerTile(TileArchitectTable.class, "tile.architect");
        RegistrationHelper.registerTile(TileElectronicLibrary.class, "tile.library");
        RegistrationHelper.registerTile(TileReplacer.class, "tile.replacer");
        RegistrationHelper.registerTile(TileQuarry.class, "tile.quarry");
    }
}
