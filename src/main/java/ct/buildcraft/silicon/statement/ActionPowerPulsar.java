/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.statement;

import ct.buildcraft.api.gates.IGate;
import ct.buildcraft.api.statements.IActionInternalSided;
import ct.buildcraft.api.statements.IActionSingle;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import ct.buildcraft.silicon.BCSiliconSprites;
import ct.buildcraft.silicon.BCSiliconStatements;
import ct.buildcraft.silicon.plug.PluggablePulsar;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class ActionPowerPulsar extends BCStatement implements IActionInternalSided, IActionSingle {

    public final boolean constant;

    public ActionPowerPulsar(boolean constant) {
        super("buildcraft:pulsar." + (constant ? "constant" : "single"),
            "buildcraft.pulsar.constant" + (constant ? "constant" : "single"));
        this.constant = constant;
    }

    @Override
    public Component getDescription() {
        return Component.translatable(constant ? "gate.action.pulsar.constant" : "gate.action.pulsar.single");
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
