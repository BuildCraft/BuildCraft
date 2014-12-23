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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.StringUtils;

public final class BoardRobotCrafterNBT extends RedstoneBoardRobotNBT {

	public static BoardRobotCrafterNBT instance = new BoardRobotCrafterNBT();

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft",
			DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_crafter.png");

	//private IIcon icon;

	@Override
	public RedstoneBoardRobot create(NBTTagCompound nbt, EntityRobotBase robot) {
		return new BoardRobotCrafter(robot);
	}

	@Override
	public ResourceLocation getRobotTexture() {
		return TEXTURE;
	}

	@Override
	public String getID() {
		return "buildcraft:boardRobotCrafter";
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		list.add(StringUtils.localize("buildcraft.boardRobotCrafter"));
	}

	/*@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:board_blue");
	}

	@Override
	public IIcon getIcon(NBTTagCompound nbt) {
		return icon;
	}*/
}
