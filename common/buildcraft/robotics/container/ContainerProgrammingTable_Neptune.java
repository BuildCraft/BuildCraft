package buildcraft.robotics.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.silicon.tile.TileProgrammingTable_Neptune;

public class ContainerProgrammingTable_Neptune extends ContainerBCTile<TileProgrammingTable_Neptune> {
    public ContainerProgrammingTable_Neptune(EntityPlayer player, TileProgrammingTable_Neptune tile) {
        super(player, tile);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tile.canInteractWith(playerIn);
    }
}
