package net.minecraft.src.buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.src.IInventory;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.EntityPassiveItem;
import net.minecraft.src.buildcraft.core.IPipeEntry;
import net.minecraft.src.buildcraft.core.Orientations;
import net.minecraft.src.buildcraft.core.Position;
import net.minecraft.src.buildcraft.core.Utils;

public abstract class TilePipe extends TileEntity implements IPipeEntry {
	World world;
	
	class EntityData {	
		boolean toCenter = true;
		EntityPassiveItem item;
		
		public Orientations orientation;
		
		public EntityData (EntityPassiveItem citem, Orientations orientation) {
			item = citem;
			
			this.orientation = orientation;
		}
	}
	
	LinkedList <EntityData> travelingEntities = new LinkedList <EntityData> ();
	LinkedList <EntityData> entitiesToLoad = new LinkedList <EntityData> ();
	
	public TilePipe () {
		world = CoreProxy.getWorld();

	}
	
	public void entityEntering (EntityPassiveItem item, Orientations orientation) {
		travelingEntities.add(new EntityData (item, orientation));		
		
		// Reajusting Ypos to make sure the object looks like sitting on the
		// pipe.
		if (orientation != Orientations.YPos && orientation != Orientations.YNeg) {
			item.setPosition(item.posX, yCoord + Utils.getPipeFloorOf(item.item), item.posZ);
		}
		
		// Readjust the speed
		
		if (item.speed > Utils.pipeNormalSpeed) {
			item.speed = item.speed - Utils.pipeNormalSpeed;
		}
		
		if (item.speed < Utils.pipeNormalSpeed) {
			item.speed = Utils.pipeNormalSpeed;
		}
	}

	/**
	 * Returns a list of all possible movements, that is to say adjacent 
	 * implementers of IPipeEntry or TileEntityChest.
	 */
	public LinkedList<Orientations> getPossibleMovements(Position pos,
			EntityPassiveItem item) {
		LinkedList<Orientations> result = new LinkedList<Orientations>();

		for (int o = 0; o <= 5; ++o) {
			if (Orientations.values()[o] != pos.orientation.reverse()) {
				Position newPos = new Position(pos);
				newPos.orientation = Orientations.values()[o];
				newPos.moveForwards(1.0);

				TileEntity entity = world.getBlockTileEntity((int) newPos.i,
						(int) newPos.j, (int) newPos.k);

				if (entity instanceof IPipeEntry) {
					result.add(newPos.orientation);
				} else if (entity instanceof IInventory) {
					if (Utils.checkAvailableSlot((IInventory) entity,
							item.item, false, newPos.orientation.reverse())) {
						result.add(newPos.orientation);
					}
				}
			}
		}

		return result;
	}
	
	public void updateEntity() {		
		for (EntityData data : entitiesToLoad) {
			world.entityJoinedWorld(data.item);
			travelingEntities.add(data);
		}
		
		entitiesToLoad.clear();
		
		LinkedList <EntityData> toRemove = new LinkedList <EntityData> ();				
		
		for (EntityData data : travelingEntities) {
			Position motion = new Position (0, 0, 0, data.orientation);
			motion.moveForwards(data.item.speed);			
						
			data.item.moveEntity(motion.i, motion.j, motion.k);
									
			if (data.toCenter && middleReached(data)) {
				data.toCenter = false;
				
				// Reajusting to the middle 

				data.item.setPosition(xCoord + 0.5,
						yCoord + Utils.getPipeFloorOf(data.item.item),
						zCoord + + 0.5);

				Orientations nextOrientation = resolveDestination (data);
				
				if (nextOrientation == Orientations.Unknown) {
					toRemove.add(data);

					data.item.toEntityItem(world, data.orientation);
				} else {
					data.orientation = nextOrientation;
				}
				
				
		    } else if (!data.toCenter && endReached (data)) {
		    	toRemove.add(data);
		    	
				Position destPos = new Position(xCoord, yCoord, zCoord,
						data.orientation);
		    	
				destPos.moveForwards(1.0);
				
				TileEntity tile = world.getBlockTileEntity((int) destPos.i,
						(int) destPos.j, (int) destPos.k);
				
				if (tile instanceof IPipeEntry) {
					((IPipeEntry) tile).entityEntering(data.item,
							data.orientation);
				} else if (tile instanceof IInventory
						&& (Utils.checkAvailableSlot((IInventory) tile,
								data.item.item, true, destPos.orientation.reverse()))) {
					
					data.item.setEntityDead();
				} else {
					data.item.toEntityItem(world, data.orientation);
				}
		    }
		}	
		
		travelingEntities.removeAll(toRemove);		
	}
	
	public boolean middleReached(EntityData entity) {
		float middleLimit = entity.item.speed * 1.01F;
		return (Math.abs(xCoord + 0.5 - entity.item.posX) < middleLimit
				&& Math.abs(yCoord + Utils.getPipeFloorOf(entity.item.item)
						- entity.item.posY) < middleLimit && Math.abs(zCoord + 0.5
				- entity.item.posZ) < middleLimit);
	}
	
	public boolean endReached (EntityData entity) {
		return entity.item.posX > xCoord + 1.0 
		|| entity.item.posX < xCoord
		|| entity.item.posY > yCoord + 1.0
		|| entity.item.posY < yCoord
		|| entity.item.posZ > zCoord + 1.0
		|| entity.item.posZ < zCoord;
	}
	
	public Position getPosition() {
		return new Position (xCoord, yCoord, zCoord);
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound)
    {
		super.readFromNBT(nbttagcompound);
		
		NBTTagList nbttaglist = nbttagcompound.getTagList("travelingEntities");		
		
		for (int j = 0; j < nbttaglist.tagCount(); ++j) {
			try {
				NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist
				.tagAt(j);			

				EntityPassiveItem entity = new EntityPassiveItem (world);
				entity.readFromNBT(nbttagcompound2);

				EntityData data = new EntityData(entity,
						Orientations.values()[nbttagcompound2.getInteger("orientation")]);
				data.toCenter = nbttagcompound2.getBoolean("toCenter"); 

				entitiesToLoad.add(data);
			} catch (Throwable t) {
				//  It may be the case that entities cannot be reloaded between
				//  two versions - ignore these errors.
			}
		}
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);    	
    
    	NBTTagList nbttaglist = new NBTTagList();
    	    	
    	for (EntityData data : travelingEntities) {    		
    		NBTTagCompound nbttagcompound2 = new NBTTagCompound ();
    		nbttaglist.setTag(nbttagcompound2);
    		data.item.writeToNBT(nbttagcompound2);
    		nbttagcompound2.setBoolean("toCenter", data.toCenter);
    		nbttagcompound2.setInteger("orientation", data.orientation.ordinal());    		
    	}
    	
    	nbttagcompound.setTag("travelingEntities", nbttaglist);
    }
    
    protected Orientations resolveDestination (EntityData data) {
    	LinkedList<Orientations> listOfPossibleMovements = getPossibleMovements(new Position(
				xCoord, yCoord, zCoord, data.orientation), data.item);
		
		if (listOfPossibleMovements.size() == 0) {					
			return Orientations.Unknown;													
		} else {					
			int i = world.rand.nextInt(listOfPossibleMovements.size());
			
			return listOfPossibleMovements.get(i);															
		}				
    }
    
    public void destroy () {
    	for (EntityData data : travelingEntities) {
    		data.item.toEntityItem(worldObj, data.orientation);
    	}
    	
    	travelingEntities.clear();
    }
    
}
