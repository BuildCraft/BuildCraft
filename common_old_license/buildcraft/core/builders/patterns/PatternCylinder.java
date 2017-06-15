/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.core.BCCoreSprites;

public class PatternCylinder extends Pattern {

    public PatternCylinder() {
        super("cylinder");
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public int minParameters() {
        return 1;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return PatternParameterHollow.HOLLOW;
    }

    @Override
    public SpriteHolder getSprite() {
        return BCCoreSprites.FILLER_CYLINDER;
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        IBox box = filler.getBox();
        FilledTemplate result = new FilledTemplate(box);
        boolean filled = params.length > 0 && ((PatternParameterHollow) params[0]).filled;

        int xMin = 0;
        int yMin = 0;
        int zMin = 0;

        int xMax = box.size().getX() - 1;
        int yMax = box.size().getY() - 1;
        int zMax = box.size().getZ() - 1;

        int xFix = (xMax - xMin) % 2;
        int zFix = (zMax - zMin) % 2;

        int xCenter = (xMax + xMin) / 2 + (xMax + xMin < 0 && xFix == 1 ? -1 : 0);
        int zCenter = (zMax + zMin) / 2 + (zMax + zMin < 0 && zFix == 1 ? -1 : 0);

        int xRadius = (xMax - xMin) / 2;
        int zRadius = (zMax - zMin) / 2;

        if (xRadius == 0 || zRadius == 0) {
            result.fillVolume(xMin, xMax, yMin, yMax, zMin, zMax);
            return result;
        }

        int dx = xRadius, dz = 0;
        int xChange = zRadius * zRadius * (1 - 2 * xRadius);
        int zChange = xRadius * xRadius;
        int ellipseError = 0;
        int twoASquare = 2 * xRadius * xRadius;
        int twoBSquare = 2 * zRadius * zRadius;
        int stoppingX = twoBSquare * xRadius;
        int stoppingZ = 0;

        if (twoASquare > 0) {
            while (stoppingX >= stoppingZ) {
                if (filled) {
                    fillSquare(xCenter, zCenter, dx, dz, xFix, zFix, yMin, yMax, result);
                } else {
                    fillFourColumns(xCenter, zCenter, dx, dz, xFix, zFix, yMin, yMax, result);
                }

                ++dz;
                stoppingZ += twoASquare;
                ellipseError += zChange;
                zChange += twoASquare;
                if (2 * ellipseError + xChange > 0) {
                    --dx;
                    stoppingX -= twoBSquare;
                    ellipseError += xChange;
                    xChange += twoBSquare;
                }
            }
        }

        dx = 0;
        dz = zRadius;
        xChange = zRadius * zRadius;
        zChange = xRadius * xRadius * (1 - 2 * zRadius);
        ellipseError = 0;
        stoppingX = 0;
        stoppingZ = twoASquare * zRadius;

        if (twoBSquare > 0) {
            while (stoppingX <= stoppingZ) {
                if (filled) {
                    fillSquare(xCenter, zCenter, dx, dz, xFix, zFix, yMin, yMax, result);
                } else {
                    fillFourColumns(xCenter, zCenter, dx, dz, xFix, zFix, yMin, yMax, result);
                }

                ++dx;
                stoppingX += twoBSquare;
                ellipseError += xChange;
                xChange += twoBSquare;
                if (2 * ellipseError + zChange > 0) {
                    --dz;
                    stoppingZ -= twoASquare;
                    ellipseError += zChange;
                    zChange += twoASquare;
                }
            }
        }

        return result;
    }

    private static boolean fillSquare(int xCenter, int zCenter, int dx, int dz, int xFix, int zFix, int yMin, int yMax, FilledTemplate template) {
        int x1, x2, z1, z2;

        x1 = xCenter + dx + xFix;
        z1 = zCenter + dz + zFix;

        x2 = xCenter - dx;
        z2 = zCenter + dz + zFix;

        template.fillVolume(x2, x1, yMin, yMax, z2, z1);

        x1 = xCenter - dx;
        z1 = zCenter - dz;

        template.fillVolume(x1, x2, yMin, yMax, z1, z2);

        x2 = xCenter + dx + xFix;
        z2 = zCenter - dz;

        template.fillVolume(x1, x2, yMin, yMax, z1, z2);

        x1 = xCenter + dx + xFix;
        z1 = zCenter + dz + zFix;

        template.fillVolume(x2, x1, yMin, yMax, z2, z1);

        return true;
    }

    private static boolean fillFourColumns(int xCenter, int zCenter, int dx, int dz, int xFix, int zFix, int yMin, int yMax, FilledTemplate template) {
        int x, z;

        x = xCenter + dx + xFix;
        z = zCenter + dz + zFix;
        template.fillVolume(x, x, yMin, yMax, z, z);

        x = xCenter - dx;
        z = zCenter + dz + zFix;
        template.fillVolume(x, x, yMin, yMax, z, z);

        x = xCenter - dx;
        z = zCenter - dz;
        template.fillVolume(x, x, yMin, yMax, z, z);

        x = xCenter + dx + xFix;
        z = zCenter - dz;
        template.fillVolume(x, x, yMin, yMax, z, z);

        return true;
    }
}
