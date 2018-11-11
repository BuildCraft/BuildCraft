/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import java.util.List;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.common.capabilities.Capability;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.transport.pipe.IFlowFluid;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.VecUtil;

public class PipeBehaviourObsidian extends PipeBehaviour implements IMjRedstoneReceiver {
    private static final long POWER_PER_ITEM = MjAPI.MJ / 2;
    private static final long POWER_PER_METRE = MjAPI.MJ / 4;

    private static final double INSERT_SPEED = 0.04;
    private static final int DROP_GAP = 20;

    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(this);
    /** Map of recently dropped item to the tick when it can be picked up */
    private final WeakHashMap<EntityItem, Long> entityDropTime = new WeakHashMap<>();
    private int toWaitTicks = 0;

    public PipeBehaviourObsidian(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourObsidian(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        // Saves us from writing out the entity item's ID
        toWaitTicks = DROP_GAP;
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        return nbt;
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isRemote) {
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
    public boolean canConnect(EnumFacing face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourObsidian);
    }

    @Override
    public void onEntityCollide(Entity entity) {
        if (pipe.getHolder().getPipeWorld().isRemote) {
            return;
        }
        EnumFacing openFace = getOpenFace();
        if (openFace != null) {
            trySuckEntity(entity, openFace, Long.MAX_VALUE, false);
        }
    }

    private EnumFacing getOpenFace() {
        EnumFacing openFace = null;
        for (EnumFacing face : EnumFacing.VALUES) {
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

    protected AxisAlignedBB getSuckingBox(EnumFacing openFace, int distance) {
        AxisAlignedBB bb = BoundingBoxUtil.makeAround(VecUtil.convertCenter(pipe.getHolder().getPipePos()), 0.4);
        switch (openFace) {
            default:
            case WEST:
                return bb.offset(-distance, 0, 0).grow(0.5, distance, distance);
            case EAST:
                return bb.offset(distance, 0, 0).grow(0.5, distance, distance);
            case DOWN:
                return bb.offset(0, -distance, 0).grow(distance, 0.5, distance);
            case UP:
                return bb.offset(0, distance, 0).grow(distance, 0.5, distance);
            case NORTH:
                return bb.offset(0, 0, -distance).grow(distance, distance, 0.5);
            case SOUTH:
                return bb.offset(0, 0, distance).grow(distance, distance, 0.5);
        }
    }

    /** @return The left over power */
    protected long trySuckEntity(Entity entity, EnumFacing faceFrom, long power, boolean simulate) {
        if (entity.isDead || entity instanceof EntityLivingBase) {
            return power;
        }

        Long tickPickupObj = entityDropTime.get(entity);
        if (tickPickupObj != null) {
            long tickPickup = tickPickupObj;
            long tickNow = pipe.getHolder().getPipeWorld().getTotalWorldTime();
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
                double distance = Math.sqrt(entity.getDistanceSqToCenter(pipe.getHolder().getPipePos()));
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
        entityDropTime.put(drop.getEntity(), pipe.getHolder().getPipeWorld().getTotalWorldTime() + DROP_GAP);
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
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
        EnumFacing openFace = getOpenFace();
        if (openFace == null) {
            return microJoules;
        }

        for (int d = 1; d < 5; d++) {
            AxisAlignedBB aabb = getSuckingBox(openFace, d);
            List<Entity> discoveredEntities = pipe.getHolder().getPipeWorld().getEntitiesWithinAABB(Entity.class, aabb);

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
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        T cap = mjCaps.getCapability(capability, facing);
        if (cap != null) {
            return cap;
        }
        return super.getCapability(capability, facing);
    }
}
