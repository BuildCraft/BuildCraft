/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;

public abstract class Addon {
    public VolumeBox volumeBox;

    @OnlyIn(Dist.CLIENT)
    public abstract IFastAddonRenderer<? extends Addon> getRenderer();

    public EnumAddonSlot getSlot() {
        return volumeBox.addons.entrySet().stream()
                .filter(slotAddon -> slotAddon.getValue() == this)
                .findFirst()
                .orElseThrow(IllegalStateException::new)
                .getKey();
    }

    public AxisAlignedBB getBoundingBox() {
        return getSlot().getBoundingBox(volumeBox);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean canBePlaceInto(VolumeBox volumeBox) {
        return !(this instanceof ISingleAddon &&
                volumeBox.addons.values().stream().anyMatch(addon -> addon.getClass() == getClass()));
    }

    public void onAdded() {
    }

    public void onRemoved() {
    }

    public void onVolumeBoxSizeChange() {
    }

    public void onPlayerRightClick(PlayerEntity player) {
    }

    public abstract CompoundNBT writeToNBT(CompoundNBT nbt);

    public abstract void readFromNBT(CompoundNBT nbt);

    public void postReadFromNbt() {
    }

    public abstract void toBytes(PacketBufferBC buf);

    public abstract void fromBytes(PacketBufferBC buf) throws IOException;
}
