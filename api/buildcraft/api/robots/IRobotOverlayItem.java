package buildcraft.api.robots;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IRobotOverlayItem {
    boolean isValidRobotOverlay(ItemStack stack);

    @SideOnly(Side.CLIENT)
    void renderRobotOverlay(ItemStack stack, TextureManager textureManager);
}
