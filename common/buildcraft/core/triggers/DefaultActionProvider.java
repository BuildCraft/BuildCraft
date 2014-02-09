package buildcraft.core.triggers;

import buildcraft.BuildCraftCore;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionProvider;
import buildcraft.core.IMachine;
import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import java.util.LinkedList;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

public class DefaultActionProvider implements IActionProvider {

	@Override
	public LinkedList<IAction> getNeighborActions(Block block, TileEntity tile) {
		LinkedList<IAction> res = new LinkedList<IAction>();

		res.add(BuildCraftCore.actionRedstone);

		try {
			if (tile instanceof IMachine) {
				IMachine machine = (IMachine) tile;
				if (machine.allowAction(BuildCraftCore.actionOn))
					res.add(BuildCraftCore.actionOn);
				if (machine.allowAction(BuildCraftCore.actionOff))
					res.add(BuildCraftCore.actionOff);
				if (machine.allowAction(BuildCraftCore.actionLoop))
					res.add(BuildCraftCore.actionLoop);
			}
		} catch (Throwable error) {			
			FMLLog.log("Buildcraft", Level.FATAL, "Outdated API detected, please update your mods!");
		}

		return res;
	}
}
