package ct.buildcraft.factory.client.gui;

import javax.annotation.Nullable;

import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.factory.BCFactoryGuis;
import ct.buildcraft.factory.tile.TileHeatExchange;
import ct.buildcraft.lib.fluid.Tank;
import ct.buildcraft.lib.gui.IMenuBCTile;
import ct.buildcraft.lib.gui.MenuBC_Neptune;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class MenuHeatExchange extends MenuBC_Neptune implements IMenuBCTile {

    private final ContainerLevelAccess access;
    protected ContainerData data;
    @Nullable
    public final TileHeatExchange tile;

    public MenuHeatExchange(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, new ItemStackHandler(4), new SimpleContainerData(8), DataSlot.standalone(), CreateClientLevelAccess(buf));
    }

    public MenuHeatExchange(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(4), new SimpleContainerData(8), DataSlot.standalone(), ContainerLevelAccess.NULL);
    }

    public MenuHeatExchange(int containerId, Inventory playerInventory, IItemHandler item,
        ContainerData tank, DataSlot bg, ContainerLevelAccess access) {
        super(playerInventory, BCFactoryGuis.MENU_HEAT_EXCHANGE.get(), containerId);
        this.access = access;
        this.data = tank;
        this.tile = access.evaluate((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileHeatExchange heatExchange) {
                if (!level.isClientSide) {
                    heatExchange.onPlayerOpen(playerInventory.player);
                }
                return heatExchange;
            }
            return null;
        }, null);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 89 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 147));
        }
        addDataSlots(tank);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ct.buildcraft.lib.gui.BCMenuUtil.quickMoveStack(this, player, index);
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
    public boolean stillValid(Player player) {
        if (tile != null) {
            return tile.canInteractWith(player);
        }
        return super.stillValid(this.access, player, BCFactoryBlocks.HEATEXCHANGE_BLOCK.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int index) {
        return access.evaluate((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileHeatExchange heatExchange) {
                Tank tank = heatExchange.getSectionTank(index / 2);
                if (tank != null) {
                    tank.onGuiClicked(this, player);
                }
            }
            return false;
        }).orElse(false);
    }

    @Override
    public TileBC_Neptune getBCTile() {
        return tile;
    }
}
