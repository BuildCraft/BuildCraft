/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.ISerializable;

public class BlueprintReadConfiguration implements ISerializable {
	public boolean rotate = true;
	public boolean excavate = true;
	public boolean allowCreative = false;

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setBoolean("rotate", rotate);
		nbttagcompound.setBoolean("excavate", excavate);
		nbttagcompound.setBoolean("allowCreative", allowCreative);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		rotate = nbttagcompound.getBoolean("rotate");
		excavate = nbttagcompound.getBoolean("excavate");
		allowCreative = nbttagcompound.getBoolean("allowCreative");
	}

	@Override
	public void readData(ByteBuf stream) {
		int flags = stream.readUnsignedByte();
		rotate = (flags & 1) != 0;
		excavate = (flags & 2) != 0;
		allowCreative = (flags & 4) != 0;
	}

	@Override
	public void writeData(ByteBuf stream) {
		stream.writeByte(
				(rotate ? 1 : 0) |
						(excavate ? 2 : 0) |
						(allowCreative ? 4 : 0)
		);
	}
}
