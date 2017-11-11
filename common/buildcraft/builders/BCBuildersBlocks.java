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

    private static final RegistrationHelper HELPER = new RegistrationHelper();

    public static BlockFiller filler;
    public static BlockBuilder builder;
    public static BlockArchitectTable architect;
    public static BlockElectronicLibrary library;
    public static BlockReplacer replacer;

    public static BlockFrame frame;
    public static BlockQuarry quarry;

    public static void fmlPreInit() {
        filler = HELPER.addBlockAndItem(new BlockFiller(Material.IRON, "block.filler"));
        builder = HELPER.addBlockAndItem(new BlockBuilder(Material.IRON, "block.builder"));
        architect = HELPER.addBlockAndItem(new BlockArchitectTable(Material.IRON, "block.architect"));
        library = HELPER.addBlockAndItem(new BlockElectronicLibrary(Material.IRON, "block.library"));
        replacer = HELPER.addBlockAndItem(new BlockReplacer(Material.IRON, "block.replacer"));

        frame = HELPER.addBlockAndItem(new BlockFrame(Material.IRON, "block.frame"));
        quarry = HELPER.addBlockAndItem(new BlockQuarry(Material.IRON, "block.quarry"));
    }

    public static void fmlInit() {
        HELPER.registerTile(TileFiller.class, "tile.filler");
        HELPER.registerTile(TileBuilder.class, "tile.builder");
        HELPER.registerTile(TileArchitectTable.class, "tile.architect");
        HELPER.registerTile(TileElectronicLibrary.class, "tile.library");
        HELPER.registerTile(TileReplacer.class, "tile.replacer");
        HELPER.registerTile(TileQuarry.class, "tile.quarry");
    }
}
