/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.util.EnumMap;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.FMLOutboundHandler.OutboundTarget;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import buildcraft.api.core.BCLog;
import buildcraft.core.DefaultProps;
import buildcraft.core.network.BuildCraftPacket;

public class BuildCraftMod {
	public EnumMap<Side, FMLEmbeddedChannel> channels;

	public void sendToPlayers(BuildCraftPacket packet, World world, int x, int y, int z, int maxDistance) {
		try {
			channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
					.set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
			channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
					.set(new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, maxDistance));
			channels.get(Side.SERVER).writeOutbound(packet);
		} catch (Throwable t) {
			BCLog.logger.log(Level.WARN, "sendToPlayers crash", t);
		}
	}

	public void sendToPlayersNear(BuildCraftPacket packet, TileEntity tileEntity, int maxDistance) {
		sendToPlayers(packet, tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, maxDistance);
	}

	public void sendToPlayersNear(BuildCraftPacket packet, TileEntity tileEntity) {
		sendToPlayersNear(packet, tileEntity, DefaultProps.NETWORK_UPDATE_RANGE);
	}

	public void sendToWorld(BuildCraftPacket packet, World world) {
		try {
			channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
					.set(FMLOutboundHandler.OutboundTarget.DIMENSION);
			channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
					.set(world.provider.dimensionId);
			channels.get(Side.SERVER).writeOutbound(packet);
		} catch (Throwable t) {
			BCLog.logger.log(Level.WARN, "sendToWorld crash", t);
		}
	}
	
	public void sendToPlayer(EntityPlayer entityplayer, BuildCraftPacket packet) {
		try {
			channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
					.set(FMLOutboundHandler.OutboundTarget.PLAYER);
			channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(entityplayer);
			channels.get(Side.SERVER).writeOutbound(packet);
		} catch (Throwable t) {
			String name = entityplayer.getDisplayName();

			if (name == null) {
				name = "<no name>";
			}

			BCLog.logger.log(Level.WARN, "sendToPlayer \"" + name + "\" crash", t);
		}
	}
	
	public void sendToAll(BuildCraftPacket packet) {
		try {
			channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
					.set(FMLOutboundHandler.OutboundTarget.ALL);
			channels.get(Side.SERVER).writeOutbound(packet);
		} catch (Throwable t) {
			BCLog.logger.log(Level.WARN, "sendToAll crash", t);
		}
	}

	public void sendToServer(BuildCraftPacket packet) {
		try {
			channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
			channels.get(Side.CLIENT).writeOutbound(packet);
		} catch (Throwable t) {
			BCLog.logger.log(Level.WARN, "sendToServer crash", t);
		}
	}
}