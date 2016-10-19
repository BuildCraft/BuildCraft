package buildcraft.transport.pipe.flow;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.misc.VecUtil;

public class TravellingItem {
    ItemStack stack;
    double speed = 0.05;

    Motion motion = new Motion();

    /** Used to save where this item should go next. Only the client uses "nextPipe" though. */
    static class Motion {
        EnumFacing from;
        Destination dest = new Destination();
        Motion nextPipe;
    }

    /** Used to store where the item will try next if it fails to be inserted into the first one */
    static class Destination {
        long tickStart, tickMiddle, tickEnd;
        EnumFacing to;
        EnumDyeColor colourAtStart, colourAtMiddle, colourAtEnd;
        Destination after;
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
        double time = distance / speed;
        time = Math.ceil(time);
        tickFinished = now + (long) (time);
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
}
