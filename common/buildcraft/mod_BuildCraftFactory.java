/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft;

import java.lang.reflect.Method;
import java.util.Map;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

import buildcraft.BuildCraftFactory;
import buildcraft.core.DefaultProps;
import buildcraft.core.render.RenderVoid;
import buildcraft.factory.EntityMechanicalArm;
import buildcraft.factory.gui.GuiAutoCrafting;
import buildcraft.factory.network.PacketHandlerFactory;


@Mod(name="BuildCraft Factory", version=DefaultProps.VERSION, useMetadata = false, modid = "BC|FACTORY")
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandlerFactory.class, clientSideRequired = true, serverSideRequired = true)
public class mod_BuildCraftFactory {

	public static mod_BuildCraftFactory instance;

	public mod_BuildCraftFactory() {
		instance = this;
	}

	@Init
	public void init(FMLInitializationEvent event) {
		BuildCraftFactory.load();
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {

		BuildCraftFactory.initialize();

		// CoreProxy.registerGUI(this,
		// Utils.packetIdToInt(PacketIds.AutoCraftingGUI));

		// Detect the presence of NEI and add overlay for the Autocrafting Table
		try {
			Class<?> neiRenderer = Class.forName("codechicken.nei.DefaultOverlayRenderer");
			Method method = neiRenderer.getMethod("registerGuiOverlay", Class.class, String.class, int.class, int.class);
			method.invoke(null, GuiAutoCrafting.class, "crafting", 5, 11);
			BuildCraftCore.bcLog.fine("NEI detected, adding NEI overlay");
		} catch (Exception e) {
			BuildCraftCore.bcLog.fine("NEI not detected.");
		}
		// Direct call (for reference)
		// DefaultOverlayRenderer.registerGuiOverlay(GuiAutoCrafting.class,
		// "crafting", 5, 11);

	}

	//@Override
	public void addRenderer(Map map) {
		map.put(EntityMechanicalArm.class, new RenderVoid());
	}

	/*
	 * @Override public GuiScreen handleGUI(int i) { if (Utils.intToPacketId(i)
	 * == PacketIds.AutoCraftingGUI) { return new GuiAutoCrafting(
	 * ModLoader.getMinecraftInstance().thePlayer.inventory,
	 * ModLoader.getMinecraftInstance().theWorld, new TileAutoWorkbench()); }
	 * else { return null; } }
	 */

}
