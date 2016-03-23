package buildcraft.robotics.path;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;

import net.minecraftforge.common.util.INBTSerializable;

import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualDestination;
import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualPoint;

public class BlockPosDestination implements IVirtualDestination<BlockPos>, INBTSerializable<NBTTagCompound> {
    private final List<BlockPosDestination> additionalDestinations;
    private BlockPos min, max;
    private final Set<BlockPos> positions = new HashSet<>();

    public BlockPosDestination(BlockPos min, BlockPos max) {
        this.min = Utils.min(min, max);
        this.max = Utils.max(min, max);
        additionalDestinations = new LinkedList<>();
    }

    public BlockPosDestination addVolume(BlockPos min, BlockPos max) {
        BlockPosDestination additional = new BlockPosDestination(min, max);
        additionalDestinations.add(additional);
        return this;
    }

    public BlockPosDestination addMultiple(BlockPos... positions) {
        for (BlockPos p : positions)
            this.positions.add(p);
        return this;
    }

    @Override
    public boolean isDestination(IVirtualPoint<BlockPos> point) {
        if (Utils.isInside(point.getPoint(), min, max)) return true;
        for (BlockPosDestination dest : additionalDestinations) {
            if (dest.isDestination(point)) return true;
        }
        return false;
    }

    @Override
    public double heuristicCostToDestination(IVirtualPoint<BlockPos> from) {
        BlockPos closest = Utils.getClosestInside(from.getPoint(), min, max);
        double lowest = closest.distanceSq(from.getPoint());
        for (BlockPos p : positions) {
            lowest = Math.min(lowest, p.distanceSq(p));
        }
        lowest = Math.sqrt(lowest);
        for (BlockPosDestination contained : additionalDestinations) {
            lowest = Math.min(lowest, contained.heuristicCostToDestination(from));
        }
        return lowest;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("min", NBTUtils.writeBlockPos(min));
        nbt.setTag("max", NBTUtils.writeBlockPos(max));
        NBTTagList setList = new NBTTagList();
        for (BlockPos p : positions) {
            setList.appendTag(NBTUtils.writeBlockPos(p));
        }
        nbt.setTag("positions", setList);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }
}
