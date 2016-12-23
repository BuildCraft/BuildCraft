/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.fluids;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import buildcraft.lib.net.cache.NetworkedFluidStackCache;

/** Provides a useful implementation of a fluid tank that can save + load, and has a few helper funtions.
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

    /** The overlay colour when the help ledger is opened. */
    public int clientHelpColour;
    public String clientHelpTitle;
    public String[] clientHelpKeys = { DEFAULT_HELP_KEY };

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
        clientHelpTitle = "buildcraft.help.tank.title." + name;
        clientHelpColour = 0xFF_00_00_00 | name.hashCode();
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
        return fluid != null && filter.test(fluid);
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

    public String getDebugString() {
        FluidStack f = getFluid();
        return getFluidAmount() + " / " + capacity + " mB of " + (f != null ? f.getFluid().getName() : "n/a");
    }

    /** Called whenever a player right-clicks on this tank in a gui.
     * 
     * @param container The container that the tank was right clicked in. */
    public void onGuiClicked(ContainerBC_Neptune container) {
        EntityPlayer player = container.player;
        ItemStack held = player.inventory.getItemStack();
        if (held.isEmpty()) {
            return;
        }
        boolean isCreative = player.capabilities.isCreativeMode;
        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(held);
        if (fluidHandler != null) {
            FluidStack drained = fluidHandler.drain(getCapacity() - getFluidAmount(), false);
            int filled = fill(drained, true);
            if (filled > 0) {
                FluidStack reallyDrained = fluidHandler.drain(filled, !isCreative);
                if (reallyDrained == null || reallyDrained.amount != filled) {
                    throw new IllegalStateException("Found a bugged implementation of IFluidHandlerItem! (first = "//
                        + filled + ", second = " + reallyDrained + ", impl = = " + fluidHandler.getClass() + ")");
                }
                if (!isCreative) {
                    player.inventory.setItemStack(fluidHandler.getContainer());
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public ElementHelpInfo getHelpInfo(IGuiArea area) {
        return new ElementHelpInfo(clientHelpTitle, area, clientHelpColour, clientHelpKeys);
    }
}
