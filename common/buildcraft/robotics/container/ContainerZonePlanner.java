/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.robotics.container;

import buildcraft.lib.gui.ContainerBCTile;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.robotics.tile.TileZonePlanner;

public class ContainerZonePlanner extends ContainerBCTile<TileZonePlanner> {
    private static final int PLAYER_INV_START_X = 88;
    private static final int PLAYER_INV_START_Y = 146;

    public ContainerZonePlanner(EntityPlayer player, TileZonePlanner tile) {
        super(player, tile);
        addFullPlayerInventory(PLAYER_INV_START_X, PLAYER_INV_START_Y);

//        for (int i = 0; i < 9; i++) {
//            // Filtered Buffer filter slots
//            SlotPhantom phantom = new SlotPhantom(tile.invFilter, i, 8 + i * 18, 27) {
//                @Override
//                public TextureAtlasSprite getBackgroundSprite() {
//                    return RoboticsSprites.EMPTY_FILTERED_BUFFER_SLOT.getSprite();
//                }
//
//                @Override
//                public boolean canAdjust() {
//                    return false;
//                }
//            };
//            addSlotToContainer(phantom);
//            // Filtered Buffer inventory slots
//            addSlotToContainer(new SlotBase(tile.invMain, i, 8 + i * 18, 61) {
//                @Override
//                public boolean isItemValid(ItemStack stack) {
//                    return phantom.getHasStack() && StackUtil.canMerge(phantom.getStack(), stack);
//                }
//            });
//        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
