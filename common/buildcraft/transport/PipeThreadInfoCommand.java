package buildcraft.transport;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
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
		for (World w : PipeThreadManager.INSTANCE.managers.keySet()) {
			PipeThreadManager.PipeWorldThreadManager manager = PipeThreadManager.INSTANCE.managers.get(w);
			sender.addChatMessage(new ChatComponentText("Dimension " + w.provider.dimensionId + ": " + manager.getAverageTime() + " ms [" + manager.getPipeCount() + " pipes]"));
		}
	}
}
