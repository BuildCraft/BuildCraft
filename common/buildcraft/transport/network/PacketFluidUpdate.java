/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.network;

import java.util.BitSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.core.lib.network.PacketCoordinates;
import buildcraft.core.lib.utils.BitSetUtils;
import buildcraft.core.network.PacketIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.utils.FluidRenderData;

import io.netty.buffer.ByteBuf;

public class PacketFluidUpdate extends PacketCoordinates {
    public FluidRenderData renderCache = new FluidRenderData();
    public BitSet delta;

    public PacketFluidUpdate(BlockPos pos) {
        super(PacketIds.PIPE_LIQUID, pos);
    }

    public PacketFluidUpdate(BlockPos pos, boolean chunkPacket) {
        super(PacketIds.PIPE_LIQUID, pos);
        this.isChunkDataPacket = chunkPacket;
    }

    public PacketFluidUpdate() {}

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);

        World world = CoreProxy.proxy.getClientWorld();
        if (world.isAirBlock(pos)) {
            return;
        }

        TileEntity entity = world.getTileEntity(pos);
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

        for (int dir = 0; dir < 7; dir++) {
            if (delta.get(dir + 1)) {
                renderCache.amount[dir] = Math.min(transLiq.getCapacity(), data.readUnsignedByte());
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

        for (int dir = 0; dir < 7; dir++) {
            if (delta.get(dir + 1)) {
                data.writeByte(renderCache.amount[dir]);
            }
        }
    }

    @Override
    public int getID() {
        return PacketIds.PIPE_LIQUID;
    }

    @Override
    public void applyData(World world) {
        // TODO Auto-generated method stub
        
    }
}
