package net.minecraft.src.buildcraft;

import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityItem;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class TilePipe extends TileEntity implements ITickListener {
	World world;
	
	class EntityData {	
		boolean toCenter = true;
		int orientation;
		EntityItem item;
		
		Position position;
		TilePipe destination;
		
		public EntityData (EntityItem citem, int orientation) {
			item = citem;
			
			position = new Position (item.posX, item.posY, item.posZ, orientation);
		}
	}
	
	LinkedList <EntityData> travelingEntities = new LinkedList <EntityData> ();		
	
	public TilePipe () {
		world = ModLoader.getMinecraftInstance().theWorld;
	}
	
	public TilePipe (int ci, int cj, int ck) {
		this ();
		xCoord = ci;
		yCoord = cj;
		zCoord = ck;	
	}
	
	public void entityEntering (EntityItem item, int orientation) {
		if (travelingEntities.size() == 0) {
			mod_BuildCraft.getInstance().registerTicksListener(this, 1);
		}
		
		travelingEntities.add(new EntityData (item, orientation));
	}

	public LinkedList <TilePipe> getPossibleMovements (Position pos) {
		LinkedList <TilePipe> result = new LinkedList <TilePipe> ();
		
		Position oppositePos = new Position (pos);
		oppositePos.reverseOrientation();
		
		// FIXME: This currently volontary avoid 0 and 1 (top and bottom)
		for (int o = 2; o <= 5; ++o) {
			if (o != oppositePos.orientation) {
				Position newPos = new Position (pos);
				newPos.orientation = o;
				newPos.moveForwards(1.0);
				
				TileEntity entity = world.getBlockTileEntity((int) newPos.i, (int) newPos.j, (int) newPos.k);
				
				if (entity != null && entity instanceof TilePipe) {
					result.add((TilePipe) entity);
				}
			}
		}
		
		return result;
	}
	
	public void tick(Minecraft minecraft) {
		int count = 0;
		
		LinkedList <EntityData> toRemove = new LinkedList <EntityData> ();				
		
		for (EntityData data : travelingEntities) {
			count ++;		
			data.position.moveForwards(0.01);
						
		    data.item.posX = data.position.i;
			data.item.posY = data.position.j;
			data.item.posZ = data.position.k;
						
			if (data.toCenter && middleReached(data)) {
				data.toCenter = false;
				
				LinkedList<TilePipe> listOfPossibleMovements = getPossibleMovements(new Position(
						xCoord, yCoord, zCoord, data.position.orientation));
				
				if (listOfPossibleMovements.size() == 0) {
					data.item.setEntityDead();
					toRemove.add(data);
					
					Position motion = new Position (0, 0, 0, data.position.orientation);
					motion.moveForwards(0.1);
										
					EntityItem entityitem = new EntityItem(world, (float) data.position.i,
							(float) data.position.j, (float) data.position.k, data.item.item);

					float f3 = 0.00F;
					entityitem.motionX = (float) world.rand.nextGaussian() * f3 + motion.i;
					entityitem.motionY = (float) world.rand.nextGaussian() * f3
							+ + motion.j;
					entityitem.motionZ = (float) world.rand.nextGaussian() * f3 + + motion.k;
					world.entityJoinedWorld(entityitem);
				} else {					
					int i = world.rand.nextInt(listOfPossibleMovements.size());
					
					data.destination = listOfPossibleMovements.get(i);										
					
					data.position.orientation = Utils.getOrientation(
							new Position(xCoord, yCoord, zCoord, 0),
							new Position(data.destination.xCoord,
									data.destination.yCoord, data.destination.zCoord, 0));															
				}				
		    } else if (!data.toCenter && endReached (data)) {
		    	toRemove.add(data);
		    	data.destination.entityEntering(data.item, data.position.orientation);
		    }
		}	
		
		travelingEntities.removeAll(toRemove);			
	}
	
	public boolean middleReached (EntityData entity) {
		//  If at least two coordinates are in the middle, then that's it (the 
		//  third one is a constant placed at + 0.4
		
		int nbCenter = 0;
		
		if (Math.abs(xCoord + 0.5 - entity.position.i) < 0.011) {
			nbCenter++;
		}
		
		if (Math.abs (yCoord + 0.5 - entity.position.j) < 0.011) {
			nbCenter++;
		}
		
		if (Math.abs (zCoord + 0.5 - entity.position.k) < 0.011) {
			nbCenter++;
		}
		
		return nbCenter >= 2;
	}
	
	public boolean endReached (EntityData entity) {
		return entity.position.i > xCoord + 1.0 
		|| entity.position.i < xCoord
		|| entity.position.j > yCoord + 1.0
		|| entity.position.j < yCoord
		|| entity.position.k > zCoord + 1.0
		|| entity.position.k < zCoord;
	}
}
