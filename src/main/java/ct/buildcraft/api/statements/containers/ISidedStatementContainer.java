package ct.buildcraft.api.statements.containers;

import ct.buildcraft.api.statements.IStatementContainer;

import net.minecraft.core.Direction;

/** Created by asie on 3/14/15. */
public interface ISidedStatementContainer extends IStatementContainer {
    Direction getSide();
}
