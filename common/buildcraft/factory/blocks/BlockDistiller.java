package buildcraft.factory.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;

import buildcraft.BuildCraftFactory;
import buildcraft.api.transport.ICustomPipeConnection;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.factory.tile.TileDistiller;

public class BlockDistiller extends BlockBuildCraft implements ICustomPipeConnection {
    public BlockDistiller() {
        super(Material.iron);
        setLightOpacity(0);
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

    @Override
    public float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state) {
        if (face.getAxis() == Axis.Y) return 0;
        return 0.125f;
    }
}
