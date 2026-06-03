/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.api.lists.ListMatchHandler;

public class ListMatchHandlerTools extends ListMatchHandler {
    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            // STUB(R.Chen): Forge Item.getToolClasses removed in Fabric — tool-type matching deferred, Phase 10
            Set<String> toolClassesSource = java.util.Collections.emptySet();
            Set<String> toolClassesTarget = java.util.Collections.emptySet();
            if (toolClassesSource.size() > 0 && toolClassesTarget.size() > 0) {
                if (precise) {
                    if (toolClassesSource.size() != toolClassesTarget.size()) {
                        return false;
                    }
                }
                for (String s : toolClassesSource) {
                    if (!toolClassesTarget.contains(s)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        return false; // STUB(R.Chen): Forge tool classes removed — Phase 10
    }
}
