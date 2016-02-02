package buildcraft.transport.render;

import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.core.lib.render.BuildCraftBakedModel;
import buildcraft.core.lib.utils.MatrixUtils;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemFacade.FacadeState;

public class FacadeItemModel extends BuildCraftBakedModel implements ISmartItemModel {
    public static final FacadeItemModel INSTANCE = new FacadeItemModel();

    private final Map<FacadeState, FacadeItemModel> map = Maps.newHashMap();

    public FacadeItemModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        super(quads, particle, format, getBlockTransforms());
    }

    private FacadeItemModel() {
        super(null, null, null);
    }

    @SubscribeEvent
    public void modelBake(ModelBakeEvent event) {
        map.clear();
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public FacadeItemModel handleItemState(ItemStack stack) {
        FacadeState[] states = ItemFacade.getFacadeStates(stack);
        if (states.length == 0) return this;
        if (states.length == 1) {
            if (!map.containsKey(states[0])) {
                map.put(states[0], createFacadeItemModel(states[0]));
            }
            return map.get(states[0]);
        }

        int length = states.length;
        long millis = System.currentTimeMillis() / 2500;

        int index = (int) (millis % length);
        FacadeState state = states[index];
        if (!map.containsKey(state)) {
            map.put(state, createFacadeItemModel(state));
        }
        return map.get(state);
    }

    private FacadeItemModel createFacadeItemModel(FacadeState state) {
        List<BakedQuad> quads = Lists.newArrayList();
        IModel model;
        if (state.hollow) model = FacadePluggableModel.INSTANCE.modelHollow();
        else model = FacadePluggableModel.INSTANCE.modelFilled();

        TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state.state);

        if (sprite == null) {
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }

        List<BakedQuad> bakedQuads = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, singleTextureFunction(sprite)).getGeneralQuads();
        Matrix4f rotation = MatrixUtils.rotateTowardsFace(EnumFacing.EAST);

        Matrix4f translateToItem = new Matrix4f();
        translateToItem.setIdentity();
        translateToItem.setTranslation(new Vector3f(0.4f, 0, 0));

        Matrix4f totalMatrix = new Matrix4f();
        totalMatrix.setIdentity();

        // The last one is applied FIRST
        totalMatrix.mul(rotation);
        totalMatrix.mul(translateToItem);

        for (BakedQuad quad : bakedQuads) {
            quad = transform(quad, totalMatrix);
            quad = replaceShade(quad, 0xFFFFFFFF);
            quads.add(quad);
        }

        return new FacadeItemModel(ImmutableList.copyOf(quads), sprite, DefaultVertexFormats.ITEM);
    }
}
