package buildcraft.core.statements;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;

import buildcraft.core.BCCoreSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class TriggerTrue extends BCStatement implements ITriggerInternal {

    public TriggerTrue() {
        super("buildcraftcore:trigger.true");
    }

    @Override
    public SpriteHolder getSpriteHolder() {
        return BCCoreSprites.TRIGGER_TRUE;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        return true;
    }
}
