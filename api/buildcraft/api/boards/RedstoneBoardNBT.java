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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.Constants;

public abstract class RedstoneBoardNBT {

	public abstract String getID();

	public abstract void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced);

	public abstract IRedstoneBoard create(NBTTagCompound nbt);

	@SideOnly(Side.CLIENT)
	public abstract void registerIcons(IIconRegister iconRegister);

	@SideOnly(Side.CLIENT)
	public abstract IIcon getIcon(NBTTagCompound nbt);

	public abstract void createRandomBoard(NBTTagCompound nbt, Random rand);

	public IBoardParameter[] getParameters(NBTTagCompound nbt) {
		NBTTagList paramsNBT = nbt.getTagList("parameters", Constants.NBT.TAG_COMPOUND);
		IBoardParameter[] result = new IBoardParameter[paramsNBT.tagCount()];

		for (int i = 0; i < paramsNBT.tagCount(); ++i) {
			NBTTagCompound subNBT = paramsNBT.getCompoundTagAt(i);
			IBoardParameter p = RedstoneBoardRegistry.instance.createParameter(subNBT.getString("kind"));
			p.readFromNBT(subNBT);
			result[i] = p;
		}

		return result;
	}

	public void setParameters(NBTTagCompound nbt, IBoardParameter[] params) {
		NBTTagList paramsNBT = new NBTTagList();

		for (IBoardParameter p : params) {
			NBTTagCompound subNBT = new NBTTagCompound();
			subNBT.setString("kind", RedstoneBoardRegistry.instance.getKindForParam(p));
			p.writeToNBT(subNBT);
			paramsNBT.appendTag(subNBT);
		}

		nbt.setTag("parameters", paramsNBT);
	}

	public int getParameterNumber(NBTTagCompound nbt) {
		if (!nbt.hasKey("parameters")) {
			return 0;
		} else {
			return nbt.getTagList("parameters", Constants.NBT.TAG_COMPOUND).tagCount();
		}
	}

}
