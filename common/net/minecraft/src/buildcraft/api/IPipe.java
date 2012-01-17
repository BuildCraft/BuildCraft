/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

import net.minecraft.src.TileEntity;

public interface IPipe {

	enum DrawingState {
		DrawingPipe, DrawingRedWire, DrawingBlueWire, DrawingGreenWire, DrawingYellowWire, DrawingGate
	}
	
	enum WireColor {
		Red, Blue, Green, Yellow
	}
	
	/**
	 * With special kind of pipes, connectors texture has to vary (e.g. 
	 * diamond or iron pipes. 
	 */
	public void prepareTextureFor (Orientations connection);
	
	public void setDrawingState (DrawingState state);
	
	public boolean isWired (WireColor color);
	
	public boolean hasInterface ();
	
	public TileEntity getContainer ();
	
	public boolean isWireConnectedTo (TileEntity tile, WireColor color);
	
}
