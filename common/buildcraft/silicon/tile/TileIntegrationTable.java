package buildcraft.silicon.tile;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.IntegrationRecipe;

import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.lib.tile.item.ItemHandlerManager;

public class TileIntegrationTable extends TileLaserTableBase {
    public final IItemHandlerModifiable invTarget = addInventory("target", 1, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
    public final IItemHandlerModifiable invToIntegrate = addInventory("toIntegrate", 3 * 3 - 1, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
    public final IItemHandlerModifiable invResult = addInventory("result", 1, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
    public IntegrationRecipe recipe;

    private boolean extract(ItemStack item, ImmutableList<ItemStack> items, boolean simulate) {
        ItemStack targetStack = invTarget.getStackInSlot(0);
        if(targetStack != null && StackUtil.canMerge(targetStack, item) && item.stackSize <= targetStack.stackSize) {
            if(!simulate) {
                targetStack.stackSize -= item.stackSize;
                invTarget.setStackInSlot(0, targetStack);
            }
        } else {
            return false;
        }
        List<ItemStack> itemsNeeded = items.stream().map(ItemStack::copy).collect(Collectors.toList());
        for(int i = 0; i < invToIntegrate.getSlots(); i++) {
            ItemStack stack = invToIntegrate.getStackInSlot(i);
            boolean found = false;
            for(Iterator<ItemStack> iterator = itemsNeeded.iterator(); iterator.hasNext(); ) {
                ItemStack itemStack = iterator.next();
                if(StackUtil.canMerge(stack, itemStack) && stack != null) {
                    found = true;
                    int spend = Math.min(itemStack.stackSize, stack.stackSize);
                    itemStack.stackSize -= spend;
                    if(!simulate) {
                        stack.stackSize -= spend;
                    }
                    if(itemStack.stackSize <= 0) {
                        iterator.remove();
                    }
                    if(!simulate) {
                        if(stack.stackSize <= 0) {
                            stack = null;
                        }
                        invToIntegrate.setStackInSlot(i, stack);
                    }
                }
            }
            if(!found && stack != null) {
                return false;
            }
        }
        return itemsNeeded.size() == 0;
    }

    private boolean isSpaceEnough(ItemStack stack) {
        ItemStack output = invResult.getStackInSlot(0);
        return output == null || (StackUtil.canMerge(stack, output) && stack.stackSize + output.stackSize <= stack.getMaxStackSize());
    }

    private boolean canUseRecipe(IntegrationRecipe recipe) {
        return recipe != null && extract(recipe.target, recipe.toIntegrate, true) && isSpaceEnough(recipe.output);
    }

    private void updateRecipe() {
        if(canUseRecipe(recipe)) {
            return;
        }

        recipe = null;
        for(IntegrationRecipe possible : IntegrationRecipeRegistry.INSTANCE.getAllRecipes()) {
            if(canUseRecipe(possible)) {
                recipe = possible;
                return;
            }
        }
    }

    public long getTarget() {
        return recipe == null ? 0 : recipe.requiredMicroJoules;
    }

    @Override
    public void update() {
        super.update();

        if(worldObj.isRemote) {
            return;
        }

        updateRecipe();

        if(power >= getTarget() && getTarget() != 0) {
            extract(recipe.target, recipe.toIntegrate, false);
            ItemStack result = invResult.getStackInSlot(0);
            if(result != null) {
                result = result.copy();
                result.stackSize += recipe.output.stackSize;
            } else {
                result = recipe.output.copy();
            }
            invResult.setStackInSlot(0, result);
            power -= getTarget();
        }

        sendNetworkGuiUpdate(NET_GUI_DATA);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if(recipe != null) {
            nbt.setTag("recipe", recipe.writeToNBT());
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if(nbt.hasKey("recipe")) {
            recipe = new IntegrationRecipe(nbt.getCompoundTag("recipe"));
        } else {
            recipe = null;
        }
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);

        if(id == NET_GUI_DATA) {
            buffer.writeBoolean(recipe != null);
            if(recipe != null) {
                recipe.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);

        if(id == NET_GUI_DATA) {
            if(buffer.readBoolean()) {
                recipe = new IntegrationRecipe(buffer);
            } else {
                recipe = null;
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("recipe - " + recipe);
        left.add("target - " + getTarget());
    }

    @Override
    public boolean hasWork() {
        return recipe != null;
    }
}
