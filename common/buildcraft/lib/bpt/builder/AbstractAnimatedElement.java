/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.bpt.builder;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AbstractAnimatedElement {
    public final long start, end;

    public AbstractAnimatedElement(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public AbstractAnimatedElement(NBTTagCompound nbt) {
        this.start = nbt.getLong("start");
        this.end = nbt.getLong("end");
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("start", start);
        nbt.setLong("end", end);
        return nbt;
    }

    @SideOnly(Side.CLIENT)
    public abstract void render(VertexBuffer vb, long now, float partialTicks);
}
