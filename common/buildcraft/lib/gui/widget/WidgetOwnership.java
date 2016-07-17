package buildcraft.lib.gui.widget;

import java.io.IOException;
import java.util.UUID;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.api.core.BCLog;
import buildcraft.api.permission.EnumProtectionStatus;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.Widget_Neptune;
import buildcraft.lib.permission.PlayerOwner;
import buildcraft.lib.permission.PlayerOwnership;

public class WidgetOwnership extends Widget_Neptune<ContainerBCTile<?>> {
    public static final int NET_INC = 0;
    public static final int NET_DEC = 1;

    public WidgetOwnership(ContainerBCTile<?> container) {
        super(container);
    }

    // TODO: Add protection status changing

    public void incrementStatus() {
        if (isRemote()) {
            this.sendWidgetData((buffer) -> {
                buffer.writeByte(NET_INC);
            });
        } else {
            EnumProtectionStatus status = getStatus();
            setStatus(status.next());
        }
    }

    public void decrementStatus() {
        if (isRemote()) {
            this.sendWidgetData((buffer) -> {
                buffer.writeByte(NET_DEC);
            });
        } else {
            EnumProtectionStatus status = getStatus();
            setStatus(status.last());
        }
    }

    public EnumProtectionStatus getStatus() {
        return PlayerOwnership.INSTANCE.resolveStatus(container.tile);
    }

    /** Only call this server side! */
    private void setStatus(EnumProtectionStatus status) {
        BCLog.logger.info("Setting status to " + status);
        container.tile.setStatus(status);
    }

    public String getOwnerName() {
        PlayerOwner owner = container.tile.getOwner();
        if (owner == null) {
            return "unknown";
        }
        return owner.getOwnerName();
    }

    @Override
    public IMessage handleWidgetDataServer(MessageContext ctx, PacketBuffer buffer) throws IOException {
        int id = buffer.readUnsignedByte();
        if (container.tile.getOwner() == null) {
            // No existing owner - leave public
            BCLog.logger.warn("Rejected packet " + id + " as we have no owner!");
            return null;
        }
        UUID owningUUID = container.tile.getOwner().getPlayerUUID();
        UUID sendingUUID = ctx.getServerHandler().playerEntity.getGameProfile().getId();
        if (owningUUID.equals(sendingUUID)) {
            if (id == NET_INC) {
                incrementStatus();
            } else if (id == NET_DEC) {
                decrementStatus();
            } else {
                BCLog.logger.warn("Unknown ID " + id);
            }
        } else {
            // *siren starts*
            // No, you can't edit some-one else's permissions.
            BCLog.logger.warn(sendingUUID + " attempted to edit " + owningUUID);
        }
        return null;
    }
}
