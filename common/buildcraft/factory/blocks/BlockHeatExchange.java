package buildcraft.factory.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import buildcraft.BuildCraftFactory;
import buildcraft.core.GuiIds;
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

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        if (super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ)) {
            return true;
        }

        TileEntity tile = world.getTileEntity(pos);

        if (!(tile instanceof TileHeatExchange)) {
            return false;
        }

        if (!world.isRemote) {
            player.openGui(BuildCraftFactory.instance, GuiIds.HEAT_EXCHANGE, world, pos.getX(), pos.getY(), pos.getZ());
        }

        return true;
    }
}
