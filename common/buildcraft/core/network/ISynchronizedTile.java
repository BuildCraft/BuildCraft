package buildcraft.core.network;

public interface ISynchronizedTile {
	BuildCraftPacket getPacketUpdate();
	BuildCraftPacket getPacketDescription();
}
