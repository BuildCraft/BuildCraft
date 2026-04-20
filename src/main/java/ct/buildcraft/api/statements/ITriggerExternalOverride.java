package ct.buildcraft.api.statements;

import net.minecraft.core.Direction;

/** This interface can be used by tiles to override external trigger behaviour.
 *
 * Please use wisely. */
public interface ITriggerExternalOverride {
    enum Result {
        TRUE,
        FALSE,
        IGNORE
    }

    Result override(Direction side, IStatementContainer source, ITriggerExternal trigger, IStatementParameter[] parameters);
}
