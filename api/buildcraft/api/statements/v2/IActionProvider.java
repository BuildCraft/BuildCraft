package buildcraft.api.statements.v2;

import java.util.Collection;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface IActionProvider {
	/**
	 * Returns the list of triggers that are available from the object holding the gate.
	 */
	Collection<Action> getInternalActions(IStatementContainer container);

	/**
	 * Returns the list of triggers available to a gate next to the given block.
	 */
	Collection<Action> getExternalActions(ForgeDirection side, TileEntity tile);
}
