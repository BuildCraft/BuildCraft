package buildcraft.builders;

import java.util.BitSet;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicFluid;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.builders.schematics.SchematicBlockCreative;
import buildcraft.core.builders.schematics.SchematicTileCreative;

public final class HeuristicBlockDetection {

    private static BitSet craftableBlockList = new BitSet(65536);

    private HeuristicBlockDetection() {

    }

    public static void start() {
        // Initialize craftableBlockList
        /* for (Object or : CraftingManager.getInstance().getRecipeList()) { if (or instanceof IRecipe) { IRecipe recipe
         * = ((IRecipe) or); if (recipe.getRecipeOutput() != null && recipe.getRecipeOutput().getItem() != null &&
         * recipe.getRecipeOutput().getItem() instanceof ItemBlock) { int pos = recipe.getRecipeOutput().getItemDamage()
         * & 15; pos |= Block.getIdFromBlock(Block.getBlockFromItem(recipe.getRecipeOutput().getItem())) << 4; if (pos
         * >= 0 && pos < 65536) { craftableBlockList.set(pos); } } } } */

        // Register blocks
        Iterator i = Block.blockRegistry.iterator();
        while (i.hasNext()) {
            Block block = (Block) i.next();
            if (block == null || block == Blocks.air) {
                continue;
            }

            for (Object obj : block.getBlockState().getValidStates()) {
                IBlockState state = (IBlockState) obj;
                if (!SchematicRegistry.INSTANCE.isSupported(state)) {
                    try {
                        if (block.hasTileEntity(state)) {
                            // All tiles are registered as creative only.
                            // This is helpful for example for server admins.
                            SchematicRegistry.INSTANCE.registerSchematicBlock(state, SchematicTileCreative.class);
                            continue;
                        }

                        boolean creativeOnly = false;

                        try {
                            if (creativeOnly) {
                                SchematicRegistry.INSTANCE.registerSchematicBlock(state, SchematicBlockCreative.class);
                            } else {
                                if (block instanceof IFluidBlock) {
                                    IFluidBlock fblock = (IFluidBlock) block;
                                    if (fblock.getFluid() != null) {
                                        SchematicRegistry.INSTANCE.registerSchematicBlock(state, SchematicFluid.class, new FluidStack(fblock
                                                .getFluid(), 1000));
                                    }
                                } else {
                                    SchematicRegistry.INSTANCE.registerSchematicBlock(state, SchematicBlock.class);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    private static boolean canCraft(Block block, int meta) {
        int pos = Block.getIdFromBlock(block) << 4 | meta;
        return craftableBlockList.get(pos);
    }
}
