package buildcraft.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.DetatchedRenderer;
import buildcraft.lib.client.render.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;

public enum LibEventDistributor {
    INSTANCE;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void textureStitchPre(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        SpriteHolderRegistry.onTextureStitchPre(map);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void textureStitchPost(TextureStitchEvent.Post event) {
        TextureMap map = event.getMap();

    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void modelBake(ModelBakeEvent event) {
        LaserRenderer_BC8.clearModels();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void renderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (player == null) return;
        float partialTicks = event.getPartialTicks();

        DetatchedRenderer.INSTANCE.renderWorldLastEvent(player, partialTicks);
    }
}
