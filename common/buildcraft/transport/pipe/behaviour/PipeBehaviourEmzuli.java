/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventStatement;

import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.transport.BCTransportGuis;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.statements.ActionExtractionPreset;

public class PipeBehaviourEmzuli extends PipeBehaviourWood {

    public enum SlotIndex {
        SQUARE(EnumDyeColor.RED),
        CIRCLE(EnumDyeColor.GREEN),
        TRIANGLE(EnumDyeColor.BLUE),
        CROSS(EnumDyeColor.YELLOW);

        public static final SlotIndex[] VALUES = values();

        public final EnumDyeColor colour;

        SlotIndex(EnumDyeColor colour) {
            this.colour = colour;
        }

        public SlotIndex next() {
            // @formatter:off
            switch (this) {
                case SQUARE: return CIRCLE;
                case CIRCLE: return TRIANGLE;
                case TRIANGLE: return CROSS;
                case CROSS: return SQUARE;
                default: throw new IllegalStateException("Unknown SlotIndex - " + this + " - " + this.ordinal());
            }
            // @formatter:on
        }
    }

    public final EnumMap<SlotIndex, EnumDyeColor> slotColours = new EnumMap<>(SlotIndex.class);
    public final ItemHandlerSimple invFilters = new ItemHandlerSimple(4, null);
    private final EnumSet<SlotIndex> activeSlots;
    private final byte[] activatedTtl = new byte[SlotIndex.VALUES.length];
    private SlotIndex currentSlot = null;

    private final IStackFilter filter = this::filterMatches;

    public PipeBehaviourEmzuli(IPipe pipe) {
        super(pipe);
        activeSlots = EnumSet.noneOf(SlotIndex.class);
    }

    public PipeBehaviourEmzuli(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        invFilters.deserializeNBT(nbt.getCompoundTag("Filters"));
        activeSlots = NBTUtilBC.readEnumSet(nbt.getTag("activeSlots"), SlotIndex.class);
        currentSlot = NBTUtilBC.readEnum(nbt.getTag("currentSlot"), SlotIndex.class);
        for (SlotIndex index : SlotIndex.VALUES) {
            byte c = nbt.getByte("slotColors[" + index.ordinal() + "]");
            if (c > 0 && c <= 16) {
                slotColours.put(index, EnumDyeColor.byMetadata(c - 1));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("Filters", invFilters.serializeNBT());
        nbt.setTag("activeSlots", NBTUtilBC.writeEnumSet(activeSlots, SlotIndex.class));
        nbt.setTag("currentSlot", NBTUtilBC.writeEnum(currentSlot));
        for (SlotIndex index : SlotIndex.VALUES) {
            EnumDyeColor c = slotColours.get(index);
            nbt.setByte("slotColors[" + index.ordinal() + "]", (byte) (c == null ? 0 : c.getMetadata() + 1));
        }
        return nbt;
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == Side.CLIENT) {
            for (SlotIndex index : SlotIndex.VALUES) {
                EnumDyeColor colour = MessageUtil.readEnumOrNull(buffer, EnumDyeColor.class);
                if (colour == null) {
                    slotColours.remove(index);
                } else {
                    slotColours.put(index, colour);
                }
            }
            activeSlots.clear();
            activeSlots.addAll(MessageUtil.readEnumSet(buffer, SlotIndex.class));
            currentSlot = MessageUtil.readEnumOrNull(buffer, SlotIndex.class);
        }
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        if (side == Side.SERVER) {
            for (SlotIndex index : SlotIndex.VALUES) {
                MessageUtil.writeEnumOrNull(buffer, slotColours.get(index));
            }
            MessageUtil.writeEnumSet(buffer, activeSlots, SlotIndex.class);
            MessageUtil.writeEnumOrNull(buffer, currentSlot);
        }
    }

    @Override
    protected int extractItems(IFlowItems flow, EnumFacing dir, int count, boolean simulate) {
        if (currentSlot == null && activeSlots.size() > 0) {
            currentSlot = getNextSlot();
        }
        if (currentSlot == null) return 0;
        int extracted = flow.tryExtractItems(count, dir, slotColours.get(currentSlot), filter, simulate);
        if (extracted > 0 && !simulate) {
            currentSlot = getNextSlot();
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
        return extracted;
    }

    private boolean filterMatches(@Nonnull ItemStack stack) {
        if (currentSlot == null) return false;
        ItemStack current = invFilters.getStackInSlot(currentSlot.ordinal());
        if (StackUtil.isMatchingItemOrList(current, stack)) {
            return true;
        }
        return false;
    }

    @Override
    public void onTick() {
        super.onTick();
        if (pipe.getHolder().getPipeWorld().isRemote) {
            return;
        }
        for (SlotIndex index : SlotIndex.VALUES) {
            byte val = activatedTtl[index.ordinal()];
            if (val > 0) {
                val--;
                activatedTtl[index.ordinal()] = val;
            }
            if (val == 0) {
                activeSlots.remove(index);
                if (currentSlot == index) {
                    currentSlot = getNextSlot();
                    pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
                }
            }
        }
    }

    private SlotIndex getNextSlot() {
        SlotIndex current = currentSlot == null ? SlotIndex.CROSS : currentSlot;
        int i = SlotIndex.VALUES.length;
        while (i-- > 0) {
            current = current.next();
            if (activeSlots.contains(current) && !invFilters.getStackInSlot(current.ordinal()).isEmpty()) {
                return current;
            }
        }
        return null;
    }

    public SlotIndex getCurrentSlot() {
        return this.currentSlot;
    }

    public EnumSet<SlotIndex> getActiveSlots() {
        return this.activeSlots;
    }

    @Override
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            return super.onPipeActivate(player, trace, hitX, hitY, hitZ, part);
        }
        if (player.isServerWorld()) {
            BCTransportGuis.PIPE_EMZULI.openGui(player, pipe.getHolder().getPipePos());
        }
        return true;
    }

    @Override
    public void addActions(PipeEventStatement.AddActionInternal event) {
        super.addActions(event);
        Collections.addAll(event.actions, BCTransportStatements.ACTION_EXTRACTION_PRESET);
    }

    @Override
    public void onActionActivate(PipeEventActionActivate event) {
        super.onActionActivate(event);
        if (event.action instanceof ActionExtractionPreset) {
            ActionExtractionPreset preset = (ActionExtractionPreset) event.action;
            activeSlots.add(preset.index);
            activatedTtl[preset.index.ordinal()] = 2;
        }
    }
}
