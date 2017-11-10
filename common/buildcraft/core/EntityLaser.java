/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import buildcraft.api.core.Position;

public abstract class EntityLaser extends Entity {

	public static final ResourceLocation[] LASER_TEXTURES = new ResourceLocation[]{
			new ResourceLocation("buildcraftcore", "textures/laserBeams/laser_1.png"),
			new ResourceLocation("buildcraftcore", "textures/laserBeams/laser_2.png"),
			new ResourceLocation("buildcraftcore", "textures/laserBeams/laser_3.png"),
			new ResourceLocation("buildcraftcore", "textures/laserBeams/laser_4.png"),
			new ResourceLocation("buildcraftcore", "textures/laserBeams/stripes.png"),
			new ResourceLocation("buildcraftcore", "textures/laserBeams/blue_stripes.png")};

	public LaserData data = new LaserData();

	protected boolean needsUpdate = true;

	public EntityLaser(World world) {
		super(world);

		data.head = new Position(0, 0, 0);
		data.tail = new Position(0, 0, 0);
	}

	public EntityLaser(World world, Position head, Position tail) {

		super(world);

		data.head = head;
		data.tail = tail;

		setPositionAndRotation(head.x, head.y, head.z, 0, 0);
		setSize(10, 10);
	}

	@Override
	protected void entityInit() {
		preventEntitySpawning = false;
		noClip = true;
		isImmuneToFire = true;

		dataWatcher.addObject(8, 0);
		dataWatcher.addObject(9, 0);
		dataWatcher.addObject(10, 0);
		dataWatcher.addObject(11, 0);
		dataWatcher.addObject(12, 0);
		dataWatcher.addObject(13, 0);

		dataWatcher.addObject(14, (byte) 0);
	}

	@Override
	public void onUpdate() {

		if (data.head == null || data.tail == null) {
			return;
		}

		if (!worldObj.isRemote && needsUpdate) {
			updateDataServer();
			needsUpdate = false;
		}

		//if (worldObj.isRemote) {
		//	updateDataClient();
		//}

		boundingBox.minX = Math.min(data.head.x, data.tail.x);
		boundingBox.minY = Math.min(data.head.y, data.tail.y);
		boundingBox.minZ = Math.min(data.head.z, data.tail.z);

		boundingBox.maxX = Math.max(data.head.x, data.tail.x);
		boundingBox.maxY = Math.max(data.head.y, data.tail.y);
		boundingBox.maxZ = Math.max(data.head.z, data.tail.z);

		boundingBox.minX--;
		boundingBox.minY--;
		boundingBox.minZ--;

		boundingBox.maxX++;
		boundingBox.maxY++;
		boundingBox.maxZ++;

		data.update();
	}

	protected void updateDataClient() {
		data.head.x = decodeDouble(dataWatcher.getWatchableObjectInt(8));
		data.head.y = decodeDouble(dataWatcher.getWatchableObjectInt(9));
		data.head.z = decodeDouble(dataWatcher.getWatchableObjectInt(10));
		data.tail.x = decodeDouble(dataWatcher.getWatchableObjectInt(11));
		data.tail.y = decodeDouble(dataWatcher.getWatchableObjectInt(12));
		data.tail.z = decodeDouble(dataWatcher.getWatchableObjectInt(13));

		data.isVisible = dataWatcher.getWatchableObjectByte(14) == 1;
	}

	protected void updateDataServer() {
		dataWatcher.updateObject(8, encodeDouble(data.head.x));
		dataWatcher.updateObject(9, encodeDouble(data.head.y));
		dataWatcher.updateObject(10, encodeDouble(data.head.z));
		dataWatcher.updateObject(11, encodeDouble(data.tail.x));
		dataWatcher.updateObject(12, encodeDouble(data.tail.y));
		dataWatcher.updateObject(13, encodeDouble(data.tail.z));

		dataWatcher.updateObject(14, (byte) (data.isVisible ? 1 : 0));
	}

	public void setPositions(Position head, Position tail) {
		data.head = head;
		data.tail = tail;

		setPositionAndRotation(head.x, head.y, head.z, 0, 0);

		needsUpdate = true;
	}

	public void show() {
		data.isVisible = true;
		needsUpdate = true;
	}

	public void hide() {
		data.isVisible = false;
		needsUpdate = true;
	}

	public boolean isVisible() {
		return data.isVisible;
	}

	public abstract ResourceLocation getTexture();

	protected int encodeDouble(double d) {
		return (int) (d * 8192);
	}

	protected double decodeDouble(int i) {
		return i / 8192D;
	}

	// The read/write to nbt seem to be useless
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {

		double headX = nbt.getDouble("headX");
		double headY = nbt.getDouble("headZ");
		double headZ = nbt.getDouble("headY");
		data.head = new Position(headX, headY, headZ);

		double tailX = nbt.getDouble("tailX");
		double tailY = nbt.getDouble("tailZ");
		double tailZ = nbt.getDouble("tailY");
		data.tail = new Position(tailX, tailY, tailZ);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {

		nbt.setDouble("headX", data.head.x);
		nbt.setDouble("headY", data.head.y);
		nbt.setDouble("headZ", data.head.z);

		nbt.setDouble("tailX", data.tail.x);
		nbt.setDouble("tailY", data.tail.y);
		nbt.setDouble("tailZ", data.tail.z);
	}

	// Workaround for the laser's posY loosing it's precision e.g 103.5 becomes 104
	public Position renderOffset() {
		return new Position(data.head.x - posX, data.head.y - posY, data.head.z - posZ);
	}

	@Override
	public int getBrightnessForRender(float par1) {
		return 210;
	}
}
