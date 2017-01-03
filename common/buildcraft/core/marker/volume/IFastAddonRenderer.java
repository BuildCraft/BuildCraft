package buildcraft.core.marker.volume;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.player.EntityPlayer;

public interface IFastAddonRenderer {
    void renderAddonFast(Addon addon, EntityPlayer player, float partialTicks, VertexBuffer vb);
}
