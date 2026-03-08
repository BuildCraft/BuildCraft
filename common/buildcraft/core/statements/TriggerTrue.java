/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.BCCoreSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TriggerTrue extends BCStatement implements ITriggerInternal {
    public TriggerTrue() {
        super("buildcraftcore:trigger.true");
    }

    @Override
    public SpriteHolder getSprite() {
        return BCCoreSprites.TRIGGER_TRUE;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        return true;
    }

    @Override
    public ITextComponent getDescription() {
//        return LocaleUtil.localize("gate.trigger.true");
        return new TranslationTextComponent("gate.trigger.true");
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.true";
    }
}
