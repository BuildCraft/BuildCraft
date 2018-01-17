/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
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
import buildcraft.api.transport.pipe.PipeEventStatement;

import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.collect.OrderedEnumMap;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.statements.ActionPipeDirection;

public abstract class PipeBehaviourDirectional extends PipeBehaviour {
    public static final OrderedEnumMap<EnumFacing> ROTATION_ORDER = VanillaRotationHandlers.ROTATE_FACING;

    protected EnumPipePart currentDir = EnumPipePart.CENTER;

    public PipeBehaviourDirectional(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDirectional(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        setCurrentDir(NBTUtilBC.readEnum(nbt.getTag("currentDir"), EnumFacing.class));
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("currentDir", NBTUtilBC.writeEnum(getCurrentDir()));
        return nbt;
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        PacketBufferBC bufBc = PacketBufferBC.asPacketBufferBc(buffer);
        bufBc.writeEnumValue(currentDir);
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        currentDir = PacketBufferBC.asPacketBufferBc(buffer).readEnumValue(EnumPipePart.class);
    }

    @Override
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ,
        EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            EntityUtil.activateWrench(player);
            if (part.face != getCurrentDir()) {
                if (part == EnumPipePart.CENTER) {
                    return advanceFacing();
                } else {
                    if (canFaceDirection(part.face)) {
                        setCurrentDir(part.face);
                    }
                }
            }
            return true;
        }
        return false;
    }

    protected abstract boolean canFaceDirection(EnumFacing dir);

    /** @return True if the facing direction changed. */
    public boolean advanceFacing() {
        EnumFacing current = currentDir.face;
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
    protected EnumFacing getCurrentDir() {
        return currentDir.face;
    }

    protected void setCurrentDir(EnumFacing setTo) {
        this.currentDir = EnumPipePart.fromFacing(setTo);
        if (!pipe.getHolder().getPipeWorld().isRemote) {
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

    @PipeEventHandler
    public void addActions(PipeEventStatement.AddActionInternal event) {
        for (EnumFacing face : EnumFacing.VALUES) {
            if (canFaceDirection(face)) {
                event.actions.add(BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]);
            }
        }
    }

    @PipeEventHandler
    public void onActionActivate(PipeEventActionActivate event) {
        if (event.action instanceof ActionPipeDirection) {
            ActionPipeDirection action = (ActionPipeDirection) event.action;
            if (canFaceDirection(action.direction)) {
                setCurrentDir(action.direction);
            }
        }
    }
}
