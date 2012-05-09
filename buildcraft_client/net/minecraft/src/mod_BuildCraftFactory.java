/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import java.lang.reflect.Method;
import java.util.Map;

import net.minecraft.src.mod_BuildCraftCore.EntityRenderIndex;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;
import net.minecraft.src.buildcraft.factory.GuiAutoCrafting;
import net.minecraft.src.buildcraft.factory.RenderHopper;
import net.minecraft.src.buildcraft.factory.RenderRefinery;
import net.minecraft.src.buildcraft.factory.RenderTank;
import net.minecraft.src.buildcraft.factory.TileHopper;
import net.minecraft.src.buildcraft.factory.TileRefinery;
import net.minecraft.src.buildcraft.factory.TileTank;
import net.minecraft.src.forge.NetworkMod;

public class mod_BuildCraftFactory extends NetworkMod {

	public static mod_BuildCraftFactory instance;

	public mod_BuildCraftFactory() {
		instance = this;
	}

	@Override
	public void modsLoaded () {
		super.modsLoaded();

		BuildCraftFactory.initialize();

		//CoreProxy.registerGUI(this, Utils.packetIdToInt(PacketIds.AutoCraftingGUI));

		ModLoader
		.registerTileEntity(TileTank.class,
				"net.minecraft.src.buildcraft.factory.TileTank",
				new RenderTank());

		ModLoader.registerTileEntity(TileRefinery.class,
				"net.minecraft.src.buildcraft.factory.Refinery",
				new RenderRefinery());

		mod_BuildCraftCore.blockByEntityRenders.put(new EntityRenderIndex(
				BuildCraftFactory.refineryBlock, 0), new RenderRefinery());
		
		ModLoader.registerTileEntity(TileHopper.class, "net.minecraft.src.buildcraft.factory.TileHopper", new RenderHopper());
		
		mod_BuildCraftCore.blockByEntityRenders.put(new EntityRenderIndex(
				BuildCraftFactory.hopperBlock, 0), new RenderHopper());

		//Detect the presence of NEI and add overlay for the Autocrafting Table
		try {
			Class<?> neiRenderer = Class.forName("codechicken.nei.DefaultOverlayRenderer");
			Method method = neiRenderer.getMethod("registerGuiOverlay", Class.class, String.class, int.class, int.class);
			method.invoke(null, GuiAutoCrafting.class, "crafting", 5, 11);
			ModLoader.getLogger().fine("NEI detected, adding NEI overlay");
		} catch (Exception e) {
			ModLoader.getLogger().fine("NEI not detected.");
		}
		//Direct call (for reference)
		//DefaultOverlayRenderer.registerGuiOverlay(GuiAutoCrafting.class, "crafting", 5, 11);

	}

	@Override
	public String getVersion() {
		return DefaultProps.VERSION;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addRenderer(Map map) {
    	map.put (EntityMechanicalArm.class, new RenderVoid());
    }

	/*
	@Override
    public GuiScreen handleGUI(int i) {
    	if (Utils.intToPacketId(i) == PacketIds.AutoCraftingGUI) {
			return new GuiAutoCrafting(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					ModLoader.getMinecraftInstance().theWorld,
					new TileAutoWorkbench());
    	} else {
    		return null;
    	}
    }
    */

	@Override
	public void load() {
		BuildCraftFactory.load();
	}

	@Override public boolean clientSideRequired() { return true; }
	@Override public boolean serverSideRequired() { return true; }
}
