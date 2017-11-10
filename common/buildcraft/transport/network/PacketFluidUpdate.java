/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.network;

import java.util.BitSet;

import io.netty.buffer.ByteBuf;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.network.PacketCoordinates;
import buildcraft.core.lib.utils.BitSetUtils;
import buildcraft.core.network.PacketIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.utils.FluidRenderData;

public class PacketFluidUpdate extends PacketCoordinates {
	public FluidRenderData renderCache = new FluidRenderData();
	public BitSet delta;
	private boolean largeFluidCapacity;

	public PacketFluidUpdate(int xCoord, int yCoord, int zCoord) {
		super(PacketIds.PIPE_LIQUID, xCoord, yCoord, zCoord);
	}

	public PacketFluidUpdate(int xCoord, int yCoord, int zCoord, boolean chunkPacket, boolean largeFluidCapacity) {
		super(PacketIds.PIPE_LIQUID, xCoord, yCoord, zCoord);
		this.isChunkDataPacket = chunkPacket;
		this.largeFluidCapacity = largeFluidCapacity;
	}

	public PacketFluidUpdate() {
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		World world = CoreProxy.proxy.getClientWorld();
		if (!world.blockExists(posX, posY, posZ)) {
			return;
		}

		TileEntity entity = world.getTileEntity(posX, posY, posZ);
		if (!(entity instanceof IPipeTile)) {
			return;
		}

		IPipeTile pipeTile = (IPipeTile) entity;
		if (!(pipeTile.getPipe() instanceof Pipe)) {
			return;
		}

		Pipe<?> pipe = (Pipe<?>) pipeTile.getPipe();

		if (!(pipe.transport instanceof PipeTransportFluids)) {
			return;
		}

		PipeTransportFluids transLiq = (PipeTransportFluids) pipe.transport;

		this.largeFluidCapacity = transLiq.getCapacity() > 255;
		renderCache = transLiq.renderCache;

		byte[] dBytes = new byte[1];
		data.readBytes(dBytes);
		delta = BitSetUtils.fromByteArray(dBytes);

		if (delta.get(0)) {
			renderCache.fluidID = data.readShort();
			renderCache.color = renderCache.fluidID != 0 ? data.readInt() : 0xFFFFFF;
			renderCache.flags = renderCache.fluidID != 0 ? data.readUnsignedByte() : 0;
		}

		for (ForgeDirection dir : ForgeDirection.values()) {
			if (delta.get(dir.ordinal() + 1)) {
				renderCache.amount[dir.ordinal()] = Math.min(transLiq.getCapacity(),
						largeFluidCapacity ? data.readUnsignedShort() : data.readUnsignedByte());
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

		for (ForgeDirection dir : ForgeDirection.values()) {
			if (delta.get(dir.ordinal() + 1)) {
				if (largeFluidCapacity) {
					data.writeShort(renderCache.amount[dir.ordinal()]);
				} else {
					data.writeByte(renderCache.amount[dir.ordinal()]);
				}
			}
		}
	}

	@Override
	public int getID() {
		return PacketIds.PIPE_LIQUID;
	}
}
