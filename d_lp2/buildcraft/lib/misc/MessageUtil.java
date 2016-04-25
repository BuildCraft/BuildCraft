package buildcraft.lib.misc;

import net.minecraft.server.management.PlayerManager.PlayerInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import buildcraft.lib.BCMessageHandler;

public class MessageUtil {
    public static SimpleNetworkWrapper getWrapper() {
        return BCMessageHandler.netWrapper;
    }

    public static void sendToAllWatching(World worldObj, BlockPos pos, IMessage message) {
        if (worldObj instanceof WorldServer) {
            WorldServer server = (WorldServer) worldObj;
            PlayerInstance playerChunkMap = server.getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
            // Slightly ugly hack to iterate through all players watching the chunk
            playerChunkMap.hasPlayerMatchingInRange(0, player -> {
                getWrapper().sendTo(message, player);
                // Always return false so that the iteration doesn't stop early
                return false;
            });
            // We could just use this instead, but that requires extra packet size as we are wrapping our
            // packet in an FML packet and sending it through the vanilla system, which is not really desired
            /** playerChunkMap.sendPacket(getWrapper().getPacketFrom(message)); */
        }
    }
}
