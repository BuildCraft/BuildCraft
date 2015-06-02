/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import buildcraft.core.utils.Utils;

public class CommandTargetTile extends CommandTarget {
	@Override
	public Class<?> getHandledClass() {
		return TileEntity.class;
	}

	@Override
	public void write(ByteBuf data, Object target) {
		TileEntity tile = (TileEntity) target;
		Utils.writeBlockPos(data, tile.getPos());
	}

	@Override
	public ICommandReceiver handle(EntityPlayer player, ByteBuf data, World world) {
		BlockPos pos = Utils.readBlockPos(data);
		if (world.isBlockLoaded(pos)) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof ICommandReceiver) {
				return (ICommandReceiver) tile;
			}
		}
		return null;
	}
}
