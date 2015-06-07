/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.items.IMapLocation;
import buildcraft.builders.tile.TileConstructionMarker;
import buildcraft.builders.tile.TileMarker;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockMarker extends BlockBuildCraft {

    public BlockMarker() {
        super(Material.circuits);

        setLightLevel(0.5F);
        setHardness(0.0F);
        setCreativeTab(BCCreativeTab.get("main"));
    }

    public static boolean canPlaceTorch(World world, BlockPos pos, EnumFacing side) {
        Block block = world.getBlock(pos);
        return block != null && (block.renderAsNormalBlock() && block.isOpaqueCube() || block.isSideSolid(world, pos, side));
    }

    private AxisAlignedBB getBoundingBox(int meta) {
        double w = 0.15;
        double h = 0.65;

        EnumFacing dir = EnumFacing.getOrientation(meta);
        switch (dir) {
            case DOWN:
                return AxisAlignedBB.getBoundingBox(0.5F - w, 1F - h, 0.5F - w, 0.5F + w, 1F, 0.5F + w);
            case UP:
                return AxisAlignedBB.getBoundingBox(0.5F - w, 0F, 0.5F - w, 0.5F + w, h, 0.5F + w);
            case SOUTH:
                return AxisAlignedBB.getBoundingBox(0.5F - w, 0.5F - w, 0F, 0.5F + w, 0.5F + w, h);
            case NORTH:
                return AxisAlignedBB.getBoundingBox(0.5F - w, 0.5F - w, 1 - h, 0.5F + w, 0.5F + w, 1);
            case EAST:
                return AxisAlignedBB.getBoundingBox(0F, 0.5F - w, 0.5F - w, h, 0.5F + w, 0.5F + w);
            default:
                return AxisAlignedBB.getBoundingBox(1 - h, 0.5F - w, 0.5F - w, 1F, 0.5F + w, 0.5F + w);
        }
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, BlockPos pos) {
        int meta = world.getBlockMetadata(pos);
        AxisAlignedBB bBox = getBoundingBox(meta);
        bBox.offset(pos);
        return bBox;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
        int meta = world.getBlockMetadata(pos);
        AxisAlignedBB bb = getBoundingBox(meta);
        setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ);
    }

    @Override
    public int getRenderType() {
        return BuildCraftCore.markerModel;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileMarker();
    }

    @Override
    public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9)) {
            return true;
        }

        if (entityplayer.inventory.getCurrentItem() != null && entityplayer.inventory.getCurrentItem().getItem() instanceof IMapLocation) {
            return false;
        }

        if (entityplayer.isSneaking()) {
            return false;
        }

        TileEntity tile = world.getTileEntity(i, j, k);
        if (tile instanceof TileMarker && !(tile instanceof TileConstructionMarker)) {
            ((TileMarker) tile).tryConnection();
            return true;
        }
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, Block block) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMarker) {
            ((TileMarker) tile).updateSignals();
        }
        dropTorchIfCantStay(world, pos);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, int side) {
        EnumFacing dir = EnumFacing.getOrientation(side);
        return canPlaceTorch(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir);
    }

    @Override
    public int onBlockPlaced(World world, BlockPos pos, int side, float par6, float par7, float par8, int meta) {
        return side;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos) {
        super.onBlockAdded(world, pos);
        dropTorchIfCantStay(world, pos);
    }

    private void dropTorchIfCantStay(World world, BlockPos pos) {
        int meta = world.getBlockMetadata(pos);
        if (!canPlaceBlockOnSide(world, pos, meta)) {
            dropBlockAsItem(world, pos, 0, 0);
            world.setBlockToAir(pos);
        }
    }
}
