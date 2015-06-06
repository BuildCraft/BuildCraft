/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.network;

import io.netty.buffer.ByteBuf;

import java.util.BitSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.core.lib.network.PacketCoordinates;
import buildcraft.core.lib.utils.BitSetUtils;
import buildcraft.core.network.PacketIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.utils.FluidRenderData;

public class PacketFluidUpdate extends PacketCoordinates {
    public FluidRenderData renderCache = new FluidRenderData();
    public BitSet delta;

    public PacketFluidUpdate(int xCoord, int yCoord, int zCoord) {
        super(PacketIds.PIPE_LIQUID, xCoord, yCoord, zCoord);
    }

    public PacketFluidUpdate(int xCoord, int yCoord, int zCoord, boolean chunkPacket) {
        super(PacketIds.PIPE_LIQUID, xCoord, yCoord, zCoord);
        this.isChunkDataPacket = chunkPacket;
    }

    public PacketFluidUpdate() {}

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);

        World world = CoreProxy.proxy.getClientWorld();
        if (!world.blockExists(posX, posY, posZ)) {
            return;
        }

        TileEntity entity = world.getTileEntity(posX, posY, posZ);
        if (!(entity instanceof TileGenericPipe)) {
            return;
        }

        TileGenericPipe pipe = (TileGenericPipe) entity;
        if (pipe.pipe == null) {
            return;
        }

        if (!(pipe.pipe.transport instanceof PipeTransportFluids)) {
            return;
        }

        PipeTransportFluids transLiq = (PipeTransportFluids) pipe.pipe.transport;

        renderCache = transLiq.renderCache;

        byte[] dBytes = new byte[1];
        data.readBytes(dBytes);
        delta = BitSetUtils.fromByteArray(dBytes);

        // System.out.printf("read %d, %d, %d = %s, %s%n", posX, posY, posZ, Arrays.toString(dBytes), delta);

        if (delta.get(0)) {
            renderCache.fluidID = data.readShort();
            renderCache.color = renderCache.fluidID != 0 ? data.readInt() : 0xFFFFFF;
        }

        for (EnumFacing dir : EnumFacing.values()) {
            if (delta.get(dir.ordinal() + 1)) {
                renderCache.amount[dir.ordinal()] = Math.min(transLiq.getCapacity(), data.readUnsignedByte());
            }
        }
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);

        byte[] dBytes = BitSetUtils.toByteArray(delta, 1);
        // System.out.printf("write %d, %d, %d = %s, %s%n", posX, posY, posZ, Arrays.toString(dBytes), delta);
        data.writeBytes(dBytes);

        if (delta.get(0)) {
            data.writeShort(renderCache.fluidID);
            if (renderCache.fluidID != 0) {
                data.writeInt(renderCache.color);
            }
        }

        for (EnumFacing dir : EnumFacing.values()) {
            if (delta.get(dir.ordinal() + 1)) {
                data.writeByte(renderCache.amount[dir.ordinal()]);
            }
        }
    }

    @Override
    public int getID() {
        return PacketIds.PIPE_LIQUID;
    }
}
