/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe.behaviour;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventStatement;

import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.collect.OrderedEnumMap;
import buildcraft.lib.net.PacketBufferBC;

public abstract class PipeBehaviourDirectional extends PipeBehaviour {
    public static final OrderedEnumMap<Direction> ROTATION_ORDER = VanillaRotationHandlers.ROTATE_FACING;

    protected EnumPipePart currentDir = EnumPipePart.CENTER;

    public PipeBehaviourDirectional(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDirectional(IPipe pipe, NbtCompound nbt) {
        super(pipe, nbt);
        setCurrentDir(NBTUtilBC.readEnum(nbt.get("currentDir"), Direction.class));
    }

    @Override
    public NbtCompound writeToNbt() {
        NbtCompound nbt = super.writeToNbt();
        nbt.put("currentDir", NBTUtilBC.writeEnum(getCurrentDir()));
        return nbt;
    }

    @Override
    public void writePayload(PacketByteBuf buffer, EnvType side) {
        super.writePayload(buffer, side);
        PacketBufferBC bufBc = PacketBufferBC.asPacketBufferBc(buffer);
        bufBc.writeEnumValue(currentDir);
    }

    @Override
    public void readPayload(PacketByteBuf buffer, EnvType side, Object ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        currentDir = PacketBufferBC.asPacketBufferBc(buffer).readEnumValue(EnumPipePart.class);
    }

    @Override
    public boolean onPipeActivate(PlayerEntity player, HitResult trace, float hitX, float hitY, float hitZ,
        EnumPipePart part) {
        // STUB(R.Chen): EntityUtil.getWrenchHand not migrated — wrench detection disabled.
        return false;
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isClient) {
            return;
        }

        if (!canFaceDirection(getCurrentDir())) {
            if (!advanceFacing()) {
                setCurrentDir(null);
            }
        }
    }

    protected abstract boolean canFaceDirection(Direction dir);

    /** @return True if the facing direction changed. */
    public boolean advanceFacing() {
        Direction current = currentDir.face;
        for (int i = 0; i < 6; i++) {
            current = ROTATION_ORDER.next(current);
            if (canFaceDirection(current)) {
                setCurrentDir(current);
                return true;
            }
        }
        return false;
    }

    @Nullable
    protected Direction getCurrentDir() {
        return currentDir.face;
    }

    protected void setCurrentDir(Direction setTo) {
        if (this.currentDir.face == setTo) {
            return;
        }
        this.currentDir = EnumPipePart.fromFacing(setTo);
        if (!pipe.getHolder().getPipeWorld().isClient) {
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

    @PipeEventHandler
    public void addActions(PipeEventStatement.AddActionInternal event) {
        // STUB(R.Chen): statement layer deferred to Phase 4E — BCTransportStatements.ACTION_PIPE_DIRECTION not registered yet.
    }
}
