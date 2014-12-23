/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IStripesPipe extends IPipe {
	void sendItem(ItemStack itemStack, EnumFacing direction);
	void dropItem(ItemStack itemStack, EnumFacing direction);
}
