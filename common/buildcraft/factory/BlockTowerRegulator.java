package buildcraft.factory;

import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.factory.render.RenderMultiblockSlave;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockTowerRegulator extends BlockBuildCraft {

	private IIcon top;
	private IIcon side;

	public BlockTowerRegulator() {
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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float fx, float fy, float fz) {
		if (!world.isRemote) {
//			ItemStack held = player.getCurrentEquippedItem();
			TileTowerRegulator tile = (TileTowerRegulator) world.getTileEntity(x, y, z);

			if (tile != null) {
				tile.onBlockActivated(player);
//				return !(held != null && held.getItem() instanceof ItemBlock);
				return true;
			}
		}

		return !player.isSneaking();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		if (!world.isRemote) {
			TileTowerRegulator tile = (TileTowerRegulator) world.getTileEntity(x, y, z);

			if (tile != null) {
				tile.deformMultiblock();
			}
		}

		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileTowerRegulator();
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return side == 1 ? this.top : this.side;
	}

	@Override
	public void registerBlockIcons(IIconRegister register) {
		top = register.registerIcon("buildcraft:refinery_component/regulator_top");
		side = register.registerIcon("buildcraft:refinery_component/regulator_side");
	}

}
