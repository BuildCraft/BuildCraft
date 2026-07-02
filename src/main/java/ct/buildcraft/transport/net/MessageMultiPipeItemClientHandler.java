/*
 * Client-only handler extracted from MessageMultiPipeItem for dist-safety.
 */
package ct.buildcraft.transport.net;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.Supplier;

import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pipe.PipeFlow;
import ct.buildcraft.transport.pipe.Pipe;
import ct.buildcraft.transport.pipe.flow.PipeFlowItems;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

@OnlyIn(Dist.CLIENT)
public class MessageMultiPipeItemClientHandler {
    public static void handle(MessageMultiPipeItem message, Supplier<NetworkEvent.Context> ctx) {
    	ctx.get().enqueueWork(() -> {       
    		Level world = Minecraft.getInstance().level;
                if (world == null) {
                    return;
                }
                for (Entry<BlockPos, List<MessageMultiPipeItem.TravellingItemData>> entry : message.items.entrySet()) {
                    BlockPos pos = entry.getKey();
                    BlockEntity tile = world.getBlockEntity(pos);
                    if (tile instanceof IPipeHolder) {
                        IPipe pipe = ((IPipeHolder) tile).getPipe();
                        if (pipe == Pipe.EMPTY) {
                            return;
                        }
                        PipeFlow flow = pipe.getFlow();
                        if (flow instanceof PipeFlowItems) {
                            ((PipeFlowItems) flow).handleClientReceviedItems(entry.getValue());
                        }
                    }
                }
    		});
    	ctx.get().setPacketHandled(true);
        
    }
}
