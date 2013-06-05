/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.transport;

import buildcraft.transport.PipeTransport;
import buildcraft.transport.pipes.PipeLogic;
import buildcraft.transport.pipes.PipeLogicStone;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface IPipe {

	enum DrawingState {
		DrawingPipe, DrawingRedWire, DrawingBlueWire, DrawingGreenWire, DrawingYellowWire, DrawingGate
	}

	enum WireColor {
		Red, Blue, Green, Yellow;

		public WireColor reverse() {
			switch (this) {
			case Red:
				return Yellow;
			case Blue:
				return Green;
			case Green:
				return Blue;
			default:
				return Red;
			}
		}
	}

	public boolean isWired(WireColor color);

	public boolean hasInterface();

	public TileEntity getContainer();

	public boolean isWireConnectedTo(TileEntity tile, WireColor color);

	public PipeLogic getLogic();
	
	public PipeTransport getTransport();

	public World getWorld();

	int getXPosition();
	int getYPosition();
	int getZPosition();

}
