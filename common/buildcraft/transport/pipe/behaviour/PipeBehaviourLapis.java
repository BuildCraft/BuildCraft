/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.statements.ActionPipeColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.Collections;

public class PipeBehaviourLapis extends PipeBehaviour {
    private DyeColor colour = DyeColor.WHITE;

    public PipeBehaviourLapis(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourLapis(IPipe pipe, CompoundNBT nbt) {
        super(pipe, nbt);
        colour = NBTUtilBC.readEnum(nbt.get("colour"), DyeColor.class);
        if (colour == null) {
            colour = DyeColor.WHITE;
        }
    }

    @Override
    public CompoundNBT writeToNbt() {
        CompoundNBT nbt = super.writeToNbt();
        nbt.put("colour", NBTUtilBC.writeEnum(colour));
        return nbt;
    }

    @Override
    public void writePayload(PacketBuffer buffer, Dist side) {
        super.writePayload(buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            buffer.writeByte(colour.getId());
        }
    }

    @Override
//    public void readPayload(PacketBuffer buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(PacketBuffer buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            colour = DyeColor.byId(buffer.readUnsignedByte());
        }
    }

    @Override
    public int getTextureIndex(Direction face) {
        return colour.getId();
    }

    @Override
    public boolean onPipeActivate(PlayerEntity player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (player.level.isClientSide) {
            return EntityUtil.getWrenchHand(player) != null;
        }
        if (EntityUtil.getWrenchHand(player) != null) {
            EntityUtil.activateWrench(player, trace);
//            int n = colour.getMetadata() + (player.isSneaking() ? 15 : 1);
            int n = colour.getId() + (player.isShiftKeyDown() ? 15 : 1);
//            colour = DyeColor.byMetadata(n & 15);
            colour = DyeColor.byId(n & 15);
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
            return true;
        }
        return false;
    }

    @PipeEventHandler
    public void onReachCenter(PipeEventItem.ReachCenter reachCenter) {
        reachCenter.colour = colour;
    }

    @PipeEventHandler
    public static void addActions(PipeEventStatement.AddActionInternal event) {
        Collections.addAll(event.actions, BCTransportStatements.ACTION_PIPE_COLOUR);
    }

    @PipeEventHandler
    public void onActionActivated(PipeEventActionActivate event) {
        if (event.action instanceof ActionPipeColor) {
            ActionPipeColor action = ((ActionPipeColor) event.action);
            if (this.colour != action.color) {
                this.colour = action.color;
                pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
            }
        }
    }
}
