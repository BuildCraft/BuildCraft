package buildcraft.lib.client.model.json;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.misc.RenderUtil;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.StandaloneModelConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class VariablePartTextureExpand extends JsonVariableModelPart {
    public final INodeDouble[] from;
    public final INodeDouble[] to;
    public final INodeBoolean visible;
    public final INodeBoolean shade;
    public final INodeLong light;
    public final INodeLong colour;
    public final INodeObject<String> face;
    public final JsonVariableFaceUV faceUv;
    private final Set<String> invalidFaceStrings = new HashSet<>();

    public VariablePartTextureExpand(JsonObject obj, FunctionContext fnCtx) {
        from = readVariablePosition(obj, "from", fnCtx);
        to = readVariablePosition(obj, "to", fnCtx);
        shade = obj.has("shade") ? readVariableBoolean(obj, "shade", fnCtx) : NodeConstantBoolean.TRUE;
        visible = obj.has("visible") ? readVariableBoolean(obj, "visible", fnCtx) : NodeConstantBoolean.TRUE;
        light = obj.has("light") ? readVariableLong(obj, "light", fnCtx) : new NodeConstantLong(0);
        colour = obj.has("colour") ? readVariableLong(obj, "colour", fnCtx) : new NodeConstantLong(-1);
        face = readVariableString(obj, "face", fnCtx);
        faceUv = new JsonVariableFaceUV(obj, fnCtx);
    }

    @Override
    public void addQuads(List<MutableQuad> addTo, JsonVariableModel.ITextureGetter spriteLookup) {
        if (visible.evaluate()) {
            float[] f = bakePosition(from);
            float[] t = bakePosition(to);
            float[] size = { t[0] - f[0], t[1] - f[1], t[2], f[2] };
            boolean s = shade.evaluate();
//            int l = (int) (light.evaluate() & 15);
            byte l = (byte) (light.evaluate() & 15);
            int rgba = RenderUtil.swapARGBforABGR((int) colour.evaluate());

            VariablePartCuboidBase.VariableFaceData data = faceUv.evaluate(spriteLookup);
            // TODO: Use the UV data! (only take part of the texture)
//            ItemLayerModel model = new ItemLayerModel(ImmutableList.of(new ResourceLocation(".")));
            ItemLayerModel model = new ItemLayerModel(ImmutableList.of());
//            BakedModel baked = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, (loc) -> data.sprite);
            BakedModel baked = model.bake(
                    StandaloneModelConfiguration.create(new ResourceLocation("")),
                    null,
                    (loc) -> data.sprite.get(),
                    BlockModelRotation.X0_Y0,
                    ItemOverrides.EMPTY,
                    null
            );
            List<BakedQuad> quads = baked.getQuads(null, null, new Random(0));
            for (BakedQuad q : quads) {
                MutableQuad mut = new MutableQuad();
                mut.fromBakedItem(q);
                mut.translated(0, 0, -(7.5 / 16.0));
                mut.scaled(1, 1, 16);
                mut.rotate(Direction.SOUTH, evaluateFace(this.face), 0.5f, 0.5f, 0.5f);
                mut.scalef(size[0], size[1], size[2]);
                mut.translated(f[0], f[1], f[2]);
                mut.setCalculatedNormal();
                mut.setShade(s);
//                mut.lighti(l, 0);
                mut.lightb(l, (byte) 0);
                mut.colouri(rgba);
                mut.setSprite(data.sprite.get());
                addTo.add(mut);
            }
        }
    }

    private Direction evaluateFace(INodeObject<String> node) {
        String s = node.evaluate();
        Direction side = Direction.byName(s);
        if (side == null) {
            if (invalidFaceStrings.add(s)) {
                BCLog.logger.warn("Invalid facing '" + s + "' from expression '" + node + "'");
            }
            return Direction.UP;
        } else {
            return side;
        }
    }
}
