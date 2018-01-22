/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.item;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;

public class ItemHandlerManager implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
    public enum EnumAccess {
        /** An {@link IItemHandler} that shouldn't be accessible by external sources. */
        NONE,
        /** Same as {@link #NONE}, but the contents of this inventory won't be dropped when the block is removed.
         * Additionally the items will be considered "free", and so items can be duplicated into these slots */
        PHANTOM,
        INSERT,
        EXTRACT,
        /** Full interaction is allowed. */
        BOTH
    }

    private final StackChangeCallback callback;
    private final List<IItemHandlerModifiable> handlersToDrop = new ArrayList<>();
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
        switch (access) {
            case NONE:
                break;
            case PHANTOM:
                external = null;
                if (parts.length > 0) {
                    throw new IllegalArgumentException("Completely useless to not allow access to multiple sides! Just don't pass any sides!");
                }
                break;
            case EXTRACT:
                external = new WrappedItemHandlerExtract(handler);
                break;
            case INSERT:
                external = new WrappedItemHandlerInsert(handler);
                break;
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
        if (access != EnumAccess.PHANTOM) {
            handlersToDrop.add(handler);
        }
        handlers.put(key, handler);
        return handler;
    }

    public ItemHandlerSimple addInvHandler(String key, int size, EnumAccess access, EnumPipePart... parts) {
        ItemHandlerSimple handler = new ItemHandlerSimple(size, callback);
        return addInvHandler(key, handler, access, parts);
    }

    public ItemHandlerSimple addInvHandler(String key, int size, StackInsertionChecker checker, EnumAccess access,
        EnumPipePart... parts) {
        ItemHandlerSimple handler = new ItemHandlerSimple(size, callback);
        handler.setChecker(checker);
        return addInvHandler(key, handler, access, parts);
    }

    public ItemHandlerSimple addInvHandler(String key, int size, StackInsertionFunction insertionFunction,
        EnumAccess access, EnumPipePart... parts) {
        ItemHandlerSimple handler = new ItemHandlerSimple(size, callback);
        handler.setInsertor(insertionFunction);
        return addInvHandler(key, handler, access, parts);
    }

    public ItemHandlerSimple addInvHandler(String key, int size, StackInsertionChecker checker,
        StackInsertionFunction insertionFunction, EnumAccess access, EnumPipePart... parts) {
        ItemHandlerSimple handler = new ItemHandlerSimple(size, checker, insertionFunction, callback);
        return addInvHandler(key, handler, access, parts);
    }

    public void addDrops(List<ItemStack> toDrop) {
        for (IItemHandlerModifiable itemHandler : handlersToDrop) {
            InventoryUtil.addAll(itemHandler, toDrop);
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_ITEMS) {
            Wrapper wrapper = wrappers.get(EnumPipePart.fromFacing(facing));
            return wrapper.combined != null;
        }
        return false;
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_ITEMS) {
            Wrapper wrapper = wrappers.get(EnumPipePart.fromFacing(facing));
            return CapUtil.CAP_ITEMS.cast(wrapper.combined);
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
