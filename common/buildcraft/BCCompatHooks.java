/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Loader;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.TileGenericPipe;

public class BCCompatHooks {
	public static BlockGenericPipe createPipeBlock() {
		BlockGenericPipe genericPipeBlock;

		if (Loader.isModLoaded("BuildCraft|Compat")) {
			try {
				genericPipeBlock = (BlockGenericPipe) BCCompatHooks.class.getClassLoader().loadClass("buildcraft.transport.BlockGenericPipeCompat").newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				genericPipeBlock = new BlockGenericPipe();
			}
		} else {
			genericPipeBlock = new BlockGenericPipe();
		}

		return genericPipeBlock;
	}

	public static Class<? extends TileEntity> getPipeTile() {
		Class<? extends TileEntity> tileClass;

		if (Loader.isModLoaded("BuildCraft|Compat")) {
			try {
				tileClass = (Class<? extends TileEntity>) BCCompatHooks.class.getClassLoader().loadClass("buildcraft.transport.TileGenericPipeCompat");
			} catch (Exception e) {
				e.printStackTrace();
				tileClass = TileGenericPipe.class;
			}
		} else {
			tileClass = TileGenericPipe.class;
		}

		return tileClass;
	}
}
