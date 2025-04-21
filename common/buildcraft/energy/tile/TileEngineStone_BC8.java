/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileEngineStone_BC8 extends TileEngineBase_BC8 implements IBCTileMenuProvider {
    private static final long MAX_OUTPUT = MjAPI.MJ;
    private static final long MIN_OUTPUT = MAX_OUTPUT / 3;
    // private static final long TARGET_OUTPUT = 0.375f;
    private static final float kp = 1f;
    private static final float ki = 0.05f;
    private static final long eLimit = (MAX_OUTPUT - MIN_OUTPUT) * 20;

    public final DeltaInt deltaFuelLeft = deltaManager.addDelta("fuel_left", EnumNetworkVisibility.GUI_ONLY);
    public final ItemHandlerSimple invFuel;

    int burnTime = 0;
    int totalBurnTime = 0;
    long esum = 0;

    private boolean isForceInserting = false;

    public TileEngineStone_BC8() {
        super(BCEnergyBlocks.engineStoneTile.get());
        invFuel = itemManager.addInvHandler("fuel", 1, this::isValidFuel, EnumAccess.BOTH, EnumPipePart.VALUES);
    }

    private boolean isValidFuel(int slot, ItemStack stack) {
        // Always allow inserting container items if they aren't fuel
        return isForceInserting || getItemBurnTime(stack) > 0;
    }

    // TileEntity overrides

    @Override
//    public void readFromNBT(NBTTagCompound nbt)
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        burnTime = nbt.getInt("burnTime");
        totalBurnTime = nbt.getInt("totalBurnTime");
        esum = nbt.getLong("esum");
    }

    @Override
//    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.putInt("burnTime", burnTime);
        nbt.putInt("totalBurnTime", totalBurnTime);
        nbt.putLong("esum", esum);
        return nbt;
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        // if (handler == invFuel)
        if (handler == invFuel && !StackUtil.isSameItemSameDamageSameTagSameCount(before, after)) {
            if (isForceInserting && after.isEmpty()) {
                isForceInserting = false;
            }
        }
    }

    // Engine overrides

    @Override
    public ActionResultType onActivated(PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        if (!level.isClientSide) {
//            BCEnergyGuis.ENGINE_STONE.openGUI(player, getPos());
            MessageUtil.serverOpenTileGui(player, this);
        }
        return ActionResultType.SUCCESS;
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
                // this seems wrong...
                long output = getCurrentOutput();
                currentOutput = output; // Comment out for constant power
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
                ItemStack container = fuel.getItem().getContainerItem(fuel);
                if (!container.isEmpty()) {
                    if (invFuel.getStackInSlot(0).isEmpty()) {
                        isForceInserting = false;
                        ItemStack leftover = invFuel.insert(container, false, false);
                        if (!leftover.isEmpty()) {
                            isForceInserting = true;
                            invFuel.setStackInSlot(0, leftover);
                        }
                    } else {
                        // Not good!
                        InventoryUtil.addToBestAcceptor(getLevel(), getBlockPos(), null, container);
                    }
                }
            }
        }
    }

    /**
     * {@link FurnaceTileEntity#getFuel()#getItemBurnTime(ItemStack)} will return null if not found, which cannot be cast to int.
     * {@link ForgeHooks#getBurnTime(ItemStack, IRecipeType)} is recommended by forge
     */
    private static int getItemBurnTime(ItemStack itemstack) {
//        return TileEntityFurnace.getItemBurnTime(itemstack);
        return ForgeHooks.getBurnTime(itemstack, IRecipeType.SMELTING);
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
        // double e = 0.375 * getMaxEnergy() - energy;
        // esum = MathUtils.clamp(esum + e, -eLimit, eLimit);
        // return MathUtils.clamp(e * 1 + esum * 0.05, MIN_OUTPUT, MAX_OUTPUT);

        long e = 3 * getMaxPower() / 8 - power;
        esum = clamp(esum + e, -eLimit, eLimit);
        return clamp(e + esum / 20, MIN_OUTPUT, MAX_OUTPUT);
    }

    private static long clamp(long val, long min, long max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<ITextComponent> left, List<ITextComponent> right, Direction side) {
//        super.getDebugInfo(left, right, side);
//        left.add("esum = " + MjAPI.formatMj(esum) + " M");
//        long e = 3 * getMaxPower() / 8 - power;
//        left.add("output = " + MjAPI.formatMj(clamp(e + esum / 20, MIN_OUTPUT, MAX_OUTPUT)) + " MJ");
//        left.add("burnTime = " + burnTime);
//        left.add("delta = " + deltaFuelLeft.getDynamic(0));
        super.getDebugInfo(left, right, side);
        left.add(new StringTextComponent("esum = " + MjAPI.formatMj(esum) + " M"));
        long e = 3 * getMaxPower() / 8 - power;
        left.add(new StringTextComponent("output = " + MjAPI.formatMj(clamp(e + esum / 20, MIN_OUTPUT, MAX_OUTPUT)) + " MJ"));
        left.add(new StringTextComponent("burnTime = " + burnTime));
        left.add(new StringTextComponent("delta = " + deltaFuelLeft.getDynamic(0)));
    }

    // INamedContainerProvider

    @Override
    public ITextComponent getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new ContainerEngineStone_BC8(BCEnergyMenuTypes.ENGINE_STONE, id, player, this);
    }
}
