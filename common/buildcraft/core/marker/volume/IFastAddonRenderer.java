package buildcraft.core.marker.volume;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.player.EntityPlayer;

public interface IFastAddonRenderer<T extends Addon> {
    void renderAddonFast(T addon, EntityPlayer player, float partialTicks, VertexBuffer vb);

    default IFastAddonRenderer<T> then(IFastAddonRenderer<? super T> after) {
        return (addon, player, partialTicks, vb) -> {
            renderAddonFast(addon, player, partialTicks, vb);
            after.renderAddonFast(addon, player, partialTicks, vb);
        };
    }
}
