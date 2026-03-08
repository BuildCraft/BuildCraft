/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import buildcraft.api.lists.ListMatchHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nonnull;
import java.util.Set;

public class ListMatchHandlerTools extends ListMatchHandler {
    /** Matches tool type (axe...), not tier (wooden/iron/...). */
    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
//            Set<String> toolClassesSource = stack.getItem().getToolClasses(stack);
            Set<ToolType> toolClassesSource = stack.getItem().getToolTypes(stack);
//            Set<String> toolClassesTarget = target.getItem().getToolClasses(stack);
            Set<ToolType> toolClassesTarget = target.getItem().getToolTypes(stack);
            if (toolClassesSource.size() > 0 && toolClassesTarget.size() > 0) {
                if (precise) {
                    if (toolClassesSource.size() != toolClassesTarget.size()) {
                        return false;
                    }
                }
//                for (String s : toolClassesSource)
                for (ToolType s : toolClassesSource) {
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
//        return stack.getItem().getToolClasses(stack).size() > 0;
        return stack.getItem() instanceof TieredItem;
    }
}
