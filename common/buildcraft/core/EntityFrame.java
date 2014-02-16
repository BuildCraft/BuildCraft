/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.LinkedList;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityFrame extends Entity {

	private static final ResourceLocation RED_LASER_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_1.png");
	private static final ResourceLocation STRIPES_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/stripes.png");

	public enum Kind {
		RED_LASER,
		STRIPES
	}

	@SideOnly(Side.CLIENT)
	public IIcon texture;

	public Kind currentKind = Kind.RED_LASER;

	public double xSize = 0, ySize = 0, zSize = 0;

	public LinkedList<LaserData> lasers = new LinkedList<LaserData>();

	boolean needsUpdate = true;

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

	public EntityFrame(World world, Box box) {
		this(world, box.xMin + 0.5F, box.yMin + 0.5F, box.zMin + 0.5F, box
				.sizeX() - 0.5F, box.sizeY() - 0.5F, box.sizeZ() - 0.5F);

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
		dataWatcher.addObject(11, Integer.valueOf(0));
	}

	protected void updateDataClient() {
		float nxSize = dataWatcher.getWatchableObjectFloat(8);
		float nySize = dataWatcher.getWatchableObjectFloat(9);
		float nzSize = dataWatcher.getWatchableObjectFloat(10);
		int nKind = dataWatcher.getWatchableObjectInt(11);

		if (nxSize != xSize || nySize != ySize || nzSize != zSize || nKind != currentKind.ordinal()) {
			xSize = nxSize;
			ySize = nySize;
			zSize = nzSize;
			currentKind = Kind.values()[nKind];

			updateData();
		}
	}

	protected void updateDataServer() {
		dataWatcher.updateObject(8, Float.valueOf((float) xSize));
		dataWatcher.updateObject(9, Float.valueOf((float) ySize));
		dataWatcher.updateObject(10, Float.valueOf((float) zSize));
		dataWatcher.updateObject(11, currentKind.ordinal());
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
		if (!worldObj.isRemote && needsUpdate) {
			updateDataServer();
			needsUpdate = false;
		}

		if (worldObj.isRemote) {
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

	public void setKind (Kind kind) {
		currentKind = kind;
		needsUpdate = true;
	}

	public ResourceLocation getTexture () {
		switch (currentKind) {
		case RED_LASER:
			return RED_LASER_TEXTURE;
		case STRIPES:
			return STRIPES_TEXTURE;
		}

		return null;
	}
}
