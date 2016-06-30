/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package buildcraft.factory.container;

import buildcraft.core.lib.gui.slots.SlotBase;
import buildcraft.core.lib.gui.slots.SlotOutput;
import buildcraft.core.lib.gui.slots.SlotPhantom;
import buildcraft.core.lib.gui.slots.SlotUntouchable;
import buildcraft.factory.TileAutoWorkbench;
import buildcraft.factory.gui.SlotWorkbench;
import net.minecraft.entity.player.EntityPlayer;

import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.lib.gui.ContainerBCTile;
import net.minecraft.inventory.Slot;

public class ContainerAutoCraftItems extends ContainerBCTile<TileAutoWorkbenchItems> {
    private static final int PLAYER_INV_START = 115;

    public ContainerAutoCraftItems(EntityPlayer player, TileAutoWorkbenchItems tile) {
        super(player, tile);
        addFullPlayerInventory(PLAYER_INV_START);

        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new SlotBase(tile.invMaterials, x, 8 + x * 18, 84));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotPhantom(tile.invBlueprint, x + y * 3, 30 + x * 18, 17 + y * 18));
            }
        }

//        addSlotToContainer(new SlotUntouchable(craftResult, 0, 93, 27));
        addSlotToContainer(new SlotOutput(tile.invResult, 0, 124, 35));

        if (!tile.getWorld().isRemote) {
            tile.deltaProgress.addDelta(0, 200, 100);
            tile.deltaProgress.addDelta(200, 220, -100);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
