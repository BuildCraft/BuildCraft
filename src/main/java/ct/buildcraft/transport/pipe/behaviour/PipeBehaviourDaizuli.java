/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe.behaviour;

import java.io.IOException;
import java.util.Collections;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import ct.buildcraft.api.transport.pipe.PipeEventActionActivate;
import ct.buildcraft.api.transport.pipe.PipeEventHandler;
import ct.buildcraft.api.transport.pipe.PipeEventItem;
import ct.buildcraft.api.transport.pipe.PipeEventStatement;
import ct.buildcraft.lib.misc.EntityUtil;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.transport.BCTransportStatements;
import ct.buildcraft.transport.statements.ActionPipeColor;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class PipeBehaviourDaizuli extends PipeBehaviourDirectional {
    private DyeColor colour = DyeColor.WHITE;

    public PipeBehaviourDaizuli(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDaizuli(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        colour = NBTUtilBC.readEnum(nbt.get("colour"), DyeColor.class);
        if (colour == null) {
            colour = DyeColor.WHITE;
        }
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.put("colour", NBTUtilBC.writeEnum(colour));
        return nbt;
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(buffer, side);
        if (side == LogicalSide.SERVER) {
            buffer.writeByte(colour.getId());
        }
    }

    @Override
    public void readPayload(FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            colour = DyeColor.byId(buffer.readUnsignedByte());
        }
    }

    @Override
    public int getTextureIndex(Direction face) {
        if (face != currentDir.face && face != null) {
            return 16;
        }
        return colour.getId();
    }

    @Override
    protected boolean canFaceDirection(Direction dir) {
        return pipe.isConnected(dir);
    }

    @Override
    public boolean onPipeActivate(Player player, BlockHitResult trace, Level level, EnumPipePart part) {
        if (part != EnumPipePart.CENTER && part != currentDir) {
            // Activating the centre of a pipe always falls back to changing the colour
            // And so does clicking on the current facing side
            return super.onPipeActivate(player, trace, level, part);
        }
        if (player.level.isClientSide()) {
            return EntityUtil.getWrenchHand(player) != null;
        }
        if (EntityUtil.getWrenchHand(player) != null) {
            EntityUtil.activateWrench(player, trace);
            int n = colour.getId() + (player.isSteppingCarefully() ? 15 : 1);
            colour = DyeColor.byId(n & 15);
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
            return true;
        }
        return false;
    }

    @PipeEventHandler
    public void sideCheck(PipeEventItem.SideCheck sideCheck) {
        if (colour == sideCheck.colour) {
            sideCheck.disallowAllExcept(currentDir.face);
        } else {
            sideCheck.disallow(currentDir.face);
        }
    }

    @Override
    public void addActions(PipeEventStatement.AddActionInternal event) {
        super.addActions(event);
        Collections.addAll(event.actions, BCTransportStatements.ACTION_PIPE_COLOUR);
    }

    @Override
    public void onActionActivate(PipeEventActionActivate event) {
        super.onActionActivate(event);
        if (event.action instanceof ActionPipeColor) {
            ActionPipeColor action = ((ActionPipeColor) event.action);
            if (this.colour != action.color) {
                this.colour = action.color;
                pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
            }
        }
    }
}
