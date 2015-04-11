package buildcraft.api.statements.v2;

import net.minecraftforge.common.util.ForgeDirection;

public interface IActionReceptor {
	void actionActivated(ForgeDirection side, Action action, IStatementContainer container);
}
