/*
 * Client-only handler extracted from MessageVolumeBoxes so the dedicated server
 * never verifies/loads client classes (RuntimeDistCleaner dist-safety).
 */
package ct.buildcraft.core.marker.volume;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

@OnlyIn(Dist.CLIENT)
public class MessageVolumeBoxesClientHandler {
    public static void handle(MessageVolumeBoxes message, Supplier<NetworkEvent.Context> ctx) {
        Map<FriendlyByteBuf, VolumeBox> volumeBoxes = message.buffers.stream()
            .map(buffer -> {
                VolumeBox volumeBox;
                try {
                	Minecraft mc = Minecraft.getInstance();
                    volumeBox = new VolumeBox(mc.level, buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                volumeBox.toBytes(buf);
                return Pair.of(buf, volumeBox);
            })
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        ClientVolumeBoxes.INSTANCE.volumeBoxes.removeIf(volumeBox -> !volumeBoxes.values().contains(volumeBox));
        for (Map.Entry<FriendlyByteBuf, VolumeBox> entry : volumeBoxes.entrySet()) {
            boolean wasContained = false;
            for (VolumeBox clientVolumeBox : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
                if (clientVolumeBox.equals(entry.getValue())) {
                    try {
                        clientVolumeBox.fromBytes(entry.getKey());
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                    wasContained = true;
                    break;
                }
            }
            if (!wasContained) {
                ClientVolumeBoxes.INSTANCE.volumeBoxes.add(entry.getValue());
                for (Addon addon : entry.getValue().addons.values()) {
                    if (addon != null) {
                        addon.onAdded();
                    }
                }
            }
        }
        ctx.get().setPacketHandled(true);
    
    }
}
