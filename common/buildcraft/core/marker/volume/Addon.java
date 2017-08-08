/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.net.PacketBufferBC;

public abstract class Addon {
    public VolumeBox box;

    @SideOnly(Side.CLIENT)
    public abstract IFastAddonRenderer<? extends Addon> getRenderer();

    public EnumAddonSlot getSlot() {
        return box.addons.entrySet().stream().filter(slotAddon -> slotAddon.getValue() == this).findFirst().orElse(null).getKey();
    }

    public AxisAlignedBB getBoundingBox() {
        return getSlot().getBoundingBox(box);
    }

    public boolean canBePlaceInto(VolumeBox box) {
        return !(this instanceof ISingleAddon) || box.addons.values().stream().noneMatch(addon -> addon.getClass() == getClass());
    }

    public void onAdded() {
    }

    public void onRemoved() {
    }

    public void onBoxSizeChange() {
    }

    public void onPlayerRightClick(EntityPlayer player) {
    }

    public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

    public abstract void readFromNBT(NBTTagCompound nbt);

    public abstract void toBytes(PacketBufferBC buf);

    public abstract void fromBytes(PacketBufferBC buf) throws IOException;
}
