package buildcraft.transport;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import buildcraft.core.command.SubCommandBase;

public class PipeThreadInfoCommand extends SubCommandBase {
	@Override
	public String getName() {
		return "pipeThreadInfo";
	}

	@Override
	public String getDescription() {
		return "Returns debug information about pipe threading.";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) {
		sender.addChatMessage(new ChatComponentText("Last tick duration: " + PipeThreadManager.INSTANCE.getCurrentTime() + " ms"));
		sender.addChatMessage(new ChatComponentText("Avg. tick duration: " + PipeThreadManager.INSTANCE.getAverageTime() + " ms"));
		sender.addChatMessage(new ChatComponentText("Pipe count: " + PipeThreadManager.INSTANCE.getPipeCount()));
	}
}
