/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import net.minecraft.item.Item;
import buildcraft.transport.PipeIconProvider;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class PipeFluidsEmerald extends PipeFluidsWood {

	public PipeFluidsEmerald(Item item) {
		super(item);

		standardIconIndex = PipeIconProvider.TYPE.PipeFluidsEmerald_Standard.ordinal();
		solidIconIndex = PipeIconProvider.TYPE.PipeAllEmerald_Solid.ordinal();

		transport.flowRate = 40;
		transport.travelDelay = 4;
	}
}
