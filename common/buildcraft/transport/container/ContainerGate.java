package buildcraft.transport.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.transport.gate.GateLogic;

public class ContainerGate extends ContainerBC_Neptune {

    public final GateLogic gate;

    public ContainerGate(EntityPlayer player, GateLogic logic) {
        super(player);
        this.gate = logic;
        gate.getPipeHolder().onPlayerOpen(player);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        gate.getPipeHolder().onPlayerClose(player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
