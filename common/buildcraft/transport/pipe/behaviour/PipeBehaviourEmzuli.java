package buildcraft.transport.pipe.behaviour;

import java.util.Collections;
import java.util.EnumMap;

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
import buildcraft.api.transport.PipeEventActionActivate;
import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventStatement;
import buildcraft.api.transport.neptune.IFlowItems;
import buildcraft.api.transport.neptune.IPipe;

import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.transport.BCTransportGuis;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.statements.ActionExtractionPreset;

// WIP
public class PipeBehaviourEmzuli extends PipeBehaviourWood {

    public enum SlotIndex {
        SQUARE(EnumDyeColor.RED),
        CIRCLE(EnumDyeColor.GREEN),
        TRIANGLE(EnumDyeColor.BLUE),
        CROSS(EnumDyeColor.YELLOW);

        public static final SlotIndex[] VALUES = values();

        public final EnumDyeColor colour;

        private SlotIndex(EnumDyeColor colour) {
            this.colour = colour;
        }
    }

    public final EnumMap<SlotIndex, EnumDyeColor> slotColours = new EnumMap<>(SlotIndex.class);
    public final ItemHandlerSimple invFilters = new ItemHandlerSimple(4, null);
    private SlotIndex currentSlot = null;

    private final IStackFilter filter = this::filterMatches;

    public PipeBehaviourEmzuli(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourEmzuli(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        invFilters.deserializeNBT(nbt.getCompoundTag("Filters"));
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("Filters", invFilters.serializeNBT());
        return nbt;
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) {
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
            MessageUtil.writeEnumOrNull(buffer, currentSlot);
        }
    }

    @Override
    protected int extractItems(IFlowItems flow, EnumFacing dir, int count) {
        if (currentSlot == null) return 0;
        return flow.tryExtractItems(count, dir, slotColours.get(currentSlot), filter);
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
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            return super.onPipeActivate(player, trace, hitX, hitY, hitZ, part);
        }
        if (player.isServerWorld()) {
            BCTransportGuis.PIPE_EMZULI.openGui(player, pipe.getHolder().getPipePos());
        }
        return true;
    }

    @PipeEventHandler
    public static void addActions(PipeEventStatement.AddActionInternal event) {
        Collections.addAll(event.actions, BCTransportStatements.ACTION_EXTRACTION_PRESET);
    }

    @PipeEventHandler
    public void onActionActivate(PipeEventActionActivate event) {
        if (event.action instanceof ActionExtractionPreset) {
            ActionExtractionPreset preset = (ActionExtractionPreset) event.action;
            currentSlot = preset.index;
        }
    }
}
