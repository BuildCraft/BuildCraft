/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import java.util.List;

import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.ProxyCore;
import buildcraft.core.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeLogicObsidian;
import buildcraft.transport.PipeTransportItems;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityArrow;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityMinecart;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

public class PipeItemsObsidian extends Pipe implements IPowerReceptor {

	private IPowerProvider powerProvider;

	private int[] entitiesDropped;
	private int entitiesDroppedIndex = 0;

	public PipeItemsObsidian(int itemID) {
		super(new PipeTransportItems(), new PipeLogicObsidian(), itemID);

		entitiesDropped = new int[32];

		for (int i = 0; i < entitiesDropped.length; ++i)
			entitiesDropped[i] = -1;

		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(25, 1, 64, 1, 256);
		powerProvider.configurePowerPerdition(1, 1);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}
	
	@Override
	public int getTextureIndex(Orientations direction) {
		return 1 * 16 + 12;
	}


	@Override
	public void onEntityCollidedWithBlock(Entity entity) {
		super.onEntityCollidedWithBlock(entity);

		if (entity.isDead)
			return;

		if (canSuck(entity, 0))
			pullItemIntoPipe(entity, 0);
	}

	private AxisAlignedBB getSuckingBox(Orientations orientation, int distance) {
		if (orientation == Orientations.Unknown)
			return null;
		Position p1 = new Position(xCoord, yCoord, zCoord, orientation);
		Position p2 = new Position(xCoord, yCoord, zCoord, orientation);

		switch (orientation) {
		case XPos:
			p1.x += distance;
			p2.x += 1 + distance;
			break;
		case XNeg:
			p1.x -= (distance - 1);
			p2.x -= distance;
			break;
		case YPos:
		case YNeg:
			p1.x += distance + 1;
			p2.x -= distance;
			p1.z += distance + 1;
			p2.z -= distance;
			break;
		case ZPos:
			p1.z += distance;
			p2.z += distance + 1;
			break;
		case ZNeg:
		default:
			p1.z -= (distance - 1);
			p2.z -= distance;
			break;
		}

		switch (orientation) {
		case XPos:
		case XNeg:
			p1.y += distance + 1;
			p2.y -= distance;
			p1.z += distance + 1;
			p2.z -= distance;
			break;
		case YPos:
			p1.y += distance + 1;
			p2.y += distance;
			break;
		case YNeg:
			p1.y -= (distance - 1);
			p2.y -= distance;
			break;
		case ZPos:
		case ZNeg:
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
	public void doWork() {
		for (int j = 1; j < 5; ++j)
			if (trySucc(j))
				return;

		powerProvider.useEnergy(1, 1, true);
	}

	private boolean trySucc(int distance) {
		AxisAlignedBB box = getSuckingBox(getOpenOrientation(), distance);

		if (box == null)
			return false;

		@SuppressWarnings("rawtypes")
		List list = worldObj.getEntitiesWithinAABB(net.minecraft.src.Entity.class, box);

		for (int g = 0; g < list.size(); g++)
			if (list.get(g) instanceof Entity) {
				Entity entity = (Entity) list.get(g);

				if (canSuck(entity, distance)) {
					pullItemIntoPipe(entity, distance);
					return true;
				}

				if (distance == 1 && list.get(g) instanceof EntityMinecart) {
					EntityMinecart cart = (EntityMinecart) list.get(g);
					if (!cart.isDead && cart.minecartType == 1) {
						ItemStack stack = checkExtractGeneric(cart, true, getOpenOrientation());
						if (stack != null && powerProvider.useEnergy(1, 1, true) == 1) {
							EntityItem entityitem = new EntityItem(worldObj, cart.posX, cart.posY + 0.3F, cart.posZ, stack);
							entityitem.delayBeforeCanPickup = 10;
							worldObj.spawnEntityInWorld(entityitem);
							pullItemIntoPipe(entityitem, 1);
							return true;
						}
					}
				}
			}

		return false;
	}

	public ItemStack checkExtractGeneric(IInventory inventory, boolean doRemove, Orientations from) {
		for (int k = 0; k < inventory.getSizeInventory(); ++k)
			if (inventory.getStackInSlot(k) != null && inventory.getStackInSlot(k).stackSize > 0) {

				ItemStack slot = inventory.getStackInSlot(k);

				if (slot != null && slot.stackSize > 0)
					if (doRemove)
						return inventory.decrStackSize(k, 1);
					else
						return slot;
			}

		return null;
	}

	public void pullItemIntoPipe(Entity entity, int distance) {
		if (ProxyCore.proxy.isRemote(worldObj))
			return;

		Orientations orientation = getOpenOrientation().reverse();

		if (orientation != Orientations.Unknown) {
			worldObj.playSoundAtEntity(entity, "random.pop", 0.2F,
					((worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

			ItemStack stack = null;

			double speed = 0.01F;

			if (entity instanceof EntityItem) {
				EntityItem item = (EntityItem) entity;
				ProxyCore.proxy.obsidianPipePickup(worldObj, item, this.container);

				float energyUsed = powerProvider.useEnergy(distance, item.item.stackSize * distance, true);

				if (distance == 0 || energyUsed / distance == item.item.stackSize) {
					stack = item.item;
					ProxyCore.proxy.removeEntity(entity);
				} else
					stack = item.item.splitStack((int) (energyUsed / distance));

				speed = Math.sqrt(item.motionX * item.motionX + item.motionY * item.motionY + item.motionZ * item.motionZ);
				speed = speed / 2F - 0.05;

				if (speed < 0.01)
					speed = 0.01;
			} else if (entity instanceof EntityArrow) {
				powerProvider.useEnergy(distance, distance, true);
				stack = new ItemStack(Item.arrow, 1);
				ProxyCore.proxy.removeEntity(entity);
			}

			IPipedItem passive = new EntityPassiveItem(worldObj, xCoord + 0.5, yCoord + Utils.getPipeFloorOf(stack),
					zCoord + 0.5, stack);

			passive.setSpeed((float) speed);

			((PipeTransportItems) transport).entityEntering(passive, orientation);
		}
	}

	@Override
	public void onDropped(EntityItem item) {
		if (entitiesDroppedIndex + 1 >= entitiesDropped.length)
			entitiesDroppedIndex = 0;
		else
			entitiesDroppedIndex++;

		entitiesDropped[entitiesDroppedIndex] = item.entityId;
	}

	public boolean canSuck(Entity entity, int distance) {
		if (!entity.isEntityAlive())
			return false;
		if (entity instanceof EntityItem) {
			EntityItem item = (EntityItem) entity;

			if (item.item.stackSize <= 0)
				return false;

			for (int i = 0; i < entitiesDropped.length; ++i)
				if (item.entityId == entitiesDropped[i])
					return false;

			return powerProvider.useEnergy(1, distance, false) >= distance;
		} else if (entity instanceof EntityArrow)
			return powerProvider.useEnergy(1, distance, false) >= distance;
		else
			return false;
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().getMaxEnergyReceived();
	}
}
