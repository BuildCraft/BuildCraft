// STUB(R.Chen): Minecraft 1.12 TextureManager (mc.renderEngine) — compile shim.
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Identifier;

public class TextureManager {
    public static final TextureManager INSTANCE = new TextureManager();

    public void bindTexture(Identifier location) {
        // TODO(R.Chen): replace with RenderSystem.setShaderTexture(0, location)
        RenderSystem.setShaderTexture(0, location);
    }
}
