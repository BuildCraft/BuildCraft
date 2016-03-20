/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.BuildCraftCore;
import buildcraft.core.lib.network.base.Packet;

// TODO: Rename to PacketGuiUpdate
public class PacketGuiReturn extends Packet {
    private IGuiReturnHandler obj;
    private byte[] extraData;

    private boolean tileReturn;
    private BlockPos pos;
    private int entityId;
    private ByteBuf heldData;

    public PacketGuiReturn() {}

    public PacketGuiReturn(IGuiReturnHandler obj) {
        this.obj = obj;
        this.extraData = null;
        this.tempWorld = obj.getWorldBC();
        this.dimensionId = tempWorld.provider.getDimensionId();
    }

    public PacketGuiReturn(IGuiReturnHandler obj, byte[] extraData) {
        this.obj = obj;
        this.extraData = extraData;
        this.tempWorld = obj.getWorldBC();
        this.dimensionId = tempWorld.provider.getDimensionId();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        if (obj instanceof TileEntity) {
            TileEntity tile = (TileEntity) obj;
            data.writeBoolean(true);
            data.writeInt(tile.getPos().getX());
            data.writeInt(tile.getPos().getY());
            data.writeInt(tile.getPos().getZ());
        } else if (obj instanceof Entity) {
            Entity entity = (Entity) obj;
            data.writeBoolean(false);
            data.writeInt(entity.getEntityId());
        } else {
            return;
        }

        ByteBuf guiData = Unpooled.buffer();

        obj.writeGuiData(guiData);

        if (extraData != null) {
            guiData.writeBytes(extraData);
        }

        int length = guiData.readableBytes();
        data.writeInt(length);
        data.writeBytes(guiData);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        tileReturn = data.readBoolean();

        if (tileReturn) {
            pos = new BlockPos(data.readInt(), data.readInt(), data.readInt());

            int length = data.readInt();
            heldData = data.readBytes(length);
        } else {
            entityId = data.readInt();
            int length = data.readInt();
            heldData = data.readBytes(length);
        }
    }

    public void sendPacket() {
        BuildCraftCore.instance.sendToServer(this);
    }

    @Override
    public void applyData(World world, EntityPlayer player) {
        if (tileReturn) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IGuiReturnHandler) {
                IGuiReturnHandler handler = (IGuiReturnHandler) tile;
                handler.readGuiData(heldData, null);
            }
        } else {
            Entity ent = world.getEntityByID(entityId);
            if (ent instanceof IGuiReturnHandler) {
                IGuiReturnHandler handler = (IGuiReturnHandler) ent;
                handler.readGuiData(heldData, null);
            }
        }
    }
}
