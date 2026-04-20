/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.list;

import javax.annotation.Nonnull;

import ct.buildcraft.api.lists.ListMatchHandler;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;

public class ListMatchHandlerTools extends ListMatchHandler {
    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE&& stack.getItem() instanceof DiggerItem diggerItem) {
        	return false;//TODO
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        return stack.getItem() instanceof DiggerItem;//TODO CHECK
    }
}
