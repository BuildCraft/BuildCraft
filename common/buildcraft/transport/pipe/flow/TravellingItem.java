package buildcraft.transport.pipe.flow;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.misc.VecUtil;

public class TravellingItem {
    // Public for rendering
    public ItemStack stack;
    public EnumDyeColor colour;

    EnumTravelState state = EnumTravelState.SERVER_TO_CENTER;
    double speed = 0.05;
    /** Absolute times (relative to world.getTotalWorldTime()) with when an item started to when it finishes. */
    long tickStarted, tickFinished;
    /** Relative times (from tickStarted) until an event needs to be fired or this item needs changing. */
    int timeToCenter, timeToExit;
    EnumFacing from, to;
    /** A list of the next faces to try if the current "to" differs from "from" and "to" failed to insert. */
    List<EnumFacing> toTryOrder = null;
    List<EnumFacing> tried = null;

    /** Used to save where this item should go next. Only the client uses this field. */
    Motion motion;

    static class Motion {
        EnumFacing to;
        Motion nextPipe;
    }

    enum EnumTravelState {
        SERVER_TO_CENTER,
        SERVER_TO_EXIT,

        /** Used on the client when an item is running normally, and has instructions on what it should be doing */
        CLIENT_RUNNING,
        /** Used on the client when an item is waiting for its next instructions. It never stays in this state for more
         * than 2 ticks. */
        CLIENT_WAITING,
        /** Used on the client to determine that when this will just disappear when it reaches its next tick. */
        CLIENT_WILL_DESTROY,
        /** Used on the client if this item is not valid AT ALL and so will not be drawn. */
        CLIENT_INVALID;
    }

    public TravellingItem(ItemStack stack) {
        this.stack = stack;
        if (stack == null || stack.getItem() == null) {
            throw new NullPointerException("stack");
        }
    }

    // List<EnumFacing> tried = null;

    public TravellingItem(NBTTagCompound nbt, long tickNow) {
        stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
        int c = nbt.getByte("colour");
        this.colour = c == 0 ? null : EnumDyeColor.byMetadata(c - 1);
        this.state = nbt.getBoolean("toCenter") ? EnumTravelState.SERVER_TO_CENTER : EnumTravelState.SERVER_TO_EXIT;
        this.speed = nbt.getDouble("speed");
        if (speed < 0.001) {
            // Just to make sure that we don't have an invalid speed
            speed = 0.001;
        }
        tickStarted = nbt.getInteger("tickStarted") + tickNow;
        tickFinished = nbt.getInteger("tickFinished") + tickNow;
        timeToCenter = nbt.getInteger("timeToCenter");
        timeToExit = nbt.getInteger("timeToExit");

        int f = nbt.getInteger("from");
        from = f == 0 ? null : EnumFacing.getFront(f - 1);

        int t = nbt.getInteger("to");
        to = t == 0 ? null : EnumFacing.getFront(t - 1);

        int[] toTry = nbt.getIntArray("toTryOrder");
        if (toTry.length > 0) {
            toTryOrder = new ArrayList<>(toTry.length);
            for (int i : toTry) {
                toTryOrder.add(EnumFacing.getFront(i));
            }
        }

        int[] triedArr = nbt.getIntArray("tried");
        if (triedArr.length > 0) {
            tried = new ArrayList<>(triedArr.length);
            for (int i : toTry) {
                tried.add(EnumFacing.getFront(i));
            }
        }
    }

    public NBTTagCompound writeToNbt(long tickNow) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("stack", stack.serializeNBT());
        nbt.setByte("colour", (byte) (colour == null ? 0 : colour.getMetadata()));
        nbt.setBoolean("toCenter", state == EnumTravelState.SERVER_TO_CENTER);
        nbt.setDouble("speed", speed);
        nbt.setInteger("tickStarted", (int) (tickStarted - tickNow));
        nbt.setInteger("tickFinished", (int) (tickFinished - tickNow));
        nbt.setInteger("timeToCenter", timeToCenter);
        nbt.setInteger("timeToExit", timeToExit);
        nbt.setByte("from", (byte) (from == null ? 0 : from.ordinal() + 1));
        nbt.setByte("to", (byte) (to == null ? 0 : to.ordinal() + 1));
        if (toTryOrder != null) {
            int[] order = new int[toTryOrder.size()];
            for (int i = toTryOrder.size() - 1; i >= 0; i--) {
                order[i] = toTryOrder.get(i).getIndex();
            }
            nbt.setIntArray("toTryOrder", order);
        }
        if (tried != null) {
            int[] order = new int[tried.size()];
            for (int i = tried.size() - 1; i >= 0; i--) {
                order[i] = tried.get(i).getIndex();
            }
            nbt.setIntArray("tried", order);
        }
        return nbt;
    }

    public int getCurrentDelay(long tickNow) {
        long diff = tickFinished - tickNow;
        if (diff < 0) {
            return 0;
        } else {
            return (int) diff;
        }
    }

    public double getWayThrough(long now) {
        long diff = tickFinished - tickStarted;
        long nowDiff = now - tickStarted;
        return nowDiff / (double) diff;
    }

    public void genTimings(long now, double distance) {
        tickStarted = now;
        int time = (int) Math.ceil(distance / speed);
        tickFinished = now + (time);
        timeToCenter = time / 2;
        timeToExit = time - timeToCenter;
    }

    public Vec3d interpolatePosition(Vec3d start, Vec3d end, long tick, float partialTicks) {
        long diff = tickFinished - tickStarted;
        long nowDiff = tick - tickStarted;
        double sinceStart = nowDiff + partialTicks;
        double interpMul = sinceStart / diff;
        double oneMinus = 1 - interpMul;
        if (interpMul <= 0) return start;
        if (interpMul >= 1) return end;

        double x = oneMinus * start.xCoord + interpMul * end.xCoord;
        double y = oneMinus * start.yCoord + interpMul * end.yCoord;
        double z = oneMinus * start.zCoord + interpMul * end.zCoord;
        return new Vec3d(x, y, z);
    }

    public Vec3d getRenderPosition(BlockPos pos, long tick, float partialTicks) {
        long diff = tickFinished - tickStarted;
        long afterTick = tick - tickStarted;

        float interp = (afterTick + partialTicks) / diff;
        interp = Math.max(0, Math.min(1, interp));

        Vec3d center = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
        Vec3d start = VecUtil.offset(center, from, 0.5);
        Vec3d end = center;
        if (to != null) {
            end = VecUtil.offset(center, to, 0.5);
        }

        Vec3d vecFrom;
        Vec3d vecTo;
        if (interp < 0.5) {
            vecFrom = start;
            vecTo = center;
            interp *= 2;
        } else {
            vecFrom = center;
            vecTo = end;
            interp = (interp - 0.5f) * 2;
        }

        return VecUtil.scale(vecFrom, 1 - interp).add(VecUtil.scale(vecTo, interp));
    }

    public boolean isVisible() {
        return state != EnumTravelState.CLIENT_INVALID;
    }
}
