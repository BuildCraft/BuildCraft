/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.power.ILaserTargetBlock;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockLaserTable extends BlockBuildCraft implements ILaserTargetBlock {
	protected static final int TABLE_MAX = 6;

	public BlockLaserTable() {
		super(Material.iron);

		setBlockBounds(0, 0, 0, 1, 8F / 16F, 1);
		setHardness(10F);
		setCreativeTab(BCCreativeTab.get("main"));
		setPassCount(2);
		setAlphaPass(true);
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
	public int getRenderType() {
		return SiliconProxy.laserTableModel;
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		if (super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9)) {
			return true;
		}

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		if (!world.isRemote) {
			int meta = world.getBlockMetadata(i, j, k);
			entityplayer.openGui(BuildCraftSilicon.instance, meta, world, i, j, k);
		}
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		switch (metadata) {
			case 0:
				return new TileAssemblyTable();
			case 1:
				return new TileAdvancedCraftingTable();
			case 2:
				return new TileIntegrationTable();
			case 3:
				return new TileChargingTable();
			case 4:
				return new TileProgrammingTable();
			case 5:
				return new TileStampingTable();
		}
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return null;
	}

	@Override
	public int damageDropped(int par1) {
		return par1;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List par3List) {
		for (int i = 0; i < TABLE_MAX; i++) {
			par3List.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String[] getIconBlockNames() {
		return new String[]{
				"BuildCraft|Silicon:assemblyTable",
				"BuildCraft|Silicon:advancedCraftingTable",
				"BuildCraft|Silicon:integrationTable",
				"BuildCraft|Silicon:chargingTable",
				"BuildCraft|Silicon:programmingTable",
				"BuildCraft|Silicon:stampingTable"
		};
	}
}
