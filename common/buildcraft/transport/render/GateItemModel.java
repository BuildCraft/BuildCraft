package buildcraft.transport.render;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.ISmartItemModel;

import buildcraft.api.gates.IGateExpansion;
import buildcraft.core.lib.render.BakedModelHolder;
import buildcraft.core.lib.utils.MatrixUtils;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.render.GatePluggableRenderer.GateState;
import buildcraft.transport.render.GatePluggableRenderer.IGateStaticRenderState;

public class GateItemModel extends BakedModelHolder implements ISmartItemModel {
    private static Map<GateState, GateItemModel> map = Maps.newHashMap();

    public GateItemModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        super(quads, particle, format, getPluggableTransforms());
    }

    public GateItemModel() {}

    @Override
    public GateItemModel handleItemState(ItemStack stack) {
        GateState state = getState(stack);

        if (!map.containsKey(state)) {
            List<BakedQuad> quads = Lists.newArrayList();
            List<BakedQuad> bakedQuads = GatePluggableRenderer.INSTANCE.renderGate(state, DefaultVertexFormats.BLOCK);
            Matrix4f matrix = MatrixUtils.rotateTowardsFace(EnumFacing.SOUTH);
            Matrix4f matScale = new Matrix4f();
            matScale.setIdentity();
            matScale.setScale(2);
            matScale.setTranslation(new Vector3f(-0.5f, -0.5f, -0.5f));
            matrix.mul(matScale);
            for (BakedQuad quad : bakedQuads) {
                quad = transform(quad, matrix);
                quad = replaceShade(quad, 0xFFFFFFFF);
                quads.add(quad);
            }
            map.put(state, new GateItemModel(ImmutableList.copyOf(quads), null, DefaultVertexFormats.BLOCK));
        }
        return map.get(state);
    }

    private GateState getState(ItemStack stack) {
        GateMaterial material = ItemGate.getMaterial(stack);
        GateLogic logic = ItemGate.getLogic(stack);
        Set<IGateExpansion> expansions = ItemGate.getInstalledExpansions(stack);
        Set<IGateStaticRenderState> states = Sets.newHashSet();
        for (IGateExpansion exp : expansions)
            states.add(exp.getRenderState());
        return new GateState(material, logic, false, states);
    }
}
