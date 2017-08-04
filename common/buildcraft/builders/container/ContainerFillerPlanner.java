package buildcraft.builders.container;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.builders.addon.AddonFillingPlanner;

public class ContainerFillerPlanner extends ContainerBC_Neptune {

    public final AddonFillingPlanner fillerPlanner;

    public ContainerFillerPlanner(EntityPlayer player, AddonFillingPlanner fillerPlanner) {
        super(player);
        this.fillerPlanner = fillerPlanner;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    public void setInverted(boolean value) {
        fillerPlanner.inverted = value;
        sendData();
    }

    private void sendData() {
        sendMessage(NET_DATA, buffer -> {
            fillerPlanner.pattern.writeToBuffer(buffer);
            buffer.writeBoolean(fillerPlanner.inverted);
        });
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        if (side == Side.SERVER) {
            if (id == NET_DATA) {
                fillerPlanner.pattern.readFromBuffer(buffer);
                fillerPlanner.inverted = buffer.readBoolean();
                sendData();
            }
        } else if (side == Side.CLIENT) {
            if (id == NET_DATA) {
                fillerPlanner.pattern.readFromBuffer(buffer);
                fillerPlanner.inverted = buffer.readBoolean();
            }
        }
    }
}
