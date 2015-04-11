package buildcraft.api.statements.v2;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class Action extends Statement {
	public Action(Target target, String uniqueTag) {
		super(target, uniqueTag);
	}

	/**
	 * Activates the action for an Internal target
	 * @param container The statement container
	 */
	public void activateActionInternal(IStatementContainer container) {

	}

	/**
	 * Activates the action for an External target
	 * @param container The statement container
	 * @param side The side the tile entity is on, relative to the container
	 * @param tile The tile entity activated by the action
	 */
	public void activateActionExternal(IStatementContainer container, ForgeDirection side, TileEntity tile) {

	}
}
