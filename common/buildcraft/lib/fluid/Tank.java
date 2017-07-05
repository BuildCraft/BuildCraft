/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.fluid;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import buildcraft.lib.net.cache.NetworkedFluidStackCache;

/** Provides a useful implementation of a fluid tank that can save + load, and has a few helper functions.
 * 
 * Can optionally specify a filter to only allow a limited types of fluids in the tank. */
public class Tank extends FluidTank implements IFluidHandlerAdv, INBTSerializable<NBTTagCompound> {
    public static final String DEFAULT_HELP_KEY = "buildcraft.help.tank.generic";

    public int colorRenderCache = 0xFFFFFF;

    protected final ToolTip toolTip = new ToolTip() {
        @Override
        public void refresh() {
            refreshTooltip();
        }
    };

    @Nonnull
    private final String name;

    @Nonnull
    private final Predicate<FluidStack> filter;

    private NetworkedFluidStackCache.Link clientFluid = null;
    private int clientAmount = 0;

    public ElementHelpInfo helpInfo;

    protected static Map<Fluid, Integer> fluidColors = new HashMap<>();

    /** Creates a tank with the given name and capacity (in milli buckets) with no filter set (so any fluid can go into
     * the tank) */
    public Tank(@Nonnull String name, int capacity, TileEntity tile) {
        this(name, capacity, tile, null);
    }

    /** Creates a tank with the given name and capacity (in milli buckets) with the specified filter set. If the filter
     * returns true for a given fluidstack then it will be allowed in the tank. The given fluidstack will NEVER be
     * null. */
    public Tank(@Nonnull String name, int capacity, TileEntity tile, @Nullable Predicate<FluidStack> filter) {
        super(capacity);
        this.name = name;
        this.tile = tile;
        this.filter = filter == null ? ((f) -> true) : filter;
        helpInfo = new ElementHelpInfo("buildcraft.help.tank.title." + name, 0xFF_00_00_00 | name.hashCode(), DEFAULT_HELP_KEY);
    }

    @Nonnull
    public String getTankName() {
        return name;
    }

    public boolean isEmpty() {
        FluidStack fluidStack = getFluid();
        return fluidStack == null || fluidStack.amount <= 0;
    }

    public boolean isFull() {
        FluidStack fluidStack = getFluid();
        return fluidStack != null && fluidStack.amount >= getCapacity();
    }

    public Fluid getFluidType() {
        FluidStack fluidStack = getFluid();
        return fluidStack != null ? fluidStack.getFluid() : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        readFromNBT(nbt);
    }

    @Override
    public final NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagCompound tankData = new NBTTagCompound();
        super.writeToNBT(tankData);
        writeTankToNBT(tankData);
        nbt.setTag(name, tankData);
        return nbt;
    }

    @Override
    public final FluidTank readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(name)) {
            // allow to read empty tanks
            setFluid(null);

            NBTTagCompound tankData = nbt.getCompoundTag(name);
            super.readFromNBT(tankData);
            readTankFromNBT(tankData);
        }
        return this;
    }

    /** Writes some additional information to the nbt, for example {@link SingleUseTank} will write out the filtering
     * fluid. */
    public void writeTankToNBT(NBTTagCompound nbt) {}

    /** Reads some additional information to the nbt, for example {@link SingleUseTank} will read in the filtering
     * fluid. */
    public void readTankFromNBT(NBTTagCompound nbt) {}

    public ToolTip getToolTip() {
        return toolTip;
    }

    protected void refreshTooltip() {
        toolTip.clear();
        int amount = clientAmount;
        FluidStack fluidStack = clientFluid == null ? null : clientFluid.get().copy();
        if (fluidStack != null && amount > 0) {
            // toolTip.add(TextFormatting.WHITE + fluidStack.getFluid().getLocalizedName(fluidStack));
            toolTip.add(LocaleUtil.localizeFluidStatic(new FluidStack(fluidStack, amount), getCapacity()));
        } else {
            toolTip.add(LocaleUtil.localizeFluidStatic(null, getCapacity()));
        }
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return super.canFillFluidType(fluid) && fluid != null && filter.test(fluid);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (canFillFluidType(resource)) {
            return super.fill(resource, doFill);
        }
        return 0;
    }

    @Override
    public FluidStack drain(IFluidFilter drainFilter, int maxDrain, boolean doDrain) {
        if (drainFilter == null) {
            return null;
        }
        if (drainFilter.matches(getFluid())) {
            return drain(maxDrain, doDrain);
        }
        return null;
    }

    @Override
    public void setFluid(FluidStack fluid) {
        if (fluid == null || canFillFluidType(fluid)) {
            super.setFluid(fluid);
        }
    }

    @Override
    public String toString() {
        return "Tank [" + getContentsString() + "]";
    }

    public String getContentsString() {
        return LocaleUtil.localizeFluidStatic(fluid, capacity);
    }

    public void writeToBuffer(PacketBufferBC buffer) {
        if (fluid == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeInt(BuildCraftObjectCaches.CACHE_FLUIDS.server().store(fluid));
        }
        buffer.writeInt(getFluidAmount());
    }

    public void readFromBuffer(PacketBufferBC buffer) {
        if (buffer.readBoolean()) {
            clientFluid = BuildCraftObjectCaches.CACHE_FLUIDS.client().retrieve(buffer.readInt());
        } else {
            clientFluid = null;
        }
        clientAmount = buffer.readInt();
    }

    public FluidStack getFluidForRender() {
        if (clientFluid == null) {
            return null;
        } else {
            FluidStack stackBase = clientFluid.get();
            return new FluidStack(stackBase, clientAmount);
        }
    }

    public int getClientAmount() {
        return clientAmount;
    }

    public String getDebugString() {
        FluidStack f = getFluidForRender();
        if (f == null) f = getFluid();
        return (f == null ? 0 : f.amount) + " / " + capacity + " mB of " + (f != null ? f.getFluid().getName() : "n/a");
    }

    public void onGuiClicked(ContainerBC_Neptune container) {
        EntityPlayer player = container.player;
        ItemStack held = player.inventory.getItemStack();
        if (held.isEmpty()) {
            return;
        }

        // first try to fill this tank from the item

        boolean hasFilled = false;

        ItemStack copy = held.copy();
        copy.setCount(1);
        int space = capacity - getFluidAmount();

        boolean isCreative = player.capabilities.isCreativeMode;
        boolean isSurvival = !isCreative;

        FluidGetResult result = map(copy, space);
        if (result != null && result.fluidStack != null && result.fluidStack.amount > 0) {
            if (isCreative) {
                held = copy;// so we don't change the stack held by the player.
            }
            int potential = held.getCount();
            // Insert a single item until a fluid was not accepted.
            for (int p = 0; p < potential; p++) {
                int accepted = fill(result.fluidStack, false);
                if (isCreative ? (accepted > 0) : (accepted == result.fluidStack.amount)) {
                    hasFilled = true;
                    int reallyAccepted = fill(result.fluidStack, true);
                    if (reallyAccepted != accepted) {
                        throw new IllegalStateException("We seem to be buggy! (accepted = " + accepted + ", reallyAccepted = " + reallyAccepted + ")");
                    }
                    held.shrink(1);
                    if (isSurvival) {
                        if (held.isEmpty()) {
                            held = result.itemStack;
                            break;
                        } else if (!result.itemStack.isEmpty()) {
                            player.inventory.addItemStackToInventory(result.itemStack);
                            player.inventoryContainer.detectAndSendChanges();
                        }
                    } else if (held.isEmpty()) {
                        break;
                    }
                } else {
                    break;
                }
            }
            if (isSurvival) {
                player.inventory.setItemStack(held.isEmpty() ? StackUtil.EMPTY : held);
                ((EntityPlayerMP) player).updateHeldItem();
            }
            if (hasFilled) {
                FluidStack fl = getFluid();
                if (fl != null) {
                    SoundEvent sound = fl.getFluid().getEmptySound(container.player.world, container.player.getPosition());
                    container.player.world.playSound(null, player.getPosition(), sound, SoundCategory.BLOCKS, 1, 1);
                }
                return;
            }
        }
        // Now try to drain the fluid into the item
        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(held.copy());
        if (fluidHandler == null) return;
        FluidStack drained = drain(capacity, false);
        if (drained == null || drained.amount <= 0) return;
        int filled = fluidHandler.fill(drained, true);
        if (filled > 0) {
            FluidStack reallyDrained = drain(filled, true);
            if ((reallyDrained == null || reallyDrained.amount != filled)) {
                throw new IllegalStateException("Somehow drained differently than expected! ( drained = "//
                    + drained + ", filled = " + filled + ", reallyDrained = " + reallyDrained + " )");
            }
            if (isSurvival) {
                ItemStack filledContainer = fluidHandler.getContainer();
                player.inventory.setItemStack(filledContainer);
                ((EntityPlayerMP) player).updateHeldItem();
            }
            SoundEvent sound = reallyDrained.getFluid().getFillSound(container.player.world, container.player.getPosition());
            container.player.world.playSound(null, player.getPosition(), sound, SoundCategory.BLOCKS, 1, 1);
        }
    }

    /** Maps the given stack to a fluid result.
     * 
     * @param stack The stack to map. This will ALWAYS have an {@link ItemStack#getCount()} of 1.
     * @param space The maximum amount of fluid that can be accepted by this tank. */
    protected FluidGetResult map(ItemStack stack, int space) {
        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(stack.copy());
        if (fluidHandler == null) return null;
        FluidStack drained = fluidHandler.drain(space, true);
        if (drained == null || drained.amount <= 0) return null;
        ItemStack leftOverStack = fluidHandler.getContainer();
        if (leftOverStack.isEmpty()) leftOverStack = StackUtil.EMPTY;
        return new FluidGetResult(leftOverStack, drained);
    }

    public static class FluidGetResult {
        public final ItemStack itemStack;
        public final FluidStack fluidStack;

        public FluidGetResult(ItemStack itemStack, FluidStack fluidStack) {
            this.itemStack = itemStack;
            this.fluidStack = fluidStack;
        }
    }
}
