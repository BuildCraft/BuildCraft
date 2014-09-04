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

/**
 * Interface to handle custom stripes pipe behaviors.
 * Can be applied on block, item, itemBlock and on custom class(registered via {@link PipeManager}).
 */
public interface IStripesHandler {

    public static enum StripesAction {
        /**
         * Called to handle item usage.
         */
        PLACE,
        /**
         * Called to handle block breaking(called only for blocks!).
         */
        DESTROY
    }

    public static enum StripesBehavior {
        /**
         * Stripes pipe will destroy {@link ItemStack} and do nothing.
         */
        NONE,
        /**
         * Stripes pipe will drop {@link ItemStack} passed to {@link #behave}. On blocks same as {@link #NONE}
         */
        DROP,
        /**
         * Stripes pipe will act like it should do it without this handler.
         */
        DEFAULT
    }

    /**
     * Manages stripes pipe behavior.
     *
     * @param pipe Currently handled pipe
     * @param act See {@link StripesAction}
     * @param is Item to handle. For block breaking it created only to hold block item and meta.
     * @return How stripes pipe should behave.
     */
    StripesBehavior behave(IStripesPipe pipe, StripesAction act, ItemStack is);
}
