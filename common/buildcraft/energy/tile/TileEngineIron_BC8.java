/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.energy.tile;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IItemPipe;

import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.energy.BCEnergyGuis;

// Forge→Fabric migration notes (R.Chen):
//   EntityPlayer / EnumFacing / EnumHand     → PlayerEntity / Direction / Hand
//   NBTTagCompound / readFromNBT/writeToNBT  → NbtCompound / readNbt/writeNbt
//   nbt.setInteger/setDouble                 → nbt.putInt/putDouble
//   Side.CLIENT / Side.SERVER                → NetSide.CLIENT / NetSide.SERVER
//   MessageContext                           → Object ctx
//   world.isRemote                           → world.isClient
//   player.getHeldItem                       → player.getStackInHand
//   EntityUtil.getWrenchHand                 → STUB (not yet in libLeaf)
//   Tank / FluidStack / IFluidHandlerAdv     → STUB — entire fluid fuel system deferred until
//                                              Transfer-API fluid layer lands (Phase fluid)
//   BuildcraftFuelRegistry / IFuel           → STUB — fuel API uses FluidStack; deferred
//   caps.addCapabilityInstance               → removed (CapabilityHelper stub has no such method)
//   tankManager.addAll / readData / writeData → removed (TankManager stub)
public class TileEngineIron_BC8 extends TileEngineBase_BC8 {
    public static final int MAX_FLUID = 10_000;

    public static final double COOLDOWN_RATE = 0.05;
    public static final int MAX_COOLANT_PER_TICK = 40;

    // STUB(R.Chen): tankFuel / tankCoolant / tankResidue / fluidHandler removed.
    // Fluid fuel system entirely deferred until Transfer-API fluid layer (Tank.java, FluidStack)
    // and the buildcraft.api.fuels (IFuel, BuildcraftFuelRegistry) are ported to Fabric.

    private int penaltyCooling = 0;
    private boolean lastPowered = false;
    private double burnTime;
    private double residueAmount = 0;
    // STUB(R.Chen): IFuel currentFuel removed — depends on FluidStack (Forge). Deferred.

    public TileEngineIron_BC8(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // STUB(R.Chen): tankManager.addAll(tankFuel, tankCoolant, tankResidue) — deferred.
        // STUB(R.Chen): caps.addCapabilityInstance(CAP_FLUIDS, fluidHandler, ...) — removed;
        //               Transfer-API FluidStorage.SIDED registration deferred to BCEnergyInitializer.
    }

    // TileEntity overrides

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("penaltyCooling", penaltyCooling);
        nbt.putDouble("burnTime", burnTime);
        nbt.putDouble("residueAmount", residueAmount);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        penaltyCooling = nbt.getInt("penaltyCooling");
        burnTime = nbt.getDouble("burnTime");
        residueAmount = Math.max(0, nbt.getDouble("residueAmount"));
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetSide.CLIENT) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                // STUB(R.Chen): tankManager.readData(buffer) — deferred until fluid layer lands.
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        if (side == NetSide.SERVER) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                // STUB(R.Chen): tankManager.writeData(buffer) — deferred until fluid layer lands.
            }
        }
    }

    // TileEngineBase overrides

    @Override
    public boolean onActivated(PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        ItemStack current = player.getStackInHand(hand).copy();
        if (super.onActivated(player, hand, side, hitX, hitY, hitZ)) {
            return true;
        }
        if (!current.isEmpty()) {
            // STUB(R.Chen): EntityUtil.getWrenchHand not yet in libLeaf; wrench check disabled.
            if (current.getItem() instanceof IItemPipe) {
                return false;
            }
        }
        if (!world.isClient) {
            BCEnergyGuis.ENGINE_IRON.openGUI(player, getPos());
        }
        return true;
    }

    @Override
    public double getPistonSpeed() {
        switch (getPowerStage()) {
            case BLUE:
                return 0.04;
            case GREEN:
                return 0.05;
            case YELLOW:
                return 0.06;
            case RED:
                return 0.07;
            default:
                return 0;
        }
    }

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        // STUB(R.Chen): tankFuel removed — fluid fuel system deferred to Transfer-API fluid pass.
        return false;
    }

    @Override
    protected void burn() {
        // STUB(R.Chen): fluid fuel system entirely deferred — Tank / FluidStack / IFuel not in libLeaf.
    }

    @Override
    public void updateHeatLevel() {
        // Cooling logic preserved sans coolant-fluid drain (Tank not available yet).
        if (heat > MIN_HEAT && (penaltyCooling > 0 || !isRedstonePowered)) {
            heat -= COOLDOWN_RATE;
        }
        // STUB(R.Chen): tankCoolant drain removed — BuildcraftFuelRegistry.coolant deferred.

        if (heat <= MIN_HEAT && penaltyCooling > 0) {
            penaltyCooling--;
        }

        if (heat <= MIN_HEAT) {
            heat = MIN_HEAT;
        }

        getPowerStage();
    }

    @Override
    public boolean isActive() {
        return penaltyCooling <= 0;
    }

    @Override
    public long getMaxPower() {
        return 10_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerReceived() {
        return 2_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 500 * MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 4;
    }

    @Override
    protected int getMaxChainLength() {
        return 4;
    }

    @Override
    public long getCurrentOutput() {
        // STUB(R.Chen): currentFuel (IFuel/FluidStack) removed — returns 0 until fluid layer lands.
        return 0;
    }
}
