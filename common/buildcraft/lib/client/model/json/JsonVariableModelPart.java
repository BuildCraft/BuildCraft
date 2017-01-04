package buildcraft.lib.client.model.json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.vecmath.Vector3f;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.misc.JsonUtil;

/** {@link JsonModelPart} but with can be animated */
public abstract class JsonVariableModelPart {

    public abstract void addQuads(List<MutableQuad> to, Function<String, TextureAtlasSprite> spriteLookup);

    public static JsonVariableModelPart deserialiseModelPart(JsonElement json, FunctionContext fnCtx, ResourceLoaderContext ctx) {
        if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected an object, got " + json);
        }
        JsonObject obj = json.getAsJsonObject();
        String type = "cuboid";
        if (obj.has("type")) {
            JsonElement jType = obj.get("type");
            if (jType.isJsonPrimitive()) {
                JsonPrimitive prim = jType.getAsJsonPrimitive();
                type = prim.getAsString();
            } else {
                throw new JsonSyntaxException("Expected a string, got " + jType);
            }
        }
        if ("face".equals(type)) {
            throw new AbstractMethodError("Implement this!");
        } else {
            return new TypeCuboid(obj, fnCtx);
        }
    }

    public static INodeDouble convertStringToDoubleNode(String expression, FunctionContext context) {
        try {
            return GenericExpressionCompiler.compileExpressionDouble(expression, context);
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Invalid expression", e);
        }
    }

    public static INodeString convertStringToStringNode(String expression, FunctionContext context) {
        try {
            return GenericExpressionCompiler.compileExpressionString(expression, context);
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Invalid expression", e);
        }
    }

    public static INodeBoolean convertStringToBooleanNode(String expression, FunctionContext context) {
        try {
            return GenericExpressionCompiler.compileExpressionBoolean(expression, context);
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Invalid expression", e);
        }
    }

    private static INodeLong convertStringToLongNode(String expression, FunctionContext context) {
        try {
            return GenericExpressionCompiler.compileExpressionLong(expression, context);
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Invalid expression", e);
        }
    }

    // private static JsonVariableQuad[] readFace(JsonObject obj, FunctionContext fnCtx) {
    // throw new AbstractMethodError("Implement this!");
    // }

    public static INodeDouble[] readVariablePosition(JsonObject obj, String member, FunctionContext fnCtx) {
        String[] got = JsonUtil.getSubAsStringArray(obj, member);
        INodeDouble[] to = new INodeDouble[3];
        if (got.length != 3) {
            throw new JsonSyntaxException("Expected exactly 3 floats, but got " + Arrays.toString(got));
        } else {
            to[0] = convertStringToDoubleNode(got[0], fnCtx);
            to[1] = convertStringToDoubleNode(got[1], fnCtx);
            to[2] = convertStringToDoubleNode(got[2], fnCtx);
        }
        return to;
    }

    public static INodeBoolean readVariableBoolean(JsonObject obj, String member, FunctionContext context) {
        if (!obj.has(member)) {
            throw new JsonSyntaxException("Required '" + member + "' in '" + obj + "'");
        }
        JsonElement elem = obj.get(member);
        if (elem.isJsonPrimitive()) {
            return convertStringToBooleanNode(elem.getAsString(), context);
        } else {
            throw new JsonSyntaxException("Expected a string, got " + elem);
        }
    }

    public static INodeLong readVariableLong(JsonObject obj, String member, FunctionContext context) {
        if (!obj.has(member)) {
            throw new JsonSyntaxException("Required '" + member + "' in '" + obj + "'");
        }
        JsonElement elem = obj.get(member);
        if (elem.isJsonPrimitive()) {
            return convertStringToLongNode(elem.getAsString(), context);
        } else {
            throw new JsonSyntaxException("Expected a string, got " + elem);
        }
    }

    private static class TypeCuboid extends JsonVariableModelPart {
        private final INodeDouble[] from, to;
        private final INodeBoolean visible, shade;
        private final INodeLong light;
        private final Map<EnumFacing, JsonVariableFaceUV> faces = new HashMap<>();

        private TypeCuboid(JsonObject obj, FunctionContext fnCtx) {
            from = readVariablePosition(obj, "from", fnCtx);
            to = readVariablePosition(obj, "to", fnCtx);
            shade = obj.has("shade") ? readVariableBoolean(obj, "shade", fnCtx) : NodeConstantBoolean.TRUE;
            visible = obj.has("visible") ? readVariableBoolean(obj, "visible", fnCtx) : NodeConstantBoolean.TRUE;
            light = obj.has("light") ? readVariableLong(obj, "light", fnCtx) : new NodeConstantLong(0);

            if (!obj.has("faces")) {
                throw new JsonSyntaxException("Expected between 1 and 6 faces, got nothing");
            }
            JsonElement elem = obj.get("faces");
            if (!elem.isJsonObject()) {
                throw new JsonSyntaxException("Expected between 1 and 6 faces, got '" + elem + "'");
            }
            JsonObject jFaces = elem.getAsJsonObject();
            for (EnumFacing face : EnumFacing.VALUES) {
                if (jFaces.has(face.getName())) {
                    JsonElement jFace = jFaces.get(face.getName());
                    if (!jFace.isJsonObject()) {
                        throw new JsonSyntaxException("Expected an object, but got " + jFace);
                    }
                    faces.put(face, new JsonVariableFaceUV(jFace.getAsJsonObject(), fnCtx));
                }
            }
            if (faces.size() == 0) {
                throw new JsonSyntaxException("Expected between 1 and 6 faces, got an empty object " + jFaces);
            }
        }

        @Override
        public void addQuads(List<MutableQuad> addTo, Function<String, TextureAtlasSprite> spriteLookup) {
            if (visible.evaluate()) {
                float[] f = bakePosition(from);
                float[] t = bakePosition(to);
                boolean s = shade.evaluate();
                int l = (int) (light.evaluate() & 15);
                for (Entry<EnumFacing, JsonVariableFaceUV> entry : faces.entrySet()) {
                    EnumFacing face = entry.getKey();
                    JsonVariableFaceUV var = entry.getValue();
                    if (var.visible.evaluate()) {
                        TextureAtlasSprite sprite = spriteLookup.apply(var.texture.evaluate());
                        UvFaceData uvs = new UvFaceData();
                        uvs.uMin = (float) var.uv[0].evaluate();
                        uvs.uMax = (float) var.uv[1].evaluate();
                        uvs.vMin = (float) var.uv[2].evaluate();
                        uvs.vMax = (float) var.uv[3].evaluate();
                        Vector3f radius = new Vector3f(t[0] - f[0], t[1] - f[1], t[2] - f[2]);
                        radius.scale(0.5f);
                        Vector3f center = new Vector3f(f);
                        center.add(radius);
                        MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
                        quad.lighti(l, 0);
                        quad.texFromSprite(sprite);
                        quad.setSprite(sprite);
                        quad.setShade(s);
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
    }
}
