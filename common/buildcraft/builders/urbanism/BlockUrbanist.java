/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.GuiIds;

public class BlockUrbanist extends BlockBuildCraft {

	public BlockUrbanist() {
		super(Material.rock);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		disableStats();
		setTickRandomly(true);
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.URBANIST,
					world, i, j, k);
		}

		return true;

	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileUrbanist();
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return false;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		return 1;
	}
}
