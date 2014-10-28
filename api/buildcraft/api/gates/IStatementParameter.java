/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.gates;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import buildcraft.api.transport.IPipeTile;

public interface IStatementParameter {
	
	/**
	 * Every parameter needs a unique tag, it should be in the format of
	 * "<modid>:<name>".
	 *
	 * @return the unique id
	 */
	String getUniqueTag();
	
	@SideOnly(Side.CLIENT)
	IIcon getIcon();

	@SideOnly(Side.CLIENT)
	ItemStack getItemStack();

	@SideOnly(Side.CLIENT)
	void registerIcons(IIconRegister iconRegister);
	
	/**
	 * Return the parameter description in the UI
	 */
	String getDescription();

	void onClick(Object source, IStatement stmt, ItemStack stack, int mouseButton);

	void readFromNBT(NBTTagCompound compound);

	void writeToNBT(NBTTagCompound compound);

	/**
	 * This returns the parameter after a left rotation. Used in particular in
	 * blueprints orientation.
	 */
	IStatementParameter rotateLeft();
}
