/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.transport;

import javax.annotation.Nonnull;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public interface IStripesActivator {
    boolean sendItem(@Nonnull ItemStack itemStack, Direction from);

    void dropItem(@Nonnull ItemStack itemStack, Direction from);
}
