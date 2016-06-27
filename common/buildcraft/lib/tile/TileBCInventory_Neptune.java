/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
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
