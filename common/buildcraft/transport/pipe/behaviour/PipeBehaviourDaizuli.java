/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe.behaviour;

import java.io.IOException;

import net.fabricmc.api.EnvType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeEventStatement;

import buildcraft.lib.misc.NBTUtilBC;

public class PipeBehaviourDaizuli extends PipeBehaviourDirectional {
    private DyeColor colour = DyeColor.WHITE;

    public PipeBehaviourDaizuli(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDaizuli(IPipe pipe, NbtCompound nbt) {
        super(pipe, nbt);
        colour = NBTUtilBC.readEnum(nbt.get("colour"), DyeColor.class);
        if (colour == null) {
            colour = DyeColor.WHITE;
        }
    }

    @Override
    public NbtCompound writeToNbt() {
        NbtCompound nbt = super.writeToNbt();
        nbt.put("colour", NBTUtilBC.writeEnum(colour));
        return nbt;
    }

    @Override
    public void writePayload(PacketByteBuf buffer, EnvType side) {
        super.writePayload(buffer, side);
        if (side == EnvType.SERVER) {
            buffer.writeByte(colour.getId());
        }
    }

    @Override
    public void readPayload(PacketByteBuf buffer, EnvType side, Object ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == EnvType.CLIENT) {
            colour = DyeColor.byId(buffer.readUnsignedByte() & 15);
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
        return true;
    }

    @Override
    public boolean onPipeActivate(PlayerEntity player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        // STUB(R.Chen): EntityUtil.getWrenchHand not migrated — wrench detection disabled.
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
        // STUB(R.Chen): statement layer deferred to Phase 4E — BCTransportStatements.ACTION_PIPE_DIRECTION/COLOUR not registered yet.
    }
}
