package buildcraft.factory.blocks;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import buildcraft.BuildCraftFactory;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.factory.tile.TileHeatExchange;

public class BlockHeatExchange extends BlockBuildCraft {
    public BlockHeatExchange() {
        super(Material.IRON, FACING_PROP);
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
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ)) {
			return true;
		}

		TileEntity tile = worldIn.getTileEntity(pos);

		if (!(tile instanceof TileHeatExchange)) {
			return false;
		}

		if (!worldIn.isRemote) {
			playerIn.openGui(BuildCraftFactory.instance, GuiIds.HEAT_EXCHANGE, worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
