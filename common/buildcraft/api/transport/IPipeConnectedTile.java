package buildcraft.api.transport;

import net.minecraftforge.common.ForgeDirection;

/**
 * Implementors can connect to pipes even without inventory/tank implementations.
 *
 * Valid for Item and Fluid Pipe Connections only.
 */
public interface IPipeConnectedTile {
	
	boolean canPipeConnect( IPipeTile.PipeType type, ForgeDirection direction );
	
}
