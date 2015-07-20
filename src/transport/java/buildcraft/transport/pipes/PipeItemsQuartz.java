/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IIconProvider;
import buildcraft.transport.BuildCraftTransport;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TransportConstants;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;

public class PipeItemsQuartz extends Pipe<PipeTransportItems> {

    public PipeItemsQuartz(Item item) {
        super(new PipeTransportItems(), item);

    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider() {
        return BuildCraftTransport.instance.pipeIconProvider;
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        return PipeIconProvider.TYPE.PipeItemsQuartz.ordinal();
    }

    public void eventHandler(PipeEventItem.AdjustSpeed event) {
        event.handled = true;
        TravelingItem item = event.item;

        if (item.getSpeed() > TransportConstants.PIPE_NORMAL_SPEED) {
            item.setSpeed(item.getSpeed() - TransportConstants.PIPE_NORMAL_SPEED / 4.0F);
        }

        if (item.getSpeed() < TransportConstants.PIPE_NORMAL_SPEED) {
            item.setSpeed(TransportConstants.PIPE_NORMAL_SPEED);
        }
    }
}
