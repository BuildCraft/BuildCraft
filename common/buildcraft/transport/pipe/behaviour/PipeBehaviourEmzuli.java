package buildcraft.transport.pipe.behaviour;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.neptune.IFlowItems;
import buildcraft.api.transport.neptune.IPipe;

import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class PipeBehaviourEmzuli extends PipeBehaviourWood {

    public enum SlotIndex {
        SQUARE,
        CIRCLE,
        TRIANGLE,
        CROSS;

        public static final SlotIndex[] VALUES = values();
    }

    public final EnumDyeColor[] slotColours = new EnumDyeColor[4];
    public final ItemHandlerSimple invFilters = new ItemHandlerSimple(4, null);
    public final EnumSet<SlotIndex> activeSlots = EnumSet.noneOf(SlotIndex.class);

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
    protected int extractItems(IFlowItems flow, EnumFacing dir, int count) {
        return flow.tryExtractItems(count, dir, filter);
    }

    private boolean filterMatches(@Nonnull ItemStack stack) {
        for (SlotIndex index : activeSlots) {
            ItemStack current = invFilters.getStackInSlot(index.ordinal());
            if (StackUtil.isMatchingItemOrList(current, stack)) {
                return true;
            }
        }
        return false;
    }
}
