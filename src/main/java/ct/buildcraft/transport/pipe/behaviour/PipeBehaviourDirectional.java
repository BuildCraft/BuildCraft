/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe.behaviour;

import java.io.IOException;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import ct.buildcraft.api.transport.pipe.PipeBehaviour;
import ct.buildcraft.api.transport.pipe.PipeEventActionActivate;
import ct.buildcraft.api.transport.pipe.PipeEventHandler;
import ct.buildcraft.api.transport.pipe.PipeEventStatement;
import ct.buildcraft.lib.block.VanillaRotationHandlers;
import ct.buildcraft.lib.misc.EntityUtil;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.collect.OrderedEnumMap;
import ct.buildcraft.transport.BCTransportStatements;
import ct.buildcraft.transport.statements.ActionPipeDirection;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;

public abstract class PipeBehaviourDirectional extends PipeBehaviour {
    public static final OrderedEnumMap<Direction> ROTATION_ORDER = VanillaRotationHandlers.ROTATE_FACING;

    protected EnumPipePart currentDir = EnumPipePart.CENTER;

    public PipeBehaviourDirectional(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDirectional(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        setCurrentDir(NBTUtilBC.readEnum(nbt.get("currentDir"), Direction.class));
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.put("currentDir", NBTUtilBC.writeEnum(getCurrentDir()));
        return nbt;
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, LogicalSide side) {

        super.writePayload(buffer, side);

        buffer.writeEnum(currentDir);
    }


    public void readPayload(FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {

    	super.readPayload(buffer, side, ctx);

        currentDir = buffer.readEnum(EnumPipePart.class);
    }

    @Override
    public boolean onPipeActivate(Player player, BlockHitResult trace, Level level,
        EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            EntityUtil.activateWrench(player, trace);

            if (part == EnumPipePart.CENTER) {
                return advanceFacing();
            } else if (part.face != getCurrentDir() && canFaceDirection(part.face)) {
                setCurrentDir(part.face);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isClientSide()) {
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
    public Direction getCurrentDir() {
        return currentDir.face;
    }

    protected void setCurrentDir(Direction setTo) {
        if (this.currentDir.face == setTo) {
            return;
        }
        this.currentDir = EnumPipePart.fromFacing(setTo);
//        if (!pipe.getHolder().getPipeWorld().isClientSide()) {
        if(Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

    @PipeEventHandler
    public void addActions(PipeEventStatement.AddActionInternal event) {
        for (Direction face : Direction.values()) {
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

	@Override
	public void rotate(Rotation rot) {
		if(currentDir == EnumPipePart.CENTER || currentDir.ordinal() <2) {
			return;
		}
		currentDir = EnumPipePart.fromFacing(rot.rotate(getCurrentDir()));
	}
    
    
}
