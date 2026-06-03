/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): IBox / Box utility methods deferred
package buildcraft.lib.misc;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.IBox;
import buildcraft.lib.misc.data.Box;

/** Various methods operating on (and creating) {@link Box} */
public class BoundingBoxUtil {

    public static Box makeFrom(BlockPos additional, @Nullable IBox box) {
        if (box == null) {
            return new Box(additional);
        }
        Box b = new Box(box);
        b.extendToEncompass(additional);
        return b;
    }

    public static Box makeFrom(Collection<BlockPos> positions) {
        Box b = null;
        for (BlockPos pos : positions) {
            if (b == null) {
                b = new Box(pos);
            } else {
                b.extendToEncompass(pos);
            }
        }
        return b;
    }
}
