/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.ResourceIdRequest;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.robots.StackRequest;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;

import io.netty.buffer.ByteBuf;

public class TileRequester extends TileBuildCraft implements IInventory, IRequestProvider, ICommandReceiver {
    public static final int NB_ITEMS = 20;

    private SimpleInventory inv = new SimpleInventory(NB_ITEMS, "items", 64);
    private SimpleInventory requests = new SimpleInventory(NB_ITEMS, "requests", 64);

    public TileRequester() {

    }

    public void setRequest(final int index, final ItemStack stack) {
        if (worldObj.isRemote) {
            BuildCraftCore.instance.sendToServer(new PacketCommand(this, "setRequest", new CommandWriter() {
                public void write(ByteBuf data) {
                    data.writeByte(index);
                    NetworkUtils.writeStack(data, stack);
                }
            }));
        } else {
            requests.setInventorySlotContents(index, stack);
        }
    }

    @Override
    public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
        if (side.isServer() && "setRequest".equals(command)) {
            setRequest(stream.readUnsignedByte(), NetworkUtils.readStack(stream));
        }
    }

    public ItemStack getRequest(int index) {
        return requests.getStackInSlot(index);
    }

    @Override
    public int getSizeInventory() {
        return inv.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slotId) {
        return inv.getStackInSlot(slotId);
    }

    @Override
    public ItemStack decrStackSize(int slotId, int count) {
        return inv.decrStackSize(slotId, count);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotId) {
        return inv.getStackInSlotOnClosing(slotId);
    }

    @Override
    public void setInventorySlotContents(int slotId, ItemStack itemStack) {
        inv.setInventorySlotContents(slotId, itemStack);
    }

    @Override
    public IChatComponent getDisplayName() {
        return inv.getDisplayName();
    }

    @Override
    public boolean hasCustomName() {
        return inv.hasCustomName();
    }

    @Override
    public int getInventoryStackLimit() {
        return inv.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
        return inv.isUseableByPlayer(entityPlayer);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        inv.openInventory(player);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        inv.closeInventory(player);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        if (requests.getStackInSlot(i) == null) {
            return false;
        } else if (!StackHelper.isMatchingItemOrList(requests.getStackInSlot(i), itemStack)) {
            return false;
        } else {
            return inv.isItemValidForSlot(i, itemStack);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        NBTTagCompound invNBT = new NBTTagCompound();
        inv.writeToNBT(invNBT);
        nbt.setTag("inv", invNBT);

        NBTTagCompound reqNBT = new NBTTagCompound();
        requests.writeToNBT(reqNBT);
        nbt.setTag("req", reqNBT);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        inv.readFromNBT(nbt.getCompoundTag("inv"));
        requests.readFromNBT(nbt.getCompoundTag("req"));
    }

    public boolean isFulfilled(int i) {
        if (requests.getStackInSlot(i) == null) {
            return true;
        } else if (inv.getStackInSlot(i) == null) {
            return false;
        } else {
            return StackHelper.isMatchingItemOrList(requests.getStackInSlot(i), inv.getStackInSlot(i)) && inv.getStackInSlot(i).stackSize >= requests
                    .getStackInSlot(i).stackSize;
        }
    }

    @Override
    public int getNumberOfRequests() {
        return NB_ITEMS;
    }

    @Override
    public StackRequest getAvailableRequest(int i) {
        if (requests.getStackInSlot(i) == null) {
            return null;
        } else if (isFulfilled(i)) {
            return null;
        } else if (RobotManager.registryProvider.getRegistry(worldObj).isTaken(new ResourceIdRequest(this, i))) {
            return null;
        } else {
            StackRequest r = new StackRequest();

            r.index = i;
            r.stack = requests.getStackInSlot(i);
            r.requester = this;

            return r;
        }
    }

    @Override
    public boolean takeRequest(int i, EntityRobotBase robot) {
        if (requests.getStackInSlot(i) == null) {
            return false;
        } else if (isFulfilled(i)) {
            return false;
        } else {
            return RobotManager.registryProvider.getRegistry(worldObj).take(new ResourceIdRequest(this, i), robot);
        }
    }

    @Override
    public ItemStack provideItemsForRequest(int i, ItemStack stack) {
        ItemStack existingStack = inv.getStackInSlot(i);

        if (requests.getStackInSlot(i) == null) {
            return stack;
        } else if (existingStack == null) {
            int maxQty = requests.getStackInSlot(i).stackSize;

            if (stack.stackSize <= maxQty) {
                inv.setInventorySlotContents(i, stack);

                return null;
            } else {
                ItemStack newStack = stack.copy();
                newStack.stackSize = maxQty;
                stack.stackSize -= maxQty;

                inv.setInventorySlotContents(i, newStack);

                return stack;
            }
        } else if (!StackHelper.isMatchingItemOrList(stack, existingStack)) {
            return stack;
        } else if (existingStack == null || StackHelper.isMatchingItemOrList(stack, requests.getStackInSlot(i))) {
            int maxQty = requests.getStackInSlot(i).stackSize;

            if (existingStack.stackSize + stack.stackSize <= maxQty) {
                existingStack.stackSize += stack.stackSize;
                return null;
            } else {
                stack.stackSize -= maxQty - existingStack.stackSize;
                existingStack.stackSize = maxQty;
                return stack;
            }
        } else {
            return stack;
        }
    }
}
