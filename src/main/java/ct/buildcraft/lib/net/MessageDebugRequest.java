/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.net;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.lib.item.ItemDebugger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class MessageDebugRequest {
	private BlockPos pos;
	private Direction side;

	@SuppressWarnings("unused")
	public MessageDebugRequest() {
	}

	public MessageDebugRequest(BlockPos pos, Direction side) {
		this.pos = pos;
		this.side = side;
	}

	public static void toBytes(MessageDebugRequest msg, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(msg.pos);
		buffer.writeEnum(msg.side);
	}

	public MessageDebugRequest(FriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
		side = buffer.readEnum(Direction.class);
	}

	public static final BiConsumer<MessageDebugRequest, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if(player == null||ctx.get().getDirection()!=NetworkDirection.PLAY_TO_SERVER)
				return;
			if (!ItemDebugger.isShowDebugInfo(player)) { 
				MessageManager.sendTo(new MessageDebugResponse(), player);
				return;
			}
			BlockEntity tile = player.level.getBlockEntity(message.pos);
			if (tile instanceof IDebuggable) {
				List<String> left = new ArrayList<>();
				List<String> right = new ArrayList<>();
				((IDebuggable) tile).getDebugInfo(left, right, message.side);
				MessageManager.sendTo(new MessageDebugResponse(left, right), player);
			}
		});
		ctx.get().setPacketHandled(true);
	};
}
