package buildcraft.transport.pipe.flow;

import java.util.List;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
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
    /** Absolute times with when an item started to when it finishes. */
    long tickStarted, tickFinished;
    /** Relative times until an event needs to be fired or this item needs changing. */
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

        Vec3d from;
        Vec3d to;
        if (interp < 0.5) {
            from = start;
            to = center;
            interp *= 2;
        } else {
            from = center;
            to = end;
            interp = (interp - 0.5f) * 2;
        }

        return VecUtil.scale(from, 1 - interp).add(VecUtil.scale(to, interp));
    }

    public boolean isVisible() {
        return state != EnumTravelState.CLIENT_INVALID;
    }
}
