/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.recipe;

public interface IRecipeViewable {
    ChangingItemStack[] getRecipeInputs();

    ChangingItemStack getRecipeOutputs();

    public interface IViewableGrid extends IRecipeViewable {
        int getRecipeWidth();

        int getRecipeHeight();
    }

    /** Use this for integration table recipes or assembly table recipes. */
    public interface IRecipePowered extends IRecipeViewable {
        ChangingObject<Long> getMjCost();
    }
}
