/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy.tile;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionFunction;

public class TileEngineStone_BC8 extends TileEngineBase_BC8 {
    public static final long MJ_PER_TICK = 1 * MjAPI.MJ;

    public final DeltaInt deltaFuelLeft = deltaManager.addDelta("fuel_left", EnumNetworkVisibility.GUI_ONLY);
    private final ItemHandlerSimple itemHandler = new ItemHandlerSimple(1, this::canInsert, StackInsertionFunction.getDefaultInserter(), this::onChange);
    private ItemStack currentFuel;
    private int ticksLeft = 0;

    public TileEngineStone_BC8() {}

    // Item handler listeners

    private boolean canInsert(int slot, ItemStack stack) {
        return slot == 0 && TileEntityFurnace.getItemBurnTime(stack) > 0;
    }

    private void onChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        markDirty();
    }

    // Capability

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_ITEMS) {
            if (facing != getCurrentDirection()) return (T) itemHandler;
            return null;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_ITEMS) {
            return facing != getCurrentDirection();
        }
        return super.hasCapability(capability, facing);
    }

    // Engine overrides

    @Override
    public EnumEnergyStage getEnergyStage() {
        return EnumEnergyStage.BLUE;
    }

    @Override
    protected boolean canCarryOver(TileEngineBase_BC8 engine) {
        return engine instanceof TileEngineStone_BC8;
    }

    @Override
    public int getMaxEngineCarryDist() {
        return 2;
    }

    @Override
    public void update() {
        super.update();
        if (cannotUpdate() || worldObj.isRemote) return;
        if (ticksLeft > 0) {
            ticksLeft--;
            addPower(MJ_PER_TICK);
            changeHeat(TEMP_ENGINE_ENERGY, TEMP_CHANGE_HEAT);
        }
        if (ticksLeft <= 0 && isActive()) {
            ItemStack potentialFuel = itemHandler.extractItem(0, 1, true);
            int value = TileEntityFurnace.getItemBurnTime(potentialFuel);
            if (value > 0) {
                currentFuel = itemHandler.extractItem(0, 1, false);
                int burnTime = TileEntityFurnace.getItemBurnTime(currentFuel);
                ticksLeft += burnTime;
                deltaFuelLeft.addDelta(0, burnTime, 100);
                deltaFuelLeft.addDelta(burnTime, burnTime + 10, -100);
            }
        }
    }

    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    protected boolean hasFuelToBurn() {
        return ticksLeft > 0 || itemHandler.getStackInSlot(0) != null;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);

        left.add("");
        left.add("  - " + itemHandler.getStackInSlot(0));
    }
}
