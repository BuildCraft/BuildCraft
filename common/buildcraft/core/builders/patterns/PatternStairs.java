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

public class PatternStairs extends FillerPattern {

    // TODO: These parameters need to be settable from the filler
    private int param2 = 0;
    private int param3 = 0;
    private int param4 = 0;

    public PatternStairs() {
        super("stairs", EnumFillerPattern.STAIRS);
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
        int xMin = 0;
        int yMin = 0;
        int zMin = 0;

        int xMax = box.sizeX() - 1;
        int yMax = box.sizeY() - 1;
        int zMax = box.sizeZ() - 1;

        int sizeX = xMax - xMin + 1;
        int sizeZ = zMax - zMin + 1;

        Template template = new Template(box.sizeX(), box.sizeY(), box.sizeZ());

        int height;
        int heightStep;
        int dimX = 0;
        int dimZ = 0;

        if (parameters[0] != null && !(((PatternParameterYDir) parameters[0]).up)) {
            height = yMin;
            heightStep = 1;
        } else {
            height = yMax;
            heightStep = -1;
        }

        int kind = 0;

        int[] steps = new int[] { 0, 0, 0, 0 };

        int x = 0, z = 0;
        int stepDiagX = 0, stepDiagZ = 0;

        if (param2 == 0) {
            steps[0] = 1;
        } else if (param2 == 1) {
            steps[1] = 1;
        } else if (param2 == 2) {
            steps[2] = 1;
        } else if (param2 == 3) {
            steps[3] = 1;
        } else {
            kind = 1;

            if (param3 == 0) {
                x = xMin;
            } else if (param3 == 1) {
                x = xMax;
            } else if (param3 == 2) {
                // no change
            }

            if (param4 == 0) {
                z = zMin;
            } else if (param4 == 1) {
                z = zMax;
            } else if (param4 == 2) {
                // no change
            }

            if (heightStep == 1) {
                stepDiagX = -1;
                dimX = sizeX - 1;

                stepDiagZ = -1;
                dimZ = sizeZ - 1;
            } else {
                stepDiagX = 1;
                dimX = 0;

                stepDiagZ = 1;
                dimZ = 0;
            }
        }

        int x1 = 0, x2 = 0, z1 = 0, z2 = 0;

        x1 = xMin;
        x2 = xMax;

        z1 = zMin;
        z2 = zMax;

        if (heightStep == -1) {
            if (steps[0] == 1) {
                x1 = xMax - sizeX + 1;
                x2 = x1;
            }

            if (steps[1] == 1) {
                x2 = xMin + sizeX - 1;
                x1 = x2;
            }

            if (steps[2] == 1) {
                z1 = zMax - sizeZ + 1;
                z2 = z1;
            }

            if (steps[3] == 1) {
                z2 = zMin + sizeZ - 1;
                z1 = z2;
            }
        }

        if (kind == 0) {
            while (x2 - x1 + 1 > 0 && z2 - z1 + 1 > 0 && x2 - x1 < sizeX && z2 - z1 < sizeZ && height >= yMin && height <= yMax) {
                fill(x1, height, z1, x2, height, z2, template);

                if (heightStep == 1) {
                    x1 += steps[0];
                    x2 -= steps[1];
                    z1 += steps[2];
                    z2 -= steps[3];
                } else {
                    x2 += steps[0];
                    x1 -= steps[1];
                    z2 += steps[2];
                    z1 -= steps[3];
                }

                height += heightStep;
            }
        } else if (kind == 1) {
            while (dimX >= 0 && dimX < sizeX && dimZ >= 0 && dimZ < sizeZ && height >= yMin && height <= yMax) {

                if (heightStep == 1) {
                    if (param3 == 1) {
                        x1 = x - sizeX + 1;
                        x2 = x1 + dimX;
                    } else {
                        x2 = x + sizeX - 1;
                        x1 = x2 - dimX;
                    }

                    if (param4 == 1) {
                        z1 = z - sizeZ + 1;
                        z2 = z1 + dimZ;
                    } else {
                        z2 = z + sizeZ - 1;
                        z1 = z2 - dimZ;
                    }
                } else if (heightStep == -1) {
                    if (param3 == 0) {
                        x1 = x;
                        x2 = x1 + dimX;
                    } else {
                        x2 = x;
                        x1 = x2 - dimX;
                    }

                    if (param3 == 1) {
                        z1 = z;
                        z2 = z1 + dimZ;
                    } else {
                        z2 = z;
                        z1 = z2 - dimZ;
                    }

                }

                fill(x1, height, z1, x2, height, z2, template);

                dimX += stepDiagX;
                dimZ += stepDiagZ;

                height += heightStep;
            }
        }

        return template;
    }
}
