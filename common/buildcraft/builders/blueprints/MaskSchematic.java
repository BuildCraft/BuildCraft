/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import buildcraft.api.builder.BlockHandler;
import net.minecraft.nbt.NBTTagCompound;

public final class MaskSchematic extends Schematic {

	public static MaskSchematic create(NBTTagCompound nbt) {
		return new MaskSchematic();
	}

	public static MaskSchematic create() {
		return new MaskSchematic();
	}

	public MaskSchematic() {
		super(0);
	}

	@Override
	public BlockHandler getHandler() {
		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("schematicType", "mask");
	}
}
