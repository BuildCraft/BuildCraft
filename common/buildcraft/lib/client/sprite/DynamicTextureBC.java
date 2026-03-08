/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeRenderTypes;

/** See {@link MapRenderer.MapInstance} */
@OnlyIn(Dist.CLIENT)
public class DynamicTextureBC {
    public final int width, height;
    // private final int[] colorMap;
    private final int widthPow2, heightPow2;
    private final DynamicTexture dynamicTexture;
    private final RenderType renderType;

    // public DynamicTextureBC(int iWidth, int iHeight)
    public DynamicTextureBC(int iWidth, int iHeight, String id) {
        width = iWidth;
        height = iHeight;
//        widthPow2 = MathHelper.smallestEncompassingPowerOfTwo(iWidth);
        widthPow2 = Mth.smallestEncompassingPowerOfTwo(iWidth);
//        heightPow2 = MathHelper.smallestEncompassingPowerOfTwo(iHeight);
        heightPow2 = Mth.smallestEncompassingPowerOfTwo(iHeight);
//        dynamicTexture = new DynamicTexture(widthPow2, heightPow2);
        dynamicTexture = new DynamicTexture(widthPow2, heightPow2, false);
//        colorMap = dynamicTexture.getTextureData();

        // Calen 1.20.1
        ResourceLocation resourcelocation = Minecraft.getInstance().textureManager.register("zone_planner/" + id, this.dynamicTexture);
        this.renderType = createRenderType(resourcelocation);
    }

    public void setColord(int x, int y, double r, double g, double b, double a) {
        int a2 = (int) (a * 255.0F);
        int r2 = (int) (r * 255.0F);
        int g2 = (int) (g * 255.0F);
        int b2 = (int) (b * 255.0F);
        setColor(x, y, a2 << 24 | r2 << 16 | g2 << 8 | b2);
    }

    public void setColori(int x, int y, int r, int g, int b, int a) {
        setColor(x, y, (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255));
    }

    public void setColor(int x, int y, int color, float alpha) {
        int a = (int) (alpha * 255.0F);

        setColor(x, y, a << 24 | (color & 0xFF_FF_FF));
    }

    public void setColor(int x, int y, int color) {
//        colorMap[x + y * widthPow2] = color;
        this.dynamicTexture.getPixels().setPixelRGBA(x, y, color); // ABGR?????????
    }

    public void updateTexture() {
//        TextureUtil.prepareImage(dynamicTexture.getId(), dynamicTexture.getPixels().getWidth(), dynamicTexture.getPixels().getHeight());
        dynamicTexture.upload();
    }

    // Calen 1.18.2: called in DynamicTexture#upload
//    public void bindGlTexture() {
//        GlStateManager.bindTexture(dynamicTexture.getGlTextureId());
//    }

    public void deleteGlTexture() {
        dynamicTexture.releaseId();
    }

    public void draw(int screenX, int screenY, float zLevel) {
        draw(screenX, screenY, zLevel, 0, 0, width, height);
    }

    public float getMaxU() {
        return width / (float) widthPow2;
    }

    public float getMaxV() {
        return height / (float) heightPow2;
    }

    public void draw(int screenX, int screenY, float zLevel, int clipX, int clipY, int clipWidth, int clipHeight) {
        updateTexture();

        float f = 1F / widthPow2;
        float f1 = 1F / heightPow2;
//        Tessellator tessellator = Tessellator.getInstance();
        Tesselator tessellator = Tesselator.getInstance();
//        BufferBuilder bb = tessellator.getBuffer();
        BufferBuilder bb = tessellator.getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, bb.getVertexFormat());
        vertexUV(bb, screenX + 0, screenY + clipHeight, zLevel, (clipX + 0) * f, (clipY + clipHeight) * f1);
        vertexUV(bb, screenX + clipWidth, screenY + clipHeight, zLevel, (clipX + clipWidth) * f, (clipY + clipHeight) * f1);
        vertexUV(bb, screenX + clipWidth, screenY + 0, zLevel, (clipX + clipWidth) * f, (clipY + 0) * f1);
        vertexUV(bb, screenX + 0, screenY + 0, zLevel, (clipX + 0) * f, (clipY + 0) * f1);
//        tessellator.draw();
        tessellator.end();
    }

    private static void vertexUV(BufferBuilder bb, double x, double y, double z, double u, double v) {
//        bb.pos(x, y, z);
        bb.vertex(x, y, z);
//        bb.tex(u, v);
        bb.uv((float) u, (float) v);
        bb.endVertex();
    }

    // Calen 1.20.1
    public RenderType getRenderType() {
        return renderType;
    }

    /** See {@link RenderType#text(ResourceLocation)} in {@link MapRenderer.MapInstance#MapInstance(int, MapItemSavedData)}. */
    private static RenderType createRenderType(ResourceLocation locationIn) {
        RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_TEXT_SHADER)
                .setCullState(RenderStateShard.NO_CULL)
                .setTextureState(new RenderUtil.BCCustomizableTextureState(locationIn, () -> ForgeRenderTypes.enableTextTextureLinearFiltering, () -> false))
                .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .createCompositeState(false);
        return RenderType.create("buildcraft_zone_planner", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true, rendertype$state);
    }
}
