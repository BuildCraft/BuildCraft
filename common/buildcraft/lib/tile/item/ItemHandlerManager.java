package buildcraft.lib.tile.item;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.misc.CapUtil;

public class ItemHandlerManager implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
    public enum EnumAccess {
        NONE,
        INSERT,
        EXTRACT,
        BOTH
    }

    private final StackChangeCallback callback;
    private final Map<EnumPipePart, Wrapper> wrappers = new EnumMap<>(EnumPipePart.class);
    private final Map<String, INBTSerializable<NBTTagCompound>> handlers = new HashMap<>();

    public ItemHandlerManager(StackChangeCallback defaultCallback) {
        this.callback = defaultCallback;
        for (EnumPipePart part : EnumPipePart.VALUES) {
            wrappers.put(part, new Wrapper());
        }
    }

    public <T extends INBTSerializable<NBTTagCompound> & IItemHandlerModifiable> T addInvHandler(String key, T handler, EnumAccess access, EnumPipePart... parts) {
        if (parts == null) {
            parts = new EnumPipePart[0];
        }
        IItemHandlerModifiable external = handler;
        if (access == EnumAccess.NONE) {
            external = null;
        } else if (access == EnumAccess.EXTRACT) {
            external = new WrappedItemHandlerExtract(handler);
        } else if (access == EnumAccess.INSERT) {
            external = new WrappedItemHandlerInsert(handler);
        }

        if (external != null) {
            Set<EnumPipePart> visited = EnumSet.noneOf(EnumPipePart.class);
            for (EnumPipePart part : parts) {
                if (part == null) part = EnumPipePart.CENTER;
                if (visited.add(part)) {
                    Wrapper wrapper = wrappers.get(part);
                    wrapper.handlers.add(external);
                    wrapper.genWrapper();
                }
            }
        }
        handlers.put(key, handler);
        return handler;
    }

    public ItemHandlerSimple addInvHandler(String key, int size, EnumAccess access, EnumPipePart... parts) {
        ItemHandlerSimple handler = new ItemHandlerSimple(size, callback);
        return addInvHandler(key, handler, access, parts);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_ITEMS) {
            Wrapper wrapper = wrappers.get(EnumPipePart.fromFacing(facing));
            return wrapper.combined != null;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")// lots of (T) instance -- but we know its safe
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_ITEMS) {
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
        private final List<IItemHandlerModifiable> handlers = new ArrayList<>();
        private CombinedInvWrapper combined = null;// TODO: This should be an IItemTransactor as well.

        public void genWrapper() {
            IItemHandlerModifiable[] arr = new IItemHandlerModifiable[handlers.size()];
            arr = handlers.toArray(arr);
            combined = new CombinedInvWrapper(arr);
        }
    }
}
