/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.api.lists.ListRegistry;

public class ListMatchHandlerClass extends ListMatchHandler {
    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            Class<?> kl = stack.getItem().getClass();
            return ListRegistry.itemClassAsType.contains(kl) && kl.equals(target.getClass());
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        if (type == Type.TYPE) {
            Class<?> kl = stack.getItem().getClass();
            return ListRegistry.itemClassAsType.contains(kl);
        }
        return false;
    }
}
