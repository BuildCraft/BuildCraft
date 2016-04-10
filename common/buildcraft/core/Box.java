/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3di;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.api.core.ISerializable;
import buildcraft.core.lib.utils.Matrix4i;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.Utils;

/** MUTABLE integer variant of AxisAlignedBB, with a few BC-specific methods */
public class Box implements IBox, ISerializable {
    public enum Kind {
        LASER_RED,
        LASER_YELLOW,
        LASER_GREEN,
        LASER_BLUE,
        STRIPES,
        BLUE_STRIPES,
    }

    public Kind kind = Kind.LASER_RED;
    public boolean isVisible = true;
    public LaserData[] lasersData;

    private BlockPos min, max;

    public Box() {
        reset();
    }

    public Box(BlockPos min, BlockPos max) {
        this();
        this.min = Utils.min(min, max);
        this.max = Utils.max(min, max);
    }

    public Box(TileEntity e) {
        this(e.getPos(), e.getPos().add(Utils.POS_ONE));
    }

    public void reset() {
        min = null;
        max = null;
    }

    public boolean isInitialized() {
        return min != null && max != null;
    }

    public void extendToEncompassBoth(BlockPos min, BlockPos max) {
        this.min = Utils.min(this.min, Utils.min(min, max));
        this.max = Utils.max(this.max, Utils.max(min, max));
    }

    public void setMin(BlockPos min) {
        if (min == null) return;
        this.min = min;
        this.max = Utils.max(min, max);
    }

    public void setMax(BlockPos max) {
        if (max == null) return;
        this.min = Utils.min(min, max);
        this.max = max;
    }

    public void initialize(Box box) {
        reset();
        extendToEncompassBoth(box.min(), box.max());
    }

    public void initialize(IAreaProvider a) {
        reset();
        extendToEncompassBoth(a.min(), a.max());
    }

    public void initialize(NBTTagCompound nbt) {
        kind = Kind.values()[nbt.getShort("kind")];

        BlockPos min;
        BlockPos max;
        if (nbt.hasKey("xMin")) {
            min = new BlockPos(nbt.getInteger("xMin"), nbt.getInteger("yMin"), nbt.getInteger("zMin"));
            max = new BlockPos(nbt.getInteger("xMax"), nbt.getInteger("yMax"), nbt.getInteger("zMax"));
        } else {
            min = NBTUtils.readBlockPos(nbt.getTag("min"));
            max = NBTUtils.readBlockPos(nbt.getTag("max"));
        }
        extendToEncompassBoth(min, max);
    }

    public void initializeCenter(BlockPos center, int size) {
        initializeCenter(center, Utils.vec3i(size));
    }

    public void initializeCenter(BlockPos center, Vec3di size) {
        extendToEncompassBoth(center.subtract(size), center.add(size));
    }

    public List<BlockPos> getBlocksInArea() {
        List<BlockPos> blocks = new ArrayList<>();

        // Add {1,1,1} to make this return all values inside the box

        // TODO: THE ABOVE MIGHT BE WRONG!
        for (BlockPos pos : BlockPos.getAllInBox(min, max.add(Utils.POS_ONE))) {
            blocks.add(pos);
        }

        return blocks;
    }

    @Override
    public Box expand(int amount) {
        if (!isInitialized()) return this;
        Vec3di am = Utils.vec3i(amount);
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
        AxisAlignedBB bb = getBoundingBox();
        if (p.xCoord < bb.minX || p.xCoord >= bb.maxX) return false;
        if (p.yCoord < bb.minY || p.yCoord >= bb.maxY) return false;
        if (p.zCoord < bb.minZ || p.zCoord >= bb.maxZ) return false;
        return true;
    }

    public boolean contains(BlockPos i) {
        return contains(Utils.convert(i));
    }

    @Override
    public BlockPos min() {
        return min;
    }

    @Override
    public BlockPos max() {
        return max;
    }

    public BlockPos size() {
        if (!isInitialized()) return BlockPos.ORIGIN;
        return max.subtract(min).add(Utils.POS_ONE);
    }

    public BlockPos center() {
        return Utils.convertFloor(centerExact());
    }

    public Vec3d centerExact() {
        return Utils.convert(min()).add(Utils.multiply(Utils.convert(size()), 0.5));
    }

    public Box rotateLeft() {
        Matrix4i mat = Matrix4i.makeRotLeftTranslatePositive(this);
        BlockPos newMin = mat.multiplyPosition(min);
        BlockPos newMax = mat.multiplyPosition(max);
        return new Box(newMin, newMax);
    }

    @Override
    public void createLaserData() {
        lasersData = Utils.createLaserDataBox(Utils.convert(min()), Utils.convert(max()));
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setByte("kind", (byte) kind.ordinal());

        if (min != null) nbt.setTag("min", NBTUtils.writeBlockPos(min));
        if (max != null) nbt.setTag("max", NBTUtils.writeBlockPos(max));
    }

    @Override
    public String toString() {
        return "Box[min = " + min + ", max = " + max + "]";
    }

    public Box extendToEncompass(IBox toBeContained) {
        if (toBeContained == null) {
            return this;
        }

        setMin(toBeContained.min());
        setMax(toBeContained.max());

        return this;
    }

    /** IMPORTANT: Use {@link #contains(Vec3d)}instead of the returned {@link AxisAlignedBB#isVecInside(Vec3d)} as the
     * logic is different! */
    public AxisAlignedBB getBoundingBox() {
        return new AxisAlignedBB(min, max.add(Utils.POS_ONE));
    }

    public Box extendToEncompass(Vec3d toBeContained) {
        setMin(Utils.min(min, Utils.convertFloor(toBeContained)));
        setMin(Utils.min(min, Utils.convertCeiling(toBeContained)));
        return this;
    }

    public Box extendToEncompass(BlockPos toBeContained) {
        setMin(Utils.min(min, toBeContained));
        setMin(Utils.min(min, toBeContained));
        return this;
    }

    @Override
    public double distanceTo(BlockPos index) {
        return Math.sqrt(distanceToSquared(index));
    }

    @Override
    public double distanceToSquared(BlockPos index) {
        return closestInsideTo(index).distanceSq(index);
    }

    public BlockPos closestInsideTo(BlockPos toTest) {
        return Utils.max(min(), Utils.min(max(), toTest));
    }

    @Override
    public BlockPos getRandomBlockPos(Random rand) {
        return min().add(Utils.randomBlockPos(rand, size().add(Utils.POS_ONE)));
    }

    @Override
    public void readData(ByteBuf stream) {
        byte flags = stream.readByte();
        kind = Kind.values()[flags & 31];
        boolean initialized = (flags & 64) != 0;
        isVisible = (flags & 32) != 0;
        if (initialized) {
            min = NetworkUtils.readBlockPos(stream);
            max = NetworkUtils.readBlockPos(stream);
        } else {
            min = null;
            max = null;
        }
    }

    @Override
    public void writeData(ByteBuf stream) {
        stream.writeByte((isInitialized() ? 64 : 0) | (isVisible ? 32 : 0) | kind.ordinal());
        if (isInitialized()) {
            NetworkUtils.writeBlockPos(stream, min);
            NetworkUtils.writeBlockPos(stream, max);
        }
    }
}
