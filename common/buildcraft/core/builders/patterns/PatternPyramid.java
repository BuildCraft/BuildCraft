/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;

public class PatternPyramid extends FillerPattern {
    private static final int[] MODIFIERS = { 0x0101, 0x1101, 0x1001, 0x0111, 0x1111, 0x1011, 0x0110, 0x1110, 0x1010 };

    public PatternPyramid() {
        super("pyramid", EnumFillerPattern.PYRAMID);
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
        return index == 1 ? new PatternParameterCenter(4) : new PatternParameterYDir(true);
    }

    @Override
    public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
        int xMin = box.min().getX();
        int yMin = box.min().getY();
        int zMin = box.min().getZ();

        int xMax = box.max().getX();
        int yMax = box.max().getY();
        int zMax = box.max().getZ();

        Template bpt = new Template(box.size());

        int[] modifiers = new int[4];
        int height;
        int stepY;

        if (parameters.length >= 1 && parameters[0] != null && !(((PatternParameterYDir) parameters[0]).up)) {
            stepY = -1;
        } else {
            stepY = 1;
        }

        int center = 4;
        if (parameters.length >= 2 && parameters[1] != null) {
            center = ((PatternParameterCenter) parameters[1]).getDirection();
        }

        modifiers[0] = (MODIFIERS[center] >> 12) & 1;
        modifiers[1] = (MODIFIERS[center] >> 8) & 1;
        modifiers[2] = (MODIFIERS[center] >> 4) & 1;
        modifiers[3] = (MODIFIERS[center]) & 1;

        if (stepY == 1) {
            height = yMin;
        } else {
            height = yMax;
        }

        int x1 = xMin;
        int x2 = xMax;
        int z1 = zMin;
        int z2 = zMax;

        while (height >= yMin && height <= yMax) {
            for (int x = x1; x <= x2; ++x) {
                for (int z = z1; z <= z2; ++z) {
                    bpt.set(new BlockPos(x - xMin, height - yMin, z - zMin), new SchematicMask(true));
                }
            }

            x1 += modifiers[0];
            x2 -= modifiers[1];
            z1 += modifiers[2];
            z2 -= modifiers[3];
            height += stepY;

            if (x1 > x2 || z1 > z2) {
                break;
            }
        }

        return bpt;
    }
}
