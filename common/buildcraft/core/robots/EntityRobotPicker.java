/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.DefaultProps;
import buildcraft.core.inventory.TransactorSimple;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;

public class EntityRobotPicker extends EntityRobot implements IInventory {

	private static ResourceLocation texture = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_picker.png");
	private static Set<Integer> targettedItems = new HashSet<Integer>();

	SafeTimeTracker scanTracker = new SafeTimeTracker(40, 10);
	SafeTimeTracker pickTracker = new SafeTimeTracker(20, 0);
	SafeTimeTracker unloadTracker = new SafeTimeTracker(20, 0);

	TransactorSimple inventoryInsert = new TransactorSimple(this);

	int pickTime = -1;

	ItemStack[] inv = new ItemStack[6];

	private EntityItem target;

	public EntityRobotPicker(World par1World) {
		super(par1World);
	}

	@Override
	public ResourceLocation getTexture () {
		return texture;
	}

	@Override
	public void onUpdate () {
		super.onUpdate();

		if (worldObj.isRemote) {
			return;
		}

		if (target != null) {
			if (target.isDead) {
				targettedItems.remove(target.getEntityId());
				target = null;
				setMainAI(new RobotAIReturnToDock(this));
				hideLaser();
				scan ();
			} else if (pickTime == -1) {
				if (getDistance(target.posX, target.posY, target.posZ) < 10) {
					setLaserDestination((float) target.posX, (float) target.posY, (float) target.posZ);
					showLaser();
					pickTracker = new SafeTimeTracker (200);
					pickTime = 0;
				}
			} else {
				pickTime++;

				if (pickTime > 20) {
					target.getEntityItem().stackSize -= inventoryInsert.inject(
							target.getEntityItem(), ForgeDirection.UNKNOWN,
							true);

					if (target.getEntityItem().stackSize <= 0) {
						target.setDead();
					}
				}
			}
		} else {
			if (currentAI instanceof RobotAIDocked) {
				TileGenericPipe pipe = (TileGenericPipe) worldObj
						.getTileEntity(dockingStation.x, dockingStation.y,
								dockingStation.z);

				if (pipe != null && pipe.pipe.transport instanceof PipeTransportItems) {
					if (unloadTracker.markTimeIfDelay(worldObj)) {
						for (int i = 0; i < inv.length; ++i) {
							if (inv[i] != null) {
								float cx = dockingStation.x + 0.5F + 0.2F * dockingStation.side.offsetX;
								float cy = dockingStation.y + 0.5F + 0.2F * dockingStation.side.offsetY;
								float cz = dockingStation.z + 0.5F + 0.2F * dockingStation.side.offsetZ;

								TravelingItem item = TravelingItem.make(cx, cy,
										cz, inv[i]);

								((PipeTransportItems) pipe.pipe.transport)
										.injectItem(item, dockingStation.side.getOpposite());

								inv[i] = null;

								break;
							}
						}
					}
				}
			}

			if (scanTracker.markTimeIfDelay(worldObj)) {
				scan ();
			}
		}
	}

	public void scan () {
		for (Object o : worldObj.loadedEntityList) {
			Entity e = (Entity) o;

			if (!e.isDead && e instanceof EntityItem && !targettedItems.contains(e.getEntityId())) {
				double dx = e.posX - posX;
				double dy = e.posY - posY;
				double dz = e.posZ - posZ;

				double sqrDistance = dx * dx + dy * dy + dz * dz;
				double maxDistance = 100 * 100;

				if (sqrDistance <= maxDistance) {
					EntityItem item = (EntityItem) e;

					if (inventoryInsert.inject(item.getEntityItem(),
							ForgeDirection.UNKNOWN, false) > 0) {

						target = item;
						targettedItems.add(e.getEntityId());
						setMainAI(new RobotAIMoveAround(this, (float) e.posX,
								(float) e.posY, (float) e.posZ));
						pickTime = -1;

						break;
					}
				}
			}
		}
	}

	@Override
	public int getSizeInventory() {
		return inv.length;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return inv [var1];
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		ItemStack result = inv [var1].splitStack(var2);

		if (inv [var1].stackSize == 0) {
			inv [var1] = null;
		}

		return result;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return inv [var1].splitStack(var1);
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		inv [var1] = var2;
	}

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return false;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return inv[var1] == null
				|| (inv[var1].isItemEqual(var2) && inv[var1].isStackable() && inv[var1].stackSize
						+ var2.stackSize <= inv[var1].getItem()
						.getItemStackLimit());
	}

	@Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		for (int i = 0; i < inv.length; ++i) {
			NBTTagCompound stackNbt = new NBTTagCompound();

			if (inv [i] != null) {
				nbt.setTag("inv[" + i + "]", inv [i].writeToNBT(stackNbt));
			}
		}
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		for (int i = 0; i < inv.length; ++i) {
			inv [i] = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("inv[" + i + "]"));
		}
	}
}
