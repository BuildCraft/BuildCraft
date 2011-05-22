package net.minecraft.src.buildcraft.transport;

import java.util.List;

import net.minecraft.src.ModLoader;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.World;
import net.minecraft.src.EntityItem;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.TileEntity;
import net.minecraft.src.IInventory;

import net.minecraft.src.buildcraft.core.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.TileEntityPickupFX;
import net.minecraft.src.buildcraft.core.EntityPassiveItem;
import net.minecraft.src.buildcraft.core.Utils;

public class TileObsidianPipe extends TileStonePipe {

	long lastSucking = 0;
	boolean lastPower = false;
	
	//Vacuum pipe can be connected only to ONE other Pipe or IInventory. Otherwise it won't do anything
	public Orientations getSuckingOrientation() {	
	
		Position pos = new Position(xCoord, yCoord, zCoord);
		int Connections_num = 0;
		
		Position target_pos = new Position(pos);
		
		for (int o = 0; o <= 5; ++o) {
			Position newPos = new Position(pos);
			newPos.orientation = Orientations.values()[o];
			newPos.moveForwards(1.0);
			
			TileEntity entity = world.getBlockTileEntity((int) newPos.x,
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
	
	public void checkPower (int i, int j, int k) {
		World w = CoreProxy.getWorld();
		boolean currentPower = w.isBlockIndirectlyGettingPowered(i, j, k);
		
		if (lastPower != currentPower) {
			suckItems(w, i, j, k);
		}
		
		lastPower = currentPower;
	}
	
	private AxisAlignedBB getSuckingBox(int i, int j, int k, Orientations orientation)
	{
		int x = 0;
		int y = 0;
		int z = 0;
		
		if(orientation != Orientations.Unknown)
		{
			if(orientation == Orientations.XPos)
			{
				x = -1;
			}
			if(orientation == Orientations.XNeg)
			{
				x = 1;
			}
			if(orientation == Orientations.YPos)
			{
				y = -1;
			}
			if(orientation == Orientations.YNeg)
			{
				y = 1;
			}
			if(orientation == Orientations.ZPos)
			{
				z = -1;
			}
			if(orientation == Orientations.ZNeg)
			{
				z = 1;
			}
			return AxisAlignedBB.getBoundingBoxFromPool(i - x, j - y, k - z, (i - x) + 1, (j - y) + 1, (float)((k - z) + 1));
		}
		return null;
	}
	
	public void suckItems(World world, int i, int j, int k)
	{
		World w = CoreProxy.getWorld();
		
		if (w.getWorldTime() - lastSucking < 25) {
			return;
		}
		
		TileObsidianPipe pipe = (TileObsidianPipe)world.getBlockTileEntity(i, j, k);	
			
		AxisAlignedBB box = getSuckingBox(i, j, k, pipe.getSuckingOrientation());
		if(box != null)
		{
			List list = world.getEntitiesWithinAABB(net.minecraft.src.EntityItem.class, box);
			
			for(int g = 0; g < list.size(); g++)
			{
				if(list.get(g) instanceof EntityItem)
				{
					EntityItem entityitem = (EntityItem)list.get(g);
					if(!entityitem.isDead)
					{
						pullItemIntoPipe(world, i, j, k, entityitem, pipe);
						lastSucking = w.getWorldTime();
						return;
					}
				}
			}
		}
	}
	
	public void pullItemIntoPipe(World world, int i, int j, int k, EntityItem item, TileObsidianPipe pipe)
	{
		Orientations orientation = pipe.getSuckingOrientation();
		if(orientation != Orientations.Unknown)
		{
			world.playSoundAtEntity(
					item,
					"random.pop",
					0.2F,
					((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			ModLoader.getMinecraftInstance().effectRenderer
					.addEffect(new TileEntityPickupFX(world, item, pipe));
			item.setEntityDead();
			EntityPassiveItem passive = new EntityPassiveItem(world, i + 0.5, j
					+ Utils.getPipeFloorOf(item.item), k + 0.5, item.item);
			world.entityJoinedWorld(passive);
			pipe.entityEntering(passive, orientation.reverse());
		}
	}
}
