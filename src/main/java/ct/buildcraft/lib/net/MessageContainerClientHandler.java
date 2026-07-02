package ct.buildcraft.lib.net;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MessageContainerClientHandler {
    public static AbstractContainerMenu getClientContainerMenu() {
        return Minecraft.getInstance().player.containerMenu;
    }
}
