/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.marker.volume;

import java.io.IOException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    public AABB getBoundingBox() {
        return getSlot().getBoundingBox(volumeBox);
    }

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

    public void onPlayerRightClick(Player player) {
    }

    public abstract CompoundTag writeToNBT(CompoundTag nbt);

    public abstract void readFromNBT(CompoundTag nbt);

    public void postReadFromNbt() {
    }

    public abstract void toBytes(FriendlyByteBuf buf);

    public abstract void fromBytes(FriendlyByteBuf buf) throws IOException;
}
