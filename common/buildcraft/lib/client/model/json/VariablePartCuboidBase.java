/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.json.JsonVariableModel.ITextureGetter;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.misc.RenderUtil;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Vector3f;
import java.util.List;

public abstract class VariablePartCuboidBase extends JsonVariableModelPart {
    public final INodeDouble[] from;
    public final INodeDouble[] to;
    public final INodeBoolean visible;
    public final INodeBoolean shade;
    public final INodeLong light;
    public final INodeLong colour;

    public VariablePartCuboidBase(JsonObject obj, FunctionContext fnCtx) {
        from = readVariablePosition(obj, "from", fnCtx);
        to = readVariablePosition(obj, "to", fnCtx);
        shade = obj.has("shade") ? readVariableBoolean(obj, "shade", fnCtx) : NodeConstantBoolean.TRUE;
        visible = obj.has("visible") ? readVariableBoolean(obj, "visible", fnCtx) : NodeConstantBoolean.TRUE;
        light = obj.has("light") ? readVariableLong(obj, "light", fnCtx) : new NodeConstantLong(0);
        colour = obj.has("colour") ? readVariableLong(obj, "colour", fnCtx) : new NodeConstantLong(-1);
    }

    @Override
    public void addQuads(List<MutableQuad> addTo, ITextureGetter spriteLookup) {
        if (visible.evaluate()) {
            float[] f = bakePosition(from);
            float[] t = bakePosition(to);
            boolean s = shade.evaluate();
            int l = (int) (light.evaluate() & 15);
            int rgba = RenderUtil.swapARGBforABGR((int) colour.evaluate());
            for (EnumFacing face : EnumFacing.VALUES) {
                VariableFaceData data = getFaceData(face, spriteLookup);
                if (data != null) {
                    Vector3f radius = new Vector3f(t[0] - f[0], t[1] - f[1], t[2] - f[2]);
                    radius.scale(0.5f);
                    Vector3f center = new Vector3f(f);
                    center.add(radius);
                    MutableQuad quad = ModelUtil.createFace(face, center, radius, data.uvs);
                    quad.rotateTextureUp(data.rotations);
                    quad.lighti(l, 0);
                    quad.colouri(rgba);
                    quad.texFromSprite(data.sprite);
                    quad.setSprite(data.sprite);
                    quad.setShade(s);
                    if (data.invertNormal) {
                        quad = quad.copyAndInvertNormal();
                    }
                    addTo.add(quad);
                }
            }
        }
    }

    private static float[] bakePosition(INodeDouble[] in) {
        float x = (float) in[0].evaluate() / 16f;
        float y = (float) in[1].evaluate() / 16f;
        float z = (float) in[2].evaluate() / 16f;
        return new float[] { x, y, z };
    }

    protected abstract VariableFaceData getFaceData(EnumFacing side, ITextureGetter spriteLookup);

    public static class VariableFaceData {
        public UvFaceData uvs = new UvFaceData();
        public TextureAtlasSprite sprite;
        public int rotations = 0;
        public boolean invertNormal = false;
    }
}
