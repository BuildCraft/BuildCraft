package buildcraft.transport.pipe.behaviour;

import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.transport.pipe.*;

import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.VecUtil;

public class PipeBehaviourObsidian extends PipeBehaviour implements IMjRedstoneReceiver {
    private static final double INSERT_SPEED = 0.04;

    private final MjBattery battery = new MjBattery(256 * MjAPI.MJ);
    /** Map of recently dropped item to the tick when it can be picked up */
    private final WeakHashMap<EntityItem, Long> entityDropTime = new WeakHashMap<>();

    public PipeBehaviourObsidian(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourObsidian(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        battery.deserializeNBT(nbt.getCompoundTag("battery"));
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
        if (battery.getStored() > 0) {
            EnumFacing openFace = null;
            for (EnumFacing face : EnumFacing.VALUES) {
                if (pipe.isConnected(face)) {
                    if (openFace == null) {
                        openFace = face.getOpposite();
                    } else {
                        openFace = null;
                        break;
                    }
                }
            }
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

    protected boolean suckEntity(EnumFacing openFace, int distance) {
        AxisAlignedBB aabb = getSuckingBox(openFace, distance);

        List<Entity> discoveredEntities = pipe.getHolder().getPipeWorld().getEntitiesWithinAABB(Entity.class, aabb);

        for (Entity entity : discoveredEntities) {
            if (trySuckEntity(entity, openFace)) {
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
                return bb.offset(-distance, 0, 0).expand(0.5, distance, distance);
            case EAST:
                return bb.offset(distance, 0, 0).expand(0.5, distance, distance);
            case DOWN:
                return bb.offset(0, -distance, 0).expand(distance, 0.5, distance);
            case UP:
                return bb.offset(0, distance, 0).expand(distance, 0.5, distance);
            case NORTH:
                return bb.offset(0, 0, -distance).expand(distance, distance, 0.5);
            case SOUTH:
                return bb.offset(0, 0, distance).expand(distance, distance, 0.5);
        }
    }

    /** Attempts to pull in the given */
    protected boolean trySuckEntity(Entity entity, EnumFacing faceFrom) {
        if (entity.isDead || battery.getStored() < MjAPI.MJ) {
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

        IItemTransactor transactor = ItemTransactorHelper.getTransactorForEntity(entity, faceFrom.getOpposite());

        if (flowItem != null) {
            ItemStack potential = transactor.extract(StackFilter.ALL, 1, 1, true);
            if (!potential.isEmpty()) {
                ItemStack leftOver = flowItem.injectItem(potential, true, faceFrom, null, INSERT_SPEED);
                if (leftOver.isEmpty()) {
                    transactor.extract(StackFilter.ALL, 1, 1, false);
                    battery.extractPower(MjAPI.MJ);
                    return true;
                }
                return false;
            }
        }
        if (flowFluid != null) {
            // TODO: Fluid extraction!
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
        if (simulate) {
            if (battery.isFull()) {
                return microJoules;
            } else {
                return 0;
            }
        }
        return battery.addPowerChecking(microJoules);
    }
}
