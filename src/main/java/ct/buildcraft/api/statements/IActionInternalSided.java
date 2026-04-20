package ct.buildcraft.api.statements;

import net.minecraft.core.Direction;

public interface IActionInternalSided extends IAction {
    void actionActivate(Direction side, IStatementContainer source, IStatementParameter[] parameters);
}
