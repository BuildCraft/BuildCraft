package buildcraft.factory;

import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.Utils;
import buildcraft.factory.render.RenderMultiblockSlave;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockRefineryController extends BlockBuildCraft {

	private IIcon front;
	private IIcon side;

	public BlockRefineryController() {
		super(Material.iron, CreativeTabBuildCraft.TIER_3);
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return RenderMultiblockSlave.renderID;
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, i, j, k, entityliving, stack);

		ForgeDirection orientation = Utils.get2dOrientation(entityliving);
		TileRefineryController tile = (TileRefineryController) world.getTileEntity(i, j, k);

		if (tile != null) {
			tile.orientation = orientation.ordinal();
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float fx, float fy, float fz) {
		if (!world.isRemote) {
			ItemStack held = player.getCurrentEquippedItem();
			TileRefineryController tile = (TileRefineryController) world.getTileEntity(x, y, z);

			if (tile != null) {
				tile.onBlockActivated(player);
				return !(held != null && held.getItem() instanceof ItemBlock);
			}
		}

		return !player.isSneaking();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		if (!world.isRemote) {
			TileRefineryController tile = (TileRefineryController) world.getTileEntity(x, y, z);

			if (tile != null) {
				tile.deformMultiblock();
			}
		}

		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileRefineryController();
	}

	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		TileRefineryController tile = (TileRefineryController) world.getTileEntity(x, y, z);

		if (tile != null) {
			return (side == ForgeDirection.getOrientation(tile.orientation).getOpposite().ordinal()) ? this.front : this.side;
		}

		return this.side;
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return side == 3 ? this.front : this.side;
	}

	@Override
	public void registerBlockIcons(IIconRegister register) {
		front = register.registerIcon("buildcraft:refinery_component/controller_alt");
		side = register.registerIcon("buildcraft:refinery_component/frame");
	}

}
