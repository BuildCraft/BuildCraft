/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.IntegrationRecipe;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.recipe.integration.IntegrationRecipeRegistry;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.container.ContainerIntegrationTable;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

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

    public TileIntegrationTable(BlockPos pos, BlockState blockState) {
        super(BCSiliconBlocks.integrationTableTile.get(), pos, blockState);
    }

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

        if (level.isClientSide) {
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
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (recipe != null) {
            nbt.putString("recipe", recipe.name.toString());
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("recipe")) {
            recipe = lookupRecipe(nbt.getString("recipe"));
        } else {
            recipe = null;
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);

        if (id == NET_GUI_DATA) {
            buffer.writeBoolean(recipe != null);
            if (recipe != null) {
                buffer.writeUtf(recipe.name.toString());
            }
        }
    }

    @Override
//    public void readPayload(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
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
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
        super.getDebugInfo(left, right, side);
//        left.add("recipe - " + recipe);
        left.add(Component.literal("recipe - " + recipe));
//        left.add("target - " + getTarget());
        left.add(Component.literal("target - " + getTarget()));
    }

    private IntegrationRecipe lookupRecipe(String name) {
        return IntegrationRecipeRegistry.INSTANCE.getRecipe(new ResourceLocation(name));
    }

    // MenuProvider

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerIntegrationTable(BCSiliconMenuTypes.INTEGRATION_TABLE, id, player, this);
    }
}
