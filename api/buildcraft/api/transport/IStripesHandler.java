/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import buildcraft.api.core.Position;

public interface IStripesHandler {
	boolean handleStripesEvent(World world, ItemStack itemStack, Position dropPosition, IStripesPipe pipe);
}
