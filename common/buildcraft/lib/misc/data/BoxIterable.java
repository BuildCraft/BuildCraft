/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import buildcraft.api.core.IBox;
import net.minecraft.core.BlockPos;

public class BoxIterable implements Iterable<BlockPos> {
    private final BlockPos min, max;
    private final AxisOrder order;
    private final boolean invert;

    public BoxIterable(IBox box, AxisOrder order) {
        this(box.max(), box.max(), order);
    }

    public BoxIterable(BlockPos min, BlockPos max, AxisOrder order) {
        this(min, max, order, false);
    }

    public BoxIterable(BlockPos min, BlockPos max, AxisOrder order, boolean invert) {
        this.min = min;
        this.max = max;
        this.order = order;
        this.invert = invert;
    }

    @Override
    public BoxIterator iterator() {
        return new BoxIterator(min, max, order, invert);
    }
}
