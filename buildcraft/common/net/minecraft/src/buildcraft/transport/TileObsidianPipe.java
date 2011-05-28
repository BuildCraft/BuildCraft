package net.minecraft.src.buildcraft.transport;

import java.util.List;

import net.minecraft.src.ModLoader;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.EntityItem;
import net.minecraft.src.TileEntity;
import net.minecraft.src.IInventory;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.TileEntityPickupFX;
import net.minecraft.src.buildcraft.core.Utils;

public class TileObsidianPipe extends TileStonePipe {

	public TileObsidianPipe () {
		super ();
		latency = 25;
	}
	
	//Vacuum pipe can be connected only to ONE other Pipe or IInventory. Otherwise it won't do anything
	public Orientations getSuckingOrientation() {	
	
		Position pos = new Position(xCoord, yCoord, zCoord);
		int Connections_num = 0;
		
		Position target_pos = new Position(pos);
		
		for (int o = 0; o <= 5; ++o) {
			Position newPos = new Position(pos);
			newPos.orientation = Orientations.values()[o];
			newPos.moveForwards(1.0);
			
			TileEntity entity = worldObj.getBlockTileEntity((int) newPos.x,
						(int) newPos.y, (int) newPos.z);
						
			if(entity instanceof IPipeEntry || entity instanceof IInventory)
			{
				Connections_num++;
				if(Connections_num == 1) target_pos = new Position(newPos);
			}
		}
		if(Connections_num > 1 || Connections_num == 0)
		{
			return Orientations.Unknown;
		}
		return target_pos.orientation.reverse();
	}
	
	private AxisAlignedBB getSuckingBox(Orientations orientation)
	{		
		if(orientation == Orientations.Unknown)
		{
			return null;
		}
		Position p1 = new Position(xCoord, yCoord, zCoord, orientation);
		Position p2 = new Position(xCoord, yCoord, zCoord, orientation);

		switch (orientation) {
		case XPos:
			p1.x += 1;
			p2.x += 2;
			break;
		case XNeg:
			p1.x += 0;
			p2.x -= 1;
			break;
		case YPos:
		case YNeg:
			p1.x += 2;
			p2.x -= 1;
			p1.z += 2;
			p2.z -= 1;
			break;
		case ZPos:
			p1.z += 1;
			p2.z += 2;
			break;
		case ZNeg:
			p1.z += 0;
			p2.z -= 1;
			break;
		}

		switch (orientation) {
		case XPos:
		case XNeg:
			p1.y += 2;
			p2.y -= 1;
			p1.z += 2;
			p2.z -= 1;
			break;
		case YPos:
			p1.y += 2;
			p2.y += 1;
			break;
		case YNeg:
			p1.y += 0;
			p2.y -= 1;
			break;
		case ZPos:
		case ZNeg:
			p1.y += 2;
			p2.y -= 1;
			p1.x += 2;
			p2.x -= 1;
			break;
		}

		Position min = p1.min(p2);
		Position max = p1.max(p2);

		return AxisAlignedBB.getBoundingBoxFromPool(min.x, min.y, min.z, max.x,
				max.y, max.z);	
	}
	
	protected void doWork () {		
		AxisAlignedBB box = getSuckingBox(getSuckingOrientation());
		if(box != null)
		{
			@SuppressWarnings("rawtypes")
			List list = worldObj.getEntitiesWithinAABB(net.minecraft.src.EntityItem.class, box);
			
			for(int g = 0; g < list.size(); g++)
			{
				if(list.get(g) instanceof EntityItem)
				{
					EntityItem entityitem = (EntityItem)list.get(g);
					if(!entityitem.isDead)
					{
						pullItemIntoPipe(entityitem);
						return;
					}
				}
			}
		}
	}
	
	public void pullItemIntoPipe(EntityItem item)
	{
		Orientations orientation = getSuckingOrientation();
		if(orientation != Orientations.Unknown)
		{
			worldObj.playSoundAtEntity(
					item,
					"random.pop",
					0.2F,
					((worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			ModLoader.getMinecraftInstance().effectRenderer
					.addEffect(new TileEntityPickupFX(worldObj, item, this));
			item.setEntityDead();
			EntityPassiveItem passive = new EntityPassiveItem(worldObj, xCoord + 0.5, yCoord
					+ Utils.getPipeFloorOf(item.item), zCoord + 0.5, item.item);
			worldObj.entityJoinedWorld(passive);
			entityEntering(passive, orientation.reverse());
		}
	}
}
