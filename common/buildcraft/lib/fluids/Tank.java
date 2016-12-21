/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.fluids;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import buildcraft.lib.net.cache.NetworkedFluidStackCache;

/** Provides a useful implementation of a fluid tank that can save + load, and has a few helper funtions.
 * 
 * Can optionally specify a filter to only allow a limited types of fluids in the tank. */
public class Tank extends FluidTank implements IFluidHandlerAdv, INBTSerializable<NBTTagCompound> {
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
        FluidStack fluidStack = clientFluid.get();
        if (fluidStack != null && amount > 0) {
            toolTip.add(TextFormatting.WHITE + fluidStack.getFluid().getLocalizedName(fluidStack));
        }
        toolTip.add(String.format(Locale.ENGLISH, "%,d / %,d", amount, getCapacity()));
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return filter.test(fluid);
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
        FluidStack fluidStack = getFluid();
        if (fluidStack == null || fluidStack.amount <= 0) {
            return "Empty";
        }
        return (fluidStack.amount / 1000.0) + "B of " + fluidStack.getLocalizedName();
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

    public String getDebugString() {
        return getFluidAmount() + " / " + capacity + " mB of " + (getFluid() != null ? getFluid().getFluid().getName() : "n/a");
    }

    /** Called whenever a player right-clicks on this tank in a gui.
     * 
     * @param container The container that the tank was right clicked in. */
    public void onGuiClicked(ContainerBC_Neptune container) {
        EntityPlayer player = container.player;
        ItemStack held = player.inventory.getItemStack();
        if (StackUtil.isInvalid(held)) {
            return;
        }
        // TODO: Tank handling
        // Really need 1.11 for this
    }
}
