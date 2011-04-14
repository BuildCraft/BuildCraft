package net.minecraft.src.buildcraft;

import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityItem;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraft;

public class TilePipe extends TileEntity implements ITickListener {

	int i, j, k;
	
	class EntityData {		
		int orientation;
		EntityItem item;
		
		double posX, posY, posZ;
		
		public EntityData (EntityItem citem) {
			item = citem;
			
			posX = item.posX;
			posY = item.posY;
			posZ = item.posZ;
		}
	}
	
	LinkedList <EntityData> travelingEntities = new LinkedList <EntityData> ();		
	
	public TilePipe () {
		
	}
	
	public TilePipe (int ci, int cj, int ck) {
		i = ci;
		j = cj;
		k = ck;				
	}
	
	public void entityEntering (EntityItem item) {
		if (travelingEntities.size() == 0) {
			mod_BuildCraft.getInstance().registerTicksListener(this, 1);
		}
		
		travelingEntities.add(new EntityData (item));
	}

	public void tick(Minecraft minecraft) {
		int count = 0;
		
		for (EntityData data : travelingEntities) {
			count ++;
			data.posX = data.posX + 0.01;
			
			data.item.posX = data.posX;
			data.item.posY = data.posY;
			data.item.posZ = data.posZ;
						
			data.item.lastTickPosX = data.posX;
			data.item.lastTickPosY = data.posY;
			data.item.lastTickPosZ = data.posZ;
			
//			data.item.motionX = 0;
//			data.item.motionY = 0;
//			data.item.motionZ = 0;						
		}
		
	}
}
