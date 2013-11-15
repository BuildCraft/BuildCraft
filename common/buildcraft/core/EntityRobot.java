/**
 * Copyright (c) SpaceToad, 2011-2012 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;


import buildcraft.api.core.Position;
import buildcraft.builders.blueprints.BlueprintBuilder.SchematicBuilder;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.BlockUtil;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityRobot extends Entity implements IEntityAdditionalSpawnData {

	private Box box;
	private int destX, destY, destZ;
	EntityEnergyLaser laser;
	public LinkedList<SchematicBuilder> targets = new LinkedList<SchematicBuilder>();
	public static int MAX_TARGETS = 20;
	public int wait = 0;

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

		setLocationAndAngles(destX, destY, destZ, 0, 0);

		laser = new EntityEnergyLaser(worldObj, new Position(posX, posY, posZ), new Position(posX, posY, posZ));
		worldObj.spawnEntityInWorld(laser);
	}

	@Override
	public void writeSpawnData(ByteArrayDataOutput data) {

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
	public void readSpawnData(ByteArrayDataInput data) {

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
	protected void entityInit() {
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
	}

	@Override
	public void onUpdate() {
		if (CoreProxy.proxy.isRenderWorld(worldObj))
			return;

		move();
		build();
		updateLaser();
	}

	protected void move() {

		setPosition(posX + motionX, posY + motionY, posZ + motionZ);

		if (reachedDesination()) {

			BlockIndex newDesination = getNewDestination();
			if (newDesination != null) {
				setDestination(newDesination.x, newDesination.y, newDesination.z);
			}

		}

	}

	protected BlockIndex getNewDestination() {

		Box movementBoundary = new Box();
		movementBoundary.initialize(box);
		movementBoundary.expand(1);

		Box moveArea = new Box();
		moveArea.initialize(destX, destY, destZ, 1);

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

	protected void setDestination(int x, int y, int z) {

		destX = x;
		destY = y;
		destZ = z;

		motionX = (destX - posX) / 75 * (laser.getPowerAverage() / 2 + 1);
		motionY = (destY - posY) / 75 * (laser.getPowerAverage() / 2 + 1);
		motionZ = (destZ - posZ) / 75 * (laser.getPowerAverage() / 2 + 1);
	}

	protected boolean reachedDesination() {

		if (getDistance(destX, destY, destZ) <= .2)
			return true;

		return false;
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
						target.build(CoreProxy.proxy.getBuildCraftPlayer(worldObj, target.getX(), target.getY() + 2, target.getZ()));
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

		if (laser == null)
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

		laser.pushPower(((float) targets.size()) / ((float) MAX_TARGETS) * 4F);
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
		setDestination((int) box.centerX(), (int) box.centerY(), (int) box.centerZ());
	}

	@Override
	public void setDead() {
		if (laser != null) {
			laser.setDead();
		}
		super.setDead();
	}
}
