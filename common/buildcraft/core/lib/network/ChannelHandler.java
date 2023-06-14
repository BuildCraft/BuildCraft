/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import java.lang.ref.WeakReference;
import java.util.List;

import buildcraft.api.core.BCLog;
import buildcraft.core.proxy.CoreProxy;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import org.apache.logging.log4j.Level;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.AttributeKey;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

import buildcraft.core.lib.network.command.PacketCommand;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Code based on FMLIndexedMessageToMessageCodec, but since some of its fields
 * are private, I needed a custom version.
 */
@io.netty.channel.ChannelHandler.Sharable
public final class ChannelHandler extends MessageToMessageCodec<FMLProxyPacket, Packet> {
	private static final class DiscriminatorData {
		private final Class<? extends Packet> discriminator;
		private final PacketSide side;

		DiscriminatorData(Class<? extends Packet> discriminator, PacketSide side) {
			this.discriminator = discriminator;
			this.side = side;
		}

		public Class<? extends Packet> getDiscriminator() {
			return discriminator;
		}

		public PacketSide getSide() {
			return side;
		}
	}

	public static final Marker SUSPICIOUS_PACKETS = MarkerManager.getMarker("SuspiciousPackets");
	public static final AttributeKey<ThreadLocal<WeakReference<FMLProxyPacket>>> INBOUNDPACKETTRACKER = new AttributeKey<ThreadLocal<WeakReference<FMLProxyPacket>>>("bc:inboundpacket");
	private TByteObjectHashMap<DiscriminatorData> discriminators = new TByteObjectHashMap<DiscriminatorData>();
	private TObjectByteHashMap<Class<? extends Packet>> types = new TObjectByteHashMap<Class<? extends Packet>>();
	private int maxDiscriminator;

	public ChannelHandler() {
		// Packets common to buildcraft.core.network
		addDiscriminator(0, PacketTileUpdate.class, PacketSide.CLIENT_ONLY);
		addDiscriminator(1, PacketTileState.class, PacketSide.CLIENT_ONLY);
		addDiscriminator(2, PacketNBT.class, PacketSide.BOTH_SIDES);
		addDiscriminator(4, PacketGuiReturn.class, PacketSide.SERVER_ONLY);
		addDiscriminator(5, PacketGuiWidget.class, PacketSide.CLIENT_ONLY);
		addDiscriminator(7, PacketCommand.class, PacketSide.BOTH_SIDES);
		addDiscriminator(8, PacketEntityUpdate.class, PacketSide.CLIENT_ONLY);
		maxDiscriminator = 9;
	}

	public byte getDiscriminator(Class<? extends Packet> clazz) {
		return types.get(clazz);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		ctx.attr(INBOUNDPACKETTRACKER).set(new ThreadLocal<WeakReference<FMLProxyPacket>>());
	}

	public ChannelHandler addDiscriminator(int discriminator, Class<? extends Packet> type) {
		return addDiscriminator(discriminator, type, PacketSide.BOTH_SIDES);
	}

	public ChannelHandler addDiscriminator(int discriminator, Class<? extends Packet> packetType, PacketSide packetSide) {
		discriminators.put((byte) discriminator, new DiscriminatorData(packetType, packetSide));
		types.put(packetType, (byte) discriminator);
		return this;
	}

	protected void logSuspiciousPacketWrongSide(EntityPlayer player, String packetType) {
		BCLog.logger.info(
				SUSPICIOUS_PACKETS,
				"Player {} tried to send packet of type {} to invalid side {}. This could be a false warning due to custom mod/addon interference, or an indicator of hacking/cheating activity.",
				player.getGameProfile(),
				packetType,
				player instanceof EntityPlayerMP ? Side.SERVER : Side.CLIENT);
	}

	protected void logSuspiciousPacketWrongDiscriminator(EntityPlayer player, int typeId) {
		BCLog.logger.info(
				SUSPICIOUS_PACKETS,
				"Player {} tried to send packet of invalid type {}. This could be a false warning due to custom mod/addon interference, or an indicator of hacking/cheating activity.",
				player.getGameProfile(),
				typeId);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg, List<Object> out) throws Exception {
		ByteBuf buffer = Unpooled.buffer();
		Class<? extends Packet> clazz = msg.getClass();
		byte discriminator = types.get(clazz);
		buffer.writeByte(discriminator);
		msg.writeData(buffer);
		FMLProxyPacket proxy = new FMLProxyPacket(buffer.copy(), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
		WeakReference<FMLProxyPacket> ref = ctx.attr(INBOUNDPACKETTRACKER).get().get();
		FMLProxyPacket old = ref == null ? null : ref.get();
		if (old != null) {
			proxy.setDispatcher(old.getDispatcher());
		}
		out.add(proxy);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception {
		testMessageValidity(msg);
		ByteBuf payload = msg.payload();
		byte discriminator = payload.readByte();
		DiscriminatorData data = discriminators.get(discriminator);
		if (data == null) {
			INetHandler handler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			EntityPlayer player = CoreProxy.proxy.getPlayerFromNetHandler(handler);
			logSuspiciousPacketWrongDiscriminator(player, discriminator);
			return;
		}
		PacketSide expectedSide = ctx.channel().attr(NetworkRegistry.CHANNEL_SOURCE).get() == Side.CLIENT
				? PacketSide.CLIENT_ONLY
				: PacketSide.SERVER_ONLY;
		if (!data.getSide().contains(expectedSide)) {
			INetHandler handler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			EntityPlayer player = CoreProxy.proxy.getPlayerFromNetHandler(handler);
			logSuspiciousPacketWrongSide(player, data.getDiscriminator().getSimpleName());
		} else {
			Packet newMsg = data.getDiscriminator().newInstance();
			ctx.attr(INBOUNDPACKETTRACKER).get().set(new WeakReference<>(msg));
			newMsg.readData(payload.slice());
			out.add(newMsg);
		}
	}

	/**
	 * Called to verify the message received. This can be used to hard disconnect in case of an unexpected packet,
	 * say due to a weird protocol mismatch. Use with caution.
	 * @param msg
	 */
	protected void testMessageValidity(FMLProxyPacket msg) {
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		FMLLog.log(Level.ERROR, cause, "BC ChannelHandler exception caught");
		super.exceptionCaught(ctx, cause);
	}

	public void registerPacketType(Class<? extends Packet> packetType) {
		registerPacketType(packetType, PacketSide.BOTH_SIDES);
	}

	public void registerPacketType(Class<? extends Packet> packetType, PacketSide packetSide) {
		addDiscriminator(maxDiscriminator++, packetType, packetSide);
	}
}
