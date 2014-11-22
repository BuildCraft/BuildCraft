package buildcraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class CommandTargetEntity extends CommandTarget {
	@Override
	public Class getHandledClass() {
		return Entity.class;
	}

	@Override
	public void write(ByteBuf data, Object target) {
		Entity entity = (Entity) target;
		data.writeInt(entity.getEntityId());
	}

	@Override
	public ICommandReceiver handle(EntityPlayer player, ByteBuf data, World world) {
		int id = data.readInt();
		Entity entity = world.getEntityByID(id);
		if (entity != null && entity instanceof ICommandReceiver) {
			return (ICommandReceiver) entity;
		}
		return null;
	}
}
