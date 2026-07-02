package ct.buildcraft.builders.gui;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.BCBuildersGuis;
import ct.buildcraft.builders.item.ItemSchematicSingle;
import ct.buildcraft.builders.item.ItemSnapshot;
import ct.buildcraft.builders.tile.TileReplacer;
import ct.buildcraft.lib.gui.IMenuBCTile;
import ct.buildcraft.lib.gui.MenuBC_Neptune;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MenuReplacer extends MenuBC_Neptune implements IMenuBCTile {

    private static final int MACHINE_SLOT_COUNT = 3;
    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    protected final ContainerLevelAccess access;
    @Nullable
    public final TileReplacer tile;

    public MenuReplacer(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(
            containerId,
            playerInventory,
            new ItemStackHandler(1),
            new ItemStackHandler(1),
            new ItemStackHandler(1),
            CreateClientLevelAccess(buf)
        );
    }

    public MenuReplacer(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(1), new ItemStackHandler(1), new ItemStackHandler(1), ContainerLevelAccess.NULL);
    }

    public MenuReplacer(int containerId, Inventory playerInventory, IItemHandler snapshot, IItemHandler from, IItemHandler to, ContainerLevelAccess access) {
        super(playerInventory, BCBuildersGuis.MENU_REPLACER.get(), containerId);
        this.access = access;
        this.tile = access.evaluate((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileReplacer replacer) {
                if (!level.isClientSide) {
                    replacer.onPlayerOpen(playerInventory.player);
                }
                return replacer;
            }
            return null;
        }, null);

        // BC8 layout: blueprint preview at the top, source/target schematic slots near the bottom, then player inventory.
        this.addSlot(filteredSlot(snapshot, 0, 8, 115, MenuReplacer::isUsedBlueprint));
        this.addSlot(filteredSlot(from, 0, 8, 137, ItemSchematicSingle::isValidUsed));
        this.addSlot(filteredSlot(to, 0, 56, 137, ItemSchematicSingle::isValidUsed));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 159 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 217));
        }
    }

    private static SlotItemHandler filteredSlot(IItemHandler handler, int index, int x, int y, Predicate<ItemStack> filter) {
        return new SlotItemHandler(handler, index, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return filter.test(stack) && super.mayPlace(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return 1;
            }
        };
    }

    private static boolean isUsedBlueprint(ItemStack stack) {
        return stack.getItem() instanceof ItemSnapshot &&
            ItemSnapshot.EnumItemSnapshotType.getFromStack(stack) == ItemSnapshot.EnumItemSnapshotType.BLUEPRINT_USED;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (tile != null) {
            tile.onPlayerClose(player);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (tile != null) {
            tile.sendNetworkGuiTick(playerInventory.player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        original = stack.copy();

        if (index < MACHINE_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stack, 0, MACHINE_SLOT_COUNT, false)) {
                if (index >= PLAYER_INV_START && index < PLAYER_INV_END) {
                    if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= HOTBAR_START && index < HOTBAR_END) {
                    if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        if (tile != null) {
            return tile.canInteractWith(player);
        }
        return super.stillValid(this.access, player, BCBuildersBlocks.REPLACER.get());
    }

    @Override
    public TileBC_Neptune getBCTile() {
        return tile;
    }
}
