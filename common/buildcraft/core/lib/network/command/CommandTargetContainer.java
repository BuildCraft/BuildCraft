/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network.command;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

public class CommandTargetContainer extends CommandTarget {
	@Override
	public Class<?> getHandledClass() {
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
