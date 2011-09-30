/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.buildcraft.core.TextureLiquidsFX;
import net.minecraft.src.forge.ITextureProvider;

public class TextureOilFX extends TextureLiquidsFX {

	public TextureOilFX() {
		super(10, 31, 10, 31, 10, 31,
				BuildCraftEnergy.oilStill.blockIndexInTexture,
				((ITextureProvider) BuildCraftEnergy.oilStill).getTextureFile());
	}
}
