/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.net.IMessage;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.robotics.BCRoboticsBlocks;
import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.container.ContainerRequester;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class TileRequester extends TileBC_Neptune implements IRequestProvider, IBCTileMenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("requester");
    public static final int NET_SET_REQUEST = IDS.allocId("SET_REQUEST");

    public static final int NB_ITEMS = 20;

    public final ItemHandlerSimple inv;
    // private SimpleInventory requests = new SimpleInventory(NB_ITEMS, "requests", 64);
    public final NonNullList<ItemStack> requests = NonNullList.withSize(NB_ITEMS, StackUtil.EMPTY);

    public TileRequester(BlockPos pos, BlockState blockState) {
        super(BCRoboticsBlocks.requesterTile.get(), pos, blockState);
        inv = itemManager.addInvHandler("items", NB_ITEMS, this::isItemValid, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
    }

    public void setRequest(final int index, final ItemStack stack) {
        if (level.isClientSide) {
            IMessage message = createMessage(NET_SET_REQUEST, (data) ->
            {
                data.writeByte(index);
                data.writeItem(stack);
            });
            MessageManager.sendToServer(message);
        } else {
            requests.set(index, stack);
        }
    }

    @Override
    public void readPayload(int command, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(command, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_SERVER && NET_SET_REQUEST == command) {
            setRequest(buffer.readUnsignedByte(), buffer.readItem());
        }
    }

    public ItemStack getRequestTemplate(int index) {
        // return requests.getStackInSlot(index);
        return requests.get(index);
    }

    // Calen: 1.8.9 impl IInventory -> 1.18.2 capability ItemHandlerSimple
//    @Override
//    public int getSizeInventory() {
//        return inv.getSizeInventory();
//    }

//    @Override
//    public ItemStack getStackInSlot(int slotId) {
//        return inv.getStackInSlot(slotId);
//    }

//    @Override
//    public ItemStack decrStackSize(int slotId, int count) {
//        return inv.decrStackSize(slotId, count);
//    }

//    @Override
//    public ItemStack removeStackFromSlot(int slotId) {
//        return inv.removeStackFromSlot(slotId);
//    }

//    @Override
//    public void setInventorySlotContents(int slotId, ItemStack itemStack) {
//        inv.setInventorySlotContents(slotId, itemStack);
//    }

//    @Override
//    public boolean hasCustomName() {
//        return inv.hasCustomName();
//    }

//    @Override
//    public int getInventoryStackLimit() {
//        return inv.getInventoryStackLimit();
//    }

//    @Override
//    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
//        return inv.isUseableByPlayer(entityPlayer);
//    }

//    @Override
//    public void openInventory(Player player) {
//        inv.openInventory(player);
//    }

//    @Override
//    public void closeInventory(Player player) {
//        inv.closeInventory(player);
//    }

    // MenuProvider

    @Override
    public Component getDisplayName() {
        // return inv.getDisplayName();
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerRequester(BCRoboticsMenuTypes.REQUESTER, id, player, this);
    }

    // @Override
    // public boolean isItemValidForSlot(int i, ItemStack itemStack)
    public boolean isItemValid(int i, ItemStack itemStack) {
        // if (requests.getStackInSlot(i) == null)
        if (requests.get(i).isEmpty()) {
            return false;
        } else if (!StackUtil.isMatchingItemOrList(requests.get(i), itemStack)) {
            return false;
        } else {
            // return inv.isItemValidForSlot(i, itemStack);
            return true;
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        CompoundTag invNBT = inv.serializeNBT();
        nbt.put("inv", invNBT);

        // requests.serializeNBT();
        ListTag reqNBT = new ListTag();
        for (ItemStack request : requests) {
            reqNBT.add(request.serializeNBT());
        }
        nbt.put("req", reqNBT);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        inv.deserializeNBT(nbt.getCompound("inv"));
        // requests.deserializeNBT(nbt.getCompound("req"));
        ListTag reqNBT = nbt.getList("req", Tag.TAG_COMPOUND);
        for (int i = 0; i < reqNBT.size(); i++) {
            requests.set(i, ItemStack.of(reqNBT.getCompound(i)));
        }
    }

    public boolean isFulfilled(int i) {
        // if (requests.getStackInSlot(i) == null)
        if (requests.get(i).isEmpty()) {
            return true;
        }
        // else if (inv.getStackInSlot(i) == null)
        else if (inv.getStackInSlot(i).isEmpty()) {
            return false;
        } else {
            return StackUtil.isMatchingItemOrList(requests.get(i), inv.getStackInSlot(i)) && inv.getStackInSlot(i).getCount() >= requests.get(i).getCount();
        }
    }

    @Override
    public int getRequestsCount() {
        return NB_ITEMS;
    }

    @Nonnull
    @Override
    public ItemStack getRequest(int i) {
        // if (requests.getStackInSlot(i) == null)
        if (requests.get(i).isEmpty()) {
            return StackUtil.EMPTY;
        } else if (isFulfilled(i)) {
            return StackUtil.EMPTY;
        } else {
            ItemStack request = requests.get(i).copy();

            ItemStack existingStack = inv.getStackInSlot(i);
            // if (existingStack == null)
            if (existingStack.isEmpty()) {
                return request;
            }

            if (!StackUtil.isMatchingItemOrList(request, existingStack)) {
                return StackUtil.EMPTY;
            }

            request.shrink(existingStack.getCount());
            if (request.getCount() <= 0) {
                return StackUtil.EMPTY;
            }

            return request;
        }
    }

    @Nonnull
    @Override
    public ItemStack offerItem(int i, ItemStack stack) {
        ItemStack existingStack = inv.getStackInSlot(i);

        // if (requests.getStackInSlot(i) == null)
        if (requests.get(i).isEmpty()) {
            return stack;
        }
        // else if (existingStack == null)
        else if (existingStack.isEmpty()) {
            if (!StackUtil.isMatchingItemOrList(stack, requests.get(i))) {
                return stack;
            }

            int maxQty = requests.get(i).getCount();

            if (stack.getCount() <= maxQty) {
                // inv.setInventorySlotContents(i, stack);
                inv.setStackInSlot(i, stack);

                return StackUtil.EMPTY;
            } else {
                ItemStack newStack = stack.copy();
                newStack.setCount(maxQty);
                stack.shrink(maxQty);

                // inv.setInventorySlotContents(i, newStack);
                inv.setStackInSlot(i, newStack);

                return stack;
            }
        } else if (!StackUtil.isMatchingItemOrList(stack, existingStack)) {
            return stack;
        } else if (StackUtil.isMatchingItemOrList(stack, requests.get(i))) {
            int maxQty = requests.get(i).getCount();

            if (existingStack.getCount() + stack.getCount() <= maxQty) {
                existingStack.grow(stack.getCount());
                return StackUtil.EMPTY;
            } else {
                stack.shrink(maxQty - existingStack.getCount());
                existingStack.setCount(maxQty);
                return stack;
            }
        } else {
            return stack;
        }
    }
}
