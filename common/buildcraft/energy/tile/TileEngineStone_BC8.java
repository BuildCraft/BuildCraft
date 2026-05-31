/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.energy.tile;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.InventoryUtil; // STUB(R.Chen): drop() only; addToBestAcceptor removed
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.energy.BCEnergyGuis;

// Forge→Fabric migration notes (R.Chen):
//   EntityPlayer                 → PlayerEntity
//   EnumFacing                   → Direction
//   EnumHand                     → Hand
//   NBTTagCompound                → NbtCompound
//   readFromNBT / writeToNBT     → readNbt / writeNbt
//   nbt.setInteger/setLong        → nbt.putInt/putLong
//   nbt.getInteger                → nbt.getInt
//   IItemHandlerModifiable handler → Object handler (stub)
//   TileEntityFurnace.getItemBurnTime → STUB (returns 0; see TODO below)
//   world.isRemote               → world.isClient
//   InventoryUtil.addToBestAcceptor → STUB (not yet in migrated InventoryUtil)
//   BCEnergyGuis.ENGINE_STONE.openGUI → stub (GUI deferred)
public class TileEngineStone_BC8 extends TileEngineBase_BC8 {
    private static final long MAX_OUTPUT = MjAPI.MJ;
    private static final long MIN_OUTPUT = MAX_OUTPUT / 3;
    private static final float kp = 1f;
    private static final float ki = 0.05f;
    private static final long eLimit = (MAX_OUTPUT - MIN_OUTPUT) * 20;

    public final DeltaInt deltaFuelLeft = deltaManager.addDelta("fuel_left", EnumNetworkVisibility.GUI_ONLY);
    public final ItemHandlerSimple invFuel;

    int burnTime = 0;
    int totalBurnTime = 0;
    long esum = 0;

    private boolean isForceInserting = false;

    public TileEngineStone_BC8(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        invFuel = itemManager.addInvHandler("fuel", 1, this::isValidFuel, EnumAccess.BOTH, EnumPipePart.VALUES);
    }

    private boolean isValidFuel(int slot, ItemStack stack) {
        return isForceInserting || getItemBurnTime(stack) > 0;
    }

    // TileEntity overrides

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        burnTime = nbt.getInt("burnTime");
        totalBurnTime = nbt.getInt("totalBurnTime");
        esum = nbt.getLong("esum");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("burnTime", burnTime);
        nbt.putInt("totalBurnTime", totalBurnTime);
        nbt.putLong("esum", esum);
    }

    @Override
    protected void onSlotChange(Object handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        if (handler == invFuel) {
            if (isForceInserting && after.isEmpty()) {
                isForceInserting = false;
            }
        }
    }

    // Engine overrides

    @Override
    public boolean onActivated(PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isClient) {
            BCEnergyGuis.ENGINE_STONE.openGUI(player, getPos());
        }
        return true;
    }

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        return burnTime > 0;
    }

    @Override
    protected void engineUpdate() {
        super.engineUpdate();
        if (burnTime > 0) {
            burnTime--;
            if (getPowerStage() != EnumPowerStage.OVERHEAT) {
                long output = getCurrentOutput();
                currentOutput = output;
                addPower(output);
            }
        }
    }

    @Override
    public void burn() {
        if (burnTime == 0 && isRedstonePowered) {
            burnTime = totalBurnTime = getItemBurnTime(invFuel.getStackInSlot(0));

            if (burnTime > 0) {
                deltaFuelLeft.setValue(100);
                deltaFuelLeft.addDelta(0, totalBurnTime, -100);

                ItemStack fuel = invFuel.extractItem(0, 1, false);
                // Forge item.getContainerItem(fuel) → Fabric ItemStack.getRecipeRemainder()
                ItemStack container = fuel.getRecipeRemainder();
                if (!container.isEmpty()) {
                    if (invFuel.getStackInSlot(0).isEmpty()) {
                        isForceInserting = false;
                        // STUB(R.Chen): IItemTransactor.insert(all-slots) not in libLeaf; try slot 0.
                        ItemStack leftover = invFuel.insertItem(0, container, false);
                        if (!leftover.isEmpty()) {
                            isForceInserting = true;
                            invFuel.setStackInSlot(0, leftover);
                        }
                    } else {
                        // STUB(R.Chen): InventoryUtil.addToBestAcceptor not yet in migrated InventoryUtil.
                        InventoryUtil.drop(world, getPos(), container);
                    }
                }
            }
        }
    }

    // TODO(R.Chen): Forge TileEntityFurnace.getItemBurnTime → Fabric 1.20.1.
    // AbstractFurnaceBlockEntity.createFuelTimeMap() is protected in vanilla; the correct Fabric
    // approach is via FuelRegistryImpl or a mixin. Returns 0 until lib.recipe is migrated.
    private static int getItemBurnTime(ItemStack stack) {
        if (AbstractFurnaceBlockEntity.canUseAsFuel(stack)) {
            // STUB(R.Chen): real burn duration unavailable; placeholder 200 ticks (vanilla coal default).
            return 200;
        }
        return 0;
    }

    @Override
    public long maxPowerReceived() {
        return 200 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 100 * MjAPI.MJ;
    }

    @Override
    public long getMaxPower() {
        return 1000 * MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 2;
    }

    @Override
    public long getCurrentOutput() {
        long e = 3 * getMaxPower() / 8 - power;
        esum = clamp(esum + e, -eLimit, eLimit);
        return clamp(e + esum / 20, MIN_OUTPUT, MAX_OUTPUT);
    }

    private static long clamp(long val, long min, long max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("esum = " + MjAPI.formatMj(esum) + " M");
        long e = 3 * getMaxPower() / 8 - power;
        left.add("output = " + MjAPI.formatMj(clamp(e + esum / 20, MIN_OUTPUT, MAX_OUTPUT)) + " MJ");
        left.add("burnTime = " + burnTime);
        left.add("delta = " + deltaFuelLeft.getDynamic(0));
    }
}
