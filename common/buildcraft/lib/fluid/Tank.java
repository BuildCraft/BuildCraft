/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.fluid;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Formatting;

import net.minecraft.fluid.Fluid;
import buildcraft.lib.compat.FluidStackBC;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import buildcraft.lib.net.cache.NetworkedFluidStackCache;
import buildcraft.lib.tile.TileBC_Neptune;

/** Provides a useful implementation of a fluid tank that can save + load, and has a few helper functions. Can
 * optionally specify a filter to only allow a limited types of fluids in the tank. */
public class Tank extends FluidTank implements IFluidHandlerAdv {
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
    private Predicate<FluidStackBC> filter;

    NetworkedFluidStackCache.Link clientFluid = null;
    int clientAmount = 0;

    public ElementHelpInfo helpInfo;

    protected static Map<Fluid, Integer> fluidColors = new HashMap<>();

    /** Creates a tank with the given name and capacity (in milli buckets) with no filter set (so any fluid can go into
     * the tank) */
    public Tank(@Nonnull String name, int capacity, BlockEntity tile) {
        this(name, capacity, tile, null);
    }

    /** Creates a tank with the given name and capacity (in milli buckets) with the specified filter set. If the filter
     * returns true for a given fluidstack then it will be allowed in the tank. The given fluidstack will NEVER be
     * null. */
    public Tank(@Nonnull String name, int capacity, BlockEntity tile, @Nullable Predicate<FluidStackBC> filter) {
        super(capacity);
        this.name = name;
        this.tile = tile;
        this.filter = filter == null ? ((f) -> true) : filter;
        helpInfo = new ElementHelpInfo("buildcraft.help.tank.title." + name, 0xFF_00_00_00 | name.hashCode(),
            DEFAULT_HELP_KEY);
    }

    public void setFilter(Predicate<FluidStackBC> filter) {
        if (filter == null) {
            throw new NullPointerException("filter");
        }
        this.filter = filter;
    }

    @Nonnull
    public String getTankName() {
        return name;
    }

    public boolean isEmpty() {
        FluidStackBC fluidStack = getFluid();
        return fluidStack == null || fluidStack.amount <= 0;
    }

    public boolean isFull() {
        FluidStackBC fluidStack = getFluid();
        return fluidStack != null && fluidStack.amount >= getCapacity();
    }

    public Fluid getFluidType() {
        FluidStackBC fluidStack = getFluid();
        return fluidStack != null ? fluidStack.getFluid() : null;
    }

    public NbtCompound createNbt() { return serializeNBT(); }
    public NbtCompound serializeNBT() {
        return writeToNBT(new NbtCompound());
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public final NbtCompound writeToNBT(NbtCompound nbt) {
        super.writeToNBT(nbt);
        writeTankToNBT(nbt);
        return nbt;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public final FluidTank readFromNBT(NbtCompound nbt) {
        if (nbt.contains(name)) {
            // Old style of saving + loading
            NbtCompound tankData = nbt.getCompound(name);
            super.readFromNBT(tankData);
            readTankFromNBT(tankData);
        } else {
            super.readFromNBT(nbt);
            readTankFromNBT(nbt);
        }
        return this;
    }

    /** Writes some additional information to the nbt, for example {@link SingleUseTank} will write out the filtering
     * fluid. */
    protected void writeTankToNBT(NbtCompound nbt) {}

    /** Reads some additional information to the nbt, for example {@link SingleUseTank} will read in the filtering
     * fluid. */
    protected void readTankFromNBT(NbtCompound nbt) {}

    public ToolTip getToolTip() {
        return toolTip;
    }

    protected void refreshTooltip() {
        toolTip.clear();
        int amount = clientAmount;
        FluidStackBC fluidStack = clientFluid == null ? null : clientFluid.get().copy();
        if (fluidStack != null && amount > 0) {
            toolTip.add(fluidStack.getLocalizedName());
        }
        toolTip.add(Formatting.GRAY + LocaleUtil.localizeFluidStaticAmount(amount, getCapacity()));
        FluidStackBC serverFluid = getFluid();
        if (serverFluid != null && serverFluid.amount > 0) {
            toolTip.add(Formatting.RED + "BUG: Server-side fluid on client!");
            toolTip.add(serverFluid.getLocalizedName());
            toolTip.add(LocaleUtil.localizeFluidStaticAmount(serverFluid.amount, getCapacity()));
        }
    }

    @Override
    public boolean canFillFluidType(FluidStackBC fluid) {
        return super.canFillFluidType(fluid) && fluid != null && filter.test(fluid);
    }

    @Override
    public int fill(FluidStackBC resource, boolean doFill) {
        if (canFillFluidType(resource)) {
            return super.fill(resource, doFill);
        }
        return 0;
    }

    @Override
    public long drain(buildcraft.api.core.IFluidFilter drainFilter, long maxDrain, boolean doDrain) {
        if (drainFilter == null) return 0;
        FluidStackBC currentFluid = getFluid();
        if (currentFluid != null && drainFilter.matches(currentFluid.getFluidVariant(), maxDrain)) {
            FluidStackBC drained = drain((int) maxDrain, doDrain);
            return drained != null ? drained.getAmount() : 0;
        }
        return 0;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    protected void onContentsChanged() {
        super.onContentsChanged();
        if (tile instanceof TileBC_Neptune) {
            ((TileBC_Neptune) tile).markChunkDirty();
        }
    }

    @Override
    public String toString() {
        return "Tank [" + getContentsString() + "]";
    }

    public String getContentsString() {
        if (fluid != null) {
            return fluid.getLocalizedName() + LocaleUtil.localizeFluidStaticAmount(this);
        }
        return LocaleUtil.localizeFluidStaticAmount(0, getCapacity());
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

    @Environment(EnvType.CLIENT)
    public void readFromBuffer(PacketBufferBC buffer) {
        if (buffer.readBoolean()) {
            clientFluid = BuildCraftObjectCaches.CACHE_FLUIDS.client().retrieve(buffer.readInt());
        } else {
            clientFluid = null;
        }
        clientAmount = buffer.readInt();
    }

    public FluidStackBC getFluidForRender() {
        if (clientFluid == null) {
            return null;
        } else {
            FluidStackBC stackBase = clientFluid.get();
            return new FluidStackBC(stackBase, clientAmount);
        }
    }

    public int getClientAmount() {
        return clientAmount;
    }

    public String getDebugString() {
        FluidStackBC f = getFluidForRender();
        if (f == null) f = getFluid();
        return (f == null ? 0 : f.amount) + " / " + capacity + " mB of " + (f != null ? f.getFluid().getName() : "n/a");
    }

    public void onGuiClicked(ContainerBC_Neptune container) {
        PlayerEntity player = container.player;
        ItemStack held = player.getInventory().getItemStack();
        if (held.isEmpty()) {
            return;
        }
        ItemStack stack = transferStackToTank(container, held);
        player.getInventory().setItemStack(stack);
        ((ServerPlayerEntity) player).updateHeldItem();
        player.inventoryContainer.detectAndSendChanges();
        if (player.openContainer != null) {
            player.openContainer.detectAndSendChanges();
        }
    }

    /** Attempts to transfer the given stack to this tank.
     *
     * @return The left over item after attempting to add the stack to this tank. */
    public ItemStack transferStackToTank(ContainerBC_Neptune container, ItemStack stack) {
        PlayerEntity player = container.player;
        // first try to fill this tank from the item

        if (player.getWorld().isClient) {
            return stack;
        }

        ItemStack original = stack;
        ItemStack copy = stack.copy();
        copy.setCount(1);
        int space = capacity - getFluidAmount();

        boolean isCreative = player.capabilities.isCreativeMode;
        boolean isSurvival = !isCreative;

        FluidGetResult result = map(copy, space);
        if (result != null && result.fluidStack != null && result.fluidStack.amount > 0) {
            if (isCreative) {
                stack = copy;// so we don't change the stack held by the player.
            }
            int accepted = fill(result.fluidStack, false);
            if (isCreative ? (accepted > 0) : (accepted == result.fluidStack.amount)) {
                int reallyAccepted = fill(result.fluidStack, true);
                if (reallyAccepted != accepted) {
                    throw new IllegalStateException(
                        "We seem to be buggy! (accepted = " + accepted + ", reallyAccepted = " + reallyAccepted + ")");
                }
                stack.shrink(1);
                FluidStackBC fl = getFluid();
                if (fl != null) {
                    SoundUtil.playBucketEmpty(player.getWorld(), player.getPosition(), fl);
                }
                if (isSurvival) {
                    if (stack.isEmpty()) {
                        return result.itemStack;
                    } else if (!result.itemStack.isEmpty()) {
                        InventoryUtil.addToPlayer(player, result.itemStack);
                        return stack;
                    }
                }
                return original;
            }
        }
        // Now try to drain the fluid into the item
        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(copy);
        if (fluidHandler == null) return stack;
        FluidStackBC drained = drainInternal(capacity, false);
        if (drained == null || drained.amount <= 0) return stack;
        int filled = fluidHandler.fill(drained, true);
        if (filled > 0) {
            FluidStackBC reallyDrained = drainInternal(filled, true);
            if ((reallyDrained == null || reallyDrained.amount != filled)) {
                throw new IllegalStateException("Somehow drained differently than expected! ( drained = "//
                    + drained + ", filled = " + filled + ", reallyDrained = " + reallyDrained + " )");
            }
            SoundUtil.playBucketFill(player.getWorld(), player.getPosition(), reallyDrained);
            if (isSurvival) {
                if (original.getCount() == 1) {
                    return fluidHandler.getContainer();
                } else {
                    ItemStack stackContainer = fluidHandler.getContainer();
                    if (!stackContainer.isEmpty()) {
                        InventoryUtil.addToPlayer(player, stackContainer);
                    }
                    original.shrink(1);
                    return original;
                }
            }
        }
        return stack;
    }

    /** Maps the given stack to a fluid result.
     * 
     * @param stack The stack to map. This will ALWAYS have an {@link ItemStack#getCount()} of 1.
     * @param space The maximum amount of fluid that can be accepted by this tank. */
    protected FluidGetResult map(ItemStack stack, int space) {
        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(stack.copy());
        if (fluidHandler == null) return null;
        FluidStackBC drained = fluidHandler.drain(space, true);
        if (drained == null || drained.amount <= 0) return null;
        ItemStack leftOverStack = fluidHandler.getContainer();
        if (leftOverStack.isEmpty()) leftOverStack = StackUtil.EMPTY;
        return new FluidGetResult(leftOverStack, drained);
    }

    public static class FluidGetResult {
        public final ItemStack itemStack;
        public final FluidStackBC fluidStack;

        public FluidGetResult(ItemStack itemStack, FluidStackBC fluidStack) {
            this.itemStack = itemStack;
            this.fluidStack = fluidStack;
        }
    }
}
