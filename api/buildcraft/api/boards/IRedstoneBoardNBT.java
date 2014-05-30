/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.boards;

import java.util.List;
import java.util.Random;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IRedstoneBoardNBT {

	String getID();

	void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced);

	IRedstoneBoard create(NBTTagCompound nbt);

	@SideOnly(Side.CLIENT)
	void registerIcons(IIconRegister iconRegister);

	@SideOnly(Side.CLIENT)
	IIcon getIcon(NBTTagCompound nbt);

	BoardParameter[] getParameters();

	void setParameters(BoardParameter[] parameters);

	void createRandomBoard(NBTTagCompound nbt, Random rand);

}
