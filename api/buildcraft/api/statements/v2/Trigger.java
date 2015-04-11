package buildcraft.api.statements.v2;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class Trigger extends Statement {
	public Trigger(Target target, String uniqueTag) {
		super(target, uniqueTag);
	}

	public boolean isTriggeredInternal(IStatementContainer source) {
		return false;
	}

	public boolean isTriggeredExternal(IStatementContainer source, ForgeDirection side, TileEntity tile) {
		return false;
	}
}
