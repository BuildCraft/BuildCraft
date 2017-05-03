package buildcraft.silicon.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.silicon.tile.TileAssemblyTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class ContainerAssemblyTable extends ContainerBCTile<TileAssemblyTable> {
    public ContainerAssemblyTable(EntityPlayer player, TileAssemblyTable tile) {
        super(player, tile);
        addFullPlayerInventory(123);

        for(int y = 0; y < 4; y++) {
            for(int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotBase(tile.inv, x + y * 3, 8 + x * 18, 36 + y * 18));
            }
        }

        for(int y = 0; y < 4; y++) {
            for(int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotDisplay(this::getDisplay, x + y * 3, 116 + x * 18, 36 + y * 18));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    private ItemStack getDisplay(int index) {
        return index < tile.recipesStates.size()
                ? new ArrayList<>(tile.recipesStates.keySet()).get(index).output
                : ItemStack.EMPTY;
    }
}
