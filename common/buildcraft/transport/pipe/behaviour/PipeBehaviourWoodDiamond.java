/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IFlowFluid;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.lib.inventory.filter.*;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.io.IOException;

public class PipeBehaviourWoodDiamond extends PipeBehaviourWood {

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

    public PipeBehaviourWoodDiamond(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWoodDiamond(IPipe pipe, CompoundNBT nbt) {
        super(pipe, nbt);
        filters.deserializeNBT(nbt.getCompound("filters"));
        filterMode = FilterMode.get(nbt.getByte("mode"));
        currentFilter = nbt.getByte("currentFilter") % filters.getSlots();
        filterValid = !filters.extract(StackFilter.ALL, 1, 1, true).isEmpty();
    }

    @Override
    public CompoundNBT writeToNbt() {
        CompoundNBT nbt = super.writeToNbt();
        nbt.put("filters", filters.serializeNBT());
        nbt.putByte("mode", (byte) filterMode.ordinal());
        nbt.putByte("currentFilter", (byte) currentFilter);
        return nbt;
    }

    @Override
    public void readPayload(PacketBuffer buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            filterMode = FilterMode.get(buffer.readUnsignedByte());
            currentFilter = buffer.readUnsignedByte() % filters.getSlots();
            filterValid = buffer.readBoolean();
        }
    }

    @Override
    public void writePayload(PacketBuffer buffer, Dist side) {
        super.writePayload(buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            buffer.writeByte(filterMode.ordinal());
            buffer.writeByte(currentFilter);
            buffer.writeBoolean(filterValid);
        }
    }

    @Override
    public boolean onPipeActivate(PlayerEntity player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            return super.onPipeActivate(player, trace, hitX, hitY, hitZ, part);
        }
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty()) {
            if (held.getItem() instanceof IItemPluggable) {
                return false;
            }
        }
        if (!player.level.isClientSide) {
//            BCTransportGuis.PIPE_DIAMOND_WOOD.openGui(player, pipe.getHolder().getPipePos());
            MessageUtil.serverOpenTileGui(player, pipe.getHolder(), pipe.getHolder().getPipePos());
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
                return (comparison) ->
                {
                    ItemStack filter = filters.getStackInSlot(currentFilter);
                    return StackUtil.isMatchingItemOrList(filter, comparison);
                };
        }
    }

    @Override
    protected int extractItems(IFlowItems flow, Direction dir, int count, boolean simulate) {
        if (filters.getStackInSlot(currentFilter).isEmpty()) {
            advanceFilter();
        }
        int extracted = flow.tryExtractItems(1, getCurrentDir(), null, getStackFilter(), simulate);
        if (extracted > 0 & filterMode == FilterMode.ROUND_ROBIN && !simulate) {
            advanceFilter();
        }
        return extracted;
    }

    @Override
//    protected FluidStack extractFluid(IFlowFluid flow, Direction dir, int millibuckets, boolean simulate)
    protected FluidStack extractFluid(IFlowFluid flow, Direction dir, int millibuckets, IFluidHandler.FluidAction action) {
        if (filters.getStackInSlot(currentFilter).isEmpty()) {
            advanceFilter();
        }

        switch (filterMode) {
            default:
            case WHITE_LIST:
                if (filters.extract(s -> true, 1, 1, true).isEmpty()) {
//                    return flow.tryExtractFluid(millibuckets, dir, null, simulate);
                    return flow.tryExtractFluid(millibuckets, dir, null, action);
                }
                // Firstly try the advanced version - if that fails we will need to try the basic version
//                ActionResult<FluidStack> result = flow.tryExtractFluidAdv(millibuckets, dir, new ArrayFluidFilter(filters.stacks), simulate);
                ActionResult<FluidStack> result = flow.tryExtractFluidAdv(millibuckets, dir, new ArrayFluidFilter(filters.stacks), action);
                FluidStack extracted = result.getObject();
                if (result.getResult() != ActionResultType.PASS) {
                    return extracted;
                }

                if (extracted.isEmpty() || extracted.getAmount() <= 0) {
                    for (int i = 0; i < filters.getSlots(); i++) {
                        ItemStack stack = filters.getStackInSlot(i);
                        if (stack.isEmpty()) {
                            continue;
                        }
//                        extracted = flow.tryExtractFluid(millibuckets, dir, FluidUtil.getFluidContained(stack).orElse(StackUtil.EMPTY_FLUID), simulate);
                        extracted = flow.tryExtractFluid(millibuckets, dir, FluidUtil.getFluidContained(stack).orElse(StackUtil.EMPTY_FLUID), action);
                        if (extracted != null && extracted.getAmount() > 0) {
                            return extracted;
                        }
                    }
                }
                return null;
            case BLACK_LIST:
                // We cannot fallback to the basic version - only use the advanced version
                InvertedFluidFilter filter = new InvertedFluidFilter(new ArrayFluidFilter(filters.stacks));
//                return flow.tryExtractFluidAdv(millibuckets, dir, filter, simulate).getObject();
                return flow.tryExtractFluidAdv(millibuckets, dir, filter, action).getObject();
            case ROUND_ROBIN:
                // We can't do this -- amounts might differ and its just ugly
                return null;
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
}
