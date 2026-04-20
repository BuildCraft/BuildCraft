/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution.
 */

package ct.buildcraft.api.template;

import ct.buildcraft.api.core.EnumHandlerPriority;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ITemplateRegistry {
    /** Adds a handler with a {@link EnumHandlerPriority} of {@linkplain EnumHandlerPriority#NORMAL} */
    default void addHandler(ITemplateHandler handler) {
        addHandler(handler, EnumHandlerPriority.NORMAL);
    }

    void addHandler(ITemplateHandler handler, EnumHandlerPriority priority);

    boolean handle(Level world, BlockPos pos, Player player, ItemStack stack);
}
