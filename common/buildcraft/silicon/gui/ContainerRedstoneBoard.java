/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.boards.IBoardParameter;
import buildcraft.api.boards.IBoardParameterStack;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCSide;
import buildcraft.core.utils.NBTUtils;

public class ContainerRedstoneBoard extends BuildCraftContainer {

	private EntityPlayer player;
	private RedstoneBoardNBT board;
	private IBoardParameter[] params;

	public ContainerRedstoneBoard(EntityPlayer iPlayer, int x, int y, int z) {
		super(iPlayer.inventory.getSizeInventory());

		player = iPlayer;

		NBTTagCompound boardNBT = NBTUtils.getItemData(player.getHeldItem());
		board = RedstoneBoardRegistry.instance.getRedstoneBoard(boardNBT);
		params = board.getParameters(boardNBT);

		for (int sy = 0; sy < 3; sy++) {
			for (int sx = 0; sx < 9; sx++) {
				addSlotToContainer(new Slot(player.inventory, sx + sy * 9 + 9, 8 + sx * 18, 140 + sy * 18));
			}
		}

		for (int sx = 0; sx < 9; sx++) {
			addSlotToContainer(new Slot(player.inventory, sx, 8 + sx * 18, 198));
		}

	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	@RPC(RPCSide.SERVER)
	public void setParameterStack(int position, ItemStack stack) {
		NBTTagCompound boardNBT = NBTUtils.getItemData(player.getHeldItem());
		((IBoardParameterStack) params[position]).setStack(stack);
		board.setParameters(NBTUtils.getItemData(player.getHeldItem()), params);
	}
}
