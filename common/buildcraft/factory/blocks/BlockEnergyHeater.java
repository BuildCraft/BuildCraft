package buildcraft.factory.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.factory.tile.TileEnergyHeater;

public class BlockEnergyHeater extends BlockBuildCraft {
    public BlockEnergyHeater() {
        super(Material.iron, FACING_PROP, CONNECTED_UP, CONNECTED_DOWN, CONNECTED_EAST, CONNECTED_WEST, CONNECTED_SOUTH, CONNECTED_NORTH);
        setLightOpacity(0);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEnergyHeater();
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
    public boolean isFullBlock() {
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
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        state = super.getActualState(state, access, pos);
        Axis ignored = state.getValue(FACING_PROP).rotateYCCW().getAxis();
        for (EnumFacing face : EnumFacing.values()) {
            TileEntity tile = access.getTileEntity(pos.offset(face));
            boolean connected = false;
            if (face.getAxis() != ignored && tile instanceof IPipeTile) {
                IPipeTile pipe = (IPipeTile) tile;
                if (pipe.isPipeConnected(face.getOpposite())) connected = true;
            }
            state = state.withProperty(CONNECTED_MAP.get(face), connected);
        }
        return state;
    }
}
