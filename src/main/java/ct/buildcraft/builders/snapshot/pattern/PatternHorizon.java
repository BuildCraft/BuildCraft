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

public class PatternHorizon extends Pattern implements IFillerPatternShape {
    public PatternHorizon() {
        super("horizon");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_HORIZON;
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        if (filledTemplate.getSize().getY() <= 0) {
            return false;
        }
        // In BC7 Horizon used the selected X/Z area and produced a single horizontal ground layer. In this snapshot
        // based filler the layer is represented by the bottom plane of the selected volume.
        filledTemplate.setPlaneXZ(0, true);
        return true;
    }
}
