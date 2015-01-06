/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.compat;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.Loader;

import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.IInjectable;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.TileGenericPipe;

public final class CompatHooks {
	public static final CompatHooks INSTANCE;

	static {
		CompatHooks i = null;
		if (Loader.isModLoaded("BuildCraft|Compat")) {
			try {
				i = (CompatHooks) CompatHooks.class.getClassLoader().loadClass("buildcraft.compat.CompatHooksImpl").newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (i == null) {
			i = new CompatHooks();
		}

		INSTANCE = i;
	}

	public CompatHooks() {

	}

	public IInjectable getInjectableWrapper(TileEntity tile, ForgeDirection side) {
		return null;
	}

	public BlockGenericPipe createPipeBlock() {
		BlockGenericPipe genericPipeBlock;

		if (Loader.isModLoaded("BuildCraft|Compat")) {
			try {
				genericPipeBlock = (BlockGenericPipe) CompatHooks.class.getClassLoader().loadClass("buildcraft.transport.BlockGenericPipeCompat").newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				genericPipeBlock = new BlockGenericPipe();
			}
		} else {
			genericPipeBlock = new BlockGenericPipe();
		}

		return genericPipeBlock;
	}

	public Class<? extends TileEntity> getPipeTile() {
		Class<? extends TileEntity> tileClass;

		if (Loader.isModLoaded("BuildCraft|Compat")) {
			try {
				tileClass = (Class<? extends TileEntity>) CompatHooks.class.getClassLoader().loadClass("buildcraft.transport.TileGenericPipeCompat");
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
