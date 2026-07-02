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
import ct.buildcraft.builders.snapshot.pattern.parameter.PatternParameterHollow;

public class PatternCylinder extends Pattern implements IFillerPatternShape {
    public PatternCylinder() {
        super("cylinder");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_CYLINDER;
    }

    @Override
    public int minParameters() {
        return 1;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return index == 0 ? PatternParameterHollow.FILLED_INNER : null;
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        PatternParameterHollow hollow = getParam(0, params, PatternParameterHollow.FILLED_INNER);
        boolean filled = hollow == PatternParameterHollow.FILLED_INNER;
        boolean outerFilled = hollow == PatternParameterHollow.FILLED_OUTER;

        int maxX = filledTemplate.getMax().getX();
        int maxY = filledTemplate.getMax().getY();
        int maxZ = filledTemplate.getMax().getZ();

        double cx = maxX / 2.0;
        double cz = maxZ / 2.0;
        double rx = cx + 0.5;
        double rz = cz + 0.5;

        if (rx <= 0 || rz <= 0) {
            filledTemplate.setAll(true);
            return true;
        }

        for (int x = 0; x <= maxX; x++) {
            double dx = Math.abs(x - cx) / rx;
            double dxx = dx * dx;
            for (int z = 0; z <= maxZ; z++) {
                double dz = Math.abs(z - cz) / rz;
                double dzz = dz * dz;
                boolean inside = dxx + dzz <= 1.0;
                if (!inside) {
                    if (outerFilled) {
                        for (int y = 0; y <= maxY; y++) {
                            filledTemplate.set(x, y, z, true);
                        }
                    }
                    continue;
                }

                boolean edge = !isInsideEllipse(x - 1, z, cx, cz, rx, rz)
                    || !isInsideEllipse(x + 1, z, cx, cz, rx, rz)
                    || !isInsideEllipse(x, z - 1, cx, cz, rx, rz)
                    || !isInsideEllipse(x, z + 1, cx, cz, rx, rz);

                if (filled || edge) {
                    for (int y = 0; y <= maxY; y++) {
                        filledTemplate.set(x, y, z, true);
                    }
                }
            }
        }
        return true;
    }

    private static boolean isInsideEllipse(int x, int z, double cx, double cz, double rx, double rz) {
        double dx = Math.abs(x - cx) / rx;
        double dz = Math.abs(z - cz) / rz;
        return dx * dx + dz * dz <= 1.0;
    }
}
