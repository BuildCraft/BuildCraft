package buildcraft.lib.bpt.helper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import buildcraft.api.bpt.IBptAction;
import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.IMaterialProvider;
import buildcraft.api.bpt.IMaterialProvider.IRequestedItem;
import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.NBTUtils;

public class BptActionIItemHandlerSetStack implements IBptAction {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftlib", "bpt_iitemhander_set");

    private final BlockPos pos;
    private final int slot;
    private final IRequestedItem item;

    public BptActionIItemHandlerSetStack(BlockPos pos, int slot, IRequestedItem requested) {
        this.pos = pos;
        this.slot = slot;
        this.item = requested;
    }

    public BptActionIItemHandlerSetStack(NBTTagCompound nbt, IMaterialProvider accessor) {
        this.pos = NBTUtils.readBlockPos(nbt.getTag("pos"));
        this.slot = nbt.getInteger("slot");
        ItemStack stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("requested"));
        this.item = accessor.requestStack(stack);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        return nbt;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public void run(IBuilderAccessor builder) {
        if (item.lock()) {
            TileEntity tile = builder.getWorld().getTileEntity(pos);
            if (tile == null) {
                item.release();
                BCLog.logger.warn("Tile was null!");
                return;
            }
            IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                ItemStack current = handler.getStackInSlot(slot);
                if (current == null) {
                    ItemStack stack = item.getRequested();
                    stack = handler.insertItem(slot, stack, false);
                    if (stack != null) {
                        // builder.returnStack(stack);
                        BCLog.logger.warn("Stack " + stack + " was rejected!");
                    }
                    item.use();
                }
            } else {
                BCLog.logger.warn("Handler was null!");
            }
        }
        item.release();
    }
}
