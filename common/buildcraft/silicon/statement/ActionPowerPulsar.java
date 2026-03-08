/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.statement;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.*;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.silicon.BCSiliconSprites;
import buildcraft.silicon.BCSiliconStatements;
import buildcraft.silicon.plug.PluggablePulsar;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ActionPowerPulsar extends BCStatement implements IActionInternalSided, IActionSingle {

    public final boolean constant;

    public ActionPowerPulsar(boolean constant) {
        super(
                "buildcraft:pulsar." + (constant ? "constant" : "single"),
                "buildcraft.pulsar.constant" + (constant ? "constant" : "single")
        );
        this.constant = constant;
    }

    @Override
    public ITextComponent getDescription() {
//        return LocaleUtil.localize(constant ? "gate.action.pulsar.constant" : "gate.action.pulsar.single");
        return new TranslationTextComponent(constant ? "gate.action.pulsar.constant" : "gate.action.pulsar.single");
    }

    @Override
    public String getDescriptionKey() {
        return constant ? "gate.action.pulsar.constant" : "gate.action.pulsar.single";
    }

    @Override
    public void actionActivate(Direction side, IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IGate) {
            IGate gate = (IGate) source;
            IPipeHolder pipe = gate.getPipeHolder();
            PipePluggable plug = pipe.getPluggable(side);
            if (plug instanceof PluggablePulsar) {
                PluggablePulsar pulsar = (PluggablePulsar) plug;
                if (constant) {
                    pulsar.enablePulsar();
                } else {
                    pulsar.addSinglePulse();
                }
            }
        }
    }

    @Override
    public boolean singleActionTick() {
        return !constant;
    }

    @Override
    public SpriteHolder getSprite() {
        return constant ? BCSiliconSprites.ACTION_PULSAR_CONSTANT : BCSiliconSprites.ACTION_PULSAR_SINGLE;
    }

    @Override
    public IStatement[] getPossible() {
        return BCSiliconStatements.ACTION_PULSAR;
    }
}
