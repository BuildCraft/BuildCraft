/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe.flow;

import java.util.EnumSet;
import java.util.function.Supplier;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.VecUtil;

// Ported to Fabric 1.20.1 by R.Chen:
//   - DyeColor → DyeColor (byMetadata/getMetadata → byId/getId).
//   - Direction → Direction (VALUES → values()).
//   - NbtCompound → NbtCompound (getInteger/setX → getInt/putX, getCompoundTag → getCompound,
//     getTag → get, setTag → put; serializeNBT → writeNbt; new ItemStack(nbt) → ItemStack.fromNbt).
//   - new Vec3d(BlockPos.getX(), BlockPos.getY(), BlockPos.getZ()) → Vec3d.ofCenter / Vec3d.of; addVector → add.
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
    /** Absolute times (relative to world time) with when an item started to when it finishes. */
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

    public TravellingItem(@Nonnull ItemStack stack) {
        this.stack = stack;
        clientItemLink = () -> ItemStack.EMPTY;
    }

    public TravellingItem(Supplier<ItemStack> clientStackLink, int count) {
        this.clientItemLink = StackUtil.asNonNull(clientStackLink);
        this.stackSize = count;
        this.stack = StackUtil.EMPTY;
    }

    public TravellingItem(NbtCompound nbt, long tickNow) {
        clientItemLink = () -> ItemStack.EMPTY;
        stack = ItemStack.fromNbt(nbt.getCompound("stack"));
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

    public NbtCompound writeToNbt(long tickNow) {
        NbtCompound nbt = new NbtCompound();
        nbt.put("stack", stack.writeNbt(new NbtCompound()));
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
            && stack.getMaxCount() >= stack.getCount() + with.stack.getCount()//
            && StackUtil.canMerge(stack, with.stack);
    }

    /** Attempts to merge the two travelling item's together, if they are close enough.
     *
     * @param with
     * @return */
    public boolean mergeWith(TravellingItem with) {
        if (canMerge(with)) {
            this.stack.increment(with.stack.getCount());
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

    public Vec3d getRenderPosition(BlockPos pos, long tick, float partialTicks, PipeFlowItems flow) {
        long diff = tickFinished - tickStarted;
        long afterTick = tick - tickStarted;

        float interp = (afterTick + partialTicks) / diff;
        interp = Math.max(0, Math.min(1, interp));

        Vec3d center = Vec3d.ofCenter(pos);
        Vec3d vecSide = side == null ? center : VecUtil.offset(center, side, flow.getPipeLength(side));

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
