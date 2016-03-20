/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.fluids;

import java.util.Locale;
import java.util.function.Predicate;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.core.lib.gui.tooltips.ToolTipLine;

/** Provides a useful implementation of a fluid tank that can save + load, and has a few helper funtions.
 * 
 * Can optionally specify a filter to only allow a limited types of fluids in the tank. */
public class Tank extends FluidTank implements INBTSerializable<NBTTagCompound> {
    public int colorRenderCache = 0xFFFFFF;

    protected final ToolTip toolTip = new ToolTip() {
        @Override
        public void refresh() {
            refreshTooltip();
        }
    };

    private final String name;
    private final Predicate<FluidStack> filter;

    /** Creates a tank with the given name and capacity (in milli buckets) with no filter set (so any fluid can go into
     * the tank) */
    public Tank(String name, int capacity, TileEntity tile) {
        this(name, capacity, tile, null);
    }

    /** Creates a tank with the given name and capacity (in milli buckets) with the specified filter set. If the filter
     * returns true for a given fluidstack then it will be allowed in the tank. The given fluidstack will NEVER be
     * null. */
    public Tank(String name, int capacity, TileEntity tile, Predicate<FluidStack> filter) {
        super(capacity);
        this.name = name;
        this.tile = tile;
        this.filter = filter;
    }

    public String getTankName() {
        return name;
    }

    public boolean isEmpty() {
        return getFluid() == null || getFluid().amount <= 0;
    }

    public boolean isFull() {
        return getFluid() != null && getFluid().amount >= getCapacity();
    }

    public Fluid getFluidType() {
        return getFluid() != null ? getFluid().getFluid() : null;
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

    public void writeTankToNBT(NBTTagCompound nbt) {}

    public void readTankFromNBT(NBTTagCompound nbt) {}

    public ToolTip getToolTip() {
        return toolTip;
    }

    protected void refreshTooltip() {
        toolTip.clear();
        int amount = 0;
        if (getFluid() != null && getFluid().amount > 0) {
            ToolTipLine fluidName = new ToolTipLine(getFluid().getFluid().getLocalizedName(getFluid()), EnumChatFormatting.WHITE);
            fluidName.setSpacing(2);
            toolTip.add(fluidName);
            amount = getFluid().amount;
        }
        toolTip.add(new ToolTipLine(String.format(Locale.ENGLISH, "%,d / %,d", amount, getCapacity())));
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (filter == null || filter.test(resource)) return super.fill(resource, doFill);
        return 0;
    }

    @Override
    public void setFluid(FluidStack fluid) {
        if (fluid == null || filter == null || filter.test(fluid)) super.setFluid(fluid);
    }
}
