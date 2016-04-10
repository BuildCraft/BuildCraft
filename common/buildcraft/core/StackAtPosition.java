/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.core.ISerializable;
import buildcraft.core.lib.utils.NetworkUtils;

public class StackAtPosition implements ISerializable {
    public ItemStack stack;
    public Vec3d pos;
    public boolean display;

    // Rendering only!
    public boolean generatedListId;
    public int glListId;

    @Override
    public void readData(ByteBuf stream) {
        stack = NetworkUtils.readStack(stream);
    }

    @Override
    public void writeData(ByteBuf stream) {
        NetworkUtils.writeStack(stream, stack);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        StackAtPosition other = (StackAtPosition) o;
        return new EqualsBuilder().append(stack, other.stack).append(pos, other.pos).append(display, other.display).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(stack).append(pos).append(display).toHashCode();
    }
}
