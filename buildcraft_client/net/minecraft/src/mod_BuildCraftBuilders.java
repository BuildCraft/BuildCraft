/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import net.minecraft.src.buildcraft.builders.ClientBuilderHook;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.forge.NetworkMod;

public class mod_BuildCraftBuilders extends NetworkMod {

	public static mod_BuildCraftBuilders instance;

	public mod_BuildCraftBuilders () {
		instance = this;
	}

	@Override
	public void load () {
		BuildCraftBuilders.load();
	}

	@Override
	public void modsLoaded () {
		super.modsLoaded();

		BuildCraftBuilders.addHook(new ClientBuilderHook());
		BuildCraftBuilders.initialize();
		//CoreProxy.registerGUI(this, Utils.packetIdToInt(PacketIds.FillerGUI));
		//CoreProxy.registerGUI(this, Utils.packetIdToInt(PacketIds.TemplateGUI));
		//CoreProxy.registerGUI(this, Utils.packetIdToInt(PacketIds.BuilderGUI));
	}

	@Override
	public String getVersion() {
		return DefaultProps.VERSION;
	}

	/*
	@Override
	public GuiScreen handleGUI(int i) {
		switch (Utils.intToPacketId(i)) {
		case FillerGUI:
			return new GuiFiller(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					new TileFiller());
		case TemplateGUI:
			return new GuiTemplate(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					new TileArchitect());
		case BuilderGUI:
			TileBuilder tile = new TileBuilder();
			tile.worldObj = ModLoader.getMinecraftInstance().theWorld;
			return new GuiBuilder(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					tile);
		default:
			return null;
		}
	}
	*/

	@Override public boolean clientSideRequired() { return true; }
	@Override public boolean serverSideRequired() { return true; }

}
