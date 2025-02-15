/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.tile;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BCCoreBlocks;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TileEngineCreative extends TileEngineBase_BC8 {
    public static final long[] outputs = { 1, 2, 4, 8, 16, 32, 64, 128, 256 };
    public int currentOutputIndex = 0;

    public TileEngineCreative(BlockPos pos, BlockState blockState) {
        super(BCCoreBlocks.engineCreativeTile.get(), pos, blockState);
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeByte(currentOutputIndex);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) {
                currentOutputIndex = buffer.readUnsignedByte() % outputs.length;
            }
        }
    }

    @Override
    protected void engineUpdate() {
        if (isBurning()) {
            power += getCurrentOutput();
            long max = getMaxPower();
            if (power > max) {
                power = getMaxPower();
            }
        } else {
            power = 0;
        }
    }

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        return isRedstonePowered;
    }

    @Override
    public double getPistonSpeed() {
        final double max = 0.08;
        final double min = 0.01;
        double interp = currentOutputIndex / (double) (outputs.length - 1);
        return MathUtil.interp(interp, min, max);
    }

    @Override
    protected EnumPowerStage computePowerStage() {
        return EnumPowerStage.BLACK;
    }

    @Override
    public long getMaxPower() {
        return getCurrentOutput() * 10_000;
    }

    @Override
    public long maxPowerReceived() {
        return 2_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 20 * getCurrentOutput();
    }

    @Override
    public float explosionRange() {
        return 0;
    }

    @Override
    public long getCurrentOutput() {
        return outputs[MathUtil.clamp(currentOutputIndex, 0, outputs.length - 1)] * MjAPI.MJ;
    }

    @Override
    public InteractionResult onActivated(Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            if (!level.isClientSide) {
                currentOutputIndex++;
                currentOutputIndex %= outputs.length;
//                player.sendStatusMessage(
//                        Component.literalTranslation("chat.pipe.power.iron.mode", outputs[currentOutputIndex]),
//                        true
//                );
                player.displayClientMessage(
                        Component.translatable("chat.pipe.power.iron.mode", outputs[currentOutputIndex]),
                        true
                );
                sendNetworkUpdate(NET_RENDER_DATA);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("currentOutputIndex", currentOutputIndex);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        currentOutputIndex = nbt.getInt("currentOutputIndex");
        currentOutputIndex = MathUtil.clamp(currentOutputIndex, 0, outputs.length);
    }
}
