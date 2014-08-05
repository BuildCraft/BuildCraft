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

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.Position;

/**
 * Interface used to provide some control and info about certain stripes pipe.
 *
 * DO NOT IMPLEMENT IT!
 */
public interface IStripesPipe {

    /**
     * @return The {@link World world} instance.
     */
    World getWorld();

    /**
     * @return {@link buildcraft.api.core.Position} of this pipe.
     */
    Position getPosition();

    /**
     * Since you get instance of this only in {@link IStripesHandler#behave} method,
     * it's never will be {@link ForgeDirection#UNKNOWN}.
     *
     * @return Opposite {@link ForgeDirection direction} of single connection or {@link ForgeDirection#UNKNOWN}.
     */
    ForgeDirection getOpenOrientation();

    /**
     * Sends item to given side of the pipe.
     * @param is {@link ItemStack} to send.
     * @param dir {@link ForgeDirection Direction} to send to.
     */
    void sendItem(ItemStack is, ForgeDirection dir);
}