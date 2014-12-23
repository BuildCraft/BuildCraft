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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;

import buildcraft.BuildCraftCore;
import buildcraft.core.utils.Utils;

public class PacketGuiReturn extends BuildCraftPacket {
	private EntityPlayer sender;
	private IGuiReturnHandler obj;
	private byte[] extraData;

	public PacketGuiReturn() {
	}
	
	public PacketGuiReturn(EntityPlayer sender) {
		this.sender = sender;
	}

	public PacketGuiReturn(IGuiReturnHandler obj) {
		this.obj = obj;
		this.extraData = null;
	}

	public PacketGuiReturn(IGuiReturnHandler obj, byte[] extraData) {
		this.obj = obj;
		this.extraData = extraData;
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeInt(obj.getWorld().provider.getDimensionId());
		
		if (obj instanceof TileEntity) {
			TileEntity tile = (TileEntity) obj;
			data.writeBoolean(true);
			Utils.writeBlockPos(data, tile.getPos());
		} else if (obj instanceof Entity) {
			Entity entity = (Entity) obj;
			data.writeBoolean(false);
			data.writeInt(entity.getEntityId());
		} else {
			return;
		}
		
		obj.writeGuiData(data);
		
		if (extraData != null) {
			data.writeBytes(extraData);
		}
	}

	@Override
	public void readData(ByteBuf data) {
		int dim = data.readInt();
		World world = DimensionManager.getWorld(dim);
		boolean tileReturn = data.readBoolean();
		
		if (tileReturn) {
			BlockPos pos = Utils.readBlockPos(data);
			TileEntity t = world.getTileEntity(pos);

			if (t instanceof IGuiReturnHandler) {
				((IGuiReturnHandler) t).readGuiData(data, sender);
			}
		} else {
			int entityId = data.readInt();
			Entity entity = world.getEntityByID(entityId);

			if (entity instanceof IGuiReturnHandler) {
				((IGuiReturnHandler) entity).readGuiData(data, sender);
			}
		}
	}

	public void sendPacket() {
		BuildCraftCore.instance.sendToServer(this);
	}

	@Override
	public int getID() {
		return PacketIds.GUI_RETURN;
	}
}