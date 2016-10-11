package buildcraft.transport.pipe.flow;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.misc.VecUtil;

public class TravellingItem {
    private final PipeFlowItems flow;

    public final ItemStack stack;
    EnumDyeColor colour = null;
    double speed = 0.05;

    // The following fields are basically just this motion, but inlined to not use up an extra object
    EnumFacing from, to;

    /** Indicates the in-world tick of when it will reach its destination (Generally the other side of the pipe) */
    long tickStarted, tickFinished;

    Motion nextMotion = null;

    /** Used to save (on the client) where this item should go next. */
    // TODO: Use this!
    static class Motion {
        EnumFacing from, to;

        /** Indicates the in-world tick of when it will reach its destination (Generally the other side of the pipe) */
        long tickStarted, tickFinished;

        Motion nextMotion = null;
    }

    public TravellingItem(PipeFlowItems flow, ItemStack stack) {
        this.flow = flow;
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
        double time = distance / speed;
        time = Math.ceil(time);
        tickFinished = now + (long) (time);
    }

    boolean advanceMotion() {
        return setMotion(nextMotion);
    }

    boolean setMotion(Motion motion) {
        if (motion == null) {
            tickStarted = tickFinished;
            tickFinished = tickFinished + 1;
            nextMotion = null;
            return false;
        } else {
            from = motion.from;
            to = motion.to;
            tickStarted = motion.tickStarted;
            tickFinished = motion.tickFinished;
            nextMotion = motion.nextMotion;
            return true;
        }
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

    public Vec3d getRenderPosition(boolean addPipePos, long tick, float partialTicks) {
        BlockPos pos = addPipePos ? flow.pipe.getHolder().getPipePos() : BlockPos.ORIGIN;

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
}
