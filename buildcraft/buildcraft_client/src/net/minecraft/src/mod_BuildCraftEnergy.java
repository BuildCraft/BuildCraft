package net.minecraft.src;

import net.minecraft.src.mod_BuildCraftCore.EntityRenderIndex;
import net.minecraft.src.buildcraft.energy.RenderEngine;
import net.minecraft.src.buildcraft.energy.TextureOilFX;
import net.minecraft.src.buildcraft.energy.TextureOilFlowFX;

public class mod_BuildCraftEnergy extends BaseModMp {

	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftEnergy.ModsLoaded();	
		
		mod_BuildCraftCore.blockByEntityRenders.put(new EntityRenderIndex(
				BuildCraftEnergy.engineBlock, 0), new RenderEngine(
				"/net/minecraft/src/buildcraft/energy/gui/base_wood.png"));		
		mod_BuildCraftCore.blockByEntityRenders.put(new EntityRenderIndex(
				BuildCraftEnergy.engineBlock, 1), new RenderEngine(
				"/net/minecraft/src/buildcraft/energy/gui/base_stone.png"));
		mod_BuildCraftCore.blockByEntityRenders.put(new EntityRenderIndex(
				BuildCraftEnergy.engineBlock, 2), new RenderEngine(
				"/net/minecraft/src/buildcraft/energy/gui/base_iron.png"));
		
		ModLoader.getMinecraftInstance().renderEngine.registerTextureFX(new TextureOilFX());
		ModLoader.getMinecraftInstance().renderEngine.registerTextureFX(new TextureOilFlowFX());

	}
	
	@Override
	public String Version() {
		return "1.6.6.4";
	}
}
