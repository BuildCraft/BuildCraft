package ct.buildcraft.api.statements;

import net.minecraft.core.Direction;

public interface ITriggerInternalSided extends ITrigger {
    boolean isTriggerActive(Direction side, IStatementContainer source, IStatementParameter[] parameters);
}
