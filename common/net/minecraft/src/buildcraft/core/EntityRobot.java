/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import java.util.LinkedList;

import net.minecraft.src.Entity;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.BptSlotInfo;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.core.BptSlot.Mode;

public class EntityRobot extends Entity {

	Box box;

	int destX, destY, destZ;
	double vX, vY, vZ;

	EntityEnergyLaser laser;

	public int wait = 0;

	private class Action {
		public Action (BptSlot slot, BptContext context) {
			this.slot = slot;
			this.context = context;
		}

		public Action (BptBuilderBase builder) {
			this.builder = builder;
		}

		BptSlot slot;
		BptBuilderBase builder;
		BptContext context;
	}

	public LinkedList <Action> targets = new LinkedList <Action> ();

	public static int MAX_SIZE = 20;

	/**
	 * Constructor for Forge Netcode
	 * @param world
	 * @param xPos
	 * @param yPos
	 * @param zPos
	 */
	public EntityRobot(World world, double xPos, double yPos, double zPos) {
		super(world);		
		setPosition(xPos, yPos, zPos);
	}
	public EntityRobot(World world, Box box) {
		super(world);

		this.box = box;

		destX = (int) box.centerX();
		destY = (int) box.centerY();
		destZ = (int) box.centerZ();

		vX = 0;
		vY = 0;
		vZ = 0;

		setPosition(destX + 0.5, destY + 0.5, destZ + 0.5);
		laser = new EntityEnergyLaser(worldObj);
		laser.hidden = true;
		laser.setPositions(posX, posY, posZ, posX, posY, posZ);

		world.spawnEntityInWorld(laser);
	}

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub

	}

	public void update () {
		moveRobot();
		updateWait();

		if (targets.size() > 0) {
			Action a = targets.getFirst();

			if (a.slot != null) {
				BptSlot target = a.slot;

				if (wait <= 0) {
					if (target.mode == Mode.ClearIfInvalid) {
						if (!target.isValid(a.context))
							worldObj.setBlockAndMetadataWithNotify(target.x,
									target.y, target.z, 0, 0);
					} else if (target.stackToUse != null) {
						worldObj.setBlockWithNotify(target.x, target.y,
								target.z, 0);
						target.stackToUse.getItem().onItemUse(
								target.stackToUse,
								BuildCraftAPI.getBuildCraftPlayer(worldObj),
								worldObj, target.x, target.y - 1, target.z,
								1);
					} else
						try {
							target.buildBlock (a.context);
						} catch (Throwable t) {
							// Defensive code against errors in implementers
							t.printStackTrace();
							ModLoader.getLogger().throwing("EntityRobot",
									"update", t);
						}

					targets.pop();
				}
			} else if (a.builder != null) {
				a.builder.postProcessing(worldObj);
				targets.pop();
			}
		} else
			laser.hidden = true;

		updateLaser();
	}

	public void moveRobot () {
		if (Math.abs(posX - destX - 0.5) < 0.1 && Math.abs(posY - destY - 0.5) < 0.1 && Math.abs(posZ - destZ - 0.5) < 0.1) {
			LinkedList <BlockIndex> potentialDirs = new LinkedList <BlockIndex> ();

			for (int x = destX - 1; x <= destX + 1; ++x)
				for (int y = destY - 1; y <= destY + 1; ++y)
					for (int z = destZ - 1; z <= destZ + 1; ++z)
						if (x >= box.xMin - 1 && x <= box.xMax + 1
								&& y >= box.yMin - 1 && y <= box.yMax + 1
								&& z >= box.zMin - 1 && z <= box.zMax + 1
								&& BuildCraftAPI.softBlock(worldObj.getBlockId(x, y, z)))
							potentialDirs.add(new BlockIndex(x, y, z));

			if (potentialDirs.size() > 0) {
				BlockIndex b = potentialDirs.get(worldObj.rand.nextInt(potentialDirs.size()));
				setDestination (b.i, b.j, b.k);
			}
		} else
			setPosition(posX + vX * (laser.getPowerAverage() + 1), posY + vY
					* (laser.getPowerAverage() + 1),
					posZ + vZ * (laser.getPowerAverage() + 1));
	}

	public void updateWait () {
		if (targets.size() > 0)
			if (wait == 0)
				wait = MAX_SIZE - targets.size() + 2;
			else
				wait--;
	}

	private void updateLaser () {
		BptSlotInfo target = null;

		if (targets.size() > 0) {
			Action a = targets.getFirst();
			target = a.slot;
		}

		if (target != null)
			laser.setPositions (posX, posY, posZ, target.x + 0.5, target.y + 0.5, target.z + 0.5);
		else
			laser.hidden = true;

		laser.pushPower (((float) targets.size ()) / ((float) MAX_SIZE) * 4F);
	}

	public void scheduleContruction (BptSlot slot, BptContext context) {
		if (slot != null) {
			targets.add(new Action (slot, context));
			laser.hidden = false;
		}
	}

	public void markEndOfBlueprint (BptBuilderBase builder) {
		targets.add(new Action (builder));
	}

	public boolean readyToBuild () {
		return targets.size() < MAX_SIZE;
	}

	public boolean done () {
		return targets.size() == 0;
	}

	public void setBox(Box box) {
		this.box = box;

		setDestination((int) box.centerX(), (int) box.centerY(), (int) box.centerZ());
	}

	public void setDestination (int x, int y, int z) {
		destX = x;
		destY = y;
		destZ = z;

		double dX = destX - posX + 0.5;
		double dY = destY - posY + 0.5;
		double dZ = destZ - posZ + 0.5;

		double size = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

		vX = dX / size / 50.0;
		vY = dY / size / 50.0;
		vZ = dZ / size / 50.0;
	}

	@Override
	public void setDead() {
		if (laser != null)
			laser.setDead();

		super.setDead();
	}
}
