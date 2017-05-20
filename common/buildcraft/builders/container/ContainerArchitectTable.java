package buildcraft.builders.container;

import buildcraft.builders.item.ItemSnapshot;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.entity.player.EntityPlayer;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ContainerArchitectTable extends ContainerBCTile<TileArchitectTable> {
    private static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("architect_table");
    private static final int ID_NAME = IDS.allocId("NAME");

    public ContainerArchitectTable(EntityPlayer player, TileArchitectTable tile) {
        super(player, tile);
        addFullPlayerInventory(88, 84);

        addSlotToContainer(new SlotBase(tile.invSnapshotIn, 0, 135, 35) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return stack.getItem() instanceof ItemSnapshot;
            }
        });
        addSlotToContainer(new SlotOutput(tile.invSnapshotOut, 0, 194, 35));
    }
    
    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    public void sendNameToServer(String name) {
        sendMessage(ID_NAME, buffer -> buffer.writeString(name));
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == Side.SERVER) {
            if (id == ID_NAME) {
                tile.name = buffer.readString();
                tile.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
            }
        }
    }
}
