package buildcraft.lib.net.command;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.BCLibProxy;

final class CommandTargetTile implements ICommandTarget {
    private final TileEntity tile;

    public CommandTargetTile(TileEntity tile) {
        this.tile = tile;
    }

    @Override
    public ICommandReceiver getReceiver(PacketBuffer buffer, MessageContext context) {
        BlockPos pos = buffer.readBlockPos();
        EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(context);
        if (player == null || player.worldObj == null) return null;
        TileEntity tile = player.worldObj.getTileEntity(pos);
        if (tile instanceof ICommandReceiver) {
            return (ICommandReceiver) tile;
        }
        return null;
    }

    @Override
    public void writePositionData(PacketBuffer buffer) {
        buffer.writeBlockPos(tile.getPos());
    }

    @Override
    public CommandTargetType getType() {
        return CommandTargetType.TILE;
    }
}
