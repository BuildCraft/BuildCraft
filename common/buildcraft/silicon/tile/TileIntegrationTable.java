/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.IntegrationRecipe;

import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class TileIntegrationTable extends TileLaserTableBase {
    public final ItemHandlerSimple invTarget = itemManager.addInvHandler(
        "target",
        1,
        ItemHandlerManager.EnumAccess.BOTH,
        EnumPipePart.VALUES
    );
    public final ItemHandlerSimple invToIntegrate = itemManager.addInvHandler(
        "toIntegrate",
        3 * 3 - 1,
        ItemHandlerManager.EnumAccess.BOTH,
        EnumPipePart.VALUES
    );
    public final ItemHandlerSimple invResult = itemManager.addInvHandler(
        "result",
        1,
        ItemHandlerManager.EnumAccess.INSERT,
        EnumPipePart.VALUES
    );
    public IntegrationRecipe recipe;

    private boolean extract(IngredientStack item, ImmutableList<IngredientStack> items, boolean simulate) {
        ItemStack targetStack = invTarget.getStackInSlot(0);
        if (targetStack.isEmpty()) return false;
        if (!StackUtil.contains(item, targetStack)) return false;
        if (!extract(invToIntegrate, items, simulate, true)) return false;
        if (!simulate) {
            targetStack.setCount(targetStack.getCount() - item.count);
            invTarget.setStackInSlot(0, targetStack);
        }
        return true;
    }

    private boolean isSpaceEnough(ItemStack stack) {
        ItemStack output = invResult.getStackInSlot(0);
        return output.isEmpty() || (StackUtil.canMerge(stack, output) && stack.getCount() + output.getCount() <= stack.getMaxStackSize());
    }

    private void updateRecipe() {
        if (recipe != null) {
            ItemStack output = getOutput();
            if (!output.isEmpty() && extract(recipe.getCenterStack(), recipe.getRequirements(output), true))
                return;
        }
        recipe = IntegrationRecipeRegistry.INSTANCE.getRecipeFor(invTarget.getStackInSlot(0), invToIntegrate.stacks);
    }

    public ItemStack getOutput() {
        return recipe != null ? recipe.getOutput(invTarget.getStackInSlot(0), invToIntegrate.stacks) : ItemStack.EMPTY;
    }

    @Override
    public long getTarget() {
        ItemStack output = getOutput();
        return recipe != null && isSpaceEnough(output) ? recipe.getRequiredMicroJoules(output) : 0;
    }

    @Override
    public void update() {
        super.update();

        if (world.isRemote) {
            return;
        }

        updateRecipe();

        if (getTarget() > 0 && power >= getTarget()) {
            ItemStack output = getOutput();
            extract(recipe.getCenterStack(), recipe.getRequirements(output), false);
            ItemStack result = invResult.getStackInSlot(0);
            if (!result.isEmpty()) {
                result = result.copy();
                result.setCount(result.getCount() + output.getCount());
            } else {
                result = output.copy();
            }
            invResult.setStackInSlot(0, result);
            power -= getTarget();
        }

        sendNetworkGuiUpdate(NET_GUI_DATA);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (recipe != null) {
            nbt.setString("recipe", recipe.name.toString());
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("recipe")) {
            recipe = lookupRecipe(nbt.getString("recipe"));
        } else {
            recipe = null;
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);

        if (id == NET_GUI_DATA) {
            buffer.writeBoolean(recipe != null);
            if (recipe != null) {
                buffer.writeString(recipe.name.toString());
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);

        if (id == NET_GUI_DATA) {
            if (buffer.readBoolean()) {
                recipe = lookupRecipe(buffer.readString());
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

    private IntegrationRecipe lookupRecipe(String name) {
        return IntegrationRecipeRegistry.INSTANCE.getRecipe(new ResourceLocation(name));
    }
}
