package buildcraft.builders;

import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicFluid;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.builders.schematics.SchematicTileCreative;

public final class HeuristicBlockDetection {
    private HeuristicBlockDetection() {

    }

    public static void start() {
        Iterator<Block> i = Block.REGISTRY.iterator();
        while (i.hasNext()) {
            Block block = i.next();
            if (block == null || block == Blocks.AIR) {
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

                        try {
                            if (block instanceof IFluidBlock) {
                                IFluidBlock fblock = (IFluidBlock) block;
                                if (fblock.getFluid() != null) {
                                    SchematicRegistry.INSTANCE.registerSchematicBlock(state, SchematicFluid.class, new FluidStack(fblock.getFluid(),
                                            1000));
                                }
                            } else {
                                SchematicRegistry.INSTANCE.registerSchematicBlock(state, SchematicBlock.class);
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
}
