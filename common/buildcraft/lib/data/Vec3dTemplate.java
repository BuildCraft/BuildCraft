package buildcraft.lib.data;

import net.minecraft.nbt.NBTBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.misc.NBTUtils;

public enum Vec3dTemplate implements IDataTemplate {
    INSTANCE;

    @Override
    public NBTBase readFromPacketBuffer(PacketBuffer buffer) {
        Vec3d vec = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return NBTUtils.writeVec3d(vec);
    }

    @Override
    public void writeToPacketBuffer(NBTBase nbt, PacketBuffer buffer) {
        Vec3d vec = NBTUtils.readVec3d(nbt);
        buffer.writeDouble(vec.xCoord);
        buffer.writeDouble(vec.yCoord);
        buffer.writeDouble(vec.zCoord);
    }
}
