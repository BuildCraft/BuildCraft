/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy.render;

import buildcraft.BuildCraftEnergy;
import buildcraft.core.render.TextureLiquidsFX;

public class TextureFuelFX extends TextureLiquidsFX {

	public TextureFuelFX() {
		super(150, 250, 150, 250, 0, 10, BuildCraftEnergy.fuel.getIconFromDamage(0), BuildCraftEnergy.fuel.getTextureFile());
	}
}
