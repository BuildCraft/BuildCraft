/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.function.Supplier;

public class TravellingItem {
    // Client fields - public for rendering
    @Nonnull
    public final Supplier<ItemStack> clientItemLink;
    public int stackSize;
    public DyeColor colour;

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
    Direction side;
    /** A set of all the faces that this item has tried to go and failed. */
    EnumSet<Direction> tried = EnumSet.noneOf(Direction.class);
    /** If true then events won't be fired for this, and this item won't be dropped by the pipe. However it will affect
     * pipe.isEmpty and related gate triggers. */
    boolean isPhantom = false;

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

    public TravellingItem(CompoundTag nbt, long tickNow) {
        clientItemLink = () -> ItemStack.EMPTY;
//        stack = new ItemStack(nbt.getCompound("stack"));
        stack = ItemStack.of(nbt.getCompound("stack"));
        int c = nbt.getByte("colour");
        this.colour = c == 0 ? null : DyeColor.byId(c - 1);
        this.toCenter = nbt.getBoolean("toCenter");
        this.speed = nbt.getDouble("speed");
        if (speed < 0.001) {
            // Just to make sure that we don't have an invalid speed
            speed = 0.001;
        }
        tickStarted = nbt.getInt("tickStarted") + tickNow;
        tickFinished = nbt.getInt("tickFinished") + tickNow;
        timeToDest = nbt.getInt("timeToDest");

        side = NBTUtilBC.readEnum(nbt.get("side"), Direction.class);
        if (side == null || timeToDest == 0) {
            // Older 8.0.x. version
            toCenter = true;
        }
        tried = NBTUtilBC.readEnumSet(nbt.get("tried"), Direction.class);
        isPhantom = nbt.getBoolean("isPhantom");
    }

    public CompoundTag writeToNbt(long tickNow) {
        CompoundTag nbt = new CompoundTag();
        nbt.put("stack", stack.serializeNBT());
        nbt.putByte("colour", (byte) (colour == null ? 0 : colour.getId() + 1));
        nbt.putBoolean("toCenter", toCenter);
        nbt.putDouble("speed", speed);
        nbt.putInt("tickStarted", (int) (tickStarted - tickNow));
        nbt.putInt("tickFinished", (int) (tickFinished - tickNow));
        nbt.putInt("timeToDest", timeToDest);
        nbt.put("side", NBTUtilBC.writeEnum(side));
        nbt.put("tried", NBTUtilBC.writeEnumSet(tried, Direction.class));
        if (isPhantom) {
            nbt.putBoolean("isPhantom", true);
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
        timeToDest = (int) Math.ceil(distance / speed);
        tickFinished = now + timeToDest;
    }

    public boolean canMerge(TravellingItem with) {
        if (isPhantom || with.isPhantom) {
            return false;
        }
        return toCenter == with.toCenter//
                && colour == with.colour//
                && side == with.side//
                && Math.abs(tickFinished - with.tickFinished) < 4//
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

    public Vec3 interpolatePosition(Vec3 start, Vec3 end, long tick, float partialTicks) {
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
        return new Vec3(x, y, z);
    }

    public Vec3 getRenderPosition(BlockPos pos, long tick, float partialTicks, PipeFlowItems flow) {
        long diff = tickFinished - tickStarted;
        long afterTick = tick - tickStarted;

        float interp = (afterTick + partialTicks) / diff;
        interp = Math.max(0, Math.min(1, interp));

//        Vec3 center = new Vec3(pos).addVector(0.5, 0.5, 0.5);
        Vec3 center = Vec3.atLowerCornerOf(pos).add(0.5, 0.5, 0.5);
        Vec3 vecSide = side == null ? center : VecUtil.offset(center, side, flow.getPipeLength(side));

        Vec3 vecFrom;
        Vec3 vecTo;
        if (toCenter) {
            vecFrom = vecSide;
            vecTo = center;
        } else {
            vecFrom = center;
            vecTo = vecSide;
        }

        return VecUtil.scale(vecFrom, 1 - interp).add(VecUtil.scale(vecTo, interp));
    }

    public Direction getRenderDirection(long tick, float partialTicks) {
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
