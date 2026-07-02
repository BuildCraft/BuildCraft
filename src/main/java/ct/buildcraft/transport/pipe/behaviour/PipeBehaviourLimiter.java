/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe.behaviour;

import java.io.IOException;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.transport.pipe.IFlowPower;
import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import ct.buildcraft.api.transport.pipe.PipeApi;
import ct.buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import ct.buildcraft.api.transport.pipe.PipeBehaviour;
import ct.buildcraft.api.transport.pipe.PipeEventHandler;
import ct.buildcraft.api.transport.pipe.PipeEventPower;
import ct.buildcraft.lib.misc.EntityUtil;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class PipeBehaviourLimiter extends PipeBehaviour {
    public static final int MAX_SHIFT = 6;

    private int limitShift = 0;

    public PipeBehaviourLimiter(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourLimiter(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        limitShift = clampShift(nbt.getInt("limitShift"));
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.putInt("limitShift", limitShift);
        return nbt;
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(buffer, side);
        buffer.writeByte(limitShift);
    }

    @Override
    public void readPayload(FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        limitShift = clampShift(buffer.readUnsignedByte());
    }

    @PipeEventHandler
    public void configurePower(PipeEventPower.Configure event) {
        if (limitShift == MAX_SHIFT) {
            event.setMaxPower(0);
            event.setPowerLoss(0);
            event.setPowerResistance(0);
        } else {
            event.setMaxPower(event.getMaxPower() >> limitShift);
        }
    }

    @Override
    public boolean onPipeActivate(Player player, BlockHitResult trace, Level level, EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) == null) {
            return false;
        }

        if (!level.isClientSide()) {
            EntityUtil.activateWrench(player, trace);
            limitShift++;
            if (limitShift > MAX_SHIFT) {
                limitShift = 0;
            }

            PowerTransferInfo transferInfo = PipeApi.getPowerTransferInfo(pipe.getDefinition());
            long limit = limitShift == MAX_SHIFT ? 0 : (transferInfo.transferPerTick >> limitShift) / MjAPI.MJ;
            player.displayClientMessage(Component.translatable("chat.pipe.power.iron.mode", limit), true);

            requestReconfigure();
        }
        return true;
    }

    private void requestReconfigure() {
        if (pipe.getFlow() instanceof IFlowPower powerFlow) {
            powerFlow.reconfigure();
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

    @Override
    public int getTextureIndex(Direction face) {
        return MAX_SHIFT - limitShift;
    }

    private static int clampShift(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, MAX_SHIFT);
    }
}
