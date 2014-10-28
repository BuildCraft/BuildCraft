package buildcraft.api.statements;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface IActionExternal extends IStatement {

	void actionActivate(TileEntity target, ForgeDirection side, IStatementContainer source, IStatementParameter[] parameters);
	
}
