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
import buildcraft.api.transport.ICustomPipeConnection;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.factory.tile.TileDistiller;

public class BlockDistiller extends BlockBuildCraft implements ICustomPipeConnection {
    public BlockDistiller() {
        super(Material.IRON);
        setLightOpacity(0);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileDistiller();
    }

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ)) {
			return true;
		}

		TileEntity tile = worldIn.getTileEntity(pos);

		if (!(tile instanceof TileDistiller)) {
			return false;
		}

		if (!worldIn.isRemote) {
			playerIn.openGui(BuildCraftFactory.instance, GuiIds.DISTILLER, worldIn, pos.getX(), pos.getY(), pos.getZ());
		}

		return true;
	}

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
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
    public float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state) {
        if (face.getAxis() == Axis.Y) return 0;
        return 0.125f;
    }
}
