/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.core.blockEntity;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import ct.buildcraft.api.enums.EnumPowerStage;
import ct.buildcraft.api.mj.IMjConnector;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.tools.IToolWrench;
import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.core.client.render.RenderEngine_BC8;
import ct.buildcraft.lib.engine.EngineConnector;
import ct.buildcraft.lib.engine.TileEngineBase_BC8;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class TileEngineCreative extends TileEngineBase_BC8 {
	
	
    public TileEngineCreative(BlockPos pos, BlockState state) {
		super(BCCoreBlocks.ENGINE_CREATIVE_TILE_BC8.get(), pos, state);
		// TODO Auto-generated constructor stub
	}

	public static final long[] outputs = { 1, 2, 4, 8, 16, 32, 64, 128, 256 };
    public int currentOutputIndex = 0;

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeByte(currentOutputIndex);
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
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
        return Mth.lerp(interp, min, max);
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
        return outputs[Mth.clamp(currentOutputIndex, 0, outputs.length - 1)] * MjAPI.MJ;
    }

    @Override
	public InteractionResult onActivated(Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            if (!level.isClientSide) {
                currentOutputIndex++;
                currentOutputIndex %= outputs.length;
                player.displayClientMessage(Component.translatable("chat.pipe.power.iron.mode").append(""+outputs[currentOutputIndex]), true);
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
        currentOutputIndex = Mth.clamp(currentOutputIndex, 0, outputs.length);
    }

	@Override
	public TextureAtlasSprite getTextureBack() {
		return RenderEngine_BC8.CREATIVE_BACK;
	}

	@Override
	public TextureAtlasSprite getTextureSide() {
		return RenderEngine_BC8.CREATIVE_SIDE;
	}
}
