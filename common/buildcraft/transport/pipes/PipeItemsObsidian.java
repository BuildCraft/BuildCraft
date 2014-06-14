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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.mj.MjBattery;
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

public class PipeItemsObsidian extends Pipe<PipeTransportItems> {

	@MjBattery (maxCapacity = 256, maxReceivedPerCycle = 64, minimumConsumption = 0)
	private double mjStored = 0;

	private int[] entitiesDropped;
	private int entitiesDroppedIndex = 0;

	public PipeItemsObsidian(Item item) {
		super(new PipeTransportItems(), item);

		entitiesDropped = new int[32];
		Arrays.fill(entitiesDropped, -1);
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
	public void updateEntity () {
		super.updateEntity();

		if (mjStored > 0) {
			for (int j = 1; j < 5; ++j) {
				if (suckItem(j)) {
					return;
				}
			}

			mjStored = mjStored > 0.5 ? mjStored - 0.5 : 0;
		}
	}

	private boolean suckItem(int distance) {
		AxisAlignedBB box = getSuckingBox(getOpenOrientation(), distance);

		if (box == null) {
			return false;
		}

		@SuppressWarnings("rawtypes")
		List<Entity> discoveredEntities = container.getWorldObj().getEntitiesWithinAABB(Entity.class, box);

		for (Entity entity : discoveredEntities) {
			if (canSuck(entity, distance)) {
				pullItemIntoPipe(entity, distance);
				return true;
			}

			if (distance == 1 && entity instanceof EntityMinecartChest) {
				EntityMinecartChest cart = (EntityMinecartChest) entity;
				if (!cart.isDead) {
					ITransactor trans = Transactor.getTransactorFor(cart);
					ForgeDirection openOrientation = getOpenOrientation();
					ItemStack stack = trans.remove(StackFilter.ALL, openOrientation, false);

					if (stack != null && mjStored >= 1) {
						mjStored -= 1;
						trans.remove(StackFilter.ALL, openOrientation, true);
						EntityItem entityitem = new EntityItem(container.getWorldObj(), cart.posX, cart.posY + 0.3F, cart.posZ, stack);
						entityitem.delayBeforeCanPickup = 10;
						container.getWorldObj().spawnEntityInWorld(entityitem);
						pullItemIntoPipe(entityitem, 1);

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

			ItemStack stack = null;

			double speed = 0.01F;

			if (entity instanceof EntityItem) {
				EntityItem item = (EntityItem) entity;
				ItemStack contained = item.getEntityItem();

				CoreProxy.proxy.obsidianPipePickup(container.getWorldObj(), item, this.container);

				double energyUsed = mjStored > contained.stackSize * distance ? contained.stackSize * distance : mjStored;

				if (distance == 0 || energyUsed / distance == contained.stackSize) {
					stack = contained;
					CoreProxy.proxy.removeEntity(entity);
				} else {
					stack = contained.splitStack((int) (energyUsed / distance));
				}

				speed = Math.sqrt(item.motionX * item.motionX + item.motionY * item.motionY + item.motionZ * item.motionZ);
				speed = speed / 2F - 0.05;

				if (speed < 0.01) {
					speed = 0.01;
				}
			} else if (entity instanceof EntityArrow) {
				mjStored -= distance;
				stack = new ItemStack(Items.arrow, 1);
				CoreProxy.proxy.removeEntity(entity);
			}

			TravelingItem item = TravelingItem.make(container.xCoord + 0.5, container.yCoord + TransportUtils.getPipeFloorOf(stack), container.zCoord + 0.5, stack);

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

			return mjStored >= distance;
		} else if (entity instanceof EntityArrow) {
			EntityArrow arrow = (EntityArrow) entity;
			return arrow.canBePickedUp == 1 && mjStored >= distance;
		}
		return false;
	}
}
