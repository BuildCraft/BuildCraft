package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.buildcraft.core.TextureLiquidsFX;
import net.minecraft.src.forge.ITextureProvider;

public class TextureFuelFX extends TextureLiquidsFX {

	public TextureFuelFX() {
		super(150, 250, 150, 250, 0, 10, BuildCraftEnergy.fuel
				.getIconFromDamage(0),
				((ITextureProvider) BuildCraftEnergy.fuel).getTextureFile());
	}
}
