/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import buildcraft.lib.item.ItemBC_Neptune;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public abstract class ItemAddon extends ItemBC_Neptune {
    public ItemAddon(String id) {
        super(id);
    }

    public abstract Addon createAddon();

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
        }

        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);
        Pair<VolumeBox, EnumAddonSlot> selectingBoxAndSlot = EnumAddonSlot.getSelectingBoxAndSlot(player, volumeBoxes);
        VolumeBox box = selectingBoxAndSlot.getLeft();
        EnumAddonSlot slot = selectingBoxAndSlot.getRight();
        if (box != null && slot != null) {
            if (!box.addons.containsKey(slot)) {
                Addon addon = createAddon();
                if (addon.canBePlaceInto(box)) {
                    addon.box = box;
                    box.addons.put(slot, addon);
                    box.addons.get(slot).onAdded();
                    volumeBoxes.markDirty();
                    return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
                }
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }
}
