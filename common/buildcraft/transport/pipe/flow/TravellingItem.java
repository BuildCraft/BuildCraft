/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import java.util.EnumSet;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.VecUtil;

public class TravellingItem {
    // Client fields - public for rendering
    @Nonnull
    public final Supplier<ItemStack> clientItemLink;
    public int stackSize;
    public EnumDyeColor colour;

    // Server fields
    /** The server itemstack */
    @Nonnull
    ItemStack stack;
    int id = 0;
    boolean toCenter;
    double speed = 0.05;
    /** Absolute times (relative to world.getTotalWorldTime()) with when an item started to when it finishes. */
    long tickStarted, tickFinished;
    /** Relative times (from tickStarted) until an event needs to be fired or this item needs changing. */
    int timeToDest;
    /** If {@link #toCenter} is true then this represents the side that the item is coming from, otherwise this
     * represents the side that the item is going to. */
    EnumFacing side;
    /** A set of all the faces that this item has tried to go and failed. */
    EnumSet<EnumFacing> tried = EnumSet.noneOf(EnumFacing.class);

    // @formatter:off
    /* States (server side):
      
      - TO_CENTER:
        - tickStarted is the tick that the item entered the pipe (or bounced back)
        - tickFinished is the tick that the item will reach the center 
        - side is the side that the item came from
        - timeToDest is equal to timeFinished - timeStarted
      
      - TO_EXIT:
       - tickStarted is the tick that the item reached the center
       - tickFinished is the tick that the item will reach the end of a pipe 
       - side is the side that the item is going to 
       - timeToDest is equal to timeFinished - timeStarted. 
     */
    // @formatter:on

    public TravellingItem(@Nonnull ItemStack stack) {
        this.stack = stack;
        clientItemLink = () -> ItemStack.EMPTY;
    }

    public TravellingItem(Supplier<ItemStack> clientStackLink, int count) {
        this.clientItemLink = StackUtil.asNonNull(clientStackLink);
        this.stackSize = count;
        this.stack = StackUtil.EMPTY;
    }

    public TravellingItem(NBTTagCompound nbt, long tickNow) {
        clientItemLink = () -> ItemStack.EMPTY;
        stack = new ItemStack(nbt.getCompoundTag("stack"));
        int c = nbt.getByte("colour");
        this.colour = c == 0 ? null : EnumDyeColor.byMetadata(c - 1);
        this.toCenter = nbt.getBoolean("toCenter");
        this.speed = nbt.getDouble("speed");
        if (speed < 0.001) {
            // Just to make sure that we don't have an invalid speed
            speed = 0.001;
        }
        tickStarted = nbt.getInteger("tickStarted") + tickNow;
        tickFinished = nbt.getInteger("tickFinished") + tickNow;
        timeToDest = nbt.getInteger("timeToDest");

        side = NBTUtilBC.readEnum(nbt.getTag("side"), EnumFacing.class);
        if (side == null || timeToDest == 0) {
            // Older 8.0.x. version
            toCenter = true;
        }
        tried = NBTUtilBC.readEnumSet(nbt.getTag("tried"), EnumFacing.class);
    }

    public NBTTagCompound writeToNbt(long tickNow) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("stack", stack.serializeNBT());
        nbt.setByte("colour", (byte) (colour == null ? 0 : colour.getMetadata() + 1));
        nbt.setBoolean("toCenter", toCenter);
        nbt.setDouble("speed", speed);
        nbt.setInteger("tickStarted", (int) (tickStarted - tickNow));
        nbt.setInteger("tickFinished", (int) (tickFinished - tickNow));
        nbt.setInteger("timeToDest", timeToDest);
        nbt.setTag("side", NBTUtilBC.writeEnum(side));
        nbt.setTag("tried", NBTUtilBC.writeEnumSet(tried, EnumFacing.class));
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
        timeToDest = (int) Math.ceil(distance / speed);
        tickFinished = now + timeToDest;
    }

    public boolean canMerge(TravellingItem with) {
        return toCenter == with.toCenter//
            && colour == with.colour//
            && side == with.side//
            && Math.abs(tickFinished - with.tickFinished) < 10//
            && stack.getMaxStackSize() >= stack.getCount() + with.stack.getCount()//
            && StackUtil.canMerge(stack, with.stack);
    }

    /** Attempts to merge the two travelling item's together, if they are close enough.
     * 
     * @param with
     * @return */
    public boolean mergeWith(TravellingItem with) {
        if (canMerge(with)) {
            this.stack.grow(with.stack.getCount());
            return true;
        }
        return false;
    }

    public Vec3d interpolatePosition(Vec3d start, Vec3d end, long tick, float partialTicks) {
        long diff = tickFinished - tickStarted;
        long nowDiff = tick - tickStarted;
        double sinceStart = nowDiff + partialTicks;
        double interpMul = sinceStart / diff;
        double oneMinus = 1 - interpMul;
        if (interpMul <= 0) return start;
        if (interpMul >= 1) return end;

        double x = oneMinus * start.x + interpMul * end.x;
        double y = oneMinus * start.y + interpMul * end.y;
        double z = oneMinus * start.z + interpMul * end.z;
        return new Vec3d(x, y, z);
    }

    public Vec3d getRenderPosition(BlockPos pos, long tick, float partialTicks) {
        long diff = tickFinished - tickStarted;
        long afterTick = tick - tickStarted;

        float interp = (afterTick + partialTicks) / diff;
        interp = Math.max(0, Math.min(1, interp));

        Vec3d center = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
        Vec3d vecSide = side == null ? center : VecUtil.offset(center, side, 0.5);

        Vec3d vecFrom;
        Vec3d vecTo;
        if (toCenter) {
            vecFrom = vecSide;
            vecTo = center;
        } else {
            vecFrom = center;
            vecTo = vecSide;
        }

        return VecUtil.scale(vecFrom, 1 - interp).add(VecUtil.scale(vecTo, interp));
    }

    public EnumFacing getRenderDirection(long tick, float partialTicks) {
        long diff = tickFinished - tickStarted;
        long afterTick = tick - tickStarted;

        float interp = (afterTick + partialTicks) / diff;
        interp = Math.max(0, Math.min(1, interp));
        if (toCenter) {
            return side == null ? null : side.getOpposite();
        } else {
            return side;
        }
    }

    public boolean isVisible() {
        return true;
    }
}
