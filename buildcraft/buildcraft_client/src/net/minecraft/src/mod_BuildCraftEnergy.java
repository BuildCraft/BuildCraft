/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */


package net.minecraft.src;

import java.util.Random;

import net.minecraft.src.mod_BuildCraftCore.EntityRenderIndex;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.energy.EngineIron;
import net.minecraft.src.buildcraft.energy.EngineStone;
import net.minecraft.src.buildcraft.energy.GuiCombustionEngine;
import net.minecraft.src.buildcraft.energy.GuiSteamEngine;
import net.minecraft.src.buildcraft.energy.RenderEngine;
import net.minecraft.src.buildcraft.energy.TextureFuelFX;
import net.minecraft.src.buildcraft.energy.TextureOilFX;
import net.minecraft.src.buildcraft.energy.TextureOilFlowFX;
import net.minecraft.src.buildcraft.energy.TileEngine;

public class mod_BuildCraftEnergy extends BaseModMp {

	public static mod_BuildCraftEnergy instance;
	
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
		ModLoader.getMinecraftInstance().renderEngine.registerTextureFX(new TextureFuelFX());
		ModLoader.getMinecraftInstance().renderEngine.registerTextureFX(new TextureOilFlowFX());
		
		ModLoader.RegisterTileEntity(TileEngine.class,
				"net.minecraft.src.buildcraft.energy.Engine", new RenderEngine());
		
		ModLoaderMp.RegisterGUI(this, Utils.packetIdToInt(PacketIds.EngineSteamGUI));
		ModLoaderMp.RegisterGUI(this, Utils.packetIdToInt(PacketIds.EngineCombustionGUI));	
		
		instance = this;
	}
	
	@Override
	public String Version() {
		return "2.2.2";
	}

	public GuiScreen HandleGUI(int i) {
		TileEngine tile = new TileEngine();

		switch (Utils.intToPacketId(i)) {
		case EngineSteamGUI:
			tile.engine = new EngineStone(tile);
			return new GuiSteamEngine(
					ModLoader.getMinecraftInstance().thePlayer.inventory, tile);
		case EngineCombustionGUI:
			tile.engine = new EngineIron(tile);
			return new GuiCombustionEngine(
					ModLoader.getMinecraftInstance().thePlayer.inventory, tile);
		default:
			return null;
		}
	}
	
    public void GenerateSurface(World world, Random random, int i, int j) {
    	BuildCraftEnergy.generateSurface (world, random, i, j);
    }

}
