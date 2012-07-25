/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import net.minecraft.src.Chunk;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.forge.ISaveEventHandler;

public class BuildersSaveManager implements ISaveEventHandler {

	@Override
	public void onWorldLoad(World world) {
		//When a world loads clean the list of available markers
		TilePathMarker.clearAvailableMarkersList();
	}

	@Override
	public void onWorldSave(World world) {}

	@Override
	public void onChunkLoad(World world, Chunk chunk) {}

	@Override
	public void onChunkUnload(World world, Chunk chunk) {}

	@Override
	public void onChunkSaveData(World world, Chunk chunk, NBTTagCompound data) {}

	@Override
	public void onChunkLoadData(World world, Chunk chunk, NBTTagCompound data) {}
}
