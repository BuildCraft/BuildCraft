/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.gates;

import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.gates.IGateExpansion;
import buildcraft.core.lib.render.BakedModelHolder;
import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.transport.render.GatePluggableRenderer.IGateStaticRenderState;

public abstract class GateExpansionBuildcraft implements IGateExpansion {

    private final String tag;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite sprite;
    @SideOnly(Side.CLIENT)
    private IGateStaticRenderState renderState;

    public GateExpansionBuildcraft(String tag) {
        this.tag = tag;
    }

    @Override
    public String getUniqueIdentifier() {
        return "buildcraft:" + tag;
    }

    @Override
    public String getDisplayName() {
        return BCStringUtils.localize("gate.expansion." + tag);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void textureStitch(TextureMap map) {
        sprite = map.registerSprite(new ResourceLocation("buildcrafttransport:gates/gate_expansion_" + tag));
        if (renderState != null) {
            MinecraftForge.EVENT_BUS.unregister(renderState);
            renderState = null;
        }
    }

    @Override
    public IGateStaticRenderState getRenderState() {
        if (renderState == null) {
            renderState = new RenderState();
        }
        return renderState;
    }

    /** How far away from the model the expansion item layer model should render. Default value is half a pixel
     * (0.5f) */
    public float getPixelExtrusion() {
        return 0.5f;
    }

    @SideOnly(Side.CLIENT)
    private class RenderState extends BakedModelHolder implements IGateStaticRenderState {
        private final ResourceLocation identifier = new ResourceLocation("buildcrafttransport:gate/expansion/identifier");
        private List<BakedQuad> transformedQuads;

        @Override
        public List<BakedQuad> bake(VertexFormat format) {
            if (transformedQuads == null) {
                IBakedModel baked = getModelItemLayer(identifier, sprite);
                List<BakedQuad> quads = baked.getGeneralQuads();
                List<BakedQuad> transformedQuads = Lists.newArrayList();
                Matrix4f translation = new Matrix4f();
                translation.setIdentity();
                translation.setTranslation(new Vector3f((2 - getPixelExtrusion()) / 16f, 0, 0));

                for (BakedQuad quad : quads) {
                    quad = transform(quad, translation);
                    quad = replaceTint(quad, 0xFFFFFF);
                    transformedQuads.add(quad);
                }
                this.transformedQuads = transformedQuads;
            }
            return transformedQuads;
        }
    }
}
