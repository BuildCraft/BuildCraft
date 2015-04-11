package buildcraft.api.statements.v2;

import java.util.Collection;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface ITriggerProvider {
	/**
	 * Returns the list of triggers that are available from the object holding the gate.
	 */
	Collection<Trigger> getInternalTriggers(IStatementContainer container);

	/**
	 * Returns the list of triggers available to a gate next to the given block.
	 */
	Collection<Trigger> getExternalTriggers(ForgeDirection side, TileEntity tile);
}
