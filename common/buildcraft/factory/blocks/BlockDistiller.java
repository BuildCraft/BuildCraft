package buildcraft.factory.blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.BuildCraftFactory;
import buildcraft.api.properties.BuildCraftExtendedProperty;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.factory.tile.TileDistiller;

public class BlockDistiller extends BlockBuildCraft {
    public BlockDistiller() {
        super(Material.iron);
        setLightOpacity(0);
    }

    @Override
    protected boolean fillStateListsPre(boolean hasExtendedProps, List<BuildCraftProperty<?>> metas, List<BuildCraftProperty<?>> nonMetas,
            List<BuildCraftExtendedProperty<?>> infinites, BuildCraftProperty<?>... properties) {
        nonMetas.add(CONNECTED_EAST);
        nonMetas.add(CONNECTED_WEST);
        nonMetas.add(CONNECTED_NORTH);
        nonMetas.add(CONNECTED_SOUTH);
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileDistiller();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        if (super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ)) {
            return true;
        }

        TileEntity tile = world.getTileEntity(pos);

        if (!(tile instanceof TileDistiller)) {
            return false;
        }

        if (!world.isRemote) {
            player.openGui(BuildCraftFactory.instance, GuiIds.DISTILLER, world, pos.getX(), pos.getY(), pos.getZ());
        }

        return true;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        state = super.getActualState(state, access, pos);
        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            TileEntity tile = access.getTileEntity(pos.offset(face));
            boolean connected = false;
            if (tile instanceof IPipeTile) {
                IPipeTile pipe = (IPipeTile) tile;
                if (pipe.isPipeConnected(face.getOpposite())) connected = true;
            }
            state = state.withProperty(CONNECTED_MAP.get(face), connected);
        }
        return state;
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    public boolean isFullBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }
}
