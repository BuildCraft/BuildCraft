package buildcraft.lib.bpt.vanilla;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;

import buildcraft.api.bpt.BlueprintAPI;
import buildcraft.api.bpt.SchematicException;
import buildcraft.api.bpt.SchematicFactoryNBTBlock;
import buildcraft.api.bpt.SchematicFactoryWorldBlock;
import buildcraft.lib.bpt.helper.SchematicBlockSimpleSet;

public class VanillaBlueprints {
    private static final Block[] STANDARD_BLOCKS = { //
        Blocks.ACACIA_FENCE, Blocks.ACACIA_FENCE_GATE, Blocks.ACACIA_STAIRS, //
        Blocks.BIRCH_FENCE, Blocks.BIRCH_FENCE_GATE, Blocks.BIRCH_STAIRS, //
        Blocks.PLANKS, Blocks.LOG, Blocks.LOG2,//
        Blocks.BOOKSHELF,//
        Blocks.COBBLESTONE, Blocks.STONE, Blocks.STONEBRICK, Blocks.END_STONE//
    };

    private static final Block[] CHEST_BLOCKS = {//
        Blocks.CHEST, Blocks.TRAPPED_CHEST//
    };

    // TODO: this all wants to be replaced with mostly-json

    public static void fmlInit() {
        for (Block block : STANDARD_BLOCKS) {
            BlueprintAPI.registerWorldBlockSchematic(block, createStandardBlockWorld(block));
            BlueprintAPI.registerNbtBlockSchematic(block, createStandardBlockNBT(block));
        }
        for (Block chest : CHEST_BLOCKS) {
            BlueprintAPI.registerWorldBlockSchematic(chest, createChestBlockWorld(chest));
            BlueprintAPI.registerNbtBlockSchematic(chest, createChestBlockNBT(chest));
        }
    }

    private static SchematicFactoryWorldBlock createStandardBlockWorld(Block block) {
        return (world, pos) -> {
            IBlockState at = world.getBlockState(pos);
            if (block != at.getBlock()) {
                throw new SchematicException("Expected " + block.getRegistryName() + " but got " + at.getBlock().getRegistryName());
            }
            return new SchematicBlockSimpleSet(world, pos);
        };
    }

    private static SchematicFactoryNBTBlock createStandardBlockNBT(Block block) {
        return (nbt) -> {
            SchematicBlockSimpleSet schematic = new SchematicBlockSimpleSet(nbt);
            if (schematic.block != block) {
                throw new SchematicException("Expected " + block.getRegistryName() + " but got " + schematic.block.getRegistryName());
            }
            return schematic;
        };
    }

    private static SchematicFactoryWorldBlock createChestBlockWorld(Block block) {
        return (world, pos) -> {
            IBlockState at = world.getBlockState(pos);
            if (block != at.getBlock()) {
                throw new SchematicException("Expected " + block.getRegistryName() + " but got " + at.getBlock().getRegistryName());
            }
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityChest) {
                TileEntityChest chest = (TileEntityChest) tile;
                return new SchematicChest(at, chest);
            } else {
                throw new SchematicException("Expected an instanceof TileEntityChest but got " + (tile == null ? "null" : tile.getClass()));
            }
        };
    }

    private static SchematicFactoryNBTBlock createChestBlockNBT(Block block) {
        return (nbt) -> {
            SchematicChest schematic = new SchematicChest(nbt);
            if (schematic.block != block) {
                throw new SchematicException("Expected " + block.getRegistryName() + " but got " + schematic.block.getRegistryName());
            }
            return schematic;
        };
    }
}
