/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 */
package ct.buildcraft.builders.snapshot.pattern;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.filler.IFilledTemplate;
import ct.buildcraft.api.filler.IFillerPatternShape;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.builders.BCBuildersSprites;

public class PatternFlatten extends Pattern implements IFillerPatternShape {
    public PatternFlatten() {
        super("flatten");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_FLATTEN;
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        if (filledTemplate.getSize().getY() <= 0) {
            return false;
        }
        // Classic BuildCraft flatten: fill the bottom layer of the selected area. The filler/builder will excavate
        // everything above this layer when excavation is enabled.
        filledTemplate.setPlaneXZ(0, true);
        return true;
    }
}
