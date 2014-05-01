/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import java.util.List;

import buildcraft.core.BlockBuildCraft;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import buildcraft.BuildCraftSilicon;
import buildcraft.core.CreativeTabBuildCraft;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLaserTable extends BlockBuildCraft{

	@SideOnly(Side.CLIENT)
	private IIcon[][] icons;

	public BlockLaserTable() {
		super(Material.iron, CreativeTabBuildCraft.TIER_3);

		setBlockBounds(0, 0, 0, 1, 9F / 16F, 1);
		setHardness(10F);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isACube() { // Never used!!!
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		// Drop through if the player is sneaking
		if (player.isSneaking()) {
			return false;
		}

		if (!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);
			player.openGui(BuildCraftSilicon.instance, meta, world, x, y, z);
		}
		return true;
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		int s = side > 1 ? 2 : side;
		return icons[meta][s];
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		switch (meta) {
			case 0:
				return new TileAssemblyTable();
			case 1:
				return new TileAdvancedCraftingTable();
			case 2:
				return new TileIntegrationTable();
			default:
				return null;
		}
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
		list.add(new ItemStack(this, 1, 2));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		icons = new IIcon[3][];
		icons[0] = new IIcon[3];
		icons[1] = new IIcon[3];
		icons[2] = new IIcon[3];

		icons[0][0] = par1IconRegister.registerIcon("buildcraft:assemblytable_bottom");
		icons[0][1] = par1IconRegister.registerIcon("buildcraft:assemblytable_top");
		icons[0][2] = par1IconRegister.registerIcon("buildcraft:assemblytable_side");

		icons[1][0] = par1IconRegister.registerIcon("buildcraft:advworkbenchtable_bottom");
		icons[1][1] = par1IconRegister.registerIcon("buildcraft:advworkbenchtable_top");
		icons[1][2] = par1IconRegister.registerIcon("buildcraft:advworkbenchtable_side");

		icons[2][0] = par1IconRegister.registerIcon("buildcraft:integrationtable_bottom");
		icons[2][1] = par1IconRegister.registerIcon("buildcraft:integrationtable_top");
		icons[2][2] = par1IconRegister.registerIcon("buildcraft:integrationtable_side");
	}
}
