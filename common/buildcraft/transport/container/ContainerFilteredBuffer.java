/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.transport.container;

import buildcraft.core.lib.gui.slots.SlotBase;
import buildcraft.core.lib.gui.slots.SlotPhantom;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.misc.StackUtil;
import buildcraft.transport.tile.TileFilteredBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ContainerFilteredBuffer extends ContainerBCTile<TileFilteredBuffer> {
    private static final int PLAYER_INV_START = 86;
    private static SpriteHolderRegistry.SpriteHolder emptyFilterSlotHolder;

    public static void preInit() {
        emptyFilterSlotHolder = SpriteHolderRegistry.getHolder("buildcrafttransport:gui/empty_filtered_buffer_slot");
    }

    public ContainerFilteredBuffer(EntityPlayer player, TileFilteredBuffer tile) {
        super(player, tile);
        addFullPlayerInventory(PLAYER_INV_START);

        for (int i = 0; i < 9; i++) {
            // Filtered Buffer filter slots
            SlotPhantom phantom = new SlotPhantom(tile.invFilter, i, 8 + i * 18, 27) {
                @Override
                public TextureAtlasSprite getBackgroundSprite() {
                    return emptyFilterSlotHolder.getSprite();
                }
            };
            addSlotToContainer(phantom);
            // Filtered Buffer inventory slots
            addSlotToContainer(new SlotBase(tile.invMain, i, 8 + i * 18, 61) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return phantom.getHasStack() && StackUtil.canMerge(phantom.getStack(), stack);
                }
            });
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
