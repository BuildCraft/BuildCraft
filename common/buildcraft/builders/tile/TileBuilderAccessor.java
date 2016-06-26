/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package buildcraft.builders.tile;

import com.google.common.collect.ImmutableSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.bpt.BptPermissions;
import buildcraft.lib.bpt.builder.AbstractBuilderAccessor;
import buildcraft.lib.misc.VecUtil;

public class TileBuilderAccessor extends AbstractBuilderAccessor {
    private final Vec3d vec;

    public TileBuilderAccessor(TileBuilder_Neptune tile, NBTTagCompound nbt) {
        super(tile.getOwner(), tile.getWorld(), tile.animation, nbt);
        this.vec = VecUtil.add(null, tile.getPos());
    }

    public TileBuilderAccessor(TileBuilder_Neptune tile) {
        super(tile.getOwner(), tile.getWorld(), tile.animation);
        this.vec = VecUtil.add(null, tile.getPos());
    }

    @Override
    public Vec3d getBuilderPosition() {
        return vec;
    }

    @Override
    public ImmutableSet<BptPermissions> getPermissions() {
        return ImmutableSet.of();
    }
}
