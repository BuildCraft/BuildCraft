/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.tile;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.mj.IMjConnector;
import ct.buildcraft.api.mj.IMjRedstoneReceiver;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.mj.MjCapabilityHelper;
import ct.buildcraft.lib.tile.item.ItemHandlerManager;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.silicon.BCSiliconBlocks;
import ct.buildcraft.silicon.container.ContainerChargingTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.NetworkHooks;

public class TileChargingTable extends TileLaserTableBase implements MenuProvider, IMjRedstoneReceiver {
    public final ItemHandlerSimple invCharge;

    public TileChargingTable(BlockPos pos, BlockState state) {
        super(BCSiliconBlocks.CHARGING_TABLE_TILE.get(), pos, state);
        invCharge = itemManager.addInvHandler("charge", 1, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
        invCharge.setLimitedInsertor(1);
        invCharge.setChecker((slot, stack) -> isChargeable(stack));
        caps.addProvider(new MjCapabilityHelper(this));
    }

    private static boolean isChargeable(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY)
            .map(energy -> energy.canReceive() && energy.getMaxEnergyStored() > 0)
            .orElse(false);
    }

    private static int getEnergyRequested(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY)
            .map(energy -> Math.max(0, energy.getMaxEnergyStored() - energy.getEnergyStored()))
            .orElse(0);
    }

    @Override
    public long getTarget() {
        long requested = getEnergyRequested(invCharge.getStackInSlot(0));
        return Math.min(requested, Integer.MAX_VALUE) * MjAPI.MJ;
    }

    @Override
    public void update() {
        super.update();
        if (level.isClientSide) {
            return;
        }

        ItemStack stack = invCharge.getStackInSlot(0);
        if (stack.isEmpty() || power < MjAPI.MJ) {
            return;
        }

        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> chargeItem(stack, energy));
    }

    private void chargeItem(ItemStack stack, IEnergyStorage energy) {
        if (!energy.canReceive()) {
            return;
        }
        int available = (int) Math.min(Integer.MAX_VALUE, power / MjAPI.MJ);
        if (available <= 0) {
            return;
        }
        int accepted = energy.receiveEnergy(available, false);
        if (accepted > 0) {
            power -= accepted * MjAPI.MJ;
            invCharge.setStackInSlot(0, stack);
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }
    }


    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return getDirectPowerRequested();
    }

    @Override
    public long receivePower(long microJoules, FluidAction action) {
        return receiveDirectPower(microJoules, action);
    }

    @Override
    public InteractionResult onActivated(Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, this, worldPosition);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerChargingTable(id, inventory, invCharge, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }
}
