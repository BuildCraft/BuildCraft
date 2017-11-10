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

import cpw.mods.fml.relauncher.Side;

public interface ICommandReceiver {
	void receiveCommand(String command, Side side, Object sender, ByteBuf stream);
}
