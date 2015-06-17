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

public class PatternFrame extends FillerPattern {

    public PatternFrame() {
        super("frame", EnumFillerPattern.FRAME);
    }

    @Override
    public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
        Template template = new Template(box.sizeX(), box.sizeY(), box.sizeZ());

        int xMin = 0;
        int yMin = 0;
        int zMin = 0;

        int xMax = box.sizeX() - 1;
        int yMax = box.sizeY() - 1;
        int zMax = box.sizeZ() - 1;

        for (int it = 0; it < 2; it++) {
            for (int i = 0; i < template.sizeX; ++i) {
                template.contents[i][it * (box.sizeY() - 1)][0] = new SchematicMask(true);
                template.contents[i][it * (box.sizeY() - 1)][template.sizeZ - 1] = new SchematicMask(true);
            }

            for (int k = 0; k < template.sizeZ; ++k) {
                template.contents[0][it * (box.sizeY() - 1)][k] = new SchematicMask(true);
                template.contents[template.sizeX - 1][it * (box.sizeY() - 1)][k] = new SchematicMask(true);
            }
        }

        for (int h = 1; h < box.sizeY(); ++h) {
            template.contents[0][h][0] = new SchematicMask(true);
            template.contents[0][h][template.sizeZ - 1] = new SchematicMask(true);
            template.contents[template.sizeX - 1][h][0] = new SchematicMask(true);
            template.contents[template.sizeX - 1][h][template.sizeZ - 1] = new SchematicMask(true);
        }

        return template;
    }
}
