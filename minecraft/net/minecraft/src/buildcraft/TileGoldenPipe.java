package net.minecraft.src.buildcraft;

import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class TileGoldenPipe extends TileStonePipe {

	public void entityEntering (EntityPassiveItem item, Orientations orientation) {
		super.entityEntering(item, orientation);
		
		if (world.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
			item.speed = Utils.pipeNormalSpeed * 20F;
		}
	}

}
