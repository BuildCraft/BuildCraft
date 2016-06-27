package buildcraft.lib.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public abstract class HudRenderer {
    protected abstract void renderImpl(Minecraft mc, EntityPlayerSP player);

    protected abstract boolean shouldRender(Minecraft mc, EntityPlayerSP player);

    protected void setupTransforms() {}

    public static void moveToHeldStack(Minecraft mc, int slot) {
        
    }
    
    public final void render(Minecraft mc, EntityPlayerSP player) {
        if (shouldRender(mc, player)) {
            GL11.glPushMatrix();
            setupTransforms();
            renderImpl(mc, player);
            GL11.glPopMatrix();
        }
    }
}
