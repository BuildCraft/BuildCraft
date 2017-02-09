package buildcraft.core.lib.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

import buildcraft.core.lib.network.ChannelHandler;
import buildcraft.core.lib.network.Packet;

public final class ThreadSafeUtils {
	private static final ThreadLocal<Chunk> lastChunk = new ThreadLocal<Chunk>();

	private ThreadSafeUtils() {

	}

	public static Chunk getChunk(World world, int x, int z) {
		Chunk chunk;
		chunk = lastChunk.get();

		if (chunk != null) {
			if (chunk.isChunkLoaded) {
				if (chunk.worldObj == world && chunk.xPosition == x && chunk.zPosition == z) {
					return chunk;
				}
			} else {
				lastChunk.set(null);
			}
		}

		IChunkProvider provider = world.getChunkProvider();
		// These probably won't guarantee full thread safety, but it's our best bet.
		if (!Utils.CAULDRON_DETECTED && provider instanceof ChunkProviderServer) {
			// Slight optimization
			chunk = (Chunk) ((ChunkProviderServer) provider).loadedChunkHashMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
		} else {
			chunk = provider.chunkExists(x, z) ? provider.provideChunk(x, z) : null;
		}

		if (chunk != null) {
			lastChunk.set(chunk);
		}
		return chunk;
	}

	/**
	 * This function assumes that you're using BC's ChannelHandler system, which only has one
	 * channel handler. This might get very messy otherwise.
	 * TODO: HACK - Can we rewrite this for BC 7.1 along with the whole network system to be somewhat more sane? Please?
	 *
	 * @param packet
	 * @param channel
	 * @return
	 */
	public static net.minecraft.network.Packet generatePacketFrom(Packet packet, FMLEmbeddedChannel channel) {
		ByteBuf data = Unpooled.buffer();
		for (io.netty.channel.ChannelHandler h : channel.pipeline().toMap().values()) {
			if (h instanceof ChannelHandler) {
				data.writeByte(((ChannelHandler) h).getDiscriminator(packet.getClass()));
				break;
			}
		}
		packet.writeData(data);
		return new FMLProxyPacket(data.copy(), channel.attr(NetworkRegistry.FML_CHANNEL).get());
	}
}
