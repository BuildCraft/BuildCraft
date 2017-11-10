/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders.patterns;

import net.minecraft.world.World;

import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;


public class PatternFrame extends FillerPattern {

	public PatternFrame() {
		super("frame");
	}

	@Override
	public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
		Template template = new Template(box.sizeX(), box.sizeY(), box.sizeZ());

		int xMax = box.sizeX() - 1;
		int zMax = box.sizeZ() - 1;

		for (int it = 0; it < 2; it++) {
			int y = it * (box.sizeY() - 1);
			for (int i = 0; i < template.sizeX; ++i) {
				template.put(i, y, 0, new SchematicMask(true));
				template.put(i, y, zMax, new SchematicMask(true));
			}

			for (int k = 0; k < template.sizeZ; ++k) {
				template.put(0, y, k, new SchematicMask(true));
				template.put(xMax, y, k, new SchematicMask(true));
			}
		}

		for (int h = 1; h < box.sizeY(); ++h) {
			template.put(0, h, 0, new SchematicMask(true));
			template.put(0, h, zMax, new SchematicMask(true));
			template.put(xMax, h, 0, new SchematicMask(true));
			template.put(xMax, h, zMax, new SchematicMask(true));
		}

		return template;
	}
}
