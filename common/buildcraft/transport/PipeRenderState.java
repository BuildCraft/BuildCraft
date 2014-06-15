/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import io.netty.buffer.ByteBuf;

import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.core.network.IClientState;
import buildcraft.transport.utils.ConnectionMatrix;
import buildcraft.transport.utils.FacadeMatrix;
import buildcraft.transport.utils.GateMatrix;
import buildcraft.transport.utils.TextureMatrix;
import buildcraft.transport.utils.WireMatrix;

public class PipeRenderState implements IClientState {

	public final ConnectionMatrix pipeConnectionMatrix = new ConnectionMatrix();
	public final TextureMatrix textureMatrix = new TextureMatrix();
	public final WireMatrix wireMatrix = new WireMatrix();
	public final ConnectionMatrix plugMatrix = new ConnectionMatrix();
	public final ConnectionMatrix robotStationMatrix = new ConnectionMatrix();
	public final FacadeMatrix facadeMatrix = new FacadeMatrix();
	public final GateMatrix gateMatrix = new GateMatrix();

	/*
	 * This is a placeholder for the pipe renderer to set to a value that the BlockGenericPipe->TileGenericPipe will then return the the WorldRenderer
	 */
	@SideOnly(Side.CLIENT)
	public IIcon currentTexture;
	@SideOnly(Side.CLIENT)
	public IIcon[] textureArray;

	private boolean dirty = true;

	public void clean() {
		dirty = false;
		pipeConnectionMatrix.clean();
		textureMatrix.clean();
		facadeMatrix.clean();
		wireMatrix.clean();
		plugMatrix.clean();
		robotStationMatrix.clean();
		gateMatrix.clean();
	}

	public boolean isDirty() {
		return dirty || pipeConnectionMatrix.isDirty()
				|| textureMatrix.isDirty() || wireMatrix.isDirty()
				|| facadeMatrix.isDirty() || plugMatrix.isDirty()
				|| robotStationMatrix.isDirty() || gateMatrix.isDirty();
	}

	public boolean needsRenderUpdate() {
		return pipeConnectionMatrix.isDirty() || textureMatrix.isDirty()
				|| facadeMatrix.isDirty() || plugMatrix.isDirty()
				|| robotStationMatrix.isDirty() || gateMatrix.isDirty();
	}

	@Override
	public void writeData(ByteBuf data) {
		pipeConnectionMatrix.writeData(data);
		textureMatrix.writeData(data);
		wireMatrix.writeData(data);
		facadeMatrix.writeData(data);
		plugMatrix.writeData(data);
		robotStationMatrix.writeData(data);
		gateMatrix.writeData(data);
	}

	@Override
	public void readData(ByteBuf data) {
		pipeConnectionMatrix.readData(data);
		textureMatrix.readData(data);
		wireMatrix.readData(data);
		facadeMatrix.readData(data);
		plugMatrix.readData(data);
		robotStationMatrix.readData(data);
		gateMatrix.readData(data);
	}
}
