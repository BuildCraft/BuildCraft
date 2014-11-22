package buildcraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public abstract class CommandTarget {
	public abstract Class getHandledClass();
	public abstract ICommandReceiver handle(EntityPlayer player, ByteBuf data, World world);
	public abstract void write(ByteBuf data, Object target);
}
