/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.energy.tile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.Nonnull;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import team.reborn.energy.api.EnergyStorage;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjRfConversion;
import buildcraft.api.transport.pipe.IItemPipe;

import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionFunction;

import buildcraft.energy.BCEnergyGuis;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

// Forge→Fabric migration notes (R.Chen):
//   PlayerEntity / Direction / Hand     → PlayerEntity / Direction / Hand
//   NbtCompound / readFromNBT/writeToNBT  → NbtCompound / readNbt/writeNbt
//   nbt.setInteger                           → nbt.putInt
//   EnvType.CLIENT / EnvType.SERVER                → NetSide.CLIENT / NetSide.SERVER
//   MessageContext                           → Object ctx
//   world.isClient                           → world.isClient
//   player.getHeldItem                       → player.getStackInHand
//   EntityUtil.getWrenchHand                 → STUB (not yet in libLeaf)
//   CapabilityEnergy.ENERGY / IEnergyStorage → Team Reborn EnergyStorage (rfEnergyStorage)
//   caps.addCapabilityInstance               → removed; EnergyStorage.SIDED registration
//                                              deferred to BCEnergyInitializer once ENGINE_RF_TYPE exists
//   BCLibConfig.mjRfConversion               → MjRfConversion.createDefault() (BCLibConfig not in libLeaf)
//   BCCoreItems.gearIron / gearGold          → STUB (BCCoreItems not in libLeaf)
@SuppressWarnings("UnstableApiUsage")
public class TileEngineRF extends TileEngineBase_BC8 {
    public static final int MAX_RF = 10_000;
    public static final double HEAT_RATE = 0.06;
    public static final double COOLDOWN_RATE = 0.01;

    // STUB(R.Chen): BCCoreItems.gearIron / gearGold not yet in libLeaf.
    // RF upgrade items will be populated once BCCoreItems is added to libLeaf.
    public static final Map<Item, Long> RF_UPGRADE = new LinkedHashMap<>();

    int currentRF;
    public final ItemHandlerSimple invUpgrades;

    /**
     * Team Reborn EnergyStorage replacing Forge IEnergyStorage capability.
     * Registration via EnergyStorage.SIDED.registerForBlockEntityType(...) is deferred to
     * BCEnergyInitializer once the ENGINE_RF BlockEntityType is registered.
     *
     * TODO(R.Chen): Transaction rollback not fully implemented — insert() commits eagerly.
     * Replace with SnapshotParticipant when the full Transfer-API migration pass runs.
     */
    public final EnergyStorage rfEnergyStorage = new EnergyStorage() {
        @Override
        public boolean supportsInsertion() { return true; }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            long max = Math.min((long) (MAX_RF - currentRF), maxAmount);
            if (max <= 0) return 0;
            // STUB(R.Chen): transaction rollback not implemented.
            currentRF += (int) max;
            return max;
        }

        @Override
        public boolean supportsExtraction() { return false; }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) { return 0; }

        @Override
        public long getAmount() { return currentRF; }

        @Override
        public long getCapacity() { return MAX_RF; }
    };

    public TileEngineRF(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // STUB(R.Chen): caps.addCapabilityInstance(CapabilityEnergy.ENERGY, ...) removed.
        //               EnergyStorage.SIDED registration deferred to BCEnergyInitializer.
        invUpgrades = itemManager.addInvHandler("upgrades", 4, this::isValidUpgrade,
            StackInsertionFunction.getInsertionFunction(1), EnumAccess.NONE);
    }

    // BlockEntity overrides

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("currentRF", currentRF);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        currentRF = nbt.getInt("currentRF");
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetSide.CLIENT) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                currentRF = buffer.readInt();
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        if (side == NetSide.SERVER) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                buffer.writeInt(currentRF);
            }
        }
    }

    protected boolean isValidUpgrade(int slot, ItemStack stack) {
        Item item = stack.getItem();
        return RF_UPGRADE.containsKey(item);
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
            BCEnergyGuis.ENGINE_RF.openGUI(player, getPos());
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
        return currentRF > 0 && isRedstonePowered;
    }

    public long getMjPerTick() {
        long value = MjAPI.MJ * 4;
        for (int slot = 0; slot < invUpgrades.getSlots(); slot++) {
            ItemStack stack = invUpgrades.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            Long add = RF_UPGRADE.get(stack.getItem());
            if (add != null) {
                value += add;
            }
        }
        return value;
    }

    public int getRfConsumptionRate() {
        final long mjPerTick = getMjPerTick();
        // STUB(R.Chen): BCLibConfig.mjRfConversion not in libLeaf; using default conversion ratio.
        long mjPerRf = MjRfConversion.createDefault().mjPerRf;
        return (int) (mjPerTick / mjPerRf);
    }

    @Override
    protected void burn() {
        if (currentRF <= 0) {
            return;
        }

        if (isRedstonePowered) {
            // STUB(R.Chen): BCLibConfig.mjRfConversion not in libLeaf; using default.
            long mjPerRf = MjRfConversion.createDefault().mjPerRf;
            int maxRf = getRfConsumptionRate();

            int rfConsumed = Math.min(currentRF, maxRf);
            long mjGenerated = rfConsumed * mjPerRf;

            if (power + mjGenerated >= getMaxPower()) {
                return;
            }

            currentOutput = mjGenerated;
            addPower(mjGenerated);
            currentRF -= rfConsumed;
            heat += HEAT_RATE;
            if (heat >= 200) {
                heat = 200;
            }
        }
    }

    @Override
    public void updateHeatLevel() {
        if (heat > MIN_HEAT) {
            heat -= COOLDOWN_RATE;
        }

        if (heat <= MIN_HEAT) {
            heat = MIN_HEAT;
        }

        getPowerStage();
    }

    @Override
    public long getMaxPower() {
        return 1000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerReceived() {
        return 200 * MjAPI.MJ;
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
        if (currentRF > 0) {
            return getMjPerTick();
        } else {
            return 0;
        }
    }

    public int getCurrentRF() {
        return currentRF;
    }
}
