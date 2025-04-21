/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.IntegrationRecipe;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.api.tiles.IHasWork;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.recipe.integration.IntegrationRecipeRegistry;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.container.ContainerIntegrationTable;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class TileIntegrationTable extends TileLaserTableBase implements IHasWork, IBCTileMenuProvider {
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

    public TileIntegrationTable() {
        super(BCSiliconBlocks.integrationTableTile.get());
    }

    private boolean extract(IngredientStack item, ImmutableList<IngredientStack> items, boolean simulate) {
        ItemStack targetStack = invTarget.getStackInSlot(0);
        if (targetStack.isEmpty()) return false;
        if (!StackUtil.contains(item, targetStack)) return false;
        if (!extract(invToIntegrate, items, simulate, true)) return false;
        if (!simulate) {
            targetStack.shrink(item.count);
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
            // if (!output.isEmpty() && extract(recipe.getCenterStack(), recipe.getRequirements(output), true))
            if (!output.isEmpty() && extract(recipe.getCenterStack(), recipe.getRequirements(), true))
                return;
        }
        // recipe = IntegrationRecipeRegistry.INSTANCE.getRecipeFor(invTarget.getStackInSlot(0), invToIntegrate.stacks);
        recipe = IntegrationRecipeRegistry.INSTANCE.getRecipeFor(invTarget.getStackInSlot(0), invToIntegrate.stacks, level);
    }

    public ItemStack getOutput() {
        return recipe != null ? recipe.getOutput(invTarget.getStackInSlot(0), invToIntegrate.stacks) : ItemStack.EMPTY;
    }

    @Override
    public long getTarget() {
        ItemStack output = getOutput();
        // return recipe != null && isSpaceEnough(output) ? recipe.getRequiredMicroJoules(output) : 0;
        return recipe != null && isSpaceEnough(output) ? recipe.getRequiredMicroJoules() : 0;
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
            // extract(recipe.getCenterStack(), recipe.getRequirements(output), false);
            extract(recipe.getCenterStack(), recipe.getRequirements(), false);
            ItemStack result = invResult.getStackInSlot(0);
            if (!result.isEmpty()) {
                result = result.copy();
                result.grow(output.getCount());
            } else {
                result = output.copy();
            }
            invResult.setStackInSlot(0, result);
            power -= getTarget();
        }

        sendNetworkGuiUpdate(NET_GUI_DATA);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        if (recipe != null) {
            nbt.putString("recipe", recipe.name.toString());
        }
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
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
    public void getDebugInfo(List<ITextComponent> left, List<ITextComponent> right, Direction side) {
        super.getDebugInfo(left, right, side);
//        left.add("recipe - " + recipe);
        left.add(new StringTextComponent("recipe - " + recipe));
//        left.add("target - " + getTarget());
        left.add(new StringTextComponent("target - " + getTarget()));
    }

    private IntegrationRecipe lookupRecipe(String name) {
        // return IntegrationRecipeRegistry.INSTANCE.getRecipe(new ResourceLocation(name));
        return IntegrationRecipeRegistry.INSTANCE.getRecipe(new ResourceLocation(name), level);
    }

    // MenuProvider

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new ContainerIntegrationTable(BCSiliconMenuTypes.INTEGRATION_TABLE, id, player, this);
    }

    // IHasWork

    @Override
    public boolean hasWork() {
        return recipe != null;
    }
}
