/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.stripes;

import net.minecraft.item.ItemStack;

import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesPipe;

public class DefaultHandler implements IStripesHandler {

	@Override
	public StripesBehavior behave(IStripesPipe pipe, StripesAction act, ItemStack is) {
		return StripesBehavior.DEFAULT;
	}
}