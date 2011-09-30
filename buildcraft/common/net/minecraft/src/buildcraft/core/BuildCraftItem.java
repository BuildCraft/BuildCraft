/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Item;
import net.minecraft.src.forge.ITextureProvider;

public class BuildCraftItem extends Item implements ITextureProvider {

	public BuildCraftItem(int i) {
		super(i);
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftSprites;
	}

}
