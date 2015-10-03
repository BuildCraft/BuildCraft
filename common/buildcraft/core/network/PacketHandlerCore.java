package buildcraft.core.network;

import io.netty.channel.ChannelHandlerContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.network.PacketHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.tablet.PacketTabletMessage;
import buildcraft.core.tablet.TabletBase;
import buildcraft.core.tablet.manager.TabletManagerClient;
import buildcraft.core.tablet.manager.TabletManagerServer;

public class PacketHandlerCore extends PacketHandler {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
		super.channelRead0(ctx, packet);
		INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
		EntityPlayer player = CoreProxy.proxy.getPlayerFromNetHandler(netHandler);

		switch (packet.getID()) {
			case PacketIds.TABLET_MESSAGE: {
				if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
					handleTabletClient((PacketTabletMessage) packet);
				} else {
					handleTabletServer(player, (PacketTabletMessage) packet);
				}
			}
			break;
		}
	}

	private void handleTabletClient(PacketTabletMessage packet) {
		TabletBase tablet = TabletManagerClient.INSTANCE.get().getTablet();
		tablet.receiveMessage(packet.getTag());
	}

	private void handleTabletServer(EntityPlayer player, PacketTabletMessage packet) {
		TabletBase tablet = TabletManagerServer.INSTANCE.get(player);
		tablet.receiveMessage(packet.getTag());
	}
}
