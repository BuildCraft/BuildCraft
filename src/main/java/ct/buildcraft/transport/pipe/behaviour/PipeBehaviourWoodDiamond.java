/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe.behaviour;

import java.io.IOException;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.transport.IItemPluggable;
import ct.buildcraft.api.transport.pipe.IFlowFluid;
import ct.buildcraft.api.transport.pipe.IFlowItems;
import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import ct.buildcraft.lib.inventory.filter.ArrayFluidFilter;
import ct.buildcraft.lib.inventory.filter.DelegatingItemHandlerFilter;
import ct.buildcraft.lib.inventory.filter.InvertedFluidFilter;
import ct.buildcraft.lib.inventory.filter.InvertedStackFilter;
import ct.buildcraft.lib.inventory.filter.StackFilter;
import ct.buildcraft.lib.misc.EntityUtil;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.transport.container.ContainerDiamondWoodPipe;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class PipeBehaviourWoodDiamond extends PipeBehaviourWood implements MenuProvider{

    public enum FilterMode {
        WHITE_LIST,
        BLACK_LIST,
        ROUND_ROBIN;

        public static FilterMode get(int index) {
            switch (index) {
                default:
                case 0:
                    return WHITE_LIST;
                case 1:
                    return BLACK_LIST;
                case 2:
                    return ROUND_ROBIN;
            }
        }
    }

    public final ItemHandlerSimple filters = new ItemHandlerSimple(9, this::onSlotChanged);
    public FilterMode filterMode = FilterMode.WHITE_LIST;
    public int currentFilter = 0;
    public boolean filterValid = false;
    protected DataSlot modeData = new DataSlot() {
    	public int get(){
    		return filterMode.ordinal();
    	}
    	public void set(int i){
    		filterMode = FilterMode.get(i);
    	}
    };
    protected DataSlot filterData = new DataSlot() {
		@Override
		public int get() {
			return !filterValid?currentFilter:currentFilter+128;
		}
		@Override
		public void set(int p_39402_) {}
    };

    public PipeBehaviourWoodDiamond(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWoodDiamond(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        filters.deserializeNBT(nbt.getCompound("filters"));
        filterMode = FilterMode.get(nbt.getByte("mode"));
        currentFilter = nbt.getByte("currentFilter") % filters.getSlots();
        filterValid = !filters.extract(StackFilter.ALL, 1, 1, true).isEmpty();
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.put("filters", filters.serializeNBT());
        nbt.putByte("mode", (byte) filterMode.ordinal());
        nbt.putByte("currentFilter", (byte) currentFilter);
        return nbt;
    }

    @Override
    public void readPayload(FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            filterMode = FilterMode.get(buffer.readUnsignedByte());
            currentFilter = buffer.readUnsignedByte() % filters.getSlots();
            filterValid = buffer.readBoolean();
        }
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(buffer, side);
        if (side == LogicalSide.SERVER) {
            buffer.writeByte(filterMode.ordinal());
            buffer.writeByte(currentFilter);
            buffer.writeBoolean(filterValid);
        }
    }

    @Override
    public boolean onPipeActivate(Player player, BlockHitResult trace, Level level,
        EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            return super.onPipeActivate(player, trace, level, part);
        }
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!held.isEmpty()) {
            if (held.getItem() instanceof IItemPluggable) {
                return false;
            }
        }
        if (!player.level.isClientSide()) {
        	NetworkHooks.openScreen((ServerPlayer)player, this, pipe.getHolder().getPipePos());
        }
        return true;
    }
    
    private void onSlotChanged(IItemHandlerModifiable itemHandler, int slot, ItemStack before, ItemStack after) {
        if (!after.isEmpty()) {
            if (!filterValid) {
                currentFilter = slot;
                filterValid = true;
            }
        } else if (slot == currentFilter) {
            advanceFilter();
        }
        pipe.getHolder().getPipeTile().setChanged();
    }

    private IStackFilter getStackFilter() {
        switch (filterMode) {
            default:
            case WHITE_LIST:
                if (filters.extract(s -> true, 1, 1, true).isEmpty()) {
                    return s -> true;
                }
                return new DelegatingItemHandlerFilter(StackUtil::isMatchingItemOrList, filters);
            case BLACK_LIST:
                return new InvertedStackFilter(
                    new DelegatingItemHandlerFilter(StackUtil::isMatchingItemOrList, filters));
            case ROUND_ROBIN:
                return (comparison) -> {
                    ItemStack filter = filters.getStackInSlot(currentFilter);
                    return StackUtil.isMatchingItemOrList(filter, comparison);
                };
        }
    }

    @Override
    protected int extractItems(IFlowItems flow, Direction dir, int count, FluidAction simulate) {
        if (filters.getStackInSlot(currentFilter).isEmpty()) {
            advanceFilter();
        }
        int extracted = flow.tryExtractItems(1, getCurrentDir(), null, getStackFilter(), simulate);
        if (extracted > 0 & filterMode == FilterMode.ROUND_ROBIN && simulate.execute()) {
            advanceFilter();
        }
        return extracted;
    }

    @Override
    protected FluidStack extractFluid(IFlowFluid flow, Direction dir, int millibuckets, FluidAction simulate) {
        if (filters.getStackInSlot(currentFilter).isEmpty()) {
            advanceFilter();
        }

        switch (filterMode) {
            default:
            case WHITE_LIST:
                if (filters.extract(s -> true, 1, 1, true).isEmpty()) {
                    return flow.tryExtractFluid(millibuckets, dir, FluidStack.EMPTY, simulate);
                }
                // Firstly try the advanced version - if that fails we will need to try the basic version
                InteractionResultHolder<FluidStack> result  = flow.tryExtractFluidAdv(millibuckets, dir, new ArrayFluidFilter(filters.stacks), simulate);
                FluidStack extracted = result.getObject();
                if (result.getResult() != InteractionResult.PASS) {
                    return extracted;
                }

                if (extracted == null || extracted.getAmount() <= 0) {
                    for (int i = 0; i < filters.getSlots(); i++) {
                        ItemStack stack = filters.getStackInSlot(i);
                        if (stack.isEmpty()) {
                            continue;
                        }
                        extracted = flow.tryExtractFluid(millibuckets, dir, FluidUtil.getFluidContained(stack).get(), simulate);
                        if (!extracted.isEmpty() && extracted.getAmount() > 0) {
                            return extracted;
                        }
                    }
                }
                return null;
            case BLACK_LIST:
                // We cannot fallback to the basic version - only use the advanced version
                InvertedFluidFilter filter = new InvertedFluidFilter(new ArrayFluidFilter(filters.stacks));
                return flow.tryExtractFluidAdv(millibuckets, dir, filter, simulate).getObject();
            case ROUND_ROBIN:
                // We can't do this -- amounts might differ and its just ugly
                return FluidStack.EMPTY;
        }
    }

    private void advanceFilter() {
        int lastFilter = currentFilter;
        filterValid = false;
        while (true) {
            currentFilter++;
            if (currentFilter >= filters.getSlots()) {
                currentFilter = 0;
            }
            if (!filters.getStackInSlot(currentFilter).isEmpty()) {
                filterValid = true;
                break;
            }
            if (currentFilter == lastFilter) {
                break;
            }
        }
        if (lastFilter != currentFilter) {
            pipe.getHolder().scheduleNetworkGuiUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new ContainerDiamondWoodPipe(id, inventory, filters, this);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(pipe.getDefinition().identifier.toLanguageKey());
	}
}
