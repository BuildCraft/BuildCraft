/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.transport.PipeTransportLiquids;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class PipeLiquidsEmerald extends PipeLiquidsWood {

	public PipeLiquidsEmerald(int itemID) {
		super(new PipeLogicEmerald(), itemID);

		baseTexture = 6 * 16 + 15;
		plainTexture = baseTexture - 1;

		((PipeTransportLiquids) transport).flowRate = 40;
		((PipeTransportLiquids) transport).travelDelay = 4;
	}
}
