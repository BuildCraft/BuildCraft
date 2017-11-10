/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.list;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;

import buildcraft.BuildCraftCore;
import buildcraft.core.ItemList;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;

public class ContainerListOld extends BuildCraftContainer implements ICommandReceiver {

	public ListHandlerOld.StackLine[] lines;
	private EntityPlayer player;

	public ContainerListOld(EntityPlayer iPlayer) {
		super(iPlayer.inventory.getSizeInventory());

		player = iPlayer;

		lines = ListHandlerOld.getLines(player.getCurrentEquippedItem());

		for (int sy = 0; sy < 3; sy++) {
			for (int sx = 0; sx < 9; sx++) {
				addSlotToContainer(new Slot(player.inventory, sx + sy * 9 + 9, 8 + sx * 18, 153 + sy * 18));
			}
		}

		for (int sx = 0; sx < 9; sx++) {
			addSlotToContainer(new Slot(player.inventory, sx, 8 + sx * 18, 211));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	public void setStack(final int lineIndex, final int slotIndex, final ItemStack stack) {
		lines[lineIndex].setStack(slotIndex, stack);
		ListHandlerOld.saveLine(player.getCurrentEquippedItem(), lines[lineIndex], lineIndex);

		if (player.worldObj.isRemote) {
			BuildCraftCore.instance.sendToServer(new PacketCommand(this, "setStack", new CommandWriter() {
				public void write(ByteBuf data) {
					data.writeByte(lineIndex);
					data.writeByte(slotIndex);
					NetworkUtils.writeStack(data, stack);
				}
			}));
		}
	}

	public void switchButton(final int lineIndex, final int button) {
		if (button == 0) {
			lines[lineIndex].oreWildcard = false;
			lines[lineIndex].subitemsWildcard = !lines[lineIndex].subitemsWildcard;
		} else if (button == 1 && lines[lineIndex].isOre) {
			lines[lineIndex].subitemsWildcard = false;
			lines[lineIndex].oreWildcard = !lines[lineIndex].oreWildcard;
		}

		ListHandlerOld.saveLine(player.getCurrentEquippedItem(), lines[lineIndex], lineIndex);

		if (player.worldObj.isRemote) {
			BuildCraftCore.instance.sendToServer(new PacketCommand(this, "switchButton", new CommandWriter() {
				public void write(ByteBuf data) {
					data.writeByte(lineIndex);
					data.writeByte(button);
				}
			}));
		}
	}

	public void setLabel(final String text) {
		ItemList.saveLabel(player.getCurrentEquippedItem(), text);

		if (player.worldObj.isRemote) {
			BuildCraftCore.instance.sendToServer(new PacketCommand(this, "setLabel", new CommandWriter() {
				public void write(ByteBuf data) {
					NetworkUtils.writeUTF(data, text);
				}
			}));
		}
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side.isServer()) {
			if ("setLabel".equals(command)) {
				setLabel(NetworkUtils.readUTF(stream));
			} else if ("switchButton".equals(command)) {
				switchButton(stream.readUnsignedByte(), stream.readUnsignedByte());
			} else if ("setStack".equals(command)) {
				setStack(stream.readUnsignedByte(), stream.readUnsignedByte(), NetworkUtils.readStack(stream));
			}
		}
	}
}
