/**
 * Copyright (c) SpaceToad, 2011-2012
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.src.Entity;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.BptSlotInfo;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.BptSlot.Mode;
import net.minecraft.src.forge.ISpawnHandler;

public class EntityRobot extends Entity implements ISpawnHandler {

	private Box box;
	private int destX, destY, destZ;

	EntityEnergyLaser laser;

	public LinkedList<Action> targets = new LinkedList<Action>();
	public static int MAX_TARGETS = 20;
	public int wait = 0;

	private class Action {

		public Action(BptSlot slot, BptContext context) {
			this.slot = slot;
			this.context = context;
		}

		public Action(BptBuilderBase builder) {
			this.builder = builder;
		}

		BptSlot slot;
		BptBuilderBase builder;
		BptContext context;
	}

	public EntityRobot(World world) {
		super(world);
	}

	public EntityRobot(World world, Box box) {

		super(world);

		this.box = box;
		init();
	}

	protected void init() {

		destX = (int) box.centerX();
		destY = (int) box.centerY();
		destZ = (int) box.centerZ();

		motionX = 0;
		motionY = 0;
		motionZ = 0;

		setPosition(destX, destY, destZ);

		if (!APIProxy.isClient(worldObj)) {
		
			laser = new EntityEnergyLaser(worldObj, new Position(posX, posY, posZ), new Position(posX, posY, posZ));
			worldObj.spawnEntityInWorld(laser);
		}
	}

	@Override
	public void writeSpawnData(DataOutputStream data) throws IOException {

		data.writeInt(box.xMin);
		data.writeInt(box.yMin);
		data.writeInt(box.zMin);
		data.writeInt(box.xMax);
		data.writeInt(box.yMax);
		data.writeInt(box.zMax);
	}

	@Override
	public void readSpawnData(DataInputStream data) throws IOException {

		box = new Box();
		box.xMin = data.readInt();
		box.yMin = data.readInt();
		box.zMin = data.readInt();
		box.xMax = data.readInt();
		box.yMax = data.readInt();
		box.zMax = data.readInt();

		init();
	}

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void onUpdate() {

		move();
		build();
		updateLaser();
	}

	protected void move() {

		//System.out.println("move: " + new Position(motionX, motionY, motionZ));
		setPosition(posX + motionX, posY + motionY, posZ + motionZ);

		if (APIProxy.isClient(worldObj))
			return;

		if (reachedDesination()) {

			BlockIndex newDesination = getNewDesination();
			if (newDesination != null) {
				setDestination(newDesination.i, newDesination.j, newDesination.k);
			}
		
		}

	}

	protected BlockIndex getNewDesination() {

		Box movementBoundary = new Box();
		movementBoundary.initialize(box);
		movementBoundary.expand(1);

		Box moveArea = new Box();
		moveArea.initialize(destX, destY, destZ, 1);

		List<BlockIndex> potentialDestinations = new ArrayList<BlockIndex>();
		for (BlockIndex blockIndex : moveArea.getBlocksInArea()) {

			if (BuildCraftAPI.softBlock(blockIndex.getBlockId(worldObj)) && movementBoundary.contains(blockIndex)) {
				potentialDestinations.add(blockIndex);
			}
		}

		if (!potentialDestinations.isEmpty()) {

			int i = worldObj.rand.nextInt(potentialDestinations.size());
			return potentialDestinations.get(i);
		}

		return null;
	}

	protected void setDestination(int x, int y, int z) {

		destX = x;
		destY = y;
		destZ = z;

		// TODO: apply power modifier
		motionX = new BigDecimal((float) (destX - posX) / 75 * 1).setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
		motionY = new BigDecimal((float) (destY - posY) / 75 * 1).setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
		motionZ = new BigDecimal((float) (destZ - posZ) / 75 * 1).setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
		
		//System.out.println("setDest: " + new Position(motionX, motionY, motionZ));
	}

	protected boolean reachedDesination() {

		if (getDistance(destX, destY, destZ) <= .2)
			return true;

		return false;
	}

	protected void build() {

		updateWait();

		// TODO: possible rewrite
		if (targets.size() > 0) {

			Action a = targets.getFirst();
			if (a.slot != null) {

				BptSlot target = a.slot;
				if (wait <= 0) {

					if (target.mode == Mode.ClearIfInvalid) {

						if (!target.isValid(a.context))
							worldObj.setBlockAndMetadataWithNotify(target.x, target.y, target.z, 0, 0);

					} else if (target.stackToUse != null) {

						worldObj.setBlockWithNotify(target.x, target.y, target.z, 0);
						target.stackToUse.getItem().onItemUse(target.stackToUse,
								BuildCraftAPI.getBuildCraftPlayer(worldObj), worldObj, target.x, target.y - 1,
								target.z, 1);
					} else {

						try {
							target.buildBlock(a.context);
						} catch (Throwable t) {
							// Defensive code against errors in implementers
							t.printStackTrace();
							ModLoader.getLogger().throwing("EntityRobot", "update", t);
						}
					}

					targets.pop();
				}

			} else if (a.builder != null) {
				a.builder.postProcessing(worldObj);
				targets.pop();
			}
		}
	}

	public void updateWait() {

		if (targets.size() > 0)
			if (wait == 0)
				wait = MAX_TARGETS - targets.size() + 2;
			else
				wait--;
	}

	private void updateLaser() {
		
		if (APIProxy.isClient(worldObj))
			return;
			
		if (targets.size() > 0) {
			
			Action a = targets.getFirst();
			BptSlotInfo target = a.slot;

			laser.setPositions(new Position(posX, posY, posZ), new Position(target.x + 0.5, target.y + 0.5, target.z + 0.5));
			laser.show();
		}
		else {
			laser.hide();
		}
		
		laser.pushPower(((float) targets.size()) / ((float) MAX_TARGETS) * 4F);
	}

	public void scheduleContruction(BptSlot slot, BptContext context) {

		if (slot != null) {
			targets.add(new Action(slot, context));
			
		}
	}

	public void markEndOfBlueprint(BptBuilderBase builder) {
		targets.add(new Action(builder));
	}

	public boolean readyToBuild() {
		return targets.size() < MAX_TARGETS;
	}

	public boolean done() {
		return targets.isEmpty();
	}

	public void setBox(Box box) {

		this.box = box;
		setDestination((int) box.centerX(), (int) box.centerY(), (int) box.centerZ());
	}

	@Override
	public void setDead() {

		if (laser != null)
			laser.setDead();

		super.setDead();
	}

}
