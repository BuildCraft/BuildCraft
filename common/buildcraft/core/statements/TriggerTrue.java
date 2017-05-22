package buildcraft.core.statements;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;

import buildcraft.core.BCCoreSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;

public class TriggerTrue extends BCStatement implements ITriggerInternal {

    public TriggerTrue() {
        super("buildcraftcore:trigger.true");
    }

    @Override
    public SpriteHolder getGuiSprite() {
        return BCCoreSprites.TRIGGER_TRUE;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        return true;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.true");
    }
}
