/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import java.io.IOException;
import java.util.Collections;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeEventStatement;

import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.NBTUtilBC;

import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.statements.ActionPipeColor;

public class PipeBehaviourLapis extends PipeBehaviour {
    private EnumDyeColor colour = EnumDyeColor.WHITE;

    public PipeBehaviourLapis(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourLapis(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        colour = NBTUtilBC.readEnum(nbt.getTag("colour"), EnumDyeColor.class);
        if (colour == null) {
            colour = EnumDyeColor.WHITE;
        }
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("colour", NBTUtilBC.writeEnum(colour));
        return nbt;
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        if (side == Side.SERVER) {
            buffer.writeByte(colour.getMetadata());
        }
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == Side.CLIENT) {
            colour = EnumDyeColor.byMetadata(buffer.readUnsignedByte());
        }
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return colour.getMetadata();
    }

    @Override
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (player.world.isRemote) {
            return EntityUtil.getWrenchHand(player) != null;
        }
        if (EntityUtil.getWrenchHand(player) != null) {
            EntityUtil.activateWrench(player);
            int n = colour.getMetadata() + (player.isSneaking() ? 15 : 1);
            colour = EnumDyeColor.byMetadata(n & 15);
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
