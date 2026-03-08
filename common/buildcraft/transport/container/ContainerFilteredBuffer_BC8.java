/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.transport.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.tile.TileFilteredBuffer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class ContainerFilteredBuffer_BC8 extends ContainerBCTile<TileFilteredBuffer> {
    public ContainerFilteredBuffer_BC8(MenuType menuType, int id, Player player, TileFilteredBuffer tile) {
        super(menuType, id, player, tile);
        addFullPlayerInventory(86);

        for (int i = 0; i < 9; i++) {
            // Filtered Buffer filter slots
            addSlot(new SlotPhantom(tile.invFilter, i, 8 + i * 18, 27) {
                @Override
//                public TextureAtlasSprite getBackgroundSprite()
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
//                    return BCTransportSprites.EMPTY_FILTERED_BUFFER_SLOT.getSprite();
                    return Pair.of(TextureAtlas.LOCATION_BLOCKS, BCTransportSprites.EMPTY_FILTERED_BUFFER_SLOT.getSprite().getName());
                }

                @Override
                public boolean canAdjustCount() {
                    return false;
                }
            });
            // Filtered Buffer inventory slots
//            addSlotToContainer(new SlotBase(tile.invMain, i, 8 + i * 18, 61));
            addSlot(new SlotBase(tile.invMain, i, 8 + i * 18, 61));
        }
    }
}
