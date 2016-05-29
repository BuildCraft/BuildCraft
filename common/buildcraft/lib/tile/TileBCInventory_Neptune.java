package buildcraft.lib.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.INBTSerializable;
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
}
