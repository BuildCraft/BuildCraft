/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.statements;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

public class ActionExtractionPreset extends BCStatement implements IActionInternal {

    public final SlotIndex index;

    public ActionExtractionPreset(SlotIndex index) {
        super("buildcraft:extraction.preset." + index.colour.getName(),
            "buildcraft.extraction.preset." + index.colour.getName());
        this.index = index;
    }

    @Override
    public String getDescription() {
        return "gate.action.extraction." + index.colour.getName();
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_EXTRACTION_PRESET;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ISprite getSprite() {
        return null; // STUB(R.Chen): Phase 5 — BCTransportSprites.ACTION_EXTRACTION_PRESET.
    }
}
