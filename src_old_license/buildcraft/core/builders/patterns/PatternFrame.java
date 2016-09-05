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
import buildcraft.core.blueprints.Template;
import buildcraft.lib.misc.data.Box;

public class PatternFrame extends FillerPattern {

    public PatternFrame() {
        super("frame", EnumFillerPattern.FRAME);
    }

    @Override
    public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
        Template template = new Template(box.size());

        int xMax = box.size().getX() - 1;
        int zMax = box.size().getZ() - 1;

        for (int it = 0; it < 2; it++) {
            int y = it * (box.size().getY() - 1);
            for (int i = 0; i < template.size.getX(); ++i) {
                template.set(new BlockPos(i, y, 0), new SchematicMask(true));
                template.set(new BlockPos(i, y, zMax), new SchematicMask(true));
            }

            for (int k = 0; k < template.size.getZ(); ++k) {
                template.set(new BlockPos(0, y, k), new SchematicMask(true));
                template.set(new BlockPos(xMax, y, k), new SchematicMask(true));
            }
        }

        for (int h = 1; h < box.size().getY(); ++h) {
            template.set(new BlockPos(0, h, 0), new SchematicMask(true));
            template.set(new BlockPos(0, h, zMax), new SchematicMask(true));
            template.set(new BlockPos(xMax, h, 0), new SchematicMask(true));
            template.set(new BlockPos(xMax, h, zMax), new SchematicMask(true));
        }

        return template;
    }
}
