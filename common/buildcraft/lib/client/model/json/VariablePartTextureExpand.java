package buildcraft.lib.client.model.json;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.ItemLayerModel;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.json.JsonVariableModel.ITextureGetter;
import buildcraft.lib.client.model.json.VariablePartCuboidBase.VariableFaceData;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.misc.RenderUtil;

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
    public void addQuads(List<MutableQuad> addTo, ITextureGetter spriteLookup) {
        if (visible.evaluate()) {
            float[] f = bakePosition(from);
            float[] t = bakePosition(to);
            float[] size = { t[0] - f[0], t[1] - f[1], t[2], f[2] };
            boolean s = shade.evaluate();
            int l = (int) (light.evaluate() & 15);
            int rgba = RenderUtil.swapARGBforABGR((int) colour.evaluate());

            VariableFaceData data = faceUv.evaluate(spriteLookup);
            // TODO: Use the UV data! (only take part of the texture)
            ItemLayerModel model = new ItemLayerModel(ImmutableList.of(new ResourceLocation(".")));
            IBakedModel baked = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, (loc) -> data.sprite);
            List<BakedQuad> quads = baked.getQuads(null, null, 0);
            for (BakedQuad q : quads) {
                MutableQuad mut = new MutableQuad();
                mut.fromBakedItem(q);
                mut.translated(0, 0, -(7.5 / 16.0));
                mut.scaled(1, 1, 16);
                mut.rotate(EnumFacing.SOUTH, evaluateFace(this.face), 0.5f, 0.5f, 0.5f);
                mut.scalef(size[0], size[1], size[2]);
                mut.translated(f[0], f[1], f[2]);
                mut.setCalculatedNormal();
                mut.setShade(s);
                mut.lighti(l, 0);
                mut.colouri(rgba);
                mut.setSprite(data.sprite);
                addTo.add(mut);
            }
        }
    }

    private EnumFacing evaluateFace(INodeObject<String> node) {
        String s = node.evaluate();
        EnumFacing side = EnumFacing.byName(s);
        if (side == null) {
            if (invalidFaceStrings.add(s)) {
                BCLog.logger.warn("Invalid facing '" + s + "' from expression '" + node + "'");
            }
            return EnumFacing.UP;
        } else {
            return side;
        }
    }
}
