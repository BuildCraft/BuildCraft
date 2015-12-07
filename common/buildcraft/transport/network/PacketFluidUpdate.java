/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.network;

import java.util.BitSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.core.lib.network.PacketCoordinates;
import buildcraft.core.lib.utils.BitSetUtils;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.utils.FluidRenderData;

import io.netty.buffer.ByteBuf;

public class PacketFluidUpdate extends PacketCoordinates {
    public FluidRenderData renderCache = new FluidRenderData();
    public BitSet delta;

    private short fluidID = 0;
    private int color = 0;
    private int[] amount = new int[7];
    public byte[] flow = new byte[6];

    public PacketFluidUpdate(TileGenericPipe tileG) {
        super(tileG);
    }

    public PacketFluidUpdate(TileGenericPipe tileGP, boolean chunkPacket) {
        super(tileGP);
        this.isChunkDataPacket = chunkPacket;
    }

    public PacketFluidUpdate() {}

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);

        byte[] dBytes = new byte[1];
        data.readBytes(dBytes);
        delta = BitSetUtils.fromByteArray(dBytes);

        if (delta.get(0)) {
            fluidID = data.readShort();
            if (fluidID != 0) {
                color = data.readInt();
            }
        }

        for (int dir = 0; dir < 7; dir++) {
            if (delta.get(dir + 1)) {
                amount[dir] = data.readShort();
            }
            if (dir < 6) {
                flow[dir] = data.readByte();
            }
        }
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);

        byte[] dBytes = BitSetUtils.toByteArray(delta, 1);
        data.writeBytes(dBytes);

        if (delta.get(0)) {
            data.writeShort(renderCache.fluidID);
            if (renderCache.fluidID != 0) {
                data.writeInt(renderCache.color);
                data.writeByte(renderCache.flags);
            }
        }

        for (int dir = 0; dir < 7; dir++) {
            if (delta.get(dir + 1)) {
                data.writeShort(renderCache.amount[dir]);
            }
            if (dir < 6) {
                data.writeByte(flow[dir]);
            }
        }
    }

    @Override
    public void applyData(World world, EntityPlayer player) {
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

        PipeTransportFluids trans = (PipeTransportFluids) pipe.pipe.transport;

        // boolean fluidBefore = false;
        // boolean fluidAfter = false;

        renderCache = trans.renderCache;

        renderCache.flow = flow;

        // System.out.printf("read %d, %d, %d = %s, %s%n", posX, posY, posZ, Arrays.toString(dBytes), delta);

        if (delta.get(0)) {
            renderCache.fluidID = fluidID;
            Fluid fluid = FluidRegistry.getFluid(fluidID);
            if (fluid == null) {
                trans.fluidType = null;
            } else {
                trans.fluidType = new FluidStack(fluid, 1);
            }
            renderCache.color = color;
        }

        for (int dir = 0; dir < 7; dir++) {
            if (delta.get(dir + 1)) {
                // boolean before = renderCache.amount[dir] > 0;
                renderCache.amount[dir] = amount[dir];
                // boolean after = renderCache.amount[dir] > 0;
                // fluidBefore |= before;
                // fluidAfter |= after;
            }
            if (dir < 6) {
                trans.clientDisplayFlowConnection[dir] = flow[dir];
            }
        }

        // TODO Fluid shader rendering (unused for now)
        // if (fluidBefore && !fluidAfter) {
        // FluidShaderManager.INSTANCE.getRenderer(Minecraft.getMinecraft().theWorld).removeFluidTransport(trans);
        // }
        // if (!fluidBefore && fluidAfter) {
        // FluidShaderManager.INSTANCE.getRenderer(Minecraft.getMinecraft().theWorld).addFluidTransport(trans);
        // }
    }
}
