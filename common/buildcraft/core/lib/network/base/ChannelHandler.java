/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network.base;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;

import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.network.PacketEntityUpdate;
import buildcraft.core.lib.network.PacketGuiReturn;
import buildcraft.core.lib.network.PacketGuiWidget;
import buildcraft.core.lib.network.PacketSlotChange;
import buildcraft.core.lib.network.PacketTileState;
import buildcraft.core.lib.network.PacketTileUpdate;
import buildcraft.core.lib.network.PacketUpdate;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.proxy.CoreProxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ChannelHandler extends FMLIndexedMessageToMessageCodec<Packet> {
    public static boolean recordStats = false;

    public static ChannelHandler createChannelHandler() {
        return new Switcher();
    }

    private int maxDiscriminator;

    protected ChannelHandler() {
        // Packets common to buildcraft.core.network
        addDiscriminator(0, PacketTileUpdate.class);
        addDiscriminator(1, PacketTileState.class);
        addDiscriminator(2, PacketSlotChange.class);
        addDiscriminator(3, PacketGuiReturn.class);
        addDiscriminator(4, PacketGuiWidget.class);
        addDiscriminator(5, PacketUpdate.class);
        addDiscriminator(6, PacketCommand.class);
        addDiscriminator(7, PacketEntityUpdate.class);
        maxDiscriminator = 8;
    }

    public void registerPacketType(Class<? extends Packet> packetType) {
        addDiscriminator(maxDiscriminator++, packetType);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, Packet packet, ByteBuf data) throws Exception {
        try {
            packet.writeData(data);
        } catch (Throwable t) {
            BCLog.logger.error("A packet failed to write its data! THIS IS VERY BAD!", t);
        }
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer getMinecraftPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, Packet packet) {
        INetHandler handler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        packet.readData(data);
        packet.player = CoreProxy.proxy.getPlayerFromNetHandler(handler);
    }

    public static class Switcher extends ChannelHandler {
        private final ChannelHandler handler, tracker;

        public Switcher() {
            handler = new ChannelHandler();
            tracker = new ChannelHandlerStats();
        }

        @Override
        public void registerPacketType(Class<? extends Packet> packetType) {
            handler.registerPacketType(packetType);
            tracker.registerPacketType(packetType);
        }

        private ChannelHandler channelHandler() {
            return recordStats ? tracker : handler;
        }

        @Override
        public void encodeInto(ChannelHandlerContext ctx, Packet msg, ByteBuf target) throws Exception {
            channelHandler().encodeInto(ctx, msg, target);
        }

        @Override
        public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, Packet msg) {
            channelHandler().decodeInto(ctx, source, msg);
        }
    }
}
