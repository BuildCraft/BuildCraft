/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionExtractionPreset extends BCStatement implements IActionInternal {

    public final SlotIndex index;

    public ActionExtractionPreset(SlotIndex index) {
        super(
                "buildcraft:extraction.preset." + index.colour.getName(),
                "buildcraft.extraction.preset." + index.colour.getName()
        );

        this.index = index;
    }

    @Override
    public ITextComponent getDescription() {
//        return LocaleUtil.localize("gate.action.extraction", ColourUtil.getTextFullTooltip(index.colour));
        return new TranslationTextComponent("gate.action.extraction", ColourUtil.getTextFullTooltipComponent(index.colour));
    }

    @Override
    public String getDescriptionKey() {
        return "gate.action.extraction." + index.colour.getName();
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
        // The pipe handles this
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_EXTRACTION_PRESET;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return BCTransportSprites.ACTION_EXTRACTION_PRESET.get(index);
    }
}
