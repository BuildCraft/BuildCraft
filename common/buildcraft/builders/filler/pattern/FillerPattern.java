/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler.pattern;

import java.util.Map;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.Template;
import buildcraft.core.utils.StringUtils;

public abstract class FillerPattern implements IFillerPattern {

	public static final Map<String, FillerPattern> patterns = new TreeMap<String, FillerPattern>();
	private final String tag;
	private IIcon icon;

	public FillerPattern(String tag) {
		this.tag = tag;
		patterns.put(getUniqueTag (), this);
	}

	@Override
	public String getDisplayName() {
		return StringUtils.localize("fillerpattern." + tag);
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:" + tag;
	}

	public void registerIcon(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:fillerPatterns/" + tag);
	}

	@Override
	public IIcon getIcon() {
		return icon;
	}

	@Override
	public String toString() {
		return "Pattern: " + getUniqueTag();
	}

	/**
	 * Generates a filling in a given area
	 */
	public static void fill(int xMin, int yMin, int zMin, int xMax, int yMax,
			int zMax, Template template) {

		for (int y = yMin; y <= yMax; ++y) {
			for (int x = xMin; x <= xMax; ++x) {
				for (int z = zMin; z <= zMax; ++z) {
					if (isValid(x, y, z, template)) {
						template.contents[x][y][z] = new SchematicMask(true);
					}
				}
			}
		}
	}

	/**
	 * Generates an empty in a given area
	 */
	public static void empty(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Template template) {
		int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;

		for (int y = yMax; y >= yMin; y--) {
			for (int x = xMin; x <= xMax; ++x) {
				for (int z = zMin; z <= zMax; ++z) {
					if (isValid(x, y, z, template)) {
						template.contents[x][y][z] = null;
					}
				}
			}
		}
	}

	/**
	 * Generates a flatten in a given area
	 */
	public static void flatten(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Template template) {
		int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;

		for (int x = xMin; x <= xMax; ++x) {
			for (int z = zMin; z <= zMax; ++z) {
				for (int y = yMax; y >= yMin; --y) {
					if (isValid(x, y, z, template)) {
						template.contents [x][y][z] = new SchematicMask(true);
					}
				}
			}
		}
	}

	public abstract Template getTemplate (Box box, World world);

	public Blueprint getBlueprint (Box box, World world, Block block) {
		Blueprint result = new Blueprint (box.sizeX(), box.sizeY(), box.sizeZ());

		Template tmpl = getTemplate(box, world);

		for (int x = 0; x < box.sizeX(); ++x) {
			for (int y = 0; y < box.sizeY(); ++y) {
				for (int z = 0; z < box.sizeZ(); ++z) {
					if (tmpl.contents[x][y][z] != null) {
						result.contents[x][y][z] = SchematicRegistry
								.newSchematicBlock(block);
					}

				}
			}
		}

		return result;
	}

	public BptBuilderTemplate getTemplateBuilder (Box box, World world) {
		return new BptBuilderTemplate(getTemplate(box, world), world, box.xMin, box.yMin, box.zMin);
	}

	private static boolean isValid (int x, int y, int z, BlueprintBase bpt) {
		return x >= 0 && y >= 0 && z >= 0 && x < bpt.sizeX && y < bpt.sizeY && z < bpt.sizeZ;
	}
}
