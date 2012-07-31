/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft;

import buildcraft.core.DefaultProps;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.forge.NetworkMod;

public class mod_BuildCraftTransport extends NetworkMod {

	public static mod_BuildCraftTransport instance;

	public mod_BuildCraftTransport() {
		instance = this;
	}

	@Override
	public void modsLoaded() {
		super.modsLoaded();
		BuildCraftTransport.initialize();
		BuildCraftTransport.initializeModel(this);
	}

	public static void registerTilePipe(Class<? extends TileEntity> clas, String name) {
		ModLoader.registerTileEntity(clas, name);
	}

	@Override
	public String getVersion() {
		return DefaultProps.VERSION;
	}

	@Override
	public void load() {
		BuildCraftTransport.load();
	}

	@Override
	public boolean clientSideRequired() {
		return true;
	}

	@Override
	public boolean serverSideRequired() {
		return true;
	}

}
