package ct.buildcraft.lib.net.cache;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NetworkedObjectCacheClientHandler {
    public static boolean isSameThread() {
        return Minecraft.getInstance().isSameThread();
    }
}
