/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackChangeCallback;

public abstract class TileBCInventory_Neptune extends TileBC_Neptune {
    public static final int NET_IDS_INV = TileBC_Neptune.NET_IDS;

    protected final ItemHandlerManager itemManager = new ItemHandlerManager();

    public TileBCInventory_Neptune() {

    }

    protected ItemHandlerSimple addInventory(String key, int slots, EnumAccess access, EnumPipePart... parts) {
        ItemHandlerSimple handler = new ItemHandlerSimple(slots, this::onSlotChange);
        return addInventory(key, handler, access, parts);
    }

    protected ItemHandlerSimple addInventory(String key, int slots, StackChangeCallback callback, EnumAccess access, EnumPipePart... parts) {
        ItemHandlerSimple handler = new ItemHandlerSimple(slots, callback);
        return addInventory(key, handler, access, parts);
    }

    protected <T extends INBTSerializable<NBTTagCompound> & IItemHandlerModifiable> T addInventory(String key, T handler, EnumAccess access, EnumPipePart... parts) {
        return itemManager.addInvHandler(key, handler, access, parts);
    }

    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("items", itemManager.serializeNBT());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        itemManager.deserializeNBT(nbt.getCompoundTag("items"));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemManager.hasCapability(capability, facing);
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemManager.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }
}
