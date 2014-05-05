/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.core.BlockBuildCraft;
import buildcraft.core.IItemPipe;

public class BlockEnergyConverter extends BlockBuildCraft {
	@SideOnly(Side.CLIENT)
	private IIcon blockTexture;

	public BlockEnergyConverter() {
		super(Material.ground);
		setBlockName("energyConverter");
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int side, float par7, float par8, float par9) {
		TileEntity tile = world.getTileEntity(i, j, k);

		// Do not open guis when having a pipe in hand
		if (player.getCurrentEquippedItem() != null &&
				player.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
			return false;
		}

		return tile instanceof TileEnergyConverter
				&& ((TileEnergyConverter) tile).onBlockActivated(player, ForgeDirection.getOrientation(side));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEnergyConverter();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		blockTexture = par1IconRegister.registerIcon("buildcraft:blockEnergyConverter");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int i, int j) {
		return blockTexture;
	}
}
