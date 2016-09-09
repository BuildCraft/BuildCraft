package buildcraft.lib.net.command;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.BCLibProxy;

public class CommandTargetEntity implements ICommandTarget {
    private final Entity entity;

    public CommandTargetEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public ICommandReceiver getReceiver(PacketBuffer buffer, MessageContext context) {
        int id = buffer.readInt();
        EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(context);
        if (player == null) return null;
        World world = player.worldObj;
        if (world == null) return null;
        Entity entity = world.getEntityByID(id);
        if (entity instanceof ICommandReceiver) {
            return (ICommandReceiver) entity;
        }
        return null;
    }

    @Override
    public void writePositionData(PacketBuffer buffer) {
        buffer.writeInt(entity.getEntityId());
    }

    @Override
    public CommandTargetType getType() {
        return CommandTargetType.ENTITY;
    }
}
