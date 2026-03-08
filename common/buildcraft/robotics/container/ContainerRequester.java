/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.robotics.tile.TileRequester;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;

public class ContainerRequester extends ContainerBCTile<TileRequester> {
    // Network ID's

    protected static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("requester");
    private static final int ID_GET_REQUEST_LIST = IDS.allocId("getRequestList");
    private static final int ID_RECEIVE_REQUEST_LIST = IDS.allocId("receiveRequestList");

    private static final int SLOTS_Y_DIM = 5;
    private static final int SLOTS_X_DIM = 4;

    // public GuiRequester gui;

    // public ItemStack[] requests = new ItemStack[TileRequester.NB_ITEMS];

    // private TileRequester requester;

    public ContainerRequester(MenuType menuType, int id, Player player, TileRequester iRequester) {
        super(menuType, id, player, iRequester);
        // Player inventory
        addFullPlayerInventory(19, 101);

//        requester = iRequester;

        // inv
        for (int x = 0; x < SLOTS_X_DIM; ++x) {
            for (int y = 0; y < SLOTS_Y_DIM; ++y) {
                addSlot(new SlotBase(iRequester.inv, x + y * SLOTS_X_DIM, 117 + x * 18, 7 + y * 18));
            }
        }

        // Player inventory
//        for (int l = 0; l < 3; l++) {
//            for (int k1 = 0; k1 < 9; k1++) {
//                addSlot(new Slot(player.getInventory(), k1 + l * 9 + 9, 19 + k1 * 18, 101 + l * 18));
//            }
//        }

//        for (int i1 = 0; i1 < 9; i1++) {
//            addSlot(new Slot(player.getInventory(), i1, 19 + i1 * 18, 159));
//        }

        // requests
        for (int y = 0; y < SLOTS_Y_DIM; y++) {
            for (int x = 0; x < SLOTS_X_DIM; x++) {
                // slots.add(new RequestSlot(this, x * 5 + y, 9 + 18 * x, 7 + 18 * y));
                addSlot(new SlotDisplay(this::getDisplay, x + y * SLOTS_X_DIM, 9 + 18 * x, 7 + 18 * y));
            }
        }
    }

    private ItemStack getDisplay(int index) {
        return index < tile.requests.size()
                ? tile.requests.get(index)
                : ItemStack.EMPTY;
    }

    public void getRequestList() {
        sendMessage(ID_GET_REQUEST_LIST);
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_SERVER && ID_GET_REQUEST_LIST == id) {
            final ItemStack[] stacks = new ItemStack[TileRequester.NB_ITEMS];

            for (int i = 0; i < TileRequester.NB_ITEMS; ++i) {
                // stacks[i] = requester.getRequestTemplate(i);
                stacks[i] = this.tile.getRequestTemplate(i);
            }

            sendMessage(ID_RECEIVE_REQUEST_LIST, (data) -> {
                for (ItemStack s : stacks) {
                    data.writeItem(s);
                }
            });
        } else if (side == NetworkDirection.PLAY_TO_CLIENT && ID_RECEIVE_REQUEST_LIST == id) {
            // requests = new ItemStack[TileRequester.NB_ITEMS];
            for (int i = 0; i < TileRequester.NB_ITEMS; i++) {
                // requests[i] = NetworkUtils.readStack(stream);
                this.tile.requests.set(i, buffer.readItem());
            }
        }
    }
}
