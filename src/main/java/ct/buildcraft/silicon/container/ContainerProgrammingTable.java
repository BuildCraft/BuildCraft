package ct.buildcraft.silicon.container;

import java.io.IOException;

import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.slot.SlotBase;
import ct.buildcraft.lib.gui.slot.SlotDisplay;
import ct.buildcraft.lib.gui.slot.SlotOutput;
import ct.buildcraft.lib.tile.item.IItemHandlerAdv;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.silicon.BCSiliconGuis;
import ct.buildcraft.silicon.tile.TileProgrammingTable_Neptune;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;

public class ContainerProgrammingTable extends ContainerBCTile<TileProgrammingTable_Neptune> {
    public static final int NET_SELECT_OPTION = 10;

    public ContainerProgrammingTable(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, new ItemHandlerSimple(1), new ItemHandlerSimple(1),
                new ItemHandlerSimple(TileProgrammingTable_Neptune.OPTION_COUNT), CreateClientLevelAccess(buf));
    }

    public ContainerProgrammingTable(int containerId, Inventory playerInventory, IItemHandlerAdv invInput,
                                     IItemHandlerAdv invOutput, IItemHandler options,
                                     ContainerLevelAccess access) {
        super(BCSiliconGuis.MENU_PROGRAMMING_TABLE.get(), playerInventory, containerId, access);

        addSlot(new SlotBase(invInput, 0, 8, 36));
        addSlot(new SlotOutput(invOutput, 0, 8, 90));

        for (int y = 0; y < TileProgrammingTable_Neptune.HEIGHT; y++) {
            for (int x = 0; x < TileProgrammingTable_Neptune.WIDTH; x++) {
                int index = x + y * TileProgrammingTable_Neptune.WIDTH;
                addSlot(new SlotDisplay(options, index, 43 + x * 18, 36 + y * 18));
            }
        }

        addFullPlayerInventory(123);
    }

    public void sendSelectOption(int option) {
        sendMessage(NET_SELECT_OPTION, buffer -> buffer.writeVarInt(option));
    }

    @Override
    public void readMessage(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == LogicalSide.SERVER && id == NET_SELECT_OPTION && tile != null) {
            tile.selectOption(buffer.readVarInt());
        }
    }
}
