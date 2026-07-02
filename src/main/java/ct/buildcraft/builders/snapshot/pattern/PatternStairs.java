/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package ct.buildcraft.builders.snapshot.pattern;

import ct.buildcraft.api.filler.IFilledTemplate;
import ct.buildcraft.api.filler.IFillerPatternShape;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.builders.BCBuildersSprites;
import ct.buildcraft.builders.snapshot.pattern.parameter.PatternParameterXZDir;
import ct.buildcraft.builders.snapshot.pattern.parameter.PatternParameterYDir;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PatternStairs extends Pattern implements IFillerPatternShape {
    public PatternStairs() {
        super("stairs");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return BCBuildersSprites.FILLER_STAIRS;
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
        return index == 1 ? PatternParameterXZDir.EAST : PatternParameterYDir.UP;
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        PatternParameterYDir yDir = getParam(0, params, PatternParameterYDir.UP);
        PatternParameterXZDir xzDir = getParam(1, params, PatternParameterXZDir.EAST);

        int xMin = 0;
        int yMin = 0;
        int zMin = 0;
        int xMax = filledTemplate.getMax().getX();
        int yMax = filledTemplate.getMax().getY();
        int zMax = filledTemplate.getMax().getZ();

        int sizeX = xMax - xMin + 1;
        int sizeZ = zMax - zMin + 1;

        int height;
        int heightStep;
        if (yDir.up) {
            height = Math.min(yMax, Math.max(xMax, zMax));
            heightStep = -1;
        } else {
            height = Math.max(yMin, yMax - Math.max(xMax, zMax));
            heightStep = 1;
        }

        int stepEast = 0;
        int stepWest = 0;
        int stepSouth = 0;
        int stepNorth = 0;
        Direction direction = xzDir.dir;
        if (direction == Direction.EAST) {
            stepEast = 1;
        } else if (direction == Direction.WEST) {
            stepWest = 1;
        } else if (direction == Direction.SOUTH) {
            stepSouth = 1;
        } else if (direction == Direction.NORTH) {
            stepNorth = 1;
        }

        int x1 = xMin;
        int x2 = xMax;
        int z1 = zMin;
        int z2 = zMax;

        if (stepEast == 1) {
            x1 = xMax - sizeX + 1;
            x2 = x1;
        }
        if (stepWest == 1) {
            x2 = xMin + sizeX - 1;
            x1 = x2;
        }
        if (stepSouth == 1) {
            z1 = zMax - sizeZ + 1;
            z2 = z1;
        }
        if (stepNorth == 1) {
            z2 = zMin + sizeZ - 1;
            z1 = z2;
        }

        while (x2 - x1 + 1 > 0
            && z2 - z1 + 1 > 0
            && x2 - x1 < sizeX
            && z2 - z1 < sizeZ
            && height >= yMin
            && height <= yMax) {
            filledTemplate.setAreaXZ(x1, x2, height, z1, z2, true);

            x2 += stepEast;
            x1 -= stepWest;
            z2 += stepSouth;
            z1 -= stepNorth;
            height += heightStep;
        }
        return true;
    }
}
