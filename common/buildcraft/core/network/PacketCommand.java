package buildcraft.core.network;

import java.io.IOException;
import java.util.ArrayList;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import buildcraft.core.utils.Utils;

public class PacketCommand extends BuildCraftPacket {
	public static final ArrayList<CommandTarget> targets;
	public ByteBuf stream;
	public String command;
	public Object target;
	public CommandTarget handler;

	static {
		targets = new ArrayList<CommandTarget>();
		targets.add(new CommandTargetTile());
		targets.add(new CommandTargetEntity());
		targets.add(new CommandTargetContainer());
	}

	public PacketCommand() {
	}

	public PacketCommand(Object target, String command) {
		super();

		this.target = target;
		this.command = command;

		// Find the valid handler
		for (CommandTarget c : targets) {
			if (c.getHandledClass().isAssignableFrom(target.getClass())) {
				this.handler = c;
				break;
			}
		}
	}

	public void handle(EntityPlayer player) {
		System.out.println("Handling packet '" + command + "'");
		if (handler != null) {
			System.out.println("2");
			ICommandReceiver receiver = (ICommandReceiver) handler.handle(player, stream, player.worldObj);
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
