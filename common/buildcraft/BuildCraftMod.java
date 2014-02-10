package buildcraft;

import java.util.EnumMap;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.FMLOutboundHandler.OutboundTarget;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.world.World;
import buildcraft.core.network.BuildCraftPacket;

public class BuildCraftMod {
	public EnumMap<Side, FMLEmbeddedChannel> channels;
	
	public void sendToPlayers(Packet packet, World world, int x, int y, int z, int maxDistance) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
		channels.get(Side.SERVER).writeOutbound(packet);
	}
	
	public void sendToPlayers(BuildCraftPacket packet, World world, int x, int y, int z, int maxDistance) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
		channels.get(Side.SERVER).writeOutbound(packet);
	}

	public void sendToPlayer(EntityPlayer entityplayer, BuildCraftPacket packet) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(entityplayer);
		channels.get(Side.SERVER).writeOutbound(packet);
	}
	
	public void replyToPlayer(EntityPlayer entityplayer, BuildCraftPacket packet) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.REPLY);
		channels.get(Side.SERVER).writeOutbound(packet);
	}

	public void sendToServer(BuildCraftPacket packet) {
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeOutbound(packet);
	}
}