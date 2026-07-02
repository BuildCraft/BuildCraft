/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 */
package ct.buildcraft.api.robots;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;

import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.core.IZone;
import ct.buildcraft.api.mj.MjBattery;

public abstract class EntityRobotBase extends LivingEntity implements Container, IFluidHandler {
    public static final int MAX_ENERGY = 100_000;
    public static final int SAFETY_ENERGY = MAX_ENERGY / 5;
    public static final int SHUTDOWN_ENERGY = 0;
    public static final long NULL_ROBOT_ID = Long.MAX_VALUE;

    protected EntityRobotBase(EntityType<? extends EntityRobotBase> entityType, Level level) {
        super(entityType, level);
    }

    public abstract void setItemInUse(ItemStack stack);

    public abstract void setItemActive(boolean active);

    public abstract boolean isMoving();

    public abstract DockingStation getLinkedStation();

    /** Override the robot's current AI from a gate statement (e.g., go to station, wake up). */
    public abstract void setMainAIOverride(AIRobot ai);

    public abstract RedstoneBoardRobot getBoard();

    public abstract void aimItemAt(float yaw, float pitch);

    public abstract void aimItemAt(int x, int y, int z);

    public void aimItemAt(BlockPos pos) {
        aimItemAt(pos.getX(), pos.getY(), pos.getZ());
    }

    public abstract float getAimYaw();

    public abstract float getAimPitch();

    public int getEnergy() {
        return (int) Math.min(Integer.MAX_VALUE, getBattery().getStored());
    }

    public abstract MjBattery getBattery();

    public abstract DockingStation getDockingStation();

    public abstract void dock(DockingStation station);

    public abstract void undock();

    public abstract IZone getZoneToWork();

    public abstract IZone getZoneToLoadUnload();

    public abstract boolean containsItems();

    public abstract boolean hasFreeSlot();

    public abstract void unreachableEntityDetected(Entity entity);

    public abstract boolean isKnownUnreachable(Entity entity);

    public abstract long getRobotId();

    public abstract IRobotRegistry getRegistry();

    public abstract void releaseResources();

    public abstract void onChunkUnload();

    /**
     * Receive an item from a nearby tile and return the remaining stack. Return {@link ItemStack#EMPTY} when fully
     * accepted.
     */
    public abstract ItemStack receiveItem(BlockEntity blockEntity, ItemStack stack);

    public abstract void setMainStation(DockingStation station);
}
