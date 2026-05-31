/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.core.marker.volume;

import org.apache.commons.lang3.tuple.Pair;

// Yarn 1.20.1 renames:
//   PlayerEntity         → PlayerEntity
//   Hand             → Hand
//   ActionResult<T>      → TypedActionResult<T>
//   ActionResult     → ActionResult   (enum values: PASS, SUCCESS, FAIL, CONSUME)
//   World#isRemote       → World#isClient
//   player.getStackInHand() → player.getStackInHand()
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import buildcraft.lib.item.ItemBC_Neptune;

public abstract class ItemAddon extends ItemBC_Neptune {
    public ItemAddon(String id) {
        super(id);
    }

    public abstract Addon createAddon();

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient) {
            return TypedActionResult.pass(player.getStackInHand(hand));
        }

        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);
        Pair<VolumeBox, EnumAddonSlot> selectingVolumeBoxAndSlot = EnumAddonSlot.getSelectingVolumeBoxAndSlot(
            player,
            volumeBoxes.volumeBoxes
        );
        VolumeBox volumeBox = selectingVolumeBoxAndSlot.getLeft();
        EnumAddonSlot slot = selectingVolumeBoxAndSlot.getRight();
        if (volumeBox != null && slot != null) {
            if (!volumeBox.addons.containsKey(slot)) {
                Addon addon = createAddon();
                if (addon.canBePlaceInto(volumeBox)) {
                    addon.volumeBox = volumeBox;
                    volumeBox.addons.put(slot, addon);
                    volumeBox.addons.get(slot).onAdded();
                    volumeBoxes.markDirty();
                    return TypedActionResult.success(player.getStackInHand(hand));
                }
            }
        }

        return TypedActionResult.pass(player.getStackInHand(hand));
    }
}
