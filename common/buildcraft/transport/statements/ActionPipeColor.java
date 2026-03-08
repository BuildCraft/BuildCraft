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
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionPipeColor extends BCStatement implements IActionInternal {

    public final DyeColor color;

    public ActionPipeColor(DyeColor color) {
        super(
                "buildcraft:pipe.color." + color.getName(),
                "buildcraft.pipe." + color.getName()
        );
        this.color = color;
    }

    @Override
    public ITextComponent getDescription() {
//        return String.format(LocaleUtil.localize("gate.action.pipe.item.color"), ColourUtil.getTextFullTooltip(color));
        return new TranslationTextComponent("gate.action.pipe.item.color", ColourUtil.getTextFullTooltipComponent(color));
    }

    @Override
    public String getDescriptionKey() {
        return "gate.action.pipe.item.color." + color.getName();
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
        // Pipes listen for this -- we don't need to do anything here
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_PIPE_COLOUR;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return BCTransportSprites.ACTION_PIPE_COLOUR[color.ordinal()];
    }
}
