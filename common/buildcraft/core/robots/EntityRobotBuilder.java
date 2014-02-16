/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import buildcraft.builders.blueprints.BlueprintBuilder.SchematicBuilder;
import buildcraft.core.BlockIndex;
import buildcraft.core.Box;
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.BlockUtil;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityRobotBuilder extends EntityRobot implements IEntityAdditionalSpawnData {

	private Box box;

	public LinkedList<SchematicBuilder> targets = new LinkedList<SchematicBuilder>();
	public static int MAX_TARGETS = 20;
	public int wait = 0;

	public EntityRobotBuilder(World world) {
		super(world);
	}

	public EntityRobotBuilder(World world, Box box) {
		super(world);

		this.box = box;
	}

	@Override
	protected void init() {
		if (box != null) {
			//setDestination((int) box.centerX(), (int) box.centerY(), (int) box.centerZ());
		}

		super.init();
	}

	@Override
	public void writeSpawnData(ByteBuf data) {
		super.writeSpawnData(data);

		if (box == null) {
			box = new Box();
		}

		data.writeInt(box.xMin);
		data.writeInt(box.yMin);
		data.writeInt(box.zMin);
		data.writeInt(box.xMax);
		data.writeInt(box.yMax);
		data.writeInt(box.zMax);
	}

	@Override
	public void readSpawnData(ByteBuf data) {

		box = new Box();
		box.xMin = data.readInt();
		box.yMin = data.readInt();
		box.zMin = data.readInt();
		box.xMax = data.readInt();
		box.yMax = data.readInt();
		box.zMax = data.readInt();

		super.readSpawnData(data);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
	}

	@Override
	protected void move() {
		super.move();

		/*if (reachedDesination()) {
			BlockIndex newDesination = getNewDestination();

			if (newDesination != null) {
				setDestination(newDesination.x, newDesination.y, newDesination.z);
			}
		}*/
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (worldObj.isRemote) {
			return;
		}

		build();
		updateLaser();
	}

	protected BlockIndex getNewDestination() {

		Box movementBoundary = new Box();
		movementBoundary.initialize(box);
		movementBoundary.expand(1);

		Box moveArea = new Box();
		//moveArea.initialize((int) destX, (int) destY, (int) destZ, 1);

		List<BlockIndex> potentialDestinations = new ArrayList<BlockIndex>();
		for (BlockIndex blockIndex : moveArea.getBlocksInArea()) {

			if (BlockUtil.isSoftBlock(worldObj, blockIndex.x, blockIndex.y, blockIndex.z) && movementBoundary.contains(blockIndex)) {
				potentialDestinations.add(blockIndex);
			}
		}

		if (!potentialDestinations.isEmpty()) {

			int i = worldObj.rand.nextInt(potentialDestinations.size());
			return potentialDestinations.get(i);
		}

		return null;
	}

	protected void build() {
		updateWait();

		if (wait <= 0 && !targets.isEmpty()) {

			SchematicBuilder target = targets.peek();
			if (target.blockExists()) {
				target.markComplete();
				targets.pop();
			} else if (BlockUtil.canChangeBlock(worldObj, target.getX(), target.getY(), target.getZ())) {
				//System.out.printf("RobotChanging %d %d %d %s\n",target.x, target.y, target.z, target.mode);

				if (!worldObj.isAirBlock(target.getX(), target.getY(), target.getZ())) {
					BlockUtil.breakBlock(worldObj, target.getX(), target.getY(), target.getZ());
				} else {

					targets.pop();
					try {
						target.build(this);
					} catch (Throwable t) {
						target.markComplete();
						targets.pop();
						// Defensive code against errors in implementers
						t.printStackTrace();
						BCLog.logger.throwing("EntityRobot", "update", t);
					}
					if (!target.isComplete()) {
						targets.addLast(target);
					}
				}
			}
		}
	}

	public void updateWait() {

		if (targets.size() > 0)
			if (wait == 0) {
				wait = MAX_TARGETS - targets.size() + 2;
			} else {
				wait--;
			}
	}

	private void updateLaser() {

		/*if (laser == null)
			return;

		if (targets.size() > 0) {

			SchematicBuilder target = targets.getFirst();

			if (target != null) {
				laser.setPositions(new Position(posX, posY, posZ), new Position(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5));
				laser.show();
			}
		} else {
			laser.hide();
		}

		laser.pushPower(((float) targets.size()) / ((float) MAX_TARGETS) * 4F);*/
	}

	public boolean scheduleContruction(SchematicBuilder schematic) {
		if (!readyToBuild()) {
			return false;
		}

		if (schematic != null && !schematic.blockExists()) {
			return targets.add(schematic);
		}

		return false;
	}

	public boolean readyToBuild() {
		return targets.size() < MAX_TARGETS;
	}

	public boolean done() {
		return targets.isEmpty();
	}

	public void setBox(Box box) {
		this.box = box;
		//setDestination((int) box.centerX(), (int) box.centerY(), (int) box.centerZ());
	}

	@Override
	public void setDead() {
		/*if (laser != null) {
			laser.setDead();
		}*/
		super.setDead();
	}
}
