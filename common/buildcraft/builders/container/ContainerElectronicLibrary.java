package buildcraft.builders.container;

import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.tile.TileElectronicLibrary;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.misc.data.AutoId;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class ContainerElectronicLibrary extends ContainerBCTile<TileElectronicLibrary> {
    @AutoId
    public static int ID_SELECTED;

    public ContainerElectronicLibrary(EntityPlayer player, TileElectronicLibrary tile) {
        super(player, tile);
        addFullPlayerInventory(138);

        addSlotToContainer(new SlotOutput(tile.invDownOut, 0, 175, 57) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }
        });
        addSlotToContainer(new SlotBase(tile.invDownIn, 0, 219, 57) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemSnapshot && ItemSnapshot.EnumItemSnapshotType.getFromStack(stack).used;
            }

            @Override
            public int getSlotStackLimit() {
                return 1;
            }
        });

        addSlotToContainer(new SlotBase(tile.invUpIn, 0, 175, 79) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemSnapshot;
            }
        });
        addSlotToContainer(new SlotOutput(tile.invUpOut, 0, 219, 79) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }
        });
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return true;
    }

    public void sendSelectedToServer(Snapshot.Header selected) {
        sendMessage(ID_SELECTED, buffer -> {
            buffer.writeBoolean(selected != null);
            if (selected != null) {
                selected.writeToByteBuf(buffer);
            }
        });
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == Side.SERVER) {
            if (id == ID_SELECTED) {
                if (buffer.readBoolean()) {
                    tile.selected = new Snapshot.Header();
                    tile.selected.readFromByteBuf(buffer);
                } else {
                    tile.selected = null;
                }
                tile.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
            }
        }
    }
}
