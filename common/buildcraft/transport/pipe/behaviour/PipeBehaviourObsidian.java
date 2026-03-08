/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.transport.pipe.*;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.WeakHashMap;

public class PipeBehaviourObsidian extends PipeBehaviour implements IMjRedstoneReceiver {
    private static final long POWER_PER_ITEM = MjAPI.MJ / 2;
    private static final long POWER_PER_METRE = MjAPI.MJ / 4;

    private static final double INSERT_SPEED = 0.04;
    private static final int DROP_GAP = 20;

    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(this);
    /** Map of recently dropped item to the tick when it can be picked up */
    private final WeakHashMap<ItemEntity, Long> entityDropTime = new WeakHashMap<>();
    private int toWaitTicks = 0;

    public PipeBehaviourObsidian(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourObsidian(IPipe pipe, CompoundNBT nbt) {
        super(pipe, nbt);
        // Saves us from writing out the entity item's ID
        toWaitTicks = DROP_GAP;
    }

    @Override
    public CompoundNBT writeToNbt() {
        CompoundNBT nbt = super.writeToNbt();
        return nbt;
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isClientSide) {
            return;
        }
        toWaitTicks--;
        if (toWaitTicks > 0) {
            return;
        } else {
            toWaitTicks = 0;
        }
    }

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourObsidian);
    }

    @Override
    public void onEntityCollide(Entity entity) {
        if (pipe.getHolder().getPipeWorld().isClientSide) {
            return;
        }
        Direction openFace = getOpenFace();
        if (openFace != null) {
            trySuckEntity(entity, openFace, Long.MAX_VALUE, false);
        }
    }

    private Direction getOpenFace() {
        Direction openFace = null;
        for (Direction face : Direction.values()) {
            if (pipe.isConnected(face)) {
                if (openFace == null) {
                    openFace = face.getOpposite();
                } else {
                    return null;
                }
            }
        }
        return openFace;
    }

    protected AxisAlignedBB getSuckingBox(Direction openFace, int distance) {
        AxisAlignedBB bb = BoundingBoxUtil.makeAround(VecUtil.convertCenter(pipe.getHolder().getPipePos()), 0.4);
        switch (openFace) {
            default:
            case WEST:
//                return bb.offset(-distance, 0, 0).grow(0.5, distance, distance);
                return bb.move(-distance, 0, 0).inflate(0.5, distance, distance);
            case EAST:
//                return bb.offset(distance, 0, 0).grow(0.5, distance, distance);
                return bb.move(distance, 0, 0).inflate(0.5, distance, distance);
            case DOWN:
//                return bb.offset(0, -distance, 0).grow(distance, 0.5, distance);
                return bb.move(0, -distance, 0).inflate(distance, 0.5, distance);
            case UP:
//                return bb.offset(0, distance, 0).grow(distance, 0.5, distance);
                return bb.move(0, distance, 0).inflate(distance, 0.5, distance);
            case NORTH:
//                return bb.offset(0, 0, -distance).grow(distance, distance, 0.5);
                return bb.move(0, 0, -distance).inflate(distance, distance, 0.5);
            case SOUTH:
//                return bb.offset(0, 0, distance).grow(distance, distance, 0.5);
                return bb.move(0, 0, distance).inflate(distance, distance, 0.5);
        }
    }

    /** @return The left over power */
    protected long trySuckEntity(Entity entity, Direction faceFrom, long power, boolean simulate) {
        if (!entity.isAlive() || entity instanceof LivingEntity) {
            return power;
        }

        Long tickPickupObj = entityDropTime.get(entity);
        if (tickPickupObj != null) {
            long tickPickup = tickPickupObj;
//            long tickNow = pipe.getHolder().getPipeWorld().getTotalWorldTime();
            long tickNow = pipe.getHolder().getPipeWorld().getGameTime();
            if (tickNow < tickPickup) {
                return power;
            } else {
                entityDropTime.remove(entity);
            }
        }

        PipeFlow flow = pipe.getFlow();

        IFlowItems flowItem = flow instanceof IFlowItems ? (IFlowItems) flow : null;
        IFlowFluid flowFluid = flow instanceof IFlowFluid ? (IFlowFluid) flow : null;

        IItemTransactor transactor = ItemTransactorHelper.getTransactorForEntity(entity, faceFrom.getOpposite());

        if (flowItem != null) {
            long powerReqPerItem;
            int max;
            if (power == Long.MAX_VALUE) {
                max = Integer.MAX_VALUE;
                powerReqPerItem = 0;
            } else {
//                double distance = Math.sqrt(entity.getDistanceSqToCenter(pipe.getHolder().getPipePos()));
                double distance = Math.sqrt(entity.distanceToSqr(Vector3d.atCenterOf(pipe.getHolder().getPipePos())));
                powerReqPerItem = (long) (Math.max(1, distance) * POWER_PER_METRE + POWER_PER_ITEM);
                max = (int) (power / powerReqPerItem);
            }
            ItemStack extracted = transactor.extract(StackFilter.ALL, 1, max, simulate);
            if (!extracted.isEmpty()) {
                if (!simulate) {
                    flowItem.insertItemsForce(extracted, faceFrom, null, INSERT_SPEED);
                }
                return power - powerReqPerItem * extracted.getCount();
            }
        }
        if (flowFluid != null) {
            // TODO: Fluid extraction!
        }
        return power;
    }

    @PipeEventHandler
    public void onPipeDrop(PipeEventItem.Drop drop) {
//        entityDropTime.put(drop.getEntity(), pipe.getHolder().getPipeWorld().getTotalWorldTime() + DROP_GAP);
        entityDropTime.put(drop.getEntity(), pipe.getHolder().getPipeWorld().getGameTime() + DROP_GAP);
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(@javax.annotation.Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        final long power = 512 * MjAPI.MJ;
        return power - receivePower(power, true);
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        if (toWaitTicks > 0) {
            return microJoules;
        }
        Direction openFace = getOpenFace();
        if (openFace == null) {
            return microJoules;
        }

        for (int d = 1; d < 5; d++) {
            AxisAlignedBB aabb = getSuckingBox(openFace, d);
//            List<Entity> discoveredEntities = pipe.getHolder().getPipeWorld().getEntitiesWithinAABB(Entity.class, aabb);
            List<Entity> discoveredEntities = pipe.getHolder().getPipeWorld().getEntitiesOfClass(Entity.class, aabb);

            for (Entity entity : discoveredEntities) {
                long leftOver = trySuckEntity(entity, openFace, microJoules, simulate);
                if (leftOver < microJoules) {
                    return leftOver;
                }
            }
        }
        return microJoules - MjAPI.MJ;
    }

    @javax.annotation.Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        LazyOptional<T> cap = mjCaps.getCapability(capability, facing);
//        if (cap != null)
        if (cap.isPresent()) {
            return cap;
        }
        return super.getCapability(capability, facing);
    }
}
