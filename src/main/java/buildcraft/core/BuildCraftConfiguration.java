/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class BuildCraftConfiguration extends Configuration {

	public BuildCraftConfiguration(File file) {
		super(file);
	}

	@Override
	public void save() {
		Property versionProp = get(CATEGORY_GENERAL, "version", Version.VERSION);
		versionProp.set(Version.VERSION);
		super.save();
	}

}
