package buildcraft.api.statements;

import net.minecraft.tileentity.TileEntity;

/**
 * This is implemented by objects containing Statements, such as
 * Gates and TileEntities.
 */
public interface IStatementContainer {
	TileEntity getTile();
}
