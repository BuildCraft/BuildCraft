/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;

public class BlockHopper extends BlockBuildCraft {

	private static IIcon icon;

	public BlockHopper() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileHopper();
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
		return BuildCraftCore.blockByEntityModel;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		super.onBlockActivated(world, x, y, z, entityplayer, par6, par7, par8, par9);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		if (entityplayer.getCurrentEquippedItem() != null) {
			if (entityplayer.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
				return false;
			}
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftFactory.instance, GuiIds.HOPPER, world, x, y, z);
		}

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		icon = par1IconRegister.registerIcon("buildcraft:hopperBottom");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		return icon;
	}
}
