/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;
import com.google.common.base.Objects;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** MUTABLE integer variant of AxisAlignedBB, with a few BC-specific methods */
public class Box implements IBox {

    // Client side cache: used to compare current laser type with previously
    // rendered data.

    @OnlyIn(Dist.CLIENT)
    public LaserData_BC8[] laserData;

    @OnlyIn(Dist.CLIENT)
    public BlockPos lastMin, lastMax;

    @OnlyIn(Dist.CLIENT)
    public LaserType lastType;

    private BlockPos min, max;

    public Box() {
        reset();
    }

    public Box(BlockPos min, BlockPos max) {
        this();
        this.min = VecUtil.min(min, max);
        this.max = VecUtil.max(min, max);
    }

    public Box(TileEntity e) {
        this(e.getBlockPos(), e.getBlockPos());
    }

    public Box(MutableBoundingBox boundingBox) {
        this(
                new BlockPos(boundingBox.x0, boundingBox.y0, boundingBox.z0),
                new BlockPos(boundingBox.x1, boundingBox.y1, boundingBox.z1)
        );
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

    public void initialize(CompoundNBT nbt) {
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

    public void writeToNBT(CompoundNBT nbt) {
        if (min != null) nbt.put("min", NBTUtilBC.writeBlockPos(min));
        if (max != null) nbt.put("max", NBTUtilBC.writeBlockPos(max));
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT nbt = new CompoundNBT();
        writeToNBT(nbt);
        return nbt;
    }

    public void initializeCenter(BlockPos center, int size) {
        initializeCenter(center, new BlockPos(size, size, size));
    }

    public void initializeCenter(BlockPos center, Vector3i size) {
        extendToEncompassBoth(center.subtract(size), center.offset(size));
    }

    public List<BlockPos> getBlocksInArea() {
        List<BlockPos> blocks = new ArrayList<>();

//        for (BlockPos pos : BlockPos.getAllInBox(min, max))
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            blocks.add(pos.immutable());
        }

        return blocks;
    }

    public List<BlockPos> getBlocksOnEdge() {
        return PositionUtil.getAllOnEdge(min, max);
    }

    @Override
    public Box expand(int amount) {
        if (!isInitialized()) return this;
        Vector3i am = new BlockPos(amount, amount, amount);
        setMin(min().subtract(am));
        setMax(max().offset(am));
        return this;
    }

    @Override
    public IBox contract(int amount) {
        return expand(-amount);
    }

    @Override
    public boolean contains(Vector3d p) {
        AxisAlignedBB bb = getBoundingBox();
        if (p.x < bb.minX || p.x >= bb.maxX) return false;
        if (p.y < bb.minY || p.y >= bb.maxY) return false;
        if (p.z < bb.minZ || p.z >= bb.maxZ) return false;
        return true;
    }

    public boolean contains(BlockPos i) {
        return contains(new Vector3d(i.getX(), i.getY(), i.getZ()));
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
        if (!isInitialized()) return BlockPos.ZERO;
        return max.subtract(min).offset(VecUtil.POS_ONE);
    }

    public BlockPos center() {
        return new BlockPos(centerExact());
    }

    public Vector3d centerExact() {
        BlockPos posMax = size();
        BlockPos posMin = min();
        return new Vector3d(posMax.getX(), posMax.getY(), posMax.getZ()).scale(0.5).add(new Vector3d(posMin.getX(), posMin.getY(), posMin.getZ()));
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

    /** IMPORTANT: Use {@link #contains(Vector3d)}instead of the returned {@link AxisAlignedBB#contains(Vector3d)} as the
     * logic is different! */
    public AxisAlignedBB getBoundingBox() {
        return new AxisAlignedBB(min, max.offset(VecUtil.POS_ONE));
    }

    // Calen Added
    public MutableBoundingBox getBB() {
        BlockPos outerMax = max.offset(VecUtil.POS_ONE);
        return new MutableBoundingBox(min.getX(), min.getY(), min.getZ(), outerMax.getX(), outerMax.getY(), outerMax.getZ());
    }

    public Box extendToEncompass(Vector3d toBeContained) {
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
        return VecUtil.distanceSq(closestInsideTo(index), index);
    }

    public BlockPos closestInsideTo(BlockPos toTest) {
        return VecUtil.max(min, VecUtil.min(max, toTest));
    }

    @Override
    public BlockPos getRandomBlockPos(Random rand) {
        return PositionUtil.randomBlockPos(rand, min, max.offset(1, 1, 1));
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

    public void readData(PacketBuffer stream) {
        if (stream.readBoolean()) {
            min = MessageUtil.readBlockPos(stream);
            max = MessageUtil.readBlockPos(stream);
        } else {
            min = null;
            max = null;
        }
    }

    public void writeData(PacketBuffer stream) {
        boolean isValid = isInitialized();
        stream.writeBoolean(isValid);
        if (isValid) {
            MessageUtil.writeBlockPos(stream, min);
            MessageUtil.writeBlockPos(stream, max);
        }
    }

    // 1.16.5
    public MutableBoundingBox toMutableBoundingBox() {
        return new MutableBoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
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
