/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.forge.ISpawnHandler;

public class EntityLaser extends Entity implements ISpawnHandler {
	
	protected Position head, tail;

	protected double renderSize = 0;
	protected double angleY = 0;
	protected double angleZ = 0;
	protected String texture;

	public EntityLaser(World world) {

		super(world);
	}

	public EntityLaser(World world, Position head, Position tail) {

		super(world);

		this.head = head;
		this.tail = tail;

		init();
	}

	protected void init() {

		preventEntitySpawning = false;
		noClip = true;
		isImmuneToFire = true;

		setPosition(head.x, head.y, head.z);
		setSize(10, 10);

		dataWatcher.addObject(8 , Integer.valueOf((int) head.x * 10000));
		dataWatcher.addObject(9 , Integer.valueOf((int) head.y * 10000));
		dataWatcher.addObject(10, Integer.valueOf((int) head.z * 10000));
		dataWatcher.addObject(11, Integer.valueOf((int) tail.x * 10000));
		dataWatcher.addObject(12, Integer.valueOf((int) tail.y * 10000));
		dataWatcher.addObject(13, Integer.valueOf((int) tail.z * 10000));
		
		dataWatcher.addObject(14, Byte.valueOf((byte) 0));
		
		updateGraphicData();
	}

	@Override
	public void writeSpawnData(DataOutputStream data) throws IOException {

		data.writeDouble(head.x);
		data.writeDouble(head.y);
		data.writeDouble(head.z);
		data.writeDouble(tail.x);
		data.writeDouble(tail.y);
		data.writeDouble(tail.z);
	}

	@Override
	public void readSpawnData(DataInputStream data) throws IOException {

		head = new Position(data.readDouble(), data.readDouble(), data.readDouble());
		tail = new Position(data.readDouble(), data.readDouble(), data.readDouble());
		init();
	}

	public void setPositions(Position head, Position tail) {
		
		this.head = head;
		this.tail = tail;
		
		setPosition(head.x, head.y, head.z);
		
		dataWatcher.updateObject(8 , Integer.valueOf((int) head.x * 10000));
		dataWatcher.updateObject(9 , Integer.valueOf((int) head.y * 10000));
		dataWatcher.updateObject(10, Integer.valueOf((int) head.z * 10000));
		dataWatcher.updateObject(11, Integer.valueOf((int) tail.x * 10000));
		dataWatcher.updateObject(12, Integer.valueOf((int) tail.y * 10000));
		dataWatcher.updateObject(13, Integer.valueOf((int) tail.z * 10000));
		
		updateGraphicData();
	}

	@Override
	public void setPosition(double x, double y, double z) {

		posX = x;
		posY = y;
		posZ = z;
	}

	public void updateGraphicData() {
		
		if (head == null || tail == null)
			return;
		
		updatePositions();
		
		//System.out.println(head + " " + tail);
		
		boundingBox.minX = Math.min(head.x, tail.x);
		boundingBox.minY = Math.min(head.y, tail.y);
		boundingBox.minZ = Math.min(head.z, tail.z);

		boundingBox.maxX = Math.max(head.x, tail.x);
		boundingBox.maxY = Math.max(head.y, tail.y);
		boundingBox.maxZ = Math.max(head.z, tail.z);

		boundingBox.minX--;
		boundingBox.minY--;
		boundingBox.minZ--;

		boundingBox.maxX++;
		boundingBox.maxY++;
		boundingBox.maxZ++;

		double dx = head.x - tail.x;
		double dy = head.y - tail.y;
		double dz = head.z - tail.z;

		renderSize = Math.sqrt(dx * dx + dy * dy + dz * dz);
		angleZ = 360 - (Math.atan2(dz, dx) * 180.0 / Math.PI + 180.0);
		dx = Math.sqrt(renderSize * renderSize - dy * dy);
		angleY = -Math.atan2(dy, dx) * 180 / Math.PI;
	}
	
	protected void updatePositions() {

		head.x = dataWatcher.getWatchableObjectInt(8) / 10000D;
		head.y = dataWatcher.getWatchableObjectInt(9) / 10000D;
		head.z = dataWatcher.getWatchableObjectInt(10) / 10000D;
		
		tail.x = dataWatcher.getWatchableObjectInt(11) / 10000D;
		tail.y = dataWatcher.getWatchableObjectInt(12) / 10000D;
		tail.z = dataWatcher.getWatchableObjectInt(13) / 10000D;
	}
	
	public void show() {
		dataWatcher.updateObject(14, Byte.valueOf((byte) 1));
	}
	
	public void hide() {
	}
	
	public boolean isVisible() {
		return dataWatcher.getWatchableObjectByte(14) == 0 ? false : true;
	}

	public void setTexture(String texture) {
		this.texture = texture;
	}

	public String getTexture() {
		return texture;
	}

	@Override
	protected void entityInit() {
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {}
}
