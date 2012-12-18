package buildcraft.silicon;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.BuildCraftSilicon;
import buildcraft.core.DefaultProps;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;

public class BlockAssemblyTable extends BlockContainer {

	public BlockAssemblyTable(int i) {
		super(i, Material.iron);
		// TODO Auto-generated constructor stub

		setBlockBounds(0, 0, 0, 1, 9F / 16F, 1);
		setHardness(0.5F);
		setCreativeTab(CreativeTabs.tabRedstone);

	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isACube() {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		// Drop through if the player is sneaking
		if (entityplayer.isSneaking())
			return false;

		if (!CoreProxy.proxy.isRenderWorld(world)) {
			int meta = world.getBlockMetadata(i, j, k);
			entityplayer.openGui(BuildCraftSilicon.instance, meta, world, i, j, k);
		}
		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		if (i == 1)
			return 16 * 6 + 12;
		else if (i == 0)
			return 16 * 2 + 15;
		else
			return j == 0 ? 16 * 6 + 11 : 2 * 16 + 12;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return metadata == 0 ? new TileAssemblyTable() : new TileAssemblyAdvancedWorkbench();
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return null;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int damageDropped(int par1) {
		return par1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		par3List.add(new ItemStack(this, 1, 0));
		par3List.add(new ItemStack(this, 1, 1));
	}
}
