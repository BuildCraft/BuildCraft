package buildcraft.transport.render;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;

import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.gates.IGateExpansion.IGateStaticRenderState;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.pluggable.*;
import buildcraft.core.lib.render.BakedModelHolder;
import buildcraft.core.lib.utils.MatrixUtils;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.GatePluggable;

public final class GatePluggableRenderer extends BakedModelHolder implements IPipePluggableStaticRenderer, IPipePluggableDynamicRenderer {
    private static final ResourceLocation mainLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/gate_main.obj");
    private static final ResourceLocation materialLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/gate_material.obj");
    public static final GatePluggableRenderer INSTANCE = new GatePluggableRenderer();

    private GatePluggableRenderer() {}

    public IModel modelMain() {
        return getModelOBJ(mainLoc);
    }

    public IModel modelMaterial() {
        return getModelOBJ(materialLoc);
    }

    @Override
    public void renderDynamicPluggable(IPipe pipe, EnumFacing side, PipePluggable pipePluggable, double x, double y, double z) {

    }

    @Override
    public List<BakedQuad> bakeCutout(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        GatePluggable gate = (GatePluggable) pluggable;

        GateState state = new GateState(gate.getMaterial(), gate.getLogic(), gate.isLit, getExtensions(gate));

        List<BakedQuad> quads = Lists.newArrayList();
        List<BakedQuad> bakedQuads = renderGate(state, DefaultVertexFormats.BLOCK);
        Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);
        for (BakedQuad quad : bakedQuads) {
            quad = transform(quad, matrix);
            quad = replaceShade(quad, 0xFFFFFFFF);
            quad = applyDiffuse(quad);
            quads.add(quad);
        }

        return quads;
    }

    private Collection<IGateStaticRenderState> getExtensions(GatePluggable pluggable) {
        IGateExpansion[] expansions = pluggable.expansions;
        Set<IGateStaticRenderState> states = Sets.newHashSet();
        for (IGateExpansion exp : expansions)
            states.add(exp.getRenderState());
        return states;
    }

    public List<BakedQuad> renderGate(GateState gate, VertexFormat format) {
        TextureAtlasSprite logicSprite = gate.on ? gate.logic.getIconLit() : gate.logic.getIconDark();
        TextureAtlasSprite materialSprite = gate.material.getIconBlock();

        IModel main = modelMain();
        IModel material = modelMaterial();

        List<BakedQuad> quads = Lists.newArrayList();
        IFlexibleBakedModel baked = main.bake(ModelRotation.X0_Y0, format, singleTextureFunction(logicSprite));
        for (BakedQuad quad : baked.getGeneralQuads()) {
            quad = replaceShade(quad, 0xFFFFFFFF);
            quads.add(quad);
        }

        if (materialSprite != null) {// Its null for redstone (As we don't render any material for redstoen gates)
            baked = material.bake(ModelRotation.X0_Y0, format, singleTextureFunction(materialSprite));
            for (BakedQuad quad : baked.getGeneralQuads()) {
                quad = replaceShade(quad, 0xFFFFFFFF);
                quads.add(quad);
            }
        }
        for (IGateStaticRenderState ext : gate.extensionStates) {
            quads.addAll(ext.bake(format));
        }

        return quads;
    }

    public static class GateState {
        public final GateMaterial material;
        public final GateLogic logic;
        public final boolean on;
        public final ImmutableSet<IGateStaticRenderState> extensionStates;

        public GateState(GateMaterial material, GateLogic logic, boolean on, Collection<IGateStaticRenderState> extensionStates) {
            this.material = material;
            this.logic = logic;
            this.on = on;
            this.extensionStates = ImmutableSet.copyOf(extensionStates);
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(material).append(logic).append(on).append(extensionStates).build();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            GateState other = (GateState) obj;
            return new EqualsBuilder().append(material, other.material).append(logic, other.logic).append(on, other.on).append(extensionStates,
                    other.extensionStates).build();
        }
    }
}
