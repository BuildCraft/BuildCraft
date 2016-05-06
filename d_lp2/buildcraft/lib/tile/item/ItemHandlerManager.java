package buildcraft.lib.tile.item;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import buildcraft.api.core.EnumPipePart;

public class ItemHandlerManager implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
    public enum EnumAccess {
        NONE,
        INSERT,
        EXTRACT,
        BOTH
    }

    private final Map<EnumPipePart, Wrapper> wrappers = new EnumMap<>(EnumPipePart.class);
    private final Map<String, INBTSerializable<NBTTagCompound>> handlers = new HashMap<>();

    public ItemHandlerManager() {
        for (EnumPipePart part : EnumPipePart.VALUES) {
            wrappers.put(part, new Wrapper(part.face));
        }
    }

    public <T extends INBTSerializable<NBTTagCompound> & IItemHandlerModifiable> T addInvHandler(String key, T handler, EnumAccess access, EnumPipePart... parts) {
        if (parts == null) {
            parts = new EnumPipePart[0];
        }
        Set<EnumPipePart> visited = EnumSet.noneOf(EnumPipePart.class);
        for (EnumPipePart part : parts) {
            if (part == null) part = EnumPipePart.CENTER;
            if (visited.add(part)) {
                Wrapper wrapper = wrappers.get(part);
                wrapper.handlers.add(handler);
                wrapper.genWrapper();
            }
        }
        handlers.put(key, handler);
        return handler;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            Wrapper wrapper = wrappers.get(EnumPipePart.fromFacing(facing));
            return wrapper.combined != null;
        }
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            Wrapper wrapper = wrappers.get(EnumPipePart.fromFacing(facing));
            return (T) wrapper.combined;
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        for (Entry<String, INBTSerializable<NBTTagCompound>> entry : handlers.entrySet()) {
            String key = entry.getKey();
            nbt.setTag(key, entry.getValue().serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        for (Entry<String, INBTSerializable<NBTTagCompound>> entry : handlers.entrySet()) {
            String key = entry.getKey();
            entry.getValue().deserializeNBT(nbt.getCompoundTag(key));
        }
    }

    private static class Wrapper {
        private final EnumFacing face;
        private final List<IItemHandlerModifiable> handlers = new ArrayList<>();
        private CombinedInvWrapper combined = null;// FIXME: This should be an IItemTransactor

        public Wrapper(EnumFacing face) {
            this.face = face;
        }

        public void genWrapper() {
            IItemHandlerModifiable[] arr = new IItemHandlerModifiable[handlers.size()];
            arr = handlers.toArray(arr);
            combined = new CombinedInvWrapper(arr);

        }
    }
}
