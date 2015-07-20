/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network.command;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import io.netty.buffer.ByteBuf;

public class CommandTargetTile extends CommandTarget {
    @Override
    public Class<?> getHandledClass() {
        return TileEntity.class;
    }

    @Override
    public void write(ByteBuf data, Object target) {
        TileEntity tile = (TileEntity) target;
        data.writeInt(tile.getPos().getX());
        data.writeInt(tile.getPos().getY());
        data.writeInt(tile.getPos().getZ());
    }

    @Override
    public ICommandReceiver handle(EntityPlayer player, ByteBuf data, World world) {
        int posX = data.readInt();
        int posY = data.readInt();
        int posZ = data.readInt();
        BlockPos pos = new BlockPos(posX, posY, posZ);
        if (!world.isAirBlock(pos)) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof ICommandReceiver) {
                return (ICommandReceiver) tile;
            }
        }
        return null;
    }
}
