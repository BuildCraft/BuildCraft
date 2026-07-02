/* Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 */
package ct.buildcraft.robotics.tile;

import java.util.List;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.robots.IRequestProvider;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.robotics.BCRoboticsBlocks;
import ct.buildcraft.robotics.container.ContainerRequester;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkHooks;

public class TileRequester extends TileBC_Neptune implements IRequestProvider, IDebuggable, MenuProvider {
    protected static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("requester");

    public static final int NB_ITEMS = 20;

    public final ItemHandlerSimple requests = itemManager.addInvHandler(
            "requests",
            NB_ITEMS,
            (slot, stack) -> true,
            EnumAccess.PHANTOM
    );

    public final ItemHandlerSimple inv = itemManager.addInvHandler(
            "inv",
            NB_ITEMS,
            (slot, stack) -> stack.isEmpty() || canSetRealSlot(slot, stack),
            EnumAccess.BOTH,
            EnumPipePart.VALUES
    );

    public TileRequester(BlockPos pos, BlockState state) {
        super(BCRoboticsBlocks.REQUESTER_TILE.get(), pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public boolean canSetRealSlot(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemStack template = getRequestTemplate(slot);
        return !template.isEmpty() && StackUtil.isMatchingItemOrList(template, stack);
    }

    public void setRequest(int index, ItemStack stack) {
        if (!isValidSlot(index)) {
            return;
        }
        ItemStack template = sanitizeTemplate(stack);
        requests.setStackInSlot(index, template);
        setChanged();
    }

    public ItemStack getRequestTemplate(int index) {
        return isValidSlot(index) ? requests.getStackInSlot(index) : ItemStack.EMPTY;
    }

    public boolean isFulfilled(int index) {
        ItemStack template = getRequestTemplate(index);
        if (template.isEmpty()) {
            return true;
        }
        ItemStack existing = inv.getStackInSlot(index);
        return !existing.isEmpty()
                && StackUtil.isMatchingItemOrList(template, existing)
                && existing.getCount() >= template.getCount();
    }

    @Override
    public int getRequestsCount() {
        return NB_ITEMS;
    }

    @Override
    public ItemStack getRequest(int index) {
        if (!isValidSlot(index) || isFulfilled(index)) {
            return ItemStack.EMPTY;
        }

        ItemStack request = getRequestTemplate(index).copy();
        ItemStack existing = inv.getStackInSlot(index);
        if (existing.isEmpty()) {
            return request;
        }
        if (!StackUtil.isMatchingItemOrList(request, existing)) {
            return ItemStack.EMPTY;
        }

        request.shrink(existing.getCount());
        return request.getCount() > 0 ? request : ItemStack.EMPTY;
    }

    @Override
    public ItemStack offerItem(int index, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!isValidSlot(index)) {
            return stack;
        }

        ItemStack template = getRequestTemplate(index);
        if (template.isEmpty() || !StackUtil.isMatchingItemOrList(template, stack)) {
            return stack;
        }

        ItemStack existing = inv.getStackInSlot(index);
        if (existing.isEmpty()) {
            int accepted = Math.min(stack.getCount(), template.getCount());
            ItemStack inserted = stack.copy();
            inserted.setCount(accepted);
            inv.setStackInSlot(index, inserted);
            return copyRemainder(stack, accepted);
        }

        if (!StackUtil.isMatchingItemOrList(existing, stack)) {
            return stack;
        }

        int missing = template.getCount() - existing.getCount();
        if (missing <= 0) {
            return stack;
        }

        int accepted = Math.min(stack.getCount(), missing);
        ItemStack updated = existing.copy();
        updated.grow(accepted);
        inv.setStackInSlot(index, updated);
        return copyRemainder(stack, accepted);
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before,
            @Nonnull ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (level != null && !level.isClientSide) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public InteractionResult onActivated(Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, this, worldPosition);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerRequester(id, inventory, this, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    public int getComparatorSignal() {
        int countedSlots = 0;
        int nonEmptySlots = 0;
        float power = 0.0F;

        for (int slot = 0; slot < NB_ITEMS; slot++) {
            ItemStack template = getRequestTemplate(slot);
            if (template.isEmpty()) {
                continue;
            }
            countedSlots++;
            ItemStack existing = inv.getStackInSlot(slot);
            if (!existing.isEmpty() && StackUtil.isMatchingItemOrList(template, existing)) {
                nonEmptySlots++;
                int max = Math.min(inv.getSlotLimit(slot), existing.getMaxStackSize());
                if (max <= 0) {
                    max = 64;
                }
                power += (float) existing.getCount() / (float) max;
            }
        }

        if (countedSlots <= 0) {
            return 0;
        }
        power /= countedSlots;
        return (int) Math.floor(power * 14.0F) + (nonEmptySlots > 0 ? 1 : 0);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        int active = 0;
        int missing = 0;
        for (int i = 0; i < NB_ITEMS; i++) {
            if (!getRequestTemplate(i).isEmpty()) {
                active++;
                if (!getRequest(i).isEmpty()) {
                    missing++;
                }
            }
        }
        left.add("active_requests = " + active);
        left.add("missing_requests = " + missing);
    }

    private static boolean isValidSlot(int index) {
        return index >= 0 && index < NB_ITEMS;
    }

    private static ItemStack sanitizeTemplate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        int max = Math.min(copy.getMaxStackSize(), 64);
        if (copy.getCount() <= 0) {
            copy.setCount(1);
        } else if (copy.getCount() > max) {
            copy.setCount(max);
        }
        return copy;
    }

    private static ItemStack copyRemainder(ItemStack stack, int accepted) {
        int remaining = stack.getCount() - accepted;
        if (remaining <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = stack.copy();
        remainder.setCount(remaining);
        return remainder;
    }
}
