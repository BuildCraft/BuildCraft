/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.LinkedList;

import buildcraft.core.proxy.CoreProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityFrame extends Entity {

	public static final ResourceLocation RED_LASER_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_1.png");
	public static final ResourceLocation STRIPES_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/stripes.png");

	@SideOnly(Side.CLIENT)
	public Icon texture;

	public double xSize = 0, ySize = 0, zSize = 0;

	public LinkedList<LaserData> lasers = new LinkedList<LaserData>();

	boolean needsUpdate = true;

	public ResourceLocation currentTexLocation = RED_LASER_TEXTURE;

	public EntityFrame(World world) {
		super (world);
	}

	public EntityFrame(World world, double xPos, double yPos, double zPos) {
		super(world);
		setPositionAndRotation(xPos, yPos, zPos, 0, 0);

		updateData();
	}

	public EntityFrame(World world, double x, double y, double z, double xSize, double ySize, double zSize) {
		super(world);
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;
		setPositionAndRotation(x, y, z, 0, 0);

		updateData();
	}

	@Override
	protected void entityInit() {
		preventEntitySpawning = false;
		noClip = true;
		isImmuneToFire = true;
		ignoreFrustumCheck = true;

		dataWatcher.addObject(8, Float.valueOf(0));
		dataWatcher.addObject(9, Float.valueOf(0));
		dataWatcher.addObject(10, Float.valueOf(0));
	}

	protected void updateDataClient() {
		float nxSize = dataWatcher.getWatchableObjectFloat(8);
		float nySize = dataWatcher.getWatchableObjectFloat(9);
		float nzSize = dataWatcher.getWatchableObjectFloat(10);

		if (nxSize != xSize || nySize != ySize || nzSize != zSize) {
			xSize = nxSize;
			ySize = nySize;
			zSize = nzSize;

			updateData();
		}
	}

	protected void updateDataServer() {
		dataWatcher.updateObject(8, Float.valueOf((float) xSize));
		dataWatcher.updateObject(9, Float.valueOf((float) ySize));
		dataWatcher.updateObject(10, Float.valueOf((float) zSize));
	}

	public void updateData() {
		needsUpdate = true;

		if (lasers != null) {
			lasers.clear();

			addLaser(0, 0, 0, 1, 0, 0);
			addLaser(1, 0, 0, 1, 1, 0);
			addLaser(1, 1, 0, 0, 1, 0);
			addLaser(0, 1, 0, 0, 0, 0);

			addLaser(0, 0, 1, 1, 0, 1);
			addLaser(1, 0, 1, 1, 1, 1);
			addLaser(1, 1, 1, 0, 1, 1);
			addLaser(0, 1, 1, 0, 0, 1);

			addLaser(0, 0, 0, 0, 0, 1);
			addLaser(1, 0, 0, 1, 0, 1);
			addLaser(0, 1, 0, 0, 1, 1);
			addLaser(1, 1, 0, 1, 1, 1);
		}

		boundingBox.minX = posX;
		boundingBox.minY = posY;
		boundingBox.minZ = posZ;

		boundingBox.maxX = posX + xSize;
		boundingBox.maxY = posY + ySize;
		boundingBox.maxZ = posZ + zSize;
	}

	@Override
	public void onUpdate() {
		if (CoreProxy.proxy.isSimulating(worldObj) && needsUpdate) {
			updateDataServer();
			needsUpdate = false;
		}

		if (CoreProxy.proxy.isRenderWorld(worldObj)) {
			updateDataClient();
		}
	}

	void addLaser (int x1, int y1, int z1, int x2, int y2, int z2) {

		double px1 = posX + xSize * x1;
		double py1 = posY + ySize * y1;
		double pz1 = posZ + zSize * z1;

		double px2 = posX + xSize * x2;
		double py2 = posY + ySize * y2;
		double pz2 = posZ + zSize * z2;

		if (px1 != px2 || py1 != py2 || pz1 != pz2) {
			LaserData d = new LaserData();

			d.head.x = px1;
			d.head.y = py1;
			d.head.z = pz1;

			d.tail.x = px2;
			d.tail.y = py2;
			d.tail.z = pz2;

			d.isVisible = true;

			d.update();

			lasers.add(d);
		}

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound data) {
		xSize = data.getDouble("xSize");
		ySize = data.getDouble("ySize");
		zSize = data.getDouble("zSize");

		setDead();
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound data) {
		data.setDouble("xSize", xSize);
		data.setDouble("ySize", ySize);
		data.setDouble("zSize", zSize);
	}
}
