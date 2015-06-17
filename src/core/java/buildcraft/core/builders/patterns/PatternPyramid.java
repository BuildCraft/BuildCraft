/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.world.World;

import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;

public class PatternPyramid extends FillerPattern {
    public PatternPyramid() {
        super("pyramid", EnumFillerPattern.PYRAMID);
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
        return new PatternParameterYDir(true);
    }

    @Override
    public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
        int xMin = (int) box.pMin().xCoord;
        int yMin = (int) box.pMin().yCoord;
        int zMin = (int) box.pMin().zCoord;

        int xMax = (int) box.pMax().xCoord;
        int yMax = (int) box.pMax().yCoord;
        int zMax = (int) box.pMax().zCoord;

        Template bpt = new Template(xMax - xMin + 1, yMax - yMin + 1, zMax - zMin + 1);

        int xSize = xMax - xMin + 1;
        int zSize = zMax - zMin + 1;

        int step = 0;
        int height;
        int stepY;

        if (parameters[0] != null && !(((PatternParameterYDir) parameters[0]).up)) {
            stepY = -1;
        } else {
            stepY = 1;
        }

        if (stepY == 1) {
            height = yMin;
        } else {
            height = yMax;
        }

        while (step <= xSize / 2 && step <= zSize / 2 && height >= yMin && height <= yMax) {
            for (int x = xMin + step; x <= xMax - step; ++x) {
                for (int z = zMin + step; z <= zMax - step; ++z) {
                    bpt.contents[x - xMin][height - yMin][z - zMin] = new SchematicMask(true);
                }
            }

            step++;
            height += stepY;
        }

        return bpt;
    }
}
