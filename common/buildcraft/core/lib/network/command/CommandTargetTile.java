/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network.command;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CommandTargetTile extends CommandTarget {
	@Override
	public Class<?> getHandledClass() {
		return TileEntity.class;
	}

	@Override
	public void write(ByteBuf data, Object target) {
		TileEntity tile = (TileEntity) target;
		data.writeInt(tile.xCoord);
		data.writeShort(tile.yCoord);
		data.writeInt(tile.zCoord);
	}

	@Override
	public ICommandReceiver handle(EntityPlayer player, ByteBuf data, World world) {
		int posX = data.readInt();
		int posY = data.readShort();
		int posZ = data.readInt();
		if (world.blockExists(posX, posY, posZ)) {
			TileEntity tile = world.getTileEntity(posX, posY, posZ);
			if (tile instanceof ICommandReceiver) {
				return (ICommandReceiver) tile;
			}
		}
		return null;
	}
}
