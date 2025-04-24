/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.core.BCCoreItems;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TileEngineRF extends TileEngineBase_BC8 implements IBCTileMenuProvider {
    public static final int MAX_RF = 10_000;
    public static final double HEAT_RATE = 0.06;
    public static final double COOLDOWN_RATE = 0.01;

    // public static final Map<Item, Long> RF_UPGRADE = new LinkedHashMap<>();
    public static final Map<RegistryObject<ItemBC_Neptune>, Long> RF_UPGRADE = new LinkedHashMap<>();

    static {
        RF_UPGRADE.put(BCCoreItems.gearIron, MjAPI.MJ * 2);
        RF_UPGRADE.put(BCCoreItems.gearGold, MjAPI.MJ * 3);
    }

    int currentRF;
    public final ItemHandlerSimple invUpgrades;

    public TileEngineRF(BlockPos pos, BlockState blockState) {
        super(BCEnergyBlocks.engineRfTile.get(), pos, blockState);
        caps.addCapabilityInstance(CapabilityEnergy.ENERGY, new Rf(), EnumPipePart.VALUES);
        invUpgrades = itemManager.addInvHandler("upgrades", 4, this::isValidUpgrade, StackInsertionFunction.getInsertionFunction(1), EnumAccess.NONE);
    }

    // TileEntity overrides

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("currentRF", currentRF);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        currentRF = nbt.getInt("currentRF");
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                currentRF = buffer.readInt();
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                buffer.writeInt(currentRF);
            }
        }
    }

    protected boolean isValidUpgrade(int slot, ItemStack stack) {
        Item item = stack.getItem();
        // return RF_UPGRADE.containsKey(item);
        return RF_UPGRADE.keySet().stream().anyMatch(reg -> reg.get() == item);
    }

    // TileEngineBase overrides

    @Override
    public InteractionResult onActivated(
            Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ
    ) {
        ItemStack current = player.getItemInHand(hand).copy();
        if (super.onActivated(player, hand, side, hitX, hitY, hitZ) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        if (!current.isEmpty()) {
            if (EntityUtil.getWrenchHand(player) != null) {
                // return false;
                return InteractionResult.PASS;
            }
            if (current.getItem() instanceof IItemPipe) {
                // return false;
                return InteractionResult.PASS;
            }
        }
        if (!level.isClientSide) {
            // BCEnergyGuis.ENGINE_RF.openGUI(player, getPos());
            MessageUtil.serverOpenTileGui(player, this);
        }
        // return true;
        return InteractionResult.SUCCESS;
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
            // Long add = RF_UPGRADE.get(stack.getItem());
            Long add = RF_UPGRADE.entrySet().stream().filter(entry -> entry.getKey().get() == stack.getItem()).map(Map.Entry::getValue).findFirst().orElse(null);
            if (add != null) {
                value += add;
            }
        }
        return value;
    }

    public int getRfConsumptionRate() {

        final long mjPerTick = getMjPerTick();
        long mjPerRf = BCLibConfig.mjRfConversion.mjPerRf;

        return (int) (mjPerTick / mjPerRf);
    }

    @Override
    protected void burn() {
        if (currentRF <= 0) {
            return;
        }

        if (isRedstonePowered) {
            long mjPerRf = BCLibConfig.mjRfConversion.mjPerRf;
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

    private final class Rf implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int max = Math.min(MAX_RF - currentRF, maxReceive);
            if (max <= 0) {
                return 0;
            }

            if (!simulate) {
                currentRF += max;
            }
            return max;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return currentRF;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX_RF;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    // MenuProvider

    @Override
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerEngineRF(BCEnergyMenuTypes.ENGINE_RF, id, player, this);
    }
}
