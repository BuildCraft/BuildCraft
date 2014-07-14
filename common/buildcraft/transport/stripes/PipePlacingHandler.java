package buildcraft.transport.stripes;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesPipe;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class PipePlacingHandler implements IStripesHandler {

    @Override
    public StripesBehavior behave(IStripesPipe pipe, StripesAction act, ItemStack is) {
        if (act == StripesAction.DESTROY) {
            return StripesBehavior.DEFAULT;
        }

        Pipe<?> th = is.getItem() != null ? BlockGenericPipe.createPipe(is.getItem()) : null;
        if (th != null && th.transport instanceof PipeTransportItems) {
            World wrd = pipe.getWorld();
            Position loc = pipe.getPosition();
            ForgeDirection dir = pipe.getOpenOrientation();
            TileGenericPipe stripes = (TileGenericPipe) loc.getTile(wrd);
            if (!loc.shift(dir).blockExists(wrd)) {
                th.setTile(stripes);
                stripes.pipe = th;
                is.stackSize--;
                place(wrd, loc, BuildCraftTransport.pipeItemsStripes);
            }
            return StripesBehavior.DROP;
        }
        return StripesBehavior.DEFAULT;
    }

    private boolean place(World wrd, Position pos, Item item) {
        return new ItemStack(item)
            .tryPlaceItemIntoWorld(CoreProxy.proxy.getBuildCraftPlayer((WorldServer) wrd).get(), wrd, (int) pos.x, (int) pos.y, (int) pos.z, 0, 0.0F, 0.0F, 0.0F);
    }
}