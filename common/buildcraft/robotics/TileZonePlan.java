/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.IZone;
import buildcraft.api.items.IMapLocation;
import buildcraft.api.items.INamedItem;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.robotics.gui.ContainerZonePlan;

public class TileZonePlan extends TileBuildCraft implements IInventory {

	public static final int RESOLUTION = 2048;
	public static final int CRAFT_TIME = 120;
	private static int RESOLUTION_CHUNKS = RESOLUTION >> 4;

	public int chunkStartX, chunkStartZ;

	public short progress = 0;

	public String mapName = "";

	private ZonePlan[] selectedAreas = new ZonePlan[16];
	private int currentSelectedArea = 0;

	private SimpleInventory inv = new SimpleInventory(3, "inv", 64);

	@Override
	public void initialize() {
		super.initialize();

		int cx = xCoord >> 4;
		int cz = zCoord >> 4;

		chunkStartX = cx - RESOLUTION_CHUNKS / 2;
		chunkStartZ = cz - RESOLUTION_CHUNKS / 2;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (inv.getStackInSlot(0) != null
				&& inv.getStackInSlot(1) == null
				&& inv.getStackInSlot(0).getItem() instanceof ItemMapLocation) {

			if (progress < CRAFT_TIME) {
				progress++;

				if (worldObj.getTotalWorldTime() % 5 == 0) {
					sendNetworkUpdate();
				}
			} else {
				ItemStack stack = inv.decrStackSize(0, 1);

				if (selectedAreas[currentSelectedArea] != null) {
					ItemMapLocation.setZone(stack, selectedAreas[currentSelectedArea]);
					((INamedItem) stack.getItem()).setName(stack, mapName);
				}

				inv.setInventorySlotContents(1, stack);
			}
		} else if (progress != 0) {
			progress = 0;
			sendNetworkUpdate();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("name", mapName);

		NBTTagCompound invNBT = new NBTTagCompound();
		inv.writeToNBT(invNBT);
		nbt.setTag("inv", invNBT);

		for (int i = 0; i < selectedAreas.length; ++i) {
			if (selectedAreas[i] != null) {
				NBTTagCompound subNBT = new NBTTagCompound();
				selectedAreas[i].writeToNBT(subNBT);
				nbt.setTag("selectedArea[" + i + "]", subNBT);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		mapName = nbt.getString("name");

		if (mapName == null) {
			mapName = "";
		}

		inv.readFromNBT(nbt.getCompoundTag("inv"));

		for (int i = 0; i < selectedAreas.length; ++i) {
			if (nbt.hasKey("selectedArea[" + i + "]")) {
				selectedAreas[i] = new ZonePlan();
				selectedAreas[i].readFromNBT(nbt.getCompoundTag("selectedArea[" + i + "]"));
			}
		}
	}

	@Override
	public void writeData(ByteBuf stream) {
		stream.writeShort(progress);
		NetworkUtils.writeUTF(stream, mapName);
	}

	@Override
	public void readData(ByteBuf stream) {
		progress = stream.readShort();
		mapName = NetworkUtils.readUTF(stream);
	}

	private void importMap(ItemStack stack) {
		if (stack != null && stack.getItem() instanceof IMapLocation) {
			final IZone zone = ((IMapLocation) stack.getItem()).getZone(stack);
			if (zone != null && zone instanceof ZonePlan) {
				selectedAreas[currentSelectedArea] = (ZonePlan) zone;

				for (EntityPlayer e : (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
					if (e.openContainer != null && e.openContainer instanceof ContainerZonePlan
							&& ((ContainerZonePlan) e.openContainer).getTile() == this) {
						Packet p = new PacketCommand(e.openContainer, "areaLoaded", new CommandWriter() {
							public void write(ByteBuf data) {
								((ZonePlan) zone).writeData(data);
							}
						});

						BuildCraftCore.instance.sendToPlayer(e, p);
					}
				}
			}
		}
	}

	public ZonePlan selectArea(int index) {
		if (selectedAreas[index] == null) {
			selectedAreas[index] = new ZonePlan();
		}

		currentSelectedArea = index;

		return selectedAreas[index];
	}

	public void setArea(int index, ZonePlan area) {
		selectedAreas[index] = area;
	}

	@Override
	public int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return inv.getStackInSlot(slotId);
	}

	@Override
	public ItemStack decrStackSize(int slotId, int count) {
		return inv.decrStackSize(slotId, count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId) {
		return inv.getStackInSlotOnClosing(slotId);
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemstack) {
		inv.setInventorySlotContents(slotId, itemstack);

		if (!worldObj.isRemote && slotId == 2) {
			importMap(itemstack);
		}
	}

	@Override
	public String getInventoryName() {
		return inv.getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return inv.hasCustomInventoryName();
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return inv.isUseableByPlayer(entityplayer);
	}

	@Override
	public void openInventory() {
		inv.openInventory();
	}

	@Override
	public void closeInventory() {
		inv.closeInventory();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return inv.isItemValidForSlot(i, itemstack);
	}
}
