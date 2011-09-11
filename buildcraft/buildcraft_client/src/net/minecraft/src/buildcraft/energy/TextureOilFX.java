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
