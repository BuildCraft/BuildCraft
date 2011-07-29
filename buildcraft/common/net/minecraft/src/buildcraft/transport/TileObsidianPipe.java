package net.minecraft.src.buildcraft.transport;

import java.util.List;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityArrow;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityMinecart;
import net.minecraft.src.Item;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;

import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.core.Utils;

public class TileObsidianPipe extends TilePipe implements IPowerReceptor {
	
	private PowerProvider powerProvider;
	
	
	public TileObsidianPipe () {
		super ();
		
		entitiesDropped = new int [32];
		
		for (int i = 0; i < entitiesDropped.length; ++i) {
			entitiesDropped [i] = -1;
		}
		
		powerProvider = BuildCraftCore.powerFramework.createPowerProvider();
		powerProvider.configure(25, 1, 64, 1, 256);
		powerProvider.configurePowerPerdition(1, 1);
	}
	
	private int [] entitiesDropped;
	private int entitiesDroppedIndex = 0;
	
	//Vacuum pipe can be connected only to ONE other Pipe or IInventory. Otherwise it won't do anything
	public Orientations getSuckingOrientation() {	
	
		Position pos = new Position(xCoord, yCoord, zCoord);
		int Connections_num = 0;
		
		Position target_pos = new Position(pos);
		
		for (int o = 0; o <= 5; ++o) {
			Position newPos = new Position(pos);
			newPos.orientation = Orientations.values()[o];
			newPos.moveForwards(1.0);
						
			if (Utils.checkPipesConnections(worldObj, (int) newPos.x,
					(int) newPos.y, (int) newPos.z, xCoord, yCoord, zCoord)) {

				Connections_num++;

				if (Connections_num == 1) {
					target_pos = new Position(newPos);
				}
			}
		}
		
		if(Connections_num > 1 || Connections_num == 0) {
			return Orientations.Unknown;
		}
			
		return target_pos.orientation.reverse();
	}
	
	private AxisAlignedBB getSuckingBox(Orientations orientation, int distance)
	{		
		if(orientation == Orientations.Unknown)
		{
			return null;
		}
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
			p1.y += distance + 1;
			p2.y -= distance;
			p1.x += distance + 1;
			p2.x -= distance;
			break;
		}

		Position min = p1.min(p2);
		Position max = p1.max(p2);

		return AxisAlignedBB.getBoundingBoxFromPool(min.x, min.y, min.z, max.x,
				max.y, max.z);	
	}
	

	@Override
	public void doWork () {
		for (int j = 1; j < 5; ++j) {
			trySucc(j);
		}
	}
	
	private boolean trySucc (int distance) {		
		AxisAlignedBB box = getSuckingBox(getSuckingOrientation(), distance);
		
		if(box == null) {
			return false;			
		}
				
		@SuppressWarnings("rawtypes")
		List list = worldObj.getEntitiesWithinAABB(
				net.minecraft.src.Entity.class, box);

		for (int g = 0; g < list.size(); g++) {
			if (list.get(g) instanceof Entity) {
				Entity entity = (Entity) list.get(g);

				if (canSuck(entity, distance)) {
					pullItemIntoPipe(entity, distance);
					return true;
				}

				if (distance == 1 && list.get(g) instanceof EntityMinecart) {
					EntityMinecart cart = (EntityMinecart) list.get(g);
					if (!cart.isDead && cart.minecartType == 1) {
						ItemStack stack = checkExtractGeneric(
								(IInventory) cart, true,
								getSuckingOrientation().reverse());
						if (stack != null && powerProvider.useEnergy(1, 1, true) == 1) {
							EntityItem entityitem = new EntityItem(worldObj,
									cart.posX, cart.posY + 0.3F, cart.posZ,
									stack);
							entityitem.delayBeforeCanPickup = 10;
							worldObj.entityJoinedWorld(entityitem);
							pullItemIntoPipe(entityitem, 1);
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public ItemStack checkExtractGeneric(IInventory inventory,
			boolean doRemove, Orientations from) {
		for (int k = 0; k < inventory.getSizeInventory(); ++k) {
			if (inventory.getStackInSlot(k) != null
					&& inventory.getStackInSlot(k).stackSize > 0) {

				ItemStack slot = inventory.getStackInSlot(k);

				if (slot != null && slot.stackSize > 0) {
					if (doRemove) {
						return inventory.decrStackSize(k, 1);
					} else {
						return slot;
					}
				}
			}
		}

		return null;
	}
	
	public void pullItemIntoPipe(Entity entity, int distance) {
		if (APIProxy.isClient(worldObj)) {
			return;
		}
				
		Orientations orientation = getSuckingOrientation();
		
		if(orientation != Orientations.Unknown) {
			worldObj.playSoundAtEntity(
					entity,
					"random.pop",
					0.2F,
					((worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			
			ItemStack stack = null;
			
			if (entity instanceof EntityItem) {
				EntityItem item = (EntityItem) entity;
				TransportProxy.obsidianPipePickup(worldObj, item, this);
				
				int energyUsed = powerProvider.useEnergy(distance,
						item.item.stackSize * distance, true);
				
				if (distance == 0
						|| energyUsed / distance == item.item.stackSize) {
					stack = item.item;
					APIProxy.removeEntity(entity);
				} else {
					stack = item.item.splitStack(energyUsed / distance);
				}
				
			} else if (entity instanceof EntityArrow) {
				powerProvider.useEnergy(distance, distance, true);
				stack = new ItemStack(Item.arrow, 1);
				APIProxy.removeEntity(entity);
			}
			
			EntityPassiveItem passive = new EntityPassiveItem(worldObj, xCoord + 0.5, yCoord
					+ Utils.getPipeFloorOf(stack), zCoord + 0.5, stack);
			worldObj.entityJoinedWorld(passive);
			entityEntering(passive, orientation.reverse());
		}
	}
	
	public void onDropped (EntityItem item) {		
		if (entitiesDroppedIndex + 1 >= entitiesDropped.length) {
			entitiesDroppedIndex = 0;
		} else {
			entitiesDroppedIndex++;
		}
		
		entitiesDropped [entitiesDroppedIndex] = item.entityId;
	}
	
	public boolean canSuck (Entity entity, int distance) {
		if (entity.isDead) {
			return false;
		} if (entity instanceof EntityItem) {
			EntityItem item = (EntityItem) entity;
		
			for (int i = 0; i < entitiesDropped.length; ++i) {
				if (item.entityId == entitiesDropped [i]) {
					return false;
				}
			}
			
			return powerProvider.useEnergy(1, distance, false) >= distance;
		} else if (entity instanceof EntityArrow) {
			return powerProvider.useEnergy(1, distance, false) >= distance;
		} else {
			return false;
		}
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public PowerProvider getPowerProvider() {
		return powerProvider;
	}
}
