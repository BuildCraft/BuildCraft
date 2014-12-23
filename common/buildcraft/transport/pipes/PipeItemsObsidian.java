/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.util.EnumFacing;

import cofh.api.energy.IEnergyHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.core.RFBattery;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.StackFilter;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.utils.TransportUtils;

public class PipeItemsObsidian extends Pipe<PipeTransportItems> implements IEnergyHandler {
	private RFBattery battery = new RFBattery(2560, 640, 0);

	private int[] entitiesDropped;
	private int entitiesDroppedIndex = 0;
	
	public PipeItemsObsidian(Item item) {
		super(new PipeTransportItems(), item);

		entitiesDropped = new int[32];
		Arrays.fill(entitiesDropped, -1);
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}*/

	@Override
	public int getIconIndex(EnumFacing direction) {
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

	private AxisAlignedBB getSuckingBox(EnumFacing orientation, int distance) {
		if (orientation == null) {
			return null;
		}
		Position p1 = new Position(container.getPos(), orientation);
		Position p2 = new Position(container.getPos(), orientation);

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

		return AxisAlignedBB.fromBounds(min.x, min.y, min.z, max.x, max.y, max.z);
	}

	@Override
	public void updateEntity () {
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

		List<Entity> discoveredEntities = container.getWorld().getEntitiesWithinAABB(Entity.class, box);

		for (Entity entity : discoveredEntities) {
			if (canSuck(entity, distance)) {
				pullItemIntoPipe(entity, distance);
				return true;
			}

			if (distance == 1 && entity instanceof EntityMinecartChest) {
				EntityMinecartChest cart = (EntityMinecartChest) entity;
				if (!cart.isDead) {
					ITransactor trans = Transactor.getTransactorFor(cart);
					EnumFacing openOrientation = getOpenOrientation();
					ItemStack stack = trans.remove(StackFilter.ALL, openOrientation, false);

					if (stack != null && battery.useEnergy(10, 10, false) > 0) {
						trans.remove(StackFilter.ALL, openOrientation, true);
						EntityItem entityitem = new EntityItem(container.getWorld(), cart.posX, cart.posY + 0.3F, cart.posZ, stack);
						entityitem.setDefaultPickupDelay();
						container.getWorld().spawnEntityInWorld(entityitem);
						pullItemIntoPipe(entityitem, 1);

						return true;
					}
				}
			}
		}

		return false;
	}

	public void pullItemIntoPipe(Entity entity, int distance) {
		if (container.getWorld().isRemote) {
			return;
		}

		EnumFacing orientation = getOpenOrientation().getOpposite();

		if (orientation != null) {
			container.getWorld().playSoundAtEntity(entity, "random.pop", 0.2F, ((container.getWorld().rand.nextFloat() - container.getWorld().rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

			ItemStack stack = null;

			double speed = 0.01F;

			if (entity instanceof EntityItem) {
				EntityItem item = (EntityItem) entity;
				ItemStack contained = item.getEntityItem();

				if (contained == null) {
					return;
				}
				
				CoreProxy.proxy.obsidianPipePickup(container.getWorld(), item, this.container);

				int energyUsed = Math.min(10 * contained.stackSize * distance, battery.getEnergyStored());

				if (distance == 0 || energyUsed / distance / 10 == contained.stackSize) {
					stack = contained;
					CoreProxy.proxy.removeEntity(entity);
				} else {
					stack = contained.splitStack(energyUsed / distance / 10);
				}

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
			
			TravelingItem item = TravelingItem.make(container.getPos().getX() + 0.5, container.getPos().getY() + TransportUtils.getPipeFloorOf(stack), container.getPos().getZ() + 0.5, stack);

			item.setSpeed((float) speed);

			transport.injectItem(item, orientation);
		}
	}

	public void eventHandler(PipeEventItem.DropItem event) {
		if (entitiesDroppedIndex + 1 >= entitiesDropped.length) {
			entitiesDroppedIndex = 0;
		} else {
			entitiesDroppedIndex++;
		}
		entitiesDropped[entitiesDroppedIndex] = event.entity.getEntityId();
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

			for (int element : entitiesDropped) {
				if (item.getEntityId() == element) {
					return false;
				}
			}

			return battery.getEnergyStored() >= distance * 10;
		} else if (entity instanceof EntityArrow) {
			EntityArrow arrow = (EntityArrow) entity;
			return arrow.canBePickedUp == 1 && battery.getEnergyStored() >= distance * 10;
		}
		return false;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive,
			boolean simulate) {
		return battery.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract,
			boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {
		return battery.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return battery.getMaxEnergyStored();
	}
}
