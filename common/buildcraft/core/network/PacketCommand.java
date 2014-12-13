/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import java.util.ArrayList;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import buildcraft.core.utils.Utils;

public class PacketCommand extends BuildCraftPacket {
	public static final ArrayList<CommandTarget> targets;
	public ByteBuf stream;
	public String command;
	public Object target;
	public CommandTarget handler;
	private CommandWriter writer;

	static {
		targets = new ArrayList<CommandTarget>();
		targets.add(new CommandTargetTile());
		targets.add(new CommandTargetEntity());
		targets.add(new CommandTargetContainer());
	}

	public PacketCommand() {
	}

	public PacketCommand(Object target, String command, CommandWriter writer) {
		super();

		this.target = target;
		this.command = command;
		this.writer = writer;

		this.isChunkDataPacket = true;

		// Find the valid handler
		for (CommandTarget c : targets) {
			if (c.getHandledClass().isAssignableFrom(target.getClass())) {
				this.handler = c;
				break;
			}
		}
	}

	public void handle(EntityPlayer player) {
		if (handler != null) {
			ICommandReceiver receiver = handler.handle(player, stream, player.worldObj);
			if (receiver != null) {
				receiver.receiveCommand(command, FMLCommonHandler.instance().getEffectiveSide(), player, stream);
			}
		}
	}

	@Override
	public void writeData(ByteBuf data) {
		Utils.writeUTF(data, command);
		data.writeByte(targets.indexOf(handler));
		handler.write(data, target);
		if (writer != null) {
			writer.write(data);
		}
	}

	@Override
	public void readData(ByteBuf data) {
		command = Utils.readUTF(data);
		handler = targets.get(data.readUnsignedByte());
		stream = data; // for further reading
	}

	@Override
	public int getID() {
		return PacketIds.COMMAND;
	}
}
