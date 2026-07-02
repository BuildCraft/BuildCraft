package ct.buildcraft.api.robots;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IRobotOverlayItem {
    boolean isValidRobotOverlay(ItemStack stack);

    @OnlyIn(Dist.CLIENT)
    void renderRobotOverlay(ItemStack stack, TextureManager textureManager);
}
