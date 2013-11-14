package buildcraft.silicon;

import buildcraft.BuildCraftSilicon;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockLaserTable extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private Icon[][] icons;

	public BlockLaserTable(int i) {
		super(i, Material.iron);

		setBlockBounds(0, 0, 0, 1, 9F / 16F, 1);
		setHardness(10F);
		setCreativeTab(CreativeTabBuildCraft.MACHINES.get());
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
	public Icon getIcon(int i, int j) {
	    int s = i > 1 ? 2 : i;
	    return icons[j][s];
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return metadata == 0 ? new TileAssemblyTable() : new TileAdvancedCraftingTable();
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return null;
	}

	@Override
	public int damageDropped(int par1) {
		return par1;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		par3List.add(new ItemStack(this, 1, 0));
		par3List.add(new ItemStack(this, 1, 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
	    icons = new Icon[2][];
	    icons[0] = new Icon[3];
	    icons[1] = new Icon[3];
        icons[0][0] = par1IconRegister.registerIcon("buildcraft:assemblytable_bottom");
        icons[1][0] = par1IconRegister.registerIcon("buildcraft:advworkbenchtable_bottom");
	    icons[0][1] = par1IconRegister.registerIcon("buildcraft:assemblytable_top");
        icons[1][1] = par1IconRegister.registerIcon("buildcraft:advworkbenchtable_top");
        icons[0][2] = par1IconRegister.registerIcon("buildcraft:assemblytable_side");
        icons[1][2] = par1IconRegister.registerIcon("buildcraft:advworkbenchtable_side");
	}
}
