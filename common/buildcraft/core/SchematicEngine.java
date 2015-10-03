/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.core.lib.engines.TileEngineBase;

public class SchematicEngine extends SchematicTile {

    @Override
    public void rotateLeft(IBuilderContext context) {
        int o = tileNBT.getInteger("orientation");
        EnumFacing old = EnumFacing.VALUES[o];
        EnumFacing newFacing = old;
        if (old.getAxis() != Axis.Y) {
            newFacing = old.rotateY();
        }
        o = newFacing.ordinal();
        tileNBT.setInteger("orientation", o);
    }

    @Override
    public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {
        super.initializeFromObjectAt(context, pos);

        TileEngineBase engine = (TileEngineBase) context.world().getTileEntity(pos);

        tileNBT.setInteger("orientation", engine.orientation.ordinal());
        tileNBT.removeTag("progress");
        tileNBT.removeTag("energy");
        tileNBT.removeTag("heat");
        tileNBT.removeTag("tankFuel");
        tileNBT.removeTag("tankCoolant");
    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {
        super.placeInWorld(context, pos, stacks);

        TileEngineBase engine = (TileEngineBase) context.world().getTileEntity(pos);

        engine.orientation = EnumFacing.VALUES[tileNBT.getInteger("orientation")];
        engine.sendNetworkUpdate();
    }

    @Override
    public void postProcessing(IBuilderContext context, BlockPos pos) {
        TileEngineBase engine = (TileEngineBase) context.world().getTileEntity(pos);

        if (engine != null) {
            engine.orientation = EnumFacing.VALUES[tileNBT.getInteger("orientation")];
            engine.sendNetworkUpdate();
            context.world().markBlockForUpdate(pos);
            context.world().notifyNeighborsOfStateChange(pos, context.world().getBlockState(pos).getBlock());
        }
    }

    @Override
    public BuildingStage getBuildStage() {
        return BuildingStage.STANDALONE;
    }

}
