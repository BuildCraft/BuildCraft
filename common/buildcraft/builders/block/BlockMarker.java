/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.items.IMapLocation;
import buildcraft.builders.tile.TileConstructionMarker;
import buildcraft.builders.tile.TileMarker;
import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockMarker extends BlockBuildCraft {

    public BlockMarker() {
        super(Material.circuits, FACING_6_PROP);
        setDefaultState(getDefaultState().withProperty(FACING_6_PROP, EnumFacing.UP));
        setLightLevel(0.5F);
        setHardness(0.0F);
    }

    public static boolean canPlaceTorch(World world, BlockPos pos, EnumFacing side) {
        Block block = world.getBlockState(pos).getBlock();
        return (block.isOpaqueCube() || block.isSideSolid(world, pos, side));
    }

    @Override
    public AxisAlignedBB getBox(IBlockAccess world, BlockPos pos, IBlockState state) {
        double w = 0.15;
        double h = 0.65;
        EnumFacing dir = FACING_6_PROP.getValue(state);
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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY,
            float hitZ) {
        if (super.onBlockActivated(world, pos, state, player, face, hitX, hitY, hitZ)) {
            return true;
        }

        if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof IMapLocation) {
            return false;
        }

        if (player.isSneaking()) {
            return false;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMarker && !(tile instanceof TileConstructionMarker)) {
            ((TileMarker) tile).tryConnection();
            return true;
        }
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMarker) {
            ((TileMarker) tile).updateSignals();
        }
        dropTorchIfCantStay(world, pos, state);
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
        EnumFacing side = FACING_6_PROP.getValue(state);
        if (!canPlaceBlockOnSide(world, pos, side)) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }
}
