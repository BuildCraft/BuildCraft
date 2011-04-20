package net.minecraft.src.buildcraft;

import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class TilePipe extends TileEntity implements ITickListener, IPipeEntry {
	World world;
	
	class EntityData {	
		boolean toCenter = true;
		EntityPassiveItem item;
		
		Orientations orientation;
		
		public EntityData (EntityPassiveItem citem, Orientations orientation) {
			item = citem;
			
			this.orientation = orientation;
		}
	}
	
	LinkedList <EntityData> travelingEntities = new LinkedList <EntityData> ();
	LinkedList <EntityData> entitiesToLoad = new LinkedList <EntityData> ();
	
	public TilePipe () {
		world = ModLoader.getMinecraftInstance().theWorld;

	}
	
	public TilePipe (int i, int j, int k) {
		this ();
		xCoord = i;
		yCoord = j;
		zCoord = k;	
	}
	
	public void entityEntering (EntityPassiveItem item, Orientations orientation) {
		if (travelingEntities.size() == 0) {
			mod_BuildCraft.getInstance().registerTicksListener(this, 1);			
		}
		
		travelingEntities.add(new EntityData (item, orientation));
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
				} else if (entity instanceof TileEntityChest) {
					if (Utils.checkAvailableSlot((TileEntityChest) entity,
							item.item, false)) {
						result.add(newPos.orientation);
					}
				}
			}
		}

		return result;
	}
	
	public void tick(Minecraft minecraft) {
		for (EntityData data : entitiesToLoad) {
			world.entityJoinedWorld(data.item);
			travelingEntities.add(data);
		}
		
		entitiesToLoad.clear();
		
		LinkedList <EntityData> toRemove = new LinkedList <EntityData> ();				
		
		for (EntityData data : travelingEntities) {
			Position motion = new Position (0, 0, 0, data.orientation);
			motion.moveForwards(0.01);
						
			data.item.moveEntity(motion.i, motion.j, motion.k);
									
			if (data.toCenter && middleReached(data)) {
				data.toCenter = false;
				
				LinkedList<Orientations> listOfPossibleMovements = getPossibleMovements(new Position(
						xCoord, yCoord, zCoord, data.orientation), data.item);
				
				if (listOfPossibleMovements.size() == 0) {					
					toRemove.add(data);
					
					data.item.toEntityItem(world, data.orientation, 0.1F);															
				} else {					
					int i = world.rand.nextInt(listOfPossibleMovements.size());
					
					data.orientation = listOfPossibleMovements.get(i);															
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
				} else if (tile instanceof TileEntityChest
						&& (Utils.checkAvailableSlot((TileEntityChest) tile,
								data.item.item, true))) {

					data.item.setEntityDead();
				} else {
					data.item.toEntityItem(world, data.orientation, 0.1F);
				}
		    }
		}	
		
		travelingEntities.removeAll(toRemove);		
		
		if (travelingEntities.size() == 0) {
			mod_BuildCraft.getInstance().unregisterTicksListener(this);
		}
	}
	
	public boolean middleReached (EntityData entity) {
		return (Math.abs(xCoord + 0.5 - entity.item.posX) < 0.011
				&& Math.abs (yCoord + 0.4 - entity.item.posY) < 0.011
				&& Math.abs (zCoord + 0.5 - entity.item.posZ) < 0.011);
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
			NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist
					.tagAt(j);			
			
			EntityPassiveItem entity = new EntityPassiveItem (world);
			entity.readEntityFromNBT(nbttagcompound2);
			
			EntityData data = new EntityData(entity,
					Orientations.values()[nbttagcompound2.getInteger("orientation")]);
			data.toCenter = nbttagcompound2.getBoolean("toCenter"); 
			
			entitiesToLoad.add(data);
		}
		
		if (entitiesToLoad.size() > 0) {
			mod_BuildCraft.getInstance().registerTicksListener(this, 1);
		}
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);    	
    
    	NBTTagList nbttaglist = new NBTTagList();
    	    	
    	for (EntityData data : travelingEntities) {    		
    		NBTTagCompound nbttagcompound2 = new NBTTagCompound ();
    		nbttaglist.setTag(nbttagcompound2);
    		data.item.writeEntityToNBT(nbttagcompound2);
    		nbttagcompound2.setBoolean("toCenter", data.toCenter);
    		nbttagcompound2.setInteger("orientation", data.orientation.ordinal());    		
    	}
    	
    	nbttagcompound.setTag("travelingEntities", nbttaglist);
    }

}
