/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.world.World;

import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;

public class PatternCylinder extends FillerPattern {

    public PatternCylinder() {
        super("cylinder", EnumFillerPattern.CYLINDER);
    }

    @Override
    public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
        Template result = new Template(box.sizeX(), box.sizeY(), box.sizeZ());

        int xMin = 0;
        int yMin = 0;
        int zMin = 0;

        int xMax = box.sizeX() - 1;
        int yMax = box.sizeY() - 1;
        int zMax = box.sizeZ() - 1;

        int xFix = (xMax - xMin) % 2;
        int zFix = (zMax - zMin) % 2;

        int xCenter = (xMax + xMin) / 2 + (xMax + xMin < 0 && xFix == 1 ? -1 : 0);
        int zCenter = (zMax + zMin) / 2 + (zMax + zMin < 0 && zFix == 1 ? -1 : 0);

        int xRadius = (xMax - xMin) / 2;
        int zRadius = (zMax - zMin) / 2;

        if (xRadius == 0 || zRadius == 0) {
            fill(xMin, yMin, zMin, xMax, yMax, zMax, result);
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
                fillFourColumns(xCenter, zCenter, dx, dz, xFix, zFix, yMin, yMax, result);

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
                fillFourColumns(xCenter, zCenter, dx, dz, xFix, zFix, yMin, yMax, result);

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

    private boolean fillFourColumns(int xCenter, int zCenter, int dx, int dz, int xFix, int zFix, int yMin, int yMax, Template template) {
        int x, z;

        x = xCenter + dx + xFix;
        z = zCenter + dz + zFix;
        fill(x, yMin, z, x, yMax, z, template);

        x = xCenter - dx;
        z = zCenter + dz + zFix;
        fill(x, yMin, z, x, yMax, z, template);

        x = xCenter - dx;
        z = zCenter - dz;
        fill(x, yMin, z, x, yMax, z, template);

        x = xCenter + dx + xFix;
        z = zCenter - dz;
        fill(x, yMin, z, x, yMax, z, template);

        return true;
    }

}
