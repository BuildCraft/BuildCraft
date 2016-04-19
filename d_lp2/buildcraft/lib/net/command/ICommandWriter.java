package buildcraft.lib.net.command;

import net.minecraft.network.PacketBuffer;

@FunctionalInterface
public interface ICommandWriter {
    void write(PacketBuffer buffer);
}