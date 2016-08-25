package buildcraft.lib.gui;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.gui.widget.WidgetOwnership;
import buildcraft.lib.tile.TileBC_Neptune;

public abstract class ContainerBCTile<T extends TileBC_Neptune> extends ContainerBC_Neptune {
    public final T tile;
    public final WidgetOwnership ownershipWidget;

    public ContainerBCTile(EntityPlayer player, T tile) {
        super(player);
        this.tile = tile;
        tile.onPlayerOpen(player);
        ownershipWidget = addWidget(new WidgetOwnership(this));
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        tile.onPlayerClose(player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.canInteractWith(player);
    }
}
