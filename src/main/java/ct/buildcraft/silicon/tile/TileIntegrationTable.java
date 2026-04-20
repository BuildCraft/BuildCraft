/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.tile;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.recipes.IngredientStack;
import ct.buildcraft.api.recipes.IntegrationRecipe;
import ct.buildcraft.lib.gui.ItemProvider;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.lib.recipe.IntegrationRecipeRegistry;
import ct.buildcraft.lib.tile.item.ItemHandlerManager;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.silicon.BCSiliconBlocks;
import ct.buildcraft.silicon.container.ContainerIntegrationTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class TileIntegrationTable extends TileLaserTableBase implements MenuProvider{
	
	public TileIntegrationTable(BlockPos pos, BlockState state) {
		super(BCSiliconBlocks.INTERGRATION_TABLE_TILE.get(), pos, state);
	}

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
    public final ItemProvider invOutput = new ItemProvider((i) -> getOutput(), 1);
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
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);

        if (id == NET_GUI_DATA) {
            buffer.writeBoolean(recipe != null);
            if (recipe != null) {
                buffer.writeUtf(recipe.name.toString());
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);

        if (id == NET_GUI_DATA) {
            if (buffer.readBoolean()) {
                recipe = lookupRecipe(buffer.readUtf());
            } else {
                recipe = null;
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("recipe - " + recipe);
        left.add("target - " + getTarget());
    }

    private IntegrationRecipe lookupRecipe(String name) {
        return IntegrationRecipeRegistry.INSTANCE.getRecipe(new ResourceLocation(name));
    }
    
	@Override
	public InteractionResult onActivated(Player player, InteractionHand hand, BlockHitResult hit) {
		if(player instanceof ServerPlayer splayer) {
			NetworkHooks.openScreen(splayer, this, worldPosition);
		}
		return super.onActivated(player, hand, hit);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p_39956_) {
		return new ContainerIntegrationTable(id, inventory, invTarget, invToIntegrate, invOutput, invResult, ContainerLevelAccess.create(level, worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("TileIntegrationTable:TODO");//TODO
	}
}
