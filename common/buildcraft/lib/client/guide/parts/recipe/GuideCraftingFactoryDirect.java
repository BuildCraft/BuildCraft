/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.recipe.ChangingItemStack;

import java.util.Arrays;

public class GuideCraftingFactoryDirect implements GuidePartFactory {
    public final ChangingItemStack[][] input;
    public final ChangingItemStack output;

    private final int hash;

    public GuideCraftingFactoryDirect(ChangingItemStack[][] input, ChangingItemStack output) {
        this.input = input;
        this.output = output;
        hash = Arrays.deepHashCode(new Object[] { input, output });
    }

    @Override
    public GuidePart createNew(GuiGuide gui) {
        return new GuideCrafting(gui, input, output);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) {
            return false;
        }
        GuideCraftingFactoryDirect other = (GuideCraftingFactoryDirect) obj;
        if (hash != other.hash) {
            return false;
        }
        // Short-circuit equals, as checking the contents of ChangingItemStack can be really expensive
        if (output == other.output) {
            if (input.length == other.input.length) {
                outer_loop:
                {
                    for (int i = 0; i < input.length; i++) {
                        if (input[i].length == other.input[i].length) {
                            for (int j = 0; j < input[i].length; j++) {
                                if (input[i][j] != other.input[i][j]) {
                                    break outer_loop;
                                }
                            }
                        }
                    }
                    return true;
                }
            }
            return Arrays.deepEquals(input, other.input);
        } else {
            return Arrays.deepEquals(input, other.input)//
                    && output.equals(other.output);
        }
    }
}
