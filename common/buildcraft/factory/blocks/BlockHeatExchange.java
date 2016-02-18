package buildcraft.factory.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.block.BlockBuildCraftBase;
import buildcraft.factory.tile.TileHeatExchange;

public class BlockHeatExchange extends BlockBuildCraft {
    public BlockHeatExchange() {
        super(Material.iron, BlockBuildCraftBase.FACING_PROP);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileHeatExchange();
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        IBlockState state = world.getBlockState(pos);
        EnumFacing current = state.getValue(FACING_PROP);
        current = current.rotateAround(Axis.Y);
        world.setBlockState(pos, state.withProperty(FACING_PROP, current));
        return true;
    }
}
