package buildcraft.api.statements.v2;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class Action extends Statement {
	public Action(Target target, String uniqueTag) {
		super(target, uniqueTag);
	}

	public void activateActionInternal(IStatementContainer source) {

	}

	public void activateActionExternal(IStatementContainer source, ForgeDirection side, TileEntity tile) {

	}
}
