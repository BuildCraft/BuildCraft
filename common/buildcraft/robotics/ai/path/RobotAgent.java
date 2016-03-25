package buildcraft.robotics.ai.path;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.robotics.path.BlockPosDestination;
import buildcraft.robotics.path.IAgent;
import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualDestination;

public class RobotAgent implements IAgent<BlockPos> {
    private final BlockPos current;
    private final BlockPosDestination destination;

    public RobotAgent(BlockPos current, BlockPos dest) {
        this(current, new BlockPosDestination(dest, dest));
    }

    public RobotAgent(BlockPos current, BlockPosDestination destination) {
        this.current = current;
        this.destination = destination;
    }

    public static RobotAgent loadFromNBT(NBTTagCompound nbt) {
        BlockPos current = NBTUtils.readBlockPos(nbt.getTag("current"));
        BlockPosDestination dest = new BlockPosDestination();
        dest.deserializeNBT(nbt.getCompoundTag("destination"));
        return new RobotAgent(current, dest);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("current", NBTUtils.writeBlockPos(current));
        nbt.setTag("destination", destination.serializeNBT());
        return nbt;
    }

    @Override
    public BlockPos getCurrentPos() {
        return current;
    }

    @Override
    public IVirtualDestination<BlockPos> getDestination() {
        return destination;
    }
}
