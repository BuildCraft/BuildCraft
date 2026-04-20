/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package ct.buildcraft.builders.snapshot.pattern;

import java.util.EnumMap;
import java.util.Map;

import ct.buildcraft.api.filler.IFilledTemplate;
import ct.buildcraft.api.filler.IFillerPatternShape;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.builders.BCBuildersSprites;
import ct.buildcraft.builders.snapshot.pattern.parameter.PatternParameterCenter;
import ct.buildcraft.builders.snapshot.pattern.parameter.PatternParameterYDir;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class PatternPyramid extends Pattern implements IFillerPatternShape {
    private static final Map<PatternParameterCenter, PyramidDir> PYRAMID_DIRS = new EnumMap<>(PatternParameterCenter.class);

    private static class PyramidDir {
        /** Starting from the base of the pyramid, how much to add to the x,z, sizes. */
        public final int xLowerDiff, xUpperDiff, zLowerDiff, zUpperDiff;

        public PyramidDir(PatternParameterCenter param) {
            xLowerDiff = param.offsetX >= 0 ? 1 : 0;
            xUpperDiff = param.offsetX <= 0 ? -1 : 0;
            zLowerDiff = param.offsetZ >= 0 ? 1 : 0;
            zUpperDiff = param.offsetZ <= 0 ? -1 : 0;
        }
    }

    static {
        for (PatternParameterCenter param : PatternParameterCenter.values()) {
            PYRAMID_DIRS.put(param, new PyramidDir(param));
        }
    }

    public PatternPyramid() {
        super("pyramid");
    }

    @Override
    public SpriteHolder getSprite() {
        return BCBuildersSprites.FILLER_PYRAMID;
    }

    @Override
    public int maxParameters() {
        return 2;
    }

    @Override
    public int minParameters() {
        return 2;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        switch (index) {
            case 0:
                return PatternParameterYDir.UP;
            case 1:
                return PatternParameterCenter.CENTER;
            default:
                return null;
        }
    }

    // TODO: convert to for loops?
    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        // noinspection RedundantCast
        PyramidDir dir = params.length >= 2 && params[1] != null
            ? PYRAMID_DIRS.get((PatternParameterCenter) params[1])
            : PYRAMID_DIRS.get(PatternParameterCenter.CENTER);
        int stepY = params.length >= 1 && params[0] != null && !(((PatternParameterYDir) params[0]).up) ? -1 : 1;

        int y = stepY == 1 ? 0 : filledTemplate.getMax().getY();

        int xLower = 0;
        int xUpper = filledTemplate.getMax().getX();
        int zLower = 0;
        int zUpper = filledTemplate.getMax().getZ();

        while (y >= 0 && y <= filledTemplate.getMax().getY()) {
            filledTemplate.setAreaXZ(xLower, xUpper, y, zLower, zUpper, true);

            xLower += dir.xLowerDiff;
            xUpper += dir.xUpperDiff;
            zLower += dir.zLowerDiff;
            zUpper += dir.zUpperDiff;
            y += stepY;

            if (xLower > xUpper || zLower > zUpper) {
                break;
            }
        }
        return true;
    }
}
