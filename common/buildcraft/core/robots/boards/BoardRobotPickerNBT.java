/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.boards.IBoardParameter;
import buildcraft.api.boards.IRedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.utils.NBTUtils;
import buildcraft.core.utils.StringUtils;

public final class BoardRobotPickerNBT extends RedstoneBoardRobotNBT {

	public static BoardRobotPickerNBT instance = new BoardRobotPickerNBT();

	public IIcon icon;

	private BoardRobotPickerNBT() {

	}

	@Override
	public String getID() {
		return "buildcraft:boardRobotPicker";
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		list.add(StringUtils.localize("buildcraft.boardRobotPicker"));

		NBTTagCompound nbt = NBTUtils.getItemData(stack);

		int parameterNumber = getParameterNumber(nbt);

		if (parameterNumber > 0) {
			list.add(parameterNumber + " " + StringUtils.localize("buildcraft.boardDetail.parameters"));
		}

		list.add(StringUtils.localize("buildcraft.boardDetail.range") + ": " + nbt.getInteger("range"));
	}

	@Override
	public IRedstoneBoardRobot create(NBTTagCompound nbt) {
		return new BoardRobotPicker(nbt);
	}

	@Override
	public IIcon getIcon(NBTTagCompound nbt) {
		return icon;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:board_green");
	}

	@Override
	public void createRandomBoard(NBTTagCompound nbt) {
		int parameterNumber = (int) Math.floor(nextFloat(3) * 5);
		int range = (int) Math.floor(nextFloat(10) * 500) + 10;

		if (parameterNumber > 0) {
			IBoardParameter[] parameters = new IBoardParameter[parameterNumber];

			for (int i = 0; i < parameterNumber; ++i) {
				parameters[i] = RedstoneBoardRegistry.instance.createParameterStack();
			}

			setParameters(nbt, parameters);
		}

		nbt.setInteger("range", range);
	}

	@Override
	public ResourceLocation getRobotTexture() {
		return EntityRobot.ROBOT_TRANSPORT;
	}
}
