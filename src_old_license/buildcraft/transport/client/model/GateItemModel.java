package buildcraft.transport.client.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.gates.IGateExpansion;
import buildcraft.core.lib.client.model.BakedModelHolder;
import buildcraft.core.lib.utils.MatrixUtils;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.ItemGate;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class GateItemModel extends BakedModelHolder implements ISmartItemModel {
    public static final GateItemModel INSTANCE = new GateItemModel();

    private final Map<ModelKeyGate, GateItemModel> map = Maps.newHashMap();

    public GateItemModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        super(quads, particle, format, getPluggableTransforms());
    }

    private GateItemModel() {}

    @SubscribeEvent
    public void modelBake(ModelBakeEvent event) {
        map.clear();
    }

    @Override
    public GateItemModel handleItemState(ItemStack stack) {
        ModelKeyGate key = getState(stack);

        if (!map.containsKey(key)) {
            List<BakedQuad> quads = Lists.newArrayList();
            List<MutableQuad> mutableQuads = GatePluggableModel.INSTANCE.renderGate(key, DefaultVertexFormats.ITEM);
            Matrix4f rotation = MatrixUtils.rotateTowardsFace(EnumFacing.SOUTH);

            Matrix4f matScale = new Matrix4f();
            matScale.setIdentity();
            matScale.setScale(2);
            matScale.setTranslation(new Vector3f(-0.5f, -0.5f, -0.5f));

            Matrix4f translateToItem = new Matrix4f();
            translateToItem.setIdentity();
            translateToItem.setTranslation(new Vector3f(0, 0, -0.4f));

            Matrix4f totalMatrix = new Matrix4f();
            totalMatrix.setIdentity();

            totalMatrix.mul(translateToItem);
            totalMatrix.mul(matScale);
            totalMatrix.mul(rotation);

            for (MutableQuad quad : mutableQuads) {
                quad.transform(totalMatrix);
                quad.colouri(0xFF_FF_FF_FF);
                quads.add(quad.toUnpacked(DefaultVertexFormats.ITEM));
            }
            map.put(key, new GateItemModel(ImmutableList.copyOf(quads), null, DefaultVertexFormats.ITEM));
        }
        return map.get(key);
    }

    private ModelKeyGate getState(ItemStack stack) {
        GateMaterial material = ItemGate.getMaterial(stack);
        GateLogic logic = ItemGate.getLogic(stack);
        Set<IGateExpansion> expansions = ItemGate.getInstalledExpansions(stack);
//        Set<IGateStaticRenderState> states = Sets.newHashSet();
//        for (IGateExpansion exp : expansions)
//            states.add(exp.getRenderState());
        return new ModelKeyGate(EnumFacing.UP, material, logic, false, expansions.toArray(new IGateExpansion[0]));
    }
}
