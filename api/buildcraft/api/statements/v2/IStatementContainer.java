package buildcraft.api.statements.v2;

import net.minecraft.tileentity.TileEntity;

public interface IStatementContainer {
	Statement getCurrentStatement();
	StatementParameter[] getCurrentParameters();
	
	TileEntity getTile();
}
