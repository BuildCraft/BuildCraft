/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.block.material.Material;

import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.builders.block.BlockArchitectTable;
import buildcraft.builders.block.BlockBuilder;
import buildcraft.builders.block.BlockElectronicLibrary;
import buildcraft.builders.block.BlockFiller;
import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.block.BlockQuarry;
import buildcraft.builders.block.BlockReplacer;
import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileElectronicLibrary;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.builders.tile.TileReplacer;

public class BCBuildersBlocks {

    public static BlockFiller filler;
    public static BlockBuilder builder;
    public static BlockArchitectTable architect;
    public static BlockElectronicLibrary library;
    public static BlockReplacer replacer;

    public static BlockFrame frame;
    public static BlockQuarry quarry;

    public static void fmlPreInit() {
        filler = RegistrationHelper.addBlockAndItem(new BlockFiller(Material.IRON, "block.filler"));
        builder = RegistrationHelper.addBlockAndItem(new BlockBuilder(Material.IRON, "block.builder"));
        architect = RegistrationHelper.addBlockAndItem(new BlockArchitectTable(Material.IRON, "block.architect"));
        library = RegistrationHelper.addBlockAndItem(new BlockElectronicLibrary(Material.IRON, "block.library"));
        replacer = RegistrationHelper.addBlockAndItem(new BlockReplacer(Material.IRON, "block.replacer"));

        frame = RegistrationHelper.addBlockAndItem(new BlockFrame(Material.IRON, "block.frame"));
        quarry = RegistrationHelper.addBlockAndItem(new BlockQuarry(Material.IRON, "block.quarry"));
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
