/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 */
package ct.buildcraft.api.core;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Comparable integer block position used by the classic BuildCraft robotics API. */
public class BlockIndex implements Comparable<BlockIndex> {
    public int x;
    public int y;
    public int z;

    public BlockIndex() {
    }

    public BlockIndex(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockIndex(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockIndex(CompoundTag tag) {
        this(tag.getInt("i"), tag.getInt("j"), tag.getInt("k"));
    }

    public BlockIndex(Entity entity) {
        this(entity.blockPosition());
    }

    public BlockIndex(BlockEntity blockEntity) {
        this(blockEntity.getBlockPos());
    }

    public BlockPos toBlockPos() {
        return new BlockPos(x, y, z);
    }

    public void writeTo(CompoundTag tag) {
        tag.putInt("i", x);
        tag.putInt("j", y);
        tag.putInt("k", z);
    }

    public BlockState getBlockState(BlockGetter level) {
        return level.getBlockState(toBlockPos());
    }

    public Block getBlock(BlockGetter level) {
        return getBlockState(level).getBlock();
    }

    @Override
    public int compareTo(BlockIndex other) {
        if (other.x < x) return 1;
        if (other.x > x) return -1;
        if (other.z < z) return 1;
        if (other.z > z) return -1;
        if (other.y < y) return 1;
        if (other.y > y) return -1;
        return 0;
    }

    public boolean nextTo(BlockIndex other) {
        return (Math.abs(other.x - x) <= 1 && other.y == y && other.z == z)
                || (other.x == x && Math.abs(other.y - y) <= 1 && other.z == z)
                || (other.x == x && other.y == y && Math.abs(other.z - z) <= 1);
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + z + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockIndex other)) {
            return false;
        }
        return other.x == x && other.y == y && other.z == z;
    }

    @Override
    public int hashCode() {
        return (x * 37 + y) * 37 + z;
    }
}
