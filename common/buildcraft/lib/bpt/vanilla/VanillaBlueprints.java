package buildcraft.lib.bpt.vanilla;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

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

    // TODO: this all wants to be replaced with mostly-json

    public static void fmlInit() {
        for (Block block : STANDARD_BLOCKS) {
            BlueprintAPI.registerWorldBlockSchematic(block, createStandardBlockWorld(block));
            BlueprintAPI.registerSchematicBlockDeserializer(block, createStandardBlockNBT(block));
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
}
