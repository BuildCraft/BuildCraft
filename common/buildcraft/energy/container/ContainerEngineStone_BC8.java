package buildcraft.energy.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.gui.ContainerBCTile;

public class ContainerEngineStone_BC8 extends ContainerBCTile<TileEngineStone_BC8> {

    public ContainerEngineStone_BC8(EntityPlayer player, TileEngineStone_BC8 engine) {
        super(player, engine);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.canInteractWith(player);
    }
}
