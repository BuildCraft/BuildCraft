/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import buildcraft.api.items.IMapLocation;
import buildcraft.core.lib.block.BlockBuildCraft;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@Deprecated
public class BlockMarker extends BlockBuildCraft {
    public BlockMarker() {
        this(false);
    }

    protected BlockMarker(boolean on) {
        super(Material.CIRCUITS, FACING_6_PROP, on ? LED_DONE : null);
        setDefaultState(getDefaultState().withProperty(FACING_6_PROP, EnumFacing.UP));
        setLightLevel(0.5F);
        setHardness(0.0F);
    }

    public static boolean canPlaceTorch(World world, BlockPos pos, EnumFacing side) {
        IBlockState state = world.getBlockState(pos);
        return (state.isOpaqueCube() || state.isSideSolid(world, pos, side));
    }

    @Override
    public AxisAlignedBB getBox(IBlockAccess world, BlockPos pos, IBlockState state) {
        double w = 0.15;
        double h = 0.65;
        EnumFacing dir = state.getValue(FACING_6_PROP);
        switch (dir) {
            case DOWN:
                return new AxisAlignedBB(0.5F - w, 1F - h, 0.5F - w, 0.5F + w, 1F, 0.5F + w);
            case UP:
                return new AxisAlignedBB(0.5F - w, 0F, 0.5F - w, 0.5F + w, h, 0.5F + w);
            case SOUTH:
                return new AxisAlignedBB(0.5F - w, 0.5F - w, 0F, 0.5F + w, 0.5F + w, h);
            case NORTH:
                return new AxisAlignedBB(0.5F - w, 0.5F - w, 1 - h, 0.5F + w, 0.5F + w, 1);
            case EAST:
                return new AxisAlignedBB(0F, 0.5F - w, 0.5F - w, h, 0.5F + w, 0.5F + w);
            default:
                return new AxisAlignedBB(1 - h, 0.5F - w, 0.5F - w, 1F, 0.5F + w, 0.5F + w);
        }
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileMarker();
    }

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ)) {
			return true;
		}

		if (playerIn.inventory.getCurrentItem() != null && playerIn.inventory.getCurrentItem().getItem() instanceof IMapLocation) {
			return false;
		}

		if (playerIn.isSneaking()) {
			return false;
		}

		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileMarker) {
			((TileMarker) tile).tryConnection();
			return true;
		}
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
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileMarker) {
			((TileMarker) tile).updateSignals();
		}
		if (world instanceof World)
		dropTorchIfCantStay((World) world, pos, world.getBlockState(pos));
	}


    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return canPlaceTorch(world, pos.offset(side, -1), side);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        dropTorchIfCantStay(world, pos, state);
    }

    private void dropTorchIfCantStay(World world, BlockPos pos, IBlockState state) {
        EnumFacing side = state.getValue(FACING_6_PROP);
        if (!canPlaceBlockOnSide(world, pos, side)) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
