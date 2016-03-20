/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.gates;

import java.util.List;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.gates.GateExpansionModelKey;
import buildcraft.api.gates.IExpansionBaker;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.core.lib.client.model.BakedModelHolder;
import buildcraft.core.lib.client.model.MutableQuad;
import buildcraft.core.lib.utils.BCStringUtils;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public abstract class GateExpansionBuildcraft implements IGateExpansion {

    private final String tag;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite sprite;
    @SideOnly(Side.CLIENT)
    private RenderState renderState;
    @SideOnly(Side.CLIENT)
    protected BCModelKey key;

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

    public RenderState getRenderState() {
        if (renderState == null) {
            renderState = new RenderState();
        }
        return renderState;
    }

    /** How far away from the model the expansion item layer model should render. Default value is 0.02f */
    public float getPixelExtrusion() {
        return 0.03f;
    }

    @Override
    public GateExpansionModelKey<?> getRenderModelKey(EnumWorldBlockLayer layer) {
        if (layer == EnumWorldBlockLayer.CUTOUT) {
            /* Expansions are stored without state (at least all BC ones are) so we only need to use a single
             * identifiing key. */
            if (key == null) key = new BCModelKey(getRenderState());
            return key;
        }
        return null;
    }

    public static class BCModelKey extends GateExpansionModelKey<BCModelKey> {
        public BCModelKey(RenderState state) {
            super(EnumWorldBlockLayer.CUTOUT, state);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
    }

    @SideOnly(Side.CLIENT)
    private class RenderState extends BakedModelHolder implements IExpansionBaker<BCModelKey> {
        private final ResourceLocation identifier = new ResourceLocation("buildcrafttransport:gate/expansion/identifier");
        private ImmutableList<BakedQuad> transformedQuads;

        @Override
        public VertexFormat getVertexFormat() {
            return DefaultVertexFormats.BLOCK;
        }

        @Override
        public ImmutableList<BakedQuad> bake(BCModelKey key) {
            if (transformedQuads == null) {
                IBakedModel baked = getModelItemLayer(identifier, sprite);
                List<BakedQuad> quads = baked.getGeneralQuads();
                List<BakedQuad> transformedQuads = Lists.newArrayList();
                Matrix4f translation = new Matrix4f();
                translation.setIdentity();
                translation.setTranslation(new Vector3f((2 - getPixelExtrusion()) / 16f, 0, 0));

                for (BakedQuad quad : quads) {
                    MutableQuad mutable = MutableQuad.create(quad);
                    mutable.transform(translation);
                    mutable.setTint(0xFF_FF_FF);
                    BCModelHelper.appendBakeQuads(transformedQuads, mutable);
                }
                this.transformedQuads = ImmutableList.copyOf(transformedQuads);
            }
            return transformedQuads;
        }
    }
}
