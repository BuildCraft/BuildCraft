package buildcraft.lib.data;

import net.minecraft.nbt.NBTBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import buildcraft.core.lib.utils.NBTUtils;

public enum BlockPosTemplate implements IDataTemplate {
    INSTANCE;

    @Override
    public NBTBase readFromPacketBuffer(PacketBuffer buffer) {
        BlockPos pos = buffer.readBlockPos();
        return NBTUtils.writeBlockPos(pos);
    }

    @Override
    public void writeToPacketBuffer(NBTBase nbt, PacketBuffer buffer) {
        BlockPos pos = NBTUtils.readBlockPos(nbt);
        buffer.writeBlockPos(pos);
    }
}
