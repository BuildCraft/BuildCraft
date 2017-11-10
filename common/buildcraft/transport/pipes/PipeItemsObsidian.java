/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyHandler;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.inventory.filters.StackFilter;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.utils.TransportUtils;

public class PipeItemsObsidian extends Pipe<PipeTransportItems> implements IEnergyHandler {
	private final RFBattery battery = new RFBattery(2560, 640, 0);
	private final WeakHashMap<Entity, Long> entityDropTime = new WeakHashMap<Entity, Long>();

	public PipeItemsObsidian(Item item) {
		super(new PipeTransportItems(), item);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeItemsObsidian.ordinal();
	}

	@Override
	public void onEntityCollidedWithBlock(Entity entity) {
		super.onEntityCollidedWithBlock(entity);

		if (entity.isDead) {
			return;
		}

		if (canSuck(entity, 0)) {
			pullItemIntoPipe(entity, 0);
		}
	}

	private AxisAlignedBB getSuckingBox(ForgeDirection orientation, int distance) {
		if (orientation == ForgeDirection.UNKNOWN) {
			return null;
		}
		Position p1 = new Position(container.xCoord, container.yCoord, container.zCoord, orientation);
		Position p2 = new Position(container.xCoord, container.yCoord, container.zCoord, orientation);

		switch (orientation) {
			case EAST:
				p1.x += distance;
				p2.x += 1 + distance;
				break;
			case WEST:
				p1.x -= distance - 1;
				p2.x -= distance;
				break;
			case UP:
			case DOWN:
				p1.x += distance + 1;
				p2.x -= distance;
				p1.z += distance + 1;
				p2.z -= distance;
				break;
			case SOUTH:
				p1.z += distance;
				p2.z += distance + 1;
				break;
			case NORTH:
			default:
				p1.z -= distance - 1;
				p2.z -= distance;
				break;
		}

		switch (orientation) {
			case EAST:
			case WEST:
				p1.y += distance + 1;
				p2.y -= distance;
				p1.z += distance + 1;
				p2.z -= distance;
				break;
			case UP:
				p1.y += distance + 1;
				p2.y += distance;
				break;
			case DOWN:
				p1.y -= distance - 1;
				p2.y -= distance;
				break;
			case SOUTH:
			case NORTH:
			default:
				p1.y += distance + 1;
				p2.y -= distance;
				p1.x += distance + 1;
				p2.x -= distance;
				break;
		}

		Position min = p1.min(p2);
		Position max = p1.max(p2);

		return AxisAlignedBB.getBoundingBox(min.x, min.y, min.z, max.x, max.y, max.z);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (battery.getEnergyStored() > 0) {
			for (int j = 1; j < 5; ++j) {
				if (suckItem(j)) {
					return;
				}
			}

			battery.useEnergy(0, 5, false);
		}
	}

	private boolean suckItem(int distance) {
		AxisAlignedBB box = getSuckingBox(getOpenOrientation(), distance);

		if (box == null) {
			return false;
		}

		List<Entity> discoveredEntities = container.getWorldObj().getEntitiesWithinAABB(Entity.class, box);

		for (Entity entity : discoveredEntities) {
			if (canSuck(entity, distance)) {
				pullItemIntoPipe(entity, distance);
				return true;
			}

			if (distance == 1 && entity instanceof EntityMinecart && entity instanceof IInventory) {
				EntityMinecart cart = (EntityMinecart) entity;
				if (!cart.isDead) {
					ITransactor trans = Transactor.getTransactorFor(cart);
					ForgeDirection openOrientation = getOpenOrientation();
					ItemStack stack = trans.remove(StackFilter.ALL, openOrientation, false);

					if (stack != null && battery.useEnergy(10, 10, false) > 0) {
						stack = trans.remove(StackFilter.ALL, openOrientation, true);
						if (stack != null) {
							TravelingItem item = TravelingItem.make(container.xCoord + 0.5, container.yCoord + TransportUtils.getPipeFloorOf(stack), container.zCoord + 0.5, stack);
							transport.injectItem(item, openOrientation.getOpposite());
						}
						return true;
					}
				}
			}
		}

		return false;
	}

	public void pullItemIntoPipe(Entity entity, int distance) {
		if (container.getWorldObj().isRemote) {
			return;
		}

		ForgeDirection orientation = getOpenOrientation().getOpposite();

		if (orientation != ForgeDirection.UNKNOWN) {
			container.getWorldObj().playSoundAtEntity(entity, "random.pop", 0.2F, ((container.getWorldObj().rand.nextFloat() - container.getWorldObj().rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

			ItemStack stack;

			double speed = 0.01F;

			if (entity instanceof EntityItem) {
				EntityItem item = (EntityItem) entity;
				ItemStack contained = item.getEntityItem();

				if (contained == null) {
					return;
				}

				TransportProxy.proxy.obsidianPipePickup(container.getWorldObj(), item, this.container);

				int energyUsed = Math.min(10 * contained.stackSize * distance, battery.getEnergyStored());

				if (distance == 0 || energyUsed / distance / 10 == contained.stackSize) {
					stack = contained;
					CoreProxy.proxy.removeEntity(entity);
				} else {
					stack = contained.splitStack(energyUsed / distance / 10);
				}

				battery.useEnergy(energyUsed, energyUsed, false);

				speed = Math.sqrt(item.motionX * item.motionX + item.motionY * item.motionY + item.motionZ * item.motionZ);
				speed = speed / 2F - 0.05;

				if (speed < 0.01) {
					speed = 0.01;
				}
			} else if (entity instanceof EntityArrow && battery.useEnergy(distance * 10, distance * 10, false) > 0) {
				stack = new ItemStack(Items.arrow, 1);
				CoreProxy.proxy.removeEntity(entity);
			} else {
				return;
			}

			if (stack == null) {
				return;
			}

			TravelingItem item = TravelingItem.make(container.xCoord + 0.5, container.yCoord + TransportUtils.getPipeFloorOf(stack), container.zCoord + 0.5, stack);

			item.setSpeed((float) speed);

			transport.injectItem(item, orientation);
		}
	}

	public void eventHandler(PipeEventItem.DropItem event) {
		entityDropTime.put(event.entity, event.entity.worldObj.getTotalWorldTime() + 200);
	}

	public boolean canSuck(Entity entity, int distance) {
		if (!entity.isEntityAlive()) {
			return false;
		}
		if (entity instanceof EntityItem) {
			EntityItem item = (EntityItem) entity;

			if (item.getEntityItem().stackSize <= 0) {
				return false;
			}

			long wt = entity.worldObj.getTotalWorldTime();
			if (entityDropTime.containsKey(entity) && entityDropTime.get(entity) >= wt) {
				return false;
			}

			return battery.getEnergyStored() >= distance * 10;
		} else if (entity instanceof EntityArrow) {
			EntityArrow arrow = (EntityArrow) entity;
			return arrow.canBePickedUp == 1 && battery.getEnergyStored() >= distance * 10;
		}
		return false;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
							 boolean simulate) {
		return battery.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
							 boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return battery.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return battery.getMaxEnergyStored();
	}
}
