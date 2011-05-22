package net.minecraft.src.buildcraft.api;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.Utils;

public class Box {

	public int xMin, yMin, zMin;
	public int xMax, yMax, zMax;
	
	private EntityBlock lasers [];
	
	public Box (IAreaProvider area) {
		xMin = area.xMin();
		yMin = area.yMin();
		zMin = area.zMin();
		xMax = area.xMax();
		yMax = area.yMax();
		zMax = area.zMax();
	}
	
	public Box (NBTTagCompound nbttagcompound) {
		xMin = nbttagcompound.getInteger("xMin");
		yMin = nbttagcompound.getInteger("yMin");
		zMin = nbttagcompound.getInteger("zMin");
		xMax = nbttagcompound.getInteger("xMax");
		yMax = nbttagcompound.getInteger("yMax");
		zMax = nbttagcompound.getInteger("zMax");
	}
	
	public Position p1 () {
		return new Position (xMin, yMin, zMin);
	}
	
	public Position p2 () {
		return new Position (xMax, yMax, zMax);
	}
	
	public void createLasers (World world, LaserKind kind) {
		if (lasers == null) {
			lasers = Utils.createLaserBox(world, xMin, yMin, zMin, xMax, yMax,
					zMax, kind);
		}
	}
	
	public void deleteLasers () {
		if (lasers != null) {
			for (EntityBlock b : lasers) {
				b.setEntityDead();
			}
			
			lasers = null;
		}
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("xMin", xMin);
		nbttagcompound.setInteger("yMin", yMin);
		nbttagcompound.setInteger("zMin", zMin);
		
		nbttagcompound.setInteger("xMax", xMax);
		nbttagcompound.setInteger("yMax", yMax);
		nbttagcompound.setInteger("zMax", zMax);
		
	}
	
}
