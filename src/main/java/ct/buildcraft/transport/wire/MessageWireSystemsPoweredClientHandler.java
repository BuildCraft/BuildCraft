/*
 * Client-only handler extracted from MessageWireSystemsPowered for dist-safety.
 */
package ct.buildcraft.transport.wire;

import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import ct.buildcraft.api.transport.IWireManager;
import ct.buildcraft.api.transport.pipe.IPipeHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

@OnlyIn(Dist.CLIENT)
public class MessageWireSystemsPoweredClientHandler {
    public static void handle(MessageWireSystemsPowered message, Supplier<NetworkEvent.Context> ctx) {
    	ctx.get().enqueueWork(() -> {    
    		message.hashesPowered.entrySet().stream()
	                .map(hashPowered ->
	                        Pair.of(
	                                ClientWireSystems.INSTANCE.wireSystems.get(hashPowered.getKey()),
	                                hashPowered.getValue()
	                        )
	                )
	                .flatMap(systemPowered ->
	                        systemPowered.getLeft().elements.stream()
	                                .map(element ->
	                                        Pair.of(element, systemPowered.getRight())
	                                )
	                )
	                .forEach(elementPowered -> {
	                    WireSystem.WireElement element = elementPowered.getLeft();
	                    boolean powered = elementPowered.getRight();
	                    if (element.type == WireSystem.WireElement.Type.WIRE_PART) {
	                        BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(element.blockPos);
	                        if (tile instanceof IPipeHolder) {
	                            IPipeHolder holder = (IPipeHolder) tile;
	                            IWireManager iWireManager = holder.getWireManager();
	                            if (iWireManager instanceof WireManager) {
	                                WireManager wireManager = (WireManager) iWireManager;
	                                if (wireManager.getColorOfPart(element.wirePart) != null) {
	                                    if (powered) {
	                                        wireManager.poweredClient.add(element.wirePart);
	                                    } else {
	                                        wireManager.poweredClient.remove(element.wirePart);
	                                    }
	                                }
	                            }
	                        }
	                    }
	                });
	        return;
    	});
    	ctx.get().setPacketHandled(true);
    
    }
}
