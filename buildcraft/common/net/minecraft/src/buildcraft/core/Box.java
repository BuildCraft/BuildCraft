package net.minecraft.src.buildcraft.core;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.api.IBox;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Position;

public class Box implements IBox {

	public int xMin, yMin, zMin;
	public int xMax, yMax, zMax;
	
	private EntityBlock lasers [];
	
	public Box (int [] data, int firstIndex) {
		this.xMin = data [firstIndex];
		this.yMin = data [firstIndex + 1];
		this.zMin = data [firstIndex + 2];
		this.xMax = data [firstIndex + 3];
		this.yMax = data [firstIndex + 4];
		this.zMax = data [firstIndex + 5];
	}
	
	public Box (int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
		this.xMin = xMin;
		this.yMin = yMin;
		this.zMin = zMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.zMax = zMax;
	}
	
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
	
	public static int packetSize () {
		return 6;
	}
	
	public void setData (int [] data, int firstIndex) {
		data [firstIndex] = this.xMin;
		data [firstIndex + 1] = this.yMin;
		data [firstIndex + 2] = this.zMin; 
		data [firstIndex + 3] = this.xMax;
		data [firstIndex + 4] = this.yMax;
		data [firstIndex + 5] = this.zMax;
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
				APIProxy.removeEntity(b);
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
	
	public int sizeX () {
		return xMax - xMin + 1;
	}
	
	public int sizeY () {
		return yMax - yMin + 1;
	}

	public int sizeZ () {
		return zMax - zMin + 1;
	}

}
