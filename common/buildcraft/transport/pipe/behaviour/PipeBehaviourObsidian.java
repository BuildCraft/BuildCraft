package buildcraft.transport.pipe.behaviour;

import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityArrow.PickupStatus;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.neptune.*;

import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.VecUtil;

public class PipeBehaviourObsidian extends PipeBehaviour implements IMjRedstoneReceiver {
    /** Map of recently dropped item to the tick when it can be picked up */
    private final WeakHashMap<EntityItem, Long> entityDropTime = new WeakHashMap<>();

    public PipeBehaviourObsidian(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourObsidian(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public void onTick() {

    }

    protected AxisAlignedBB getSuckingBox() {
        EnumFacing facing = null;

        for (EnumFacing face : EnumFacing.VALUES) {
            if (pipe.isConnected(face)) {
                if (facing == null) {
                    facing = face;
                } else {
                    facing = null;
                    break;
                }
            }
        }
        AxisAlignedBB bb = BoundingBoxUtil.makeAround(VecUtil.convertCenter(pipe.getHolder().getPipePos()), 0.4);
        if (facing == null) {
            return bb;
        }
        return bb.addCoord(facing.getFrontOffsetX(), facing.getFrontOffsetY(), facing.getFrontOffsetZ());
    }

    /** Attempts to pull in the given */
    protected boolean trySuckEntity(Entity entity, EnumFacing faceFrom) {
        if (entity.isDead) {
            return false;
        }

        Long tickPickupObj = entityDropTime.get(entity);
        if (tickPickupObj != null) {
            long tickPickup = tickPickupObj.longValue();
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

        ItemStack toInsert;
        if (entity instanceof EntityArrow && flowItem != null) {
            EntityArrow arrow = (EntityArrow) entity;
            if (arrow.pickupStatus == PickupStatus.ALLOWED) {
                toInsert = EntityUtil.getArrowStack(arrow);
            } else {
                return false;
            }
        } else if (entity instanceof EntityItem) {
            toInsert = ((EntityItem) entity).getEntityItem();
        } else {
            return false;
        }

        if (flowFluid != null) {
            // TODO: Insert the fluid contained on a bucket or other fluid container
        } else if (flowItem != null) {
            ItemStack leftOver = flowItem.tryInsertItems(toInsert, null, 0, faceFrom);

            // Instance equality check b/c it will return the same stack if it was completely rejected
            if (leftOver == toInsert) {
                return false;
            }
            entity.setDead();

            if (leftOver != null) {
                EntityItem entityLeftOver = new EntityItem(entity.worldObj, entity.posX, entity.posY, entity.posZ, leftOver);
                entityLeftOver.worldObj.spawnEntityInWorld(entityLeftOver);
            }
            return true;
        }
        return false;
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return MjAPI.MJ;
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        // TODO: Make this require more or less than 1 Mj Per item
        // Also make this extract different numbers of items depending
        // on how much power was put in

        EntityItem potential = null;
        // TODO: Add a search function to try and find a suitable item!

        PipeFlow flow = pipe.getFlow();
        if (flow instanceof IFlowItems) {

        }
        return microJoules;
    }
}
