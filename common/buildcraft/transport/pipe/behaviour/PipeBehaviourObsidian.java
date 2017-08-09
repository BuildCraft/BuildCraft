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
import buildcraft.api.mj.MjBattery;
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
    private static final double INSERT_SPEED = 0.04;
    private static final int DROP_GAP = 20;

    private final MjBattery battery = new MjBattery(256 * MjAPI.MJ);
    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(this);
    /** Map of recently dropped item to the tick when it can be picked up */
    private final WeakHashMap<EntityItem, Long> entityDropTime = new WeakHashMap<>();
    private int toWaitTicks = 0;

    public PipeBehaviourObsidian(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourObsidian(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        battery.deserializeNBT(nbt.getCompoundTag("battery"));
        // Saves us from writing out the entity item's ID
        toWaitTicks = DROP_GAP;
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("battery", battery.serializeNBT());
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
        if (battery.getStored() > 0) {
            EnumFacing openFace = getOpenFace();
            if (openFace != null) {
                for (int distance = 1; distance < 5; distance++) {
                    if (suckEntity(openFace, distance)) {
                        return;
                    }
                }
            }
            battery.extractPower(0, MjAPI.MJ / 2);
        }
    }

    @Override
    public void onEntityCollide(Entity entity) {
        if (pipe.getHolder().getPipeWorld().isRemote) {
            return;
        }
        EnumFacing openFace = getOpenFace();
        if (openFace != null) {
            trySuckEntity(entity, openFace, false);
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

    protected boolean suckEntity(EnumFacing openFace, int distance) {
        AxisAlignedBB aabb = getSuckingBox(openFace, distance);

        List<Entity> discoveredEntities = pipe.getHolder().getPipeWorld().getEntitiesWithinAABB(Entity.class, aabb);

        for (Entity entity : discoveredEntities) {
            if (trySuckEntity(entity, openFace, true)) {
                return true;
            }
        }

        return false;
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

    /** Attempts to pull in the given */
    protected boolean trySuckEntity(Entity entity, EnumFacing faceFrom, boolean requiresPower) {
        if (entity.isDead || (requiresPower && battery.getStored() < MjAPI.MJ)) {
            return false;
        }
        if (entity instanceof EntityLivingBase) {
            return false;
        }

        Long tickPickupObj = entityDropTime.get(entity);
        if (tickPickupObj != null) {
            long tickPickup = tickPickupObj;
            long tickNow = pipe.getHolder().getPipeWorld().getTotalWorldTime();
            if (tickNow < tickPickup) {
                return false;
            } else {
                entityDropTime.remove(entity);
            }
        }

        PipeFlow flow = pipe.getFlow();

        IFlowItems flowItem = flow instanceof IFlowItems ? (IFlowItems) flow : null;
        IFlowFluid flowFluid = flow instanceof IFlowFluid ? (IFlowFluid) flow : null;

        IItemTransactor transactor = ItemTransactorHelper.getTransactorForEntity(entity, faceFrom.getOpposite());

        if (flowItem != null) {
            int max = requiresPower ? 1 : Integer.MAX_VALUE;
            ItemStack extracted = transactor.extract(StackFilter.ALL, 1, max, false);
            if (!extracted.isEmpty()) {
                flowItem.insertItemsForce(extracted, faceFrom, null, INSERT_SPEED);
                battery.extractPower(MjAPI.MJ);
                return true;
            }
        }
        if (flowFluid != null) {
            // TODO: Fluid extraction!
        }
        return false;
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
        return MjAPI.MJ;
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        return battery.addPowerChecking(microJoules, simulate);
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
