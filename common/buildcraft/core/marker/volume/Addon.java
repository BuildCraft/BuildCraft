/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.core.marker.volume;

import java.io.IOException;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Yarn 1.20.1 renames:
//   PlayerEntity → PlayerEntity, NbtCompound → NbtCompound, Box → net.minecraft.util.math.Box
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;

import buildcraft.lib.net.PacketBufferBC;

public abstract class Addon {
    public VolumeBox volumeBox;

    @Environment(EnvType.CLIENT)
    public abstract IFastAddonRenderer<? extends Addon> getRenderer();

    public EnumAddonSlot getSlot() {
        return volumeBox.addons.entrySet().stream()
            .filter(slotAddon -> slotAddon.getValue() == this)
            .findFirst()
            .orElseThrow(IllegalStateException::new)
            .getKey();
    }

    public Box getBoundingBox() {
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

    public abstract NbtCompound writeToNBT(NbtCompound nbt);

    public abstract void readFromNBT(NbtCompound nbt);

    public void postReadFromNbt() {
    }

    public abstract void toBytes(PacketBufferBC buf);

    public abstract void fromBytes(PacketBufferBC buf) throws IOException;
}
