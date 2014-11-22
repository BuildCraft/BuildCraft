package buildcraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

public class CommandTargetContainer extends CommandTarget {
	@Override
	public Class getHandledClass() {
		return Container.class;
	}

	@Override
	public ICommandReceiver handle(EntityPlayer player, ByteBuf data, World world) {
		Container container = player.openContainer;
		if (container != null && container instanceof ICommandReceiver) {
			return (ICommandReceiver) container;
		}
		return null;
	}

	@Override
	public void write(ByteBuf data, Object target) {
	}
}
