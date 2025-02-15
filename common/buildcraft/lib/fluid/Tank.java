/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.fluid;

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
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/** Provides a useful implementation of a fluid tank that can save + load, and has a few helper functions. Can
 * optionally specify a filter to only allow a limited types of fluids in the tank. */
public class Tank extends FluidTank implements IFluidHandlerAdv {
    // Calen: added from 1.12.2
    public boolean canDrain = true;

    public void setCanDrain(boolean canDrain) {
        this.canDrain = canDrain;
    }

    public boolean canFill = true;

    public void setCanFill(boolean candFill) {
        this.canFill = candFill;
    }

    public boolean canFill() {
        return canFill;
    }

    public boolean canDrain() {
        return canDrain;
    }


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
    private Predicate<FluidStack> filter;

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
    public BlockEntity tile; // Calen added

    public Tank(@Nonnull String name, int capacity, BlockEntity tile, @Nullable Predicate<FluidStack> filter) {
        super(capacity);
        this.name = name;
        this.tile = tile;
        this.filter = filter == null ? ((f) -> true) : filter;
        helpInfo = new ElementHelpInfo("buildcraft.help.tank.title." + name, 0xFF_00_00_00 | name.hashCode(),
                DEFAULT_HELP_KEY);
    }

    public void setFilter(Predicate<FluidStack> filter) {
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
        FluidStack fluidStack = getFluid();
//        return fluidStack == null || fluidStack.getAmount() <= 0;
        return fluidStack.isEmpty() || fluidStack.getAmount() <= 0;
    }

    public boolean isFull() {
        FluidStack fluidStack = getFluid();
//        return fluidStack != null && fluidStack.getAmount() >= getCapacity();
        return fluidStack.isEmpty() && fluidStack.getAmount() >= getCapacity();
    }

    public Fluid getFluidType() {
        FluidStack fluidStack = getFluid();
//        return fluidStack != null ? fluidStack.getRawFluid() : null;
        return fluidStack.isEmpty() ? null : fluidStack.getRawFluid();
    }

    public CompoundTag serializeNBT() {
        return writeToNBT(new CompoundTag());
    }

    @Override
    public final CompoundTag writeToNBT(CompoundTag nbt) {
        super.writeToNBT(nbt);
        writeTankToNBT(nbt);
        return nbt;
    }

    @Override
    public final FluidTank readFromNBT(CompoundTag nbt) {
        if (nbt.contains(name)) {
            // Old style of saving + loading
            CompoundTag tankData = nbt.getCompound(name);
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
    protected void writeTankToNBT(CompoundTag nbt) {
    }

    /** Reads some additional information to the nbt, for example {@link SingleUseTank} will read in the filtering
     * fluid. */
    protected void readTankFromNBT(CompoundTag nbt) {
    }

    public ToolTip getToolTip() {
        return toolTip;
    }

    protected void refreshTooltip() {
        toolTip.clear();
        int amount = clientAmount;
//        FluidStack fluidStack = clientFluid == null ? null : clientFluid.get().copy();
        FluidStack fluidStack = clientFluid == null ? StackUtil.EMPTY_FLUID : clientFluid.get().copy();
//        if (fluidStack != null && amount > 0)
        if (!fluidStack.isEmpty() && amount > 0) {
//            toolTip.add(fluidStack.getLocalizedName());
            toolTip.add(fluidStack.getDisplayName());
        }
        toolTip.add(Component.literal(ChatFormatting.GRAY + LocaleUtil.localizeFluidStaticAmount(amount, getCapacity())));
        FluidStack serverFluid = getFluid();
//        if (serverFluid != null && serverFluid.getAmount() > 0)
        if (!serverFluid.isEmpty() && serverFluid.getAmount() > 0) {
//            toolTip.add(TextFormatting.RED + "BUG: Server-side fluid on client!");
            toolTip.add(Component.literal(ChatFormatting.RED + "BUG: Server-side fluid on client!"));
//            toolTip.add(serverFluid.getLocalizedName());
            toolTip.add(serverFluid.getDisplayName());
//            toolTip.add(LocaleUtil.localizeFluidStaticAmount(serverFluid.amount, getCapacity()));
            toolTip.add(LocaleUtil.localizeFluidStaticAmountComponent(serverFluid.getAmount(), getCapacity()));
        }
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return this.canFill && super.isFluidValid(fluid) && !fluid.isEmpty() && filter.test(fluid);
    }

    @Override
//    public int fill(FluidStack resource, boolean doFill)
    public int fill(FluidStack resource, FluidAction doFill) {
        if (canFillFluidType(resource)) {
            return fillInternal(resource, doFill);
        }
        return 0;
    }

    public int fillInternal(FluidStack resource, FluidAction doFill) {
        return super.fill(resource, doFill);
    }

    @Nonnull
    @Override
    public FluidStack drain(IFluidFilter drainFilter, int maxDrain, FluidAction doDrain) {
        if (drainFilter == null) {
//            return null;
            return StackUtil.EMPTY_FLUID;
        }
        FluidStack currentFluid = getFluid();
//        if (currentFluid != null && drainFilter.matches(currentFluid))
        if (!currentFluid.isEmpty() && drainFilter.matches(currentFluid)) {
            return drain(maxDrain, doDrain);
        }
//        return null;
        return StackUtil.EMPTY_FLUID;
    }

    public boolean canDrainFluidType(@Nullable FluidStack fluid) {
        return this.canDrain && super.isFluidValid(fluid) && !fluid.isEmpty();
    }

    // Calen 1.18.2
    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (!canDrainFluidType(getFluid())) {
            return StackUtil.EMPTY_FLUID;
        }
        return drainInternal(resource, action);
    }

    // Calen 1.18.2
    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (!canDrainFluidType(fluid)) {
            return StackUtil.EMPTY_FLUID;
        }
        return drainInternal(maxDrain, action);
    }

    public FluidStack drainInternal(FluidStack resource, FluidAction action) {
        if (resource == null || resource.isEmpty() || !resource.isFluidEqual(getFluid())) {
            return StackUtil.EMPTY_FLUID;
        }
        return drainInternal(resource.getAmount(), action);
    }

    public FluidStack drainInternal(int maxDrain, FluidAction action) {
        if (fluid == null || fluid.isEmpty() || maxDrain < 0) {
            return StackUtil.EMPTY_FLUID;
        }
        return super.drain(maxDrain, action);
    }

    @Override
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
//        if (fluid != null)
        if (!fluid.isEmpty()) {
            return fluid.getDisplayName().getString() + LocaleUtil.localizeFluidStaticAmount(this);
        }
        return LocaleUtil.localizeFluidStaticAmount(0, getCapacity());
    }

    public void writeToBuffer(PacketBufferBC buffer) {
//        if (fluid == null)
        if (fluid.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeInt(BuildCraftObjectCaches.CACHE_FLUIDS.server().store(fluid));
        }
        buffer.writeInt(getFluidAmount());
    }

    @OnlyIn(Dist.CLIENT)
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
//        return (f == null ? 0 : f.getAmount()) + " / " + capacity + " mB of " + (f != null ? f.getFluid().getRegistryName().getPath() : "n/a");
        return (f.isEmpty() ? 0 : f.getAmount()) + " / " + capacity + " mB of " + (f.isEmpty() ? "n/a" : f.getRawFluid().builtInRegistryHolder().key().location().getPath());
    }

    public void onGuiClicked(ContainerBC_Neptune container) {
        Player player = container.player;
//        ItemStack held = player.inventory.getItemStack();
        ItemStack held = player.containerMenu.getCarried();
        if (held.isEmpty()) {
            return;
        }
        ItemStack stack = transferStackToTank(container, held);
//        player.inventory.setItemStack(stack);
        player.containerMenu.setCarried(stack);
//        ((EntityPlayerMP) player).updateHeldItem();
        player.containerMenu.broadcastChanges();
//        if (player.openContainer != null)
        if (player.inventoryMenu != null) {
//            player.openContainer.detectAndSendChanges();
            player.inventoryMenu.broadcastChanges();
        }
    }

    /** Attempts to transfer the given stack to this tank.
     *
     * @return The left over item after attempting to add the stack to this tank. */
    public ItemStack transferStackToTank(ContainerBC_Neptune container, ItemStack stack) {
        Player player = container.player;
        // first try to fill this tank from the item

        if (player.level().isClientSide) {
            return stack;
        }

        ItemStack original = stack;
        ItemStack copy = stack.copy();
        copy.setCount(1);
        int space = capacity - getFluidAmount();

        boolean isCreative = player.getAbilities().instabuild;
        boolean isSurvival = !isCreative;

        FluidGetResult result = map(copy, space);
//        if (result != null && result.fluidStack != null && result.fluidStack.getAmount() > 0)
        if (result != null && (!result.fluidStack.isEmpty()) && result.fluidStack.getAmount() > 0) {
            if (isCreative) {
                stack = copy;// so we don't change the stack held by the player.
            }
            int accepted = fill(result.fluidStack, FluidAction.SIMULATE);
            if (isCreative ? (accepted > 0) : (accepted == result.fluidStack.getAmount())) {
                int reallyAccepted = fill(result.fluidStack, FluidAction.EXECUTE);
                if (reallyAccepted != accepted) {
                    throw new IllegalStateException(
                            "We seem to be buggy! (accepted = " + accepted + ", reallyAccepted = " + reallyAccepted + ")");
                }
                stack.shrink(1);
                FluidStack fl = getFluid();
//                if (fl != null)
                if (!fl.isEmpty()) {
                    SoundUtil.playBucketEmpty(player.level(), player.blockPosition(), fl);
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
//        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(copy);
        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(copy).orElse(null);
        if (fluidHandler == null) return stack;
        FluidStack drained = drain(capacity, FluidAction.SIMULATE);
//        if (drained == null || drained.getAmount() <= 0) return stack;
        if (drained.isEmpty() || drained.getAmount() <= 0) return stack;
        int filled = fluidHandler.fill(drained, FluidAction.EXECUTE);
        if (filled > 0) {
            FluidStack reallyDrained = drain(filled, FluidAction.EXECUTE);
//            if ((reallyDrained == null || reallyDrained.getAmount() != filled))
            if ((reallyDrained.isEmpty() || reallyDrained.getAmount() != filled)) {
                throw new IllegalStateException("Somehow drained differently than expected! ( drained = "//
                        + drained + ", filled = " + filled + ", reallyDrained = " + reallyDrained + " )");
            }
            SoundUtil.playBucketFill(player.level(), player.blockPosition(), reallyDrained);
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
        LazyOptional<IFluidHandlerItem> fluidHandlerOptional = FluidUtil.getFluidHandler(stack.copy());
        IFluidHandlerItem fluidHandler = fluidHandlerOptional.orElse(null);
        if (fluidHandler == null) return null;
        FluidStack drained = fluidHandler.drain(space, FluidAction.EXECUTE);
//        if (drained == null || drained.getAmount() <= 0) return null;
        if (drained.isEmpty() || drained.getAmount() <= 0) return null;
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

    public void setTileEntity(BlockEntity tile) {
        this.tile = tile;
    }
}
