package buildcraft.lib.data;

import net.minecraft.nbt.NBTBase;
import net.minecraft.network.PacketBuffer;

public interface IDataTemplate {

    NBTBase readFromPacketBuffer(PacketBuffer buffer);

    void writeToPacketBuffer(NBTBase nbt, PacketBuffer buffer);
}
