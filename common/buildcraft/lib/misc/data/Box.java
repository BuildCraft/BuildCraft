/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.misc.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

// Yarn 1.20.1 renames:
//   NBTTagCompound → NbtCompound, PacketBuffer → PacketByteBuf, TileEntity → BlockEntity
//   AxisAlignedBB → net.minecraft.util.math.Box (referenced fully-qualified below to avoid clashing
//   with this class's own name)
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;

/** MUTABLE integer variant of AxisAlignedBB, with a few BC-specific methods */
public class Box implements IBox {

    // STUB(R.Chen): client-side laser render cache (laserData / lastMin / lastMax / lastType)
    //               removed for now — its type LaserData_BC8 pulls in the unmigrated
    //               lib.client.render.laser + lib.client.sprite stack. The only consumer is
    //               LaserBoxRenderer (also unmigrated). Re-add (annotated @Environment(EnvType.CLIENT))
    //               once lib.client.render.laser is ported.

    private BlockPos min, max;

    public Box() {
        reset();
    }

    public Box(BlockPos min, BlockPos max) {
        this();
        this.min = VecUtil.min(min, max);
        this.max = VecUtil.max(min, max);
    }

    public Box(BlockEntity e) {
        this(e.getPos(), e.getPos());
    }

    public void reset() {
        min = null;
        max = null;
    }

    public boolean isInitialized() {
        return min != null && max != null;
    }

    public void extendToEncompassBoth(BlockPos newMin, BlockPos newMax) {
        this.min = VecUtil.min(this.min, newMin, newMax);
        this.max = VecUtil.max(this.max, newMin, newMax);
    }

    public void setMin(BlockPos min) {
        if (min == null) return;
        this.min = min;
        this.max = VecUtil.max(min, max);
    }

    public void setMax(BlockPos max) {
        if (max == null) return;
        this.min = VecUtil.min(min, max);
        this.max = max;
    }

    public void initialize(IBox box) {
        reset();
        extendToEncompassBoth(box.min(), box.max());
    }

    public void initialize(IAreaProvider a) {
        reset();
        extendToEncompassBoth(a.min(), a.max());
    }

    public void initialize(NbtCompound nbt) {
        reset();
        if (nbt.contains("xMin")) {
            min = new BlockPos(nbt.getInt("xMin"), nbt.getInt("yMin"), nbt.getInt("zMin"));
            max = new BlockPos(nbt.getInt("xMax"), nbt.getInt("yMax"), nbt.getInt("zMax"));
        } else {
            min = NBTUtilBC.readBlockPos(nbt.get("min"));
            max = NBTUtilBC.readBlockPos(nbt.get("max"));
        }
        extendToEncompassBoth(min, max);
    }

    public void writeToNBT(NbtCompound nbt) {
        if (min != null) nbt.put("min", NBTUtilBC.writeBlockPos(min));
        if (max != null) nbt.put("max", NBTUtilBC.writeBlockPos(max));
    }

    public NbtCompound writeToNBT() {
        NbtCompound nbt = new NbtCompound();
        writeToNBT(nbt);
        return nbt;
    }

    public void initializeCenter(BlockPos center, int size) {
        initializeCenter(center, new BlockPos(size, size, size));
    }

    public void initializeCenter(BlockPos center, Vec3i size) {
        extendToEncompassBoth(center.subtract(size), center.add(size));
    }

    public List<BlockPos> getBlocksInArea() {
        List<BlockPos> blocks = new ArrayList<>();

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            // BlockPos.iterate reuses a mutable cursor — store an immutable copy.
            blocks.add(pos.toImmutable());
        }

        return blocks;
    }

    public List<BlockPos> getBlocksOnEdge() {
        return PositionUtil.getAllOnEdge(min, max);
    }

    @Override
    public Box expand(int amount) {
        if (!isInitialized()) return this;
        Vec3i am = new BlockPos(amount, amount, amount);
        setMin(min().subtract(am));
        setMax(max().add(am));
        return this;
    }

    @Override
    public IBox contract(int amount) {
        return expand(-amount);
    }

    @Override
    public boolean contains(Vec3d p) {
        net.minecraft.util.math.Box bb = getBoundingBox();
        if (p.x < bb.minX || p.x >= bb.maxX) return false;
        if (p.y < bb.minY || p.y >= bb.maxY) return false;
        if (p.z < bb.minZ || p.z >= bb.maxZ) return false;
        return true;
    }

    public boolean contains(BlockPos i) {
        return contains(Vec3d.of(i));
    }

    @Override
    public BlockPos min() {
        return min;
    }

    @Override
    public BlockPos max() {
        return max;
    }

    @Override
    public BlockPos size() {
        if (!isInitialized()) return BlockPos.ORIGIN;
        return max.subtract(min).add(VecUtil.POS_ONE);
    }

    public BlockPos center() {
        return BlockPos.ofFloored(centerExact());
    }

    public Vec3d centerExact() {
        return Vec3d.of(size()).multiply(0.5).add(Vec3d.of(min()));
    }

    @Override
    public String toString() {
        return "Box[min = " + min + ", max = " + max + "]";
    }

    public Box extendToEncompass(IBox toBeContained) {
        if (toBeContained == null) {
            return this;
        }
        extendToEncompassBoth(toBeContained.min(), toBeContained.max());
        return this;
    }

    /** IMPORTANT: Use {@link #contains(Vec3d)}instead of the returned {@link net.minecraft.util.math.Box#contains(Vec3d)}
     * as the logic is different! */
    public net.minecraft.util.math.Box getBoundingBox() {
        return new net.minecraft.util.math.Box(min, max.add(VecUtil.POS_ONE));
    }

    public Box extendToEncompass(Vec3d toBeContained) {
        setMin(VecUtil.min(min, VecUtil.convertFloor(toBeContained)));
        setMax(VecUtil.max(max, VecUtil.convertCeiling(toBeContained)));
        return this;
    }

    public Box extendToEncompass(BlockPos toBeContained) {
        setMin(VecUtil.min(min, toBeContained));
        setMax(VecUtil.max(max, toBeContained));
        return this;
    }

    @Override
    public double distanceTo(BlockPos index) {
        return Math.sqrt(distanceToSquared(index));
    }

    @Override
    public double distanceToSquared(BlockPos index) {
        return closestInsideTo(index).getSquaredDistance(index);
    }

    public BlockPos closestInsideTo(BlockPos toTest) {
        return VecUtil.max(min, VecUtil.min(max, toTest));
    }

    @Override
    public BlockPos getRandomBlockPos(Random rand) {
        return PositionUtil.randomBlockPos(rand, min, max.add(1, 1, 1));
    }

    /** Delegate for {@link PositionUtil#isCorner(BlockPos, BlockPos, BlockPos)} */
    public boolean isCorner(BlockPos pos) {
        return PositionUtil.isCorner(min, max, pos);
    }

    /** Delegate for {@link PositionUtil#isOnEdge(BlockPos, BlockPos, BlockPos)} */
    public boolean isOnEdge(BlockPos pos) {
        return PositionUtil.isOnEdge(min, max, pos);
    }

    /** Delegate for {@link PositionUtil#isOnFace(BlockPos, BlockPos, BlockPos)} */
    public boolean isOnFace(BlockPos pos) {
        return PositionUtil.isOnFace(min, max, pos);
    }

    public boolean doesIntersectWith(Box box) {
        if (isInitialized() && box.isInitialized()) {
            return min.getX() <= box.max.getX() && max.getX() >= box.min.getX()//
                && min.getY() <= box.max.getY() && max.getY() >= box.min.getY() //
                && min.getZ() <= box.max.getZ() && max.getZ() >= box.min.getZ();
        }
        return false;
    }

    /** @return The intersection box (if these two boxes are intersecting) or null if they were not. */
    @Nullable
    public Box getIntersect(Box box) {
        if (doesIntersectWith(box)) {
            BlockPos min2 = VecUtil.max(min, box.min);
            BlockPos max2 = VecUtil.min(max, box.max);
            return new Box(min2, max2);
        }
        return null;
    }

    /** Calculates the total number of blocks on the edge. This is identical to (but faster than) calling
     * {@link #getBlocksOnEdge()}.{@link List#size() size()}
     *
     * @return The size of the list returned by {@link #getBlocksOnEdge()}. */
    public int getBlocksOnEdgeCount() {
        return PositionUtil.getCountOnEdge(min(), max());
    }

    public void readData(PacketByteBuf stream) {
        if (stream.readBoolean()) {
            min = stream.readBlockPos();
            max = stream.readBlockPos();
        } else {
            min = null;
            max = null;
        }
    }

    public void writeData(PacketByteBuf stream) {
        boolean isValid = isInitialized();
        stream.writeBoolean(isValid);
        if (isValid) {
            stream.writeBlockPos(min);
            stream.writeBlockPos(max);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        Box box = (Box) obj;
        if (!Objects.equal(min, box.min)) return false;
        if (!Objects.equal(max, box.max)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(min, max);
    }
}
