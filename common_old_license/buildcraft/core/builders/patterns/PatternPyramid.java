/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import java.util.EnumMap;
import java.util.Map;

import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.core.BCCoreSprites;

public class PatternPyramid extends Pattern {
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
        return BCCoreSprites.FILLER_PYRAMID;
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

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        FilledTemplate bpt = new FilledTemplate(filler.getBox());

        PyramidDir dir;
        int stepY;

        if (params.length >= 1 && params[0] != null && !(((PatternParameterYDir) params[0]).up)) {
            stepY = -1;
        } else {
            stepY = 1;
        }

        if (params.length >= 2 && params[1] != null) {
            dir = PYRAMID_DIRS.get(params[1]);
        } else {
            dir = PYRAMID_DIRS.get(PatternParameterCenter.CENTER);
        }

        int y;
        if (stepY == 1) {
            y = 0;
        } else {
            y = bpt.maxY;
        }

        int xLower = 0;
        int xUpper = bpt.maxX;
        int zLower = 0;
        int zUpper = bpt.maxZ;

        while (y >= 0 && y <= bpt.maxY) {
            for (int x = xLower; x <= xUpper; ++x) {
                bpt.fillLineZ(x, y, zLower, zUpper);
            }

            xLower += dir.xLowerDiff;
            xUpper += dir.xUpperDiff;
            zLower += dir.zLowerDiff;
            zUpper += dir.zUpperDiff;
            y += stepY;

            if (xLower > xUpper || zLower > zUpper) {
                break;
            }
        }

        return bpt;
    }
}
