package net.minecraft.src.buildcraft.transport;

import java.util.LinkedList;
import java.util.TreeMap;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftTransport;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.ILiquidContainer;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.StackUtil;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.Utils;

public abstract class TilePipe extends TileBuildCraft implements IPipeEntry, ILiquidContainer {
		
	public static int flowRate = 20;
	
	private int [] sideToCenter = new int [6];
	private int [] centerToSide = new int [6];
	private int centerIn = 0;
	private int centerOut = 0;
	
	public boolean [] isInput = new boolean [6];
	
	public Orientations lastFromOrientation = Orientations.XPos;
	public Orientations lastToOrientation = Orientations.XPos;
	
	public class EntityData {	
		// TODO: Move passive data here too, like position, speed and all?
		boolean toCenter = true;
		EntityPassiveItem item;
		
		public Orientations orientation;
		
		public EntityData (EntityPassiveItem citem, Orientations orientation) {
			item = citem;
			
			this.orientation = orientation;
		}
	}
	
	public TreeMap<Integer, EntityData> travelingEntities = new TreeMap<Integer, EntityData> ();
	LinkedList <EntityData> entitiesToLoad = new LinkedList <EntityData> ();
	
	public TilePipe () {
		for (int j = 0; j < 6; ++j) {
			sideToCenter [j] = 0;
			centerToSide [j] = 0;
			isInput [j] = false;
		}
	}
	
	public void readjustSpeed (EntityPassiveItem item) {
		if (item.speed > Utils.pipeNormalSpeed) {
			item.speed = item.speed - Utils.pipeNormalSpeed;
		}
		
		if (item.speed < Utils.pipeNormalSpeed) {
			item.speed = Utils.pipeNormalSpeed;
		}
	}
	
	public void entityEntering (EntityPassiveItem item, Orientations orientation) {
		readjustSpeed(item);			
				
		if (!travelingEntities.containsKey(new Integer(item.entityId))) {
			travelingEntities.put(new Integer(item.entityId), new EntityData(
					item, orientation));
			
			item.container = this;
		}
		
		// Reajusting Ypos to make sure the object looks like sitting on the
		// pipe.
		if (orientation != Orientations.YPos && orientation != Orientations.YNeg) {
			item.setPosition(item.posX, yCoord + Utils.getPipeFloorOf(item.item), item.posZ);
		}
		
		if (APIProxy.isServerSide()) {
			if (item.synchroTracker.markTimeIfDelay(worldObj, 20)) {
				// FIXME: what about the other items???
				CoreProxy.sendToPlayers(createItemPacket(item, orientation),
						xCoord, yCoord, zCoord, 50,
						mod_BuildCraftTransport.instance);
			}
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

				if (canReceivePipeObjects(newPos, item)) {
					result.add(newPos.orientation);
				}
			}
		}

		return result;
	}
	
	public boolean canReceivePipeObjects(Position p,
			EntityPassiveItem item) {
		TileEntity entity = worldObj.getBlockTileEntity((int) p.x, (int) p.y,
				(int) p.z);
		
		if (!Utils.checkPipesConnections(worldObj, (int) p.x, (int) p.y,
				(int) p.z, xCoord, yCoord, zCoord)) {
			return false;
		}
		
		if (entity instanceof IPipeEntry) {
			return true;
		} else if (entity instanceof IInventory) {			
			if (new StackUtil(item.item).checkAvailableSlot((IInventory) entity,
					 false, p.orientation.reverse())) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean canReceiveLiquid(Position p) {
		TileEntity entity = worldObj.getBlockTileEntity((int) p.x, (int) p.y,
				(int) p.z);
		
		if (isInput [p.orientation.ordinal()]) {
			return false;
		}
		
		if (!Utils.checkPipesConnections(worldObj, (int) p.x, (int) p.y,
				(int) p.z, xCoord, yCoord, zCoord)) {
			return false;
		}
		
		if (entity instanceof IPipeEntry || entity instanceof ILiquidContainer) {
			return true;
		}
		
		return false;
	}
		
	public void updateEntity() {
		super.updateEntity();
		
		moveSolids();				
		moveLiquids();
	}
	
	private void moveSolids () {
		for (EntityData data : entitiesToLoad) {
			travelingEntities.put(new Integer(data.item.entityId), data);
		}
		
		entitiesToLoad.clear();
		
		LinkedList <EntityData> toRemove = new LinkedList <EntityData> ();				
		
		for (EntityData data : travelingEntities.values()) {
			Position motion = new Position (0, 0, 0, data.orientation);
			motion.moveForwards(data.item.speed);												
			
			data.item.setPosition(data.item.posX + motion.x, data.item.posY
					+ motion.y, data.item.posZ + motion.z);
									
			if ((data.toCenter && middleReached(data)) || outOfBounds(data)) {
				data.toCenter = false;
				
				// Reajusting to the middle 

				data.item.setPosition(xCoord + 0.5,
						yCoord + Utils.getPipeFloorOf(data.item.item),
						zCoord + + 0.5);

				Orientations nextOrientation = resolveDestination (data);
				
				if (nextOrientation == Orientations.Unknown) {
					toRemove.add(data);
					EntityItem dropped = data.item.toEntityItem(worldObj,
							data.orientation);
					
					if (dropped != null) {
						// On SMP, the client side doesn't actually drops 
						// items
						onDropped(dropped);
					}
				} else {
					data.orientation = nextOrientation;
				}
				
				
		    } else if (!data.toCenter && endReached (data)) {
		    	toRemove.add(data);
		    	
				Position destPos = new Position(xCoord, yCoord, zCoord,
						data.orientation);
		    	
				destPos.moveForwards(1.0);
				
				TileEntity tile = worldObj.getBlockTileEntity((int) destPos.x,
						(int) destPos.y, (int) destPos.z);
				
				if (tile instanceof IPipeEntry) {
					((IPipeEntry) tile).entityEntering(data.item,
							data.orientation);
				} else if (tile instanceof IInventory) {
					StackUtil utils = new StackUtil(data.item.item);
					
					if (!APIProxy.isClient(worldObj)) {
						if (utils.checkAvailableSlot((IInventory) tile, true,
								destPos.orientation.reverse())
								&& utils.items.stackSize == 0) {
							
							// Do nothing, we're adding the object to the world							
						} else {
							data.item.item = utils.items;
							EntityItem dropped = data.item.toEntityItem(
									worldObj, data.orientation);
							
							if (dropped != null) {
								// On SMP, the client side doesn't actually drops 
								// items
								onDropped(dropped);
							}
						}
					}
				} else {
					EntityItem dropped = data.item.toEntityItem(worldObj,
							data.orientation);
					
					if (dropped != null) {
						// On SMP, the client side doesn't actually drops 
						// items
						onDropped(dropped);
					}
										
				}

		    }
		}	
		
		travelingEntities.values().removeAll(toRemove);
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
	
	public boolean outOfBounds (EntityData entity) {
		return entity.item.posX > xCoord + 2.0 
		|| entity.item.posX < xCoord - 1.0
		|| entity.item.posY > yCoord + 2.0
		|| entity.item.posY < yCoord - 1.0
		|| entity.item.posZ > zCoord + 2.0
		|| entity.item.posZ < zCoord - 1.0;
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

				EntityPassiveItem entity = new EntityPassiveItem (APIProxy.getWorld());
				entity.readFromNBT(nbttagcompound2);
				entity.container = this;

				EntityData data = new EntityData(entity,
						Orientations.values()[nbttagcompound2.getInteger("orientation")]);
				data.toCenter = nbttagcompound2.getBoolean("toCenter"); 

				entitiesToLoad.add(data);
			} catch (Throwable t) {
				//  It may be the case that entities cannot be reloaded between
				//  two versions - ignore these errors.
			}
		}
		
    	for (int i = 0; i < 6; ++i) {
    		sideToCenter [i] = nbttagcompound.getInteger("sideToCenter[" + i + "]");
    		centerToSide [i] = nbttagcompound.getInteger("centerToSide[" + i + "]");
    		isInput [i] = nbttagcompound.getBoolean("isInput[" + i + "]");
    	}
    	
    	centerIn = nbttagcompound.getInteger("centerIn");
    	centerOut = nbttagcompound.getInteger("centerOut");
    	lastFromOrientation = Orientations.values()[nbttagcompound.getInteger("lastFromOrientation")];
    	lastToOrientation = Orientations.values()[nbttagcompound.getInteger("lastToOrientation")];
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);    	
    
    	NBTTagList nbttaglist = new NBTTagList();
    	    	
    	for (EntityData data : travelingEntities.values()) {    		
    		NBTTagCompound nbttagcompound2 = new NBTTagCompound ();
    		nbttaglist.setTag(nbttagcompound2);
    		data.item.writeToNBT(nbttagcompound2);
    		nbttagcompound2.setBoolean("toCenter", data.toCenter);
    		nbttagcompound2.setInteger("orientation", data.orientation.ordinal());    		
    	}
    	
    	nbttagcompound.setTag("travelingEntities", nbttaglist);

    	for (int i = 0; i < 6; ++i) {
    		nbttagcompound.setInteger("sideToCenter[" + i + "]", sideToCenter [i]);
    		nbttagcompound.setInteger("centerToSide[" + i + "]", centerToSide [i]);
    		nbttagcompound.setBoolean ("isInput[" + i + "]", isInput [i]);
    	}
    	
    	nbttagcompound.setInteger("centerIn", centerIn);
    	nbttagcompound.setInteger("centerOut", centerOut);    	
    	nbttagcompound.setInteger("lastFromOrientation", lastFromOrientation.ordinal());
    	nbttagcompound.setInteger("lastToOrientation", lastToOrientation.ordinal());
    }
    
    public Orientations resolveDestination (EntityData data) {
    	LinkedList<Orientations> listOfPossibleMovements = getPossibleMovements(new Position(
				xCoord, yCoord, zCoord, data.orientation), data.item);
		
		if (listOfPossibleMovements.size() == 0) {					
			return Orientations.Unknown;													
		} else {
			int i;
			
			if (APIProxy.isClient(worldObj) || APIProxy.isServerSide()) {
				i = Math.abs(data.item.entityId + xCoord + yCoord + zCoord
						+ data.item.deterministicRandomization)
						% listOfPossibleMovements.size();
			} else {
				i = worldObj.rand.nextInt(listOfPossibleMovements.size());
			}
					
			
			return listOfPossibleMovements.get(i);															
		}				
    }
    
    public void destroy () {
    	for (EntityData data : travelingEntities.values()) {
    		data.item.toEntityItem(worldObj, data.orientation);
    	}
    	
    	travelingEntities.clear();
    }
    
    protected void doWork () {}

	public void handleItemPacket(Packet230ModLoader packet) {
		if (packet.packetType != PacketIds.PipeItem.ordinal()) {
			return;
		}
		
//		EntityPassiveItem item = (EntityPassiveItem) APIProxy.getEntity(
//				worldObj, packet.dataInt[3]);
		
//		if (item == null) {
			EntityPassiveItem item = new EntityPassiveItem(worldObj);
			item.entityId = packet.dataInt [3];
			
			int itemId = packet.dataInt [5];
			int stackSize = packet.dataInt [6];
			int dmg = packet.dataInt [7];
			
			item.item = new ItemStack(itemId, stackSize, dmg);		
//		} else {
//			if (item.container != this) {
//				if (item.container != null) {
//					((TilePipe) item.container).travelingEntities
//							.remove(item.entityId);
//					item.container = null;
//				}
//			}
//		}
		
		Orientations orientation;						
		orientation = Orientations.values()[packet.dataInt [4]];
		
		item.setPosition(packet.dataFloat[0], packet.dataFloat[1],
				packet.dataFloat[2]);
		item.speed = packet.dataFloat [3];
		item.deterministicRandomization = packet.dataInt [8];
		
		if (item.container == null) {
			travelingEntities.put(new Integer(item.entityId), new EntityData(
					item, orientation));
			item.container = this;
		} else {
			travelingEntities.get(new Integer(item.entityId)).orientation = orientation;
		}
	}
	
	public Packet230ModLoader createItemPacket (EntityPassiveItem item, Orientations orientation) {
		Packet230ModLoader packet = new Packet230ModLoader();
		
		item.deterministicRandomization += worldObj.rand.nextInt(6);
		
		packet.modId = mod_BuildCraftTransport.instance.getId();
		packet.packetType = PacketIds.PipeItem.ordinal();
		packet.isChunkDataPacket = true;
		
		packet.dataInt = new int [9];
		packet.dataInt [0] = xCoord;
		packet.dataInt [1] = yCoord;
		packet.dataInt [2] = zCoord;
		packet.dataInt [3] = item.entityId;
		packet.dataInt [4] = orientation.ordinal();
		packet.dataInt [5] = item.item.itemID;
		packet.dataInt [6] = item.item.stackSize;
		packet.dataInt [7] = item.item.getItemDamage();
		packet.dataInt [8] = item.deterministicRandomization;
		
		packet.dataFloat = new float [4];
		packet.dataFloat [0] = (float) item.posX;
		packet.dataFloat [1] = (float) item.posY;
		packet.dataFloat [2] = (float) item.posZ;
		packet.dataFloat [3] = (float) item.speed;
		
		return packet;		
	}

	public int getNumberOfItems () {
		return travelingEntities.size();
	}
	
	public void onDropped (EntityItem item) {
		
	}
	
	/** 
	 * Fills the pipe, and return the amount of liquid that has been used.
	 */
	public int fill (Orientations from, int quantity) {		
		int space = BuildCraftCore.OIL_BUCKET_QUANTITY / 4
				- sideToCenter[from.ordinal()] - centerToSide[from.ordinal()]
				+ flowRate;
		
		isInput [from.ordinal()] = true;
		
		if (space <= 0) {
			return 0;
		} if (space > quantity) {
			sideToCenter [from.ordinal()] += quantity;
			return quantity;
		} else {
			sideToCenter [from.ordinal()] += space;
			
			return space;
		}		
	}
	
	private void moveLiquids () {					
		float centerSpace = BuildCraftCore.OIL_BUCKET_QUANTITY / 2 - centerIn
				- centerOut + flowRate;
		
		boolean moved = false;
		
		// computes the various inputs of liquids
		
		for (int i = 0; i < 6; ++i) {
			if (isInput [i]) {
				if (centerToSide [i] > 0 && centerSpace >= flowRate) {
					lastFromOrientation = Orientations.values()[i];
					centerToSide [i] -= flowRate;
					centerIn += flowRate;
					moved = true;
				}
				
				if (sideToCenter[i] + centerToSide[i] >= BuildCraftCore.OIL_BUCKET_QUANTITY / 4) {
					centerToSide[i] = sideToCenter[i] + centerToSide[i];
					sideToCenter[i] = 0;
				}
			}
		}
		
		// computes the move from the center

		if (centerIn + centerOut >= BuildCraftCore.OIL_BUCKET_QUANTITY / 2) {
			centerOut = centerIn + centerOut;
			centerIn = 0;
		} 
		
		// computes the output of liquid
		for (int i = 0; i < 6; ++i) {		
			Position p = new Position (xCoord, yCoord, zCoord, Orientations.values() [i]);
			p.moveForwards(1);

			if (canReceiveLiquid(p)) {				
				if (sideToCenter [i] > 0) {
					ILiquidContainer pipe = (ILiquidContainer) Utils.getTile(worldObj, p,
							Orientations.Unknown);
					
					sideToCenter [i] -= pipe
							.fill(p.orientation.reverse(), flowRate);
					
					moved = true;
				}
				
				if (centerOut > 0 && sideToCenter [i] + centerToSide [i] <= BuildCraftCore.OIL_BUCKET_QUANTITY / 4) {
					lastToOrientation = p.orientation;
					centerToSide [i] += flowRate;
					centerOut -= flowRate;
					
					moved = true;
				}
				
				if (centerToSide [i] + sideToCenter [i] >= BuildCraftCore.OIL_BUCKET_QUANTITY / 4) {
					sideToCenter [i] = centerToSide [i] + sideToCenter [i];
					centerToSide [i] = 0;
				}			
			}
		}
		
		if (!moved) {
			for (int i = 0; i < 6; ++i) {
				Position p = new Position (xCoord, yCoord, zCoord, Orientations.values() [i]);
				p.moveForwards(1);

				if (canReceiveLiquid(p)) {	
					return;
				}
			}
			
			// If we can't find a direction where to potentially send liquid,
			// reset all input markers
			for (int i = 0; i < 6; ++i) {
				isInput [i] = false;
			}
		}
		
	}
	
	public int getSideToCenter (int orientation) {
		if (sideToCenter [orientation] > BuildCraftCore.OIL_BUCKET_QUANTITY / 4) {
			return BuildCraftCore.OIL_BUCKET_QUANTITY / 4;
		} else {
			return sideToCenter [orientation];
		}
	}
	
	public int getCenterToSide (int orientation) {
		if (centerToSide [orientation] > BuildCraftCore.OIL_BUCKET_QUANTITY / 4) {
			return BuildCraftCore.OIL_BUCKET_QUANTITY / 4;
		} else {
			return centerToSide [orientation];
		}
	}
	
	public int getCenterIn () {
		if (centerIn > BuildCraftCore.OIL_BUCKET_QUANTITY / 2) {
			return BuildCraftCore.OIL_BUCKET_QUANTITY / 2;
		} else {
			return centerIn;
		}
	}
	
	public int getCenterOut () {
		if (centerOut > BuildCraftCore.OIL_BUCKET_QUANTITY / 2) {
			return BuildCraftCore.OIL_BUCKET_QUANTITY / 2;
		} else {
			return centerOut;
		}
	}
	

	@Override
	public int getLiquidQuantity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int empty (int quantityMax, boolean doEmpty) {
		return 0;
	}
	
}
