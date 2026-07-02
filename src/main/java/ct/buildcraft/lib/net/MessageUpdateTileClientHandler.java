package ct.buildcraft.lib.net;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.PacketListener;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MessageUpdateTileClientHandler {
    public static Level getClientLevel(PacketListener netHandler) {
        if (netHandler instanceof ClientPacketListener sim) {
            return sim.getLevel();
        }
        return null;
    }
}
