/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.ISerializable;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.network.IGuiReturnHandler;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.pipes.events.PipeEventItem;

public class PipeItemsEmerald extends PipeItemsWood implements ISerializable, IGuiReturnHandler {

    public enum FilterMode {
        WHITE_LIST,
        BLACK_LIST,
        ROUND_ROBIN
    }

    public class EmeraldPipeSettings {

        private FilterMode filterMode;

        public EmeraldPipeSettings() {
            filterMode = FilterMode.WHITE_LIST;
        }

        public FilterMode getFilterMode() {
            return filterMode;
        }

        public void setFilterMode(FilterMode mode) {
            filterMode = mode;
        }

        public void readFromNBT(NBTTagCompound nbt) {
            filterMode = FilterMode.values()[nbt.getByte("filterMode")];
        }

        public void writeToNBT(NBTTagCompound nbt) {
            nbt.setByte("filterMode", (byte) filterMode.ordinal());
        }
    }

    private EmeraldPipeSettings settings = new EmeraldPipeSettings();

    private final SimpleInventory filters = new SimpleInventory(9, "Filters", 1);

    private int currentFilter = 0;

    public PipeItemsEmerald(Item item) {
        super(item);

        standardIconIndex = PipeIconProvider.TYPE.PipeItemsEmerald_Standard.ordinal();
        solidIconIndex = PipeIconProvider.TYPE.PipeItemsEmerald_Solid.ordinal();
    }

    public void eventHandler(PipeEventItem.Entered event) {
        int meta = container.getBlockMetadata();

        if (meta <= 5) {
            EnumFacing side = EnumFacing.getFront(meta);
            if (event.item.input == side) {
                return; // Item is backtracking
            }
        }

        if (!matchesFilter(event.item.getItemStack())) {
            event.cancelled = true;
        }
    }

    @Override
    public boolean blockActivated(EntityPlayer entityplayer, EnumFacing side) {
        if (entityplayer.getCurrentEquippedItem() != null) {
            if (Block.getBlockFromItem(entityplayer.getCurrentEquippedItem().getItem()) instanceof BlockGenericPipe) {
                return false;
            }
        }

        if (super.blockActivated(entityplayer, side)) {
            return true;
        }

        if (!container.getWorld().isRemote) {
            entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_EMERALD_ITEM, container.getWorld(), container.getPos().getX(), container
                    .getPos().getY(), container.getPos().getZ());
        }

        return true;
    }

    @Override
    public int[] getExtractionTargets(IItemHandler handler, int maxItems) {
        if (handler == null) {
            return null;
        }

        if (settings.getFilterMode() == FilterMode.ROUND_ROBIN) {
            return checkExtractRoundRobin(handler);
        }

        return checkExtractFiltered(handler, maxItems);
    }

    private int[] checkExtractFiltered(IItemHandler handler, int maxItems) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);

            if (stack == null || stack.stackSize <= 0) {
                continue;
            }

            stack = handler.extractItem(i, maxItems, true);

            if (stack == null || stack.stackSize <= 0) {
                continue;
            }

            boolean matches = matchesFilter(stack);

            if (!matches) {
                continue;
            }

            return new int[] { i };
        }

        return null;
    }

    private int[] checkExtractRoundRobin(IItemHandler handler) {
        ItemStack filter = getCurrentFilter();

        if (filter == null) {
            return null;
        }

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);

            if (stack == null || stack.stackSize <= 0) {
                continue;
            }

            stack = handler.extractItem(i, 1, true);

            if (stack == null || stack.stackSize <= 0) {
                continue;
            }

            if (!StackHelper.isMatchingItemOrList(filter, stack)) {
                continue;
            }

            return new int[] { i };
        }

        return null;
    }

    public boolean matchesFilter(ItemStack stack) {
        boolean isFilter = inFilterList(stack);
        return settings.getFilterMode() == FilterMode.BLACK_LIST ? !isFilter : isFilter;
    }

    private boolean inFilterList(ItemStack stack) {
        for (int i = 0; i < filters.getSizeInventory(); i++) {
            ItemStack filter = filters.getStackInSlot(i);

            if (filter == null) {
                continue;
            }

            if (StackHelper.isMatchingItemOrList(filter, stack)) {
                return true;
            }
        }

        return false;
    }

    private void incrementFilter() {
        currentFilter = (currentFilter + 1) % filters.getSizeInventory();
        int count = 0;
        while (filters.getStackInSlot(currentFilter) == null && count < filters.getSizeInventory()) {
            currentFilter = (currentFilter + 1) % filters.getSizeInventory();
            count++;
        }
    }

    private ItemStack getCurrentFilter() {
        ItemStack filter = filters.getStackInSlot(currentFilter);
        if (filter == null) {
            incrementFilter();
        }
        return filters.getStackInSlot(currentFilter);
    }

    public IInventory getFilters() {
        return filters;
    }

    public EmeraldPipeSettings getSettings() {
        return settings;
    }

    @Override
    public void writeData(ByteBuf data) {
        NBTTagCompound nbt = new NBTTagCompound();
        filters.writeToNBT(nbt);
        settings.writeToNBT(nbt);
        NetworkUtils.writeNBT(data, nbt);
        data.writeByte(currentFilter);
    }

    @Override
    public void readData(ByteBuf data) {
        NBTTagCompound nbt = NetworkUtils.readNBT(data);
        filters.readFromNBT(nbt);
        settings.readFromNBT(nbt);
        currentFilter = data.readUnsignedByte();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        filters.readFromNBT(nbt);
        settings.readFromNBT(nbt);

        currentFilter = nbt.getInteger("currentFilter") % filters.getSizeInventory();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        filters.writeToNBT(nbt);
        settings.writeToNBT(nbt);

        nbt.setInteger("currentFilter", currentFilter);
    }

    @Override
    public void writeGuiData(ByteBuf data) {
        data.writeByte((byte) settings.getFilterMode().ordinal());
    }

    @Override
    public void readGuiData(ByteBuf data, EntityPlayer sender) {
        settings.setFilterMode(FilterMode.values()[data.readByte()]);
    }

    @Override
    public World getWorldBC() {
        return getWorld();
    }
}
