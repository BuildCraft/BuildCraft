package buildcraft.transport.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCLog;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.transport.gate.GateLogic;

public class ContainerGate extends ContainerBC_Neptune {
    public static final int ID_CONNECTION = 5;

    public final GateLogic gate;

    public final int slotHeight;

    public ContainerGate(EntityPlayer player, GateLogic logic) {
        super(player);
        this.gate = logic;
        gate.getPipeHolder().onPlayerOpen(player);

        boolean split = gate.isSplitInTwo();
        int s = gate.variant.numSlots;
        if (split) {
            s = (int) Math.ceil(s / 2.0);
        }
        slotHeight = s;

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

    @Override
    public void handleMessage(MessageContext ctx, PacketBuffer buffer, Side side) {
        int id = buffer.readUnsignedByte();
        if (side == Side.SERVER) {
            if (id == ID_CONNECTION) {
                int index = buffer.readUnsignedByte();
                boolean to = buffer.readBoolean();
                if (index < gate.connections.length) {
                    gate.connections[index] = to;
                    gate.sendResolveData();
                }
            }
        }
    }

    public void setConnected(int index, boolean to) {
        sendMessage((buffer) -> {
            buffer.writeByte(ID_CONNECTION);
            buffer.writeByte(index);
            buffer.writeBoolean(to);
        });
    }
}
