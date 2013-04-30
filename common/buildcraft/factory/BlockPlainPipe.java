/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import buildcraft.core.IFramePipeConnection;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPlainPipe extends Block implements IFramePipeConnection {

	public BlockPlainPipe(int i) {
		super(i, Material.glass);

		minX = Utils.pipeMinPos;
		minY = 0.0;
		minZ = Utils.pipeMinPos;

		maxX = Utils.pipeMaxPos;
		maxY = 1.0;
		maxZ = Utils.pipeMaxPos;
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

	public int idDropped(int i, Random random) {
		return 0;
	}

	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1, int z1, int x2, int y2, int z2) {

		return false;
	}

	public float getHeightInPipe() {
		return 0.5F;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
	    this.blockIcon = par1IconRegister.registerIcon("buildcraft:blockPlainPipe");
	}
}
