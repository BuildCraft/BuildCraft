/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.pipes.events.PipeEventPriority;

public class PipeItemsClay extends Pipe<PipeTransportItems> {

    public PipeItemsClay(Item item) {
        super(new PipeTransportItems(), item);

        transport.allowBouncing = false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider() {
        return BuildCraftTransport.instance.pipeIconProvider;
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        return PipeIconProvider.TYPE.PipeItemsClay.ordinal();
    }

    @PipeEventPriority(priority = -200)
    public void eventHandler(PipeEventItem.FindDest event) {
        List<EnumSet<EnumFacing>> newDestinations = new ArrayList<>(event.destinations.size() * 2);

        for (EnumSet<EnumFacing> faces : event.destinations) {
            EnumSet<EnumFacing> nonPipesList = EnumSet.noneOf(EnumFacing.class);
            EnumSet<EnumFacing> pipesList = EnumSet.noneOf(EnumFacing.class);

            for (EnumFacing o : faces) {
                if (!event.item.blacklist.contains(o) && container.pipe.outputOpen(o)) {
                    if (container.isPipeConnected(o)) {
                        TileEntity entity = container.getTile(o);
                        if (entity instanceof IPipeTile) {
                            pipesList.add(o);
                        } else {
                            nonPipesList.add(o);
                        }
                    }
                }
            }

            if (!nonPipesList.isEmpty()) {
                newDestinations.add(nonPipesList);
            }

            if (!pipesList.isEmpty()) {
                newDestinations.add(pipesList);
            }
        }

        event.destinations.clear();
        event.destinations.addAll(newDestinations);
    }
}
