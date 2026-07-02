/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe.behaviour;

import java.util.List;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

import ct.buildcraft.api.inventory.IItemTransactor;
import ct.buildcraft.api.mj.IMjConnector;
import ct.buildcraft.api.mj.IMjRedstoneReceiver;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.mj.MjCapabilityHelper;
import ct.buildcraft.api.transport.pipe.IFlowFluid;
import ct.buildcraft.api.transport.pipe.IFlowItems;
import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.PipeBehaviour;
import ct.buildcraft.api.transport.pipe.PipeEventHandler;
import ct.buildcraft.api.transport.pipe.PipeEventItem;
import ct.buildcraft.api.transport.pipe.PipeFlow;
import ct.buildcraft.lib.inventory.ItemTransactorHelper;
import ct.buildcraft.lib.inventory.filter.StackFilter;
import ct.buildcraft.lib.misc.BoundingBoxUtil;
import ct.buildcraft.lib.misc.VecUtil;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

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

    public PipeBehaviourObsidian(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        // Saves us from writing out the entity item's ID
        toWaitTicks = DROP_GAP;
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        return nbt;
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isClientSide()) {
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
        if (pipe.getHolder().getPipeWorld().isClientSide()) {
            return;
        }
        Direction openFace = getOpenFace();
        if (openFace != null) {
            trySuckEntity(entity, openFace, Long.MAX_VALUE, FluidAction.EXECUTE);
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

    protected AABB getSuckingBox(Direction openFace, int distance) {
        AABB bb = BoundingBoxUtil.makeAround(VecUtil.convertCenter(pipe.getHolder().getPipePos()), 0.4);
        switch (openFace) {
            default:
            case WEST:
                return bb.move(-distance, 0, 0).inflate(0.5, distance, distance);
            case EAST:
                return bb.move(distance, 0, 0).inflate(0.5, distance, distance);
            case DOWN:
                return bb.move(0, -distance, 0).inflate(distance, 0.5, distance);
            case UP:
                return bb.move(0, distance, 0).inflate(distance, 0.5, distance);
            case NORTH:
                return bb.move(0, 0, -distance).inflate(distance, distance, 0.5);
            case SOUTH:
                return bb.move(0, 0, distance).inflate(distance, distance, 0.5);
        }
    }

    /** @return The left over power */
    protected long trySuckEntity(Entity entity, Direction faceFrom, long power, FluidAction simulate) {
        if (entity.isRemoved() || entity instanceof LivingEntity) {
            return power;
        }

        Long tickPickupObj = entityDropTime.get(entity);
        if (tickPickupObj != null) {
            long tickPickup = tickPickupObj;
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
                double distance = Math.sqrt(entity.getOnPos().distSqr(pipe.getHolder().getPipePos()));
                powerReqPerItem = (long) (Math.max(1, distance) * POWER_PER_METRE + POWER_PER_ITEM);
                max = (int) (power / powerReqPerItem);
            }
            ItemStack extracted = transactor.extract(StackFilter.ALL, 1, max, simulate.simulate());
            if (!extracted.isEmpty()) {
                if (simulate.execute()) {
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
        entityDropTime.put(drop.getEntity(), pipe.getHolder().getPipeWorld().getGameTime() + DROP_GAP);
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        final long power = 512 * MjAPI.MJ;
        return power - receivePower(power, FluidAction.SIMULATE);
    }

    @Override
    public long receivePower(long microJoules, FluidAction simulate) {
        if (toWaitTicks > 0) {
            return microJoules;
        }
        Direction openFace = getOpenFace();
        if (openFace == null) {
            return microJoules;
        }

        for (int d = 1; d < 5; d++) {
            AABB aabb = getSuckingBox(openFace, d);
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

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        LazyOptional<T> cap = mjCaps.getCapability(capability, facing);
        if (cap.isPresent()) {
            return cap;
        }
        return super.getCapability(capability, facing);
    }
}
