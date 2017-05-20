package buildcraft.builders.container;

import buildcraft.builders.item.ItemSnapshot;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class ContainerBuilder extends ContainerBCTile<TileBuilder> {
    public final List<WidgetFluidTank> widgetTanks;

    public ContainerBuilder(EntityPlayer player, TileBuilder tile) {
        super(player, tile);

        addFullPlayerInventory(140);

        addSlotToContainer(new SlotBase(tile.invSnapshot, 0, 80, 27) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return stack.getItem() instanceof ItemSnapshot && ItemSnapshot.EnumItemSnapshotType.getFromStack(stack).used;
            }
        });

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlotToContainer(new SlotBase(tile.invResources, sx + sy * 9, 8 + sx * 18, 72 + sy * 18));
            }
        }

        widgetTanks = tile.getTankManager().stream()
                .map(tank -> new WidgetFluidTank(this, tank))
                .map(this::addWidget)
                .collect(Collectors.toList());

        for(int y = 0; y < 6; y++) {
            for(int x = 0; x < 4; x++) {
                addSlotToContainer(new SlotDisplay(this::getDisplay, x + y * 4, 179 + x * 18, 18 + y * 18));
            }
        }
    }

    private ItemStack getDisplay(int index) {
        return tile.snapshotType == EnumSnapshotType.BLUEPRINT &&
                index < tile.blueprintBuilder.remainingDisplayRequired.size()
                ? tile.blueprintBuilder.remainingDisplayRequired.get(index)
                : ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
