package buildcraft.transport.pipes.bc8;

import net.minecraft.nbt.NBTBase;

import buildcraft.api.transport.pipe_bc8.*;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventConnection_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventContents_BC8;
import buildcraft.transport.PipeTransportItems;

import io.netty.buffer.ByteBuf;

public class PipeTransportItem_BC8 implements IPipeListener {
    public static final int MAX_PIPE_STACKS = 64;
    public static final int MAX_PIPE_ITEMS = 1024;
    public static final double SPEED_NORMALIZER = 20;

    public final IPipe_BC8 pipe;

    public PipeTransportItem_BC8(IPipe_BC8 pipe) {
        this.pipe = pipe;
    }

    // Event disabled because it is not technically needed
    // @BCPipeEventHandler
    public void attemptInsertion(IPipeEventContents_BC8.AttemptEnter attemptEnter) {
        /* Note that we specifically do NOT disallow this here. The pipe is responsible for checking the Enter event to
         * see if it was handled, and if some-one added a fluid and an item transport to the pipe we don't want both of
         * them conflicting. */

        boolean isItem = attemptEnter.getContents() instanceof IPipeContents.IPipeContentsItem;
        if (!isItem) ;// attemptEnter.disallow();
    }

    @BCPipeEventHandler
    public void attemptConnection(IPipeEventConnection_BC8.AttemptCreate event) {
        if (event.getConnection().getInserter().acceptsItems()) {
            event.couldAccept();
        } else if (event.getConnection().getExtractor().givesItems()) {
            event.couldAccept();
        }
    }

    @BCPipeEventHandler
    public void itemInsertion(IPipeEventContents_BC8.Enter enter) {
        // If somebody else has already handled this then don't even bother to handle it
        if (enter.hasBeenHandled()) return;

        // if we are the limit then don't add it either
        int stacks = enter.getPipe().getProperties().getValue(PipeAPI_BC8.STACK_COUNT);
        // Note that a travelling item will probably add it to itself at this point.
        if (stacks >= PipeTransportItems.MAX_PIPE_STACKS) return;

        // Setup the item to make it tick immediately
        IPipeContentsEditableItem item = (IPipeContentsEditableItem) enter.getContents();
        item.setJourneyPart(EnumContentsJourneyPart.JUST_ENTERED);
        item.setDirection(enter.getFrom().getOpposite());
        long now = pipe.getWorld().getTotalWorldTime();

        TravellingItem_BC8 travellingItem = new TravellingItem_BC8(pipe, item, now, now);

        /* Actually add the item to the bus, which will make it render itself, tick itself, send client updates, etc. */
        if (pipe.addEventListener(travellingItem)) {
            // Tell the event that we have consumed the item, but only if it was actually added to the bus
            enter.handle();
        }
    }

    /* This doesn't have any state, so it won't actually read or write anything to disk or to the network- all data is
     * saved in the indervidual Travelling Item instances */

    @Override
    public PipeTransportItem_BC8 readFromNBT(NBTBase nbt) {
        return this;
    }

    @Override
    public NBTBase writeToNBT() {
        return null;
    }

    @Override
    public PipeTransportItem_BC8 readFromByteBuf(ByteBuf buf) {
        return this;
    }

    @Override
    public void writeToByteBuf(ByteBuf buf) {}

    public enum Factory implements IPipeListenerFactory {
        INSTANCE;

        @Override
        public IPipeListener createNewListener(IPipe_BC8 pipe) {
            return new PipeTransportItem_BC8(pipe);
        }
    }
}
