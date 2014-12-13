/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.ZonePlan;
import buildcraft.core.inventory.SimpleInventory;

public class TileZonePlan extends TileBuildCraft implements IInventory {

	public static final int RESOLUTION = 2048;
	public static final int CRAFT_TIME = 120;
	private static int RESOLUTION_CHUNKS = RESOLUTION >> 4;

	public int chunkStartX, chunkStartZ;
	public byte[] colors = new byte[RESOLUTION * RESOLUTION];

	public short progress = 0;

	private boolean scan = false;
	private int chunkIt = 0;

	private ZonePlan[] selectedAreas = new ZonePlan[16];
	private int currentSelectedArea = 0;

	private SimpleInventory inv = new SimpleInventory(2, "inv", 64);

	private SafeTimeTracker zonePlannerScanning = new SafeTimeTracker(5);

	@Override
	public void initialize() {
		super.initialize();
		chunkStartX = (xCoord >> 4) - RESOLUTION_CHUNKS / 2;
		chunkStartZ = (zCoord >> 4) - RESOLUTION_CHUNKS / 2;

		if (!scan) {
			chunkIt = 0;
			scan = true;
		}
	}

	private int[] getCoords() {
		int chunkCenterX = xCoord >> 4;
		int chunkCenterZ = zCoord >> 4;

		if (chunkIt == 0) {
			return new int[] {chunkCenterX, chunkCenterZ};
		}

		int radius = 1;
		int left = chunkIt;

		while (radius < RESOLUTION_CHUNKS / 2) {
			int lineLength = radius * 2;
			int perimeter = lineLength * 4;

			if (left <= perimeter) {
				int chunkX = 0, chunkZ = 0;
				int remained = (left - 1) % lineLength;

				if ((left - 1) / lineLength == 0) {
					chunkX = chunkCenterX + radius;
					chunkZ = chunkCenterZ - lineLength / 2 + remained;
				} else if ((left - 1) / lineLength == 1) {
					chunkX = chunkCenterX - radius;
					chunkZ = chunkCenterZ - lineLength / 2 + remained + 1;
				} else if ((left - 1) / lineLength == 2) {
					chunkX = chunkCenterX - lineLength / 2 + remained + 1;
					chunkZ = chunkCenterZ + radius;
				} else {
					chunkX = chunkCenterX - lineLength / 2 + remained;
					chunkZ = chunkCenterZ - radius;
				}

				return new int[] {chunkX, chunkZ};
			} else {
				left -= perimeter;
			}

			radius += 1;
		}

		return new int[] {chunkCenterX, chunkCenterZ};
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (scan && zonePlannerScanning.markTimeIfDelay(worldObj)) {
			int[] coords = getCoords();
			Chunk chunk = worldObj.getChunkFromChunkCoords(coords[0], coords[1]);
			loadChunk(chunk);

			if (chunkIt > RESOLUTION_CHUNKS * RESOLUTION_CHUNKS) {
				scan = false;
				chunkIt = 0;
			} else {
				chunkIt++;
			}
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
				}

				inv.setInventorySlotContents(1, stack);
			}
		} else if (progress != 0) {
			progress = 0;
			sendNetworkUpdate();
		}
	}

	private void loadChunk(Chunk chunk) {
		for (int cx = 0; cx < 16; ++cx) {
			for (int cz = 0; cz < 16; ++cz) {
				int x = (chunk.xPosition << 4) + cx;
				int z = (chunk.zPosition << 4) + cz;

				int y = getWorldObj().getHeightValue(x, z);
				int color;
				while ((color = chunk.getBlock(cx, y, cz).getMapColor(0).colorIndex) == MapColor.airColor.colorIndex) {
					y--;
					if (y < 0) {
						break;
					}
				}

				int ix = x - chunkStartX * 16;
				int iz = z - chunkStartZ * 16;

				colors[ix + iz * RESOLUTION] = (byte) color;
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setBoolean("scan", scan);
		nbt.setInteger("chunkIt", chunkIt);
		nbt.setByteArray("colors", colors);

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

		scan = nbt.getBoolean("scan");
		chunkIt = nbt.getInteger("chunkIt");
		colors = nbt.getByteArray("colors");

		if (colors.length != RESOLUTION * RESOLUTION || chunkIt >= RESOLUTION_CHUNKS * RESOLUTION_CHUNKS) {
			colors = new byte[RESOLUTION * RESOLUTION];
			scan = true;
			chunkIt = 0;
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
	}

	@Override
	public void readData(ByteBuf stream) {
		progress = stream.readShort();
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
