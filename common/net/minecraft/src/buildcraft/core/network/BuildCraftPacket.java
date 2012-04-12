package net.minecraft.src.buildcraft.core.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.Packet;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.forge.packets.ForgePacket;

public abstract class BuildCraftPacket extends ForgePacket {

	protected boolean isChunkDataPacket = false;
	protected String channel = "BC";

	@Override
    public Packet getPacket() {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        try {
            data.writeByte(getID());
            writeData(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = channel;
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        packet.isChunkDataPacket = this.isChunkDataPacket;
        return packet;
    }

}
