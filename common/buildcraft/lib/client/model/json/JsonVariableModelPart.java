package buildcraft.lib.client.model.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpression.IExpressionBoolean;
import buildcraft.lib.expression.api.IExpression.IExpressionDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.simple.NodeValueBoolean;
import buildcraft.lib.misc.JsonUtil;

/** {@link JsonModelPart} but with can be animated */
public abstract class JsonVariableModelPart {

    public abstract MutableQuad[] getQuads();

    public static JsonVariableModelPart deserialiseModelPart(JsonElement json, FunctionContext fnCtx) {
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

    private static INodeDouble convertStringToDoubleNode(String expression, FunctionContext context) {
        try {
            IExpressionDouble exp = GenericExpressionCompiler.compileExpressionDouble(expression, context);
            return exp.derive(Arguments.NO_ARGS);
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Invalid expression", e);
        }
    }

    private static INodeBoolean convertStringToBooleanNode(String expression, FunctionContext context) {
        try {
            IExpressionBoolean exp = GenericExpressionCompiler.compileExpressionBoolean(expression, context);
            return exp.derive(Arguments.NO_ARGS);
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Invalid expression", e);
        }
    }

    private static JsonVariableQuad[] readFace(JsonObject obj, FunctionContext fnCtx) {
        throw new AbstractMethodError("Implement this!");
    }

    private static INodeDouble[] readVariablePosition(JsonObject obj, String member, FunctionContext fnCtx) {
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

    private static INodeBoolean readVariableBoolean(JsonObject obj, String member, FunctionContext context) {
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

    private static JsonVariableQuad[] readCuboid(JsonObject obj, FunctionContext fnCtx) {
        INodeDouble[] from = readVariablePosition(obj, "from", fnCtx);
        INodeDouble[] to = readVariablePosition(obj, "to", fnCtx);
        boolean shade = JsonUtils.getBoolean(obj, "shade", false);
        // INodeBoolean visible = read

        if (obj.has("faces")) {
            JsonElement faces = obj.get("faces");
            if (faces.isJsonObject()) {
                JsonObject jFaces = faces.getAsJsonObject();
                List<JsonQuad> quads = new ArrayList<>();
                for (EnumFacing face : EnumFacing.VALUES) {
                    if (jFaces.has(face.getName())) {
                        JsonElement jFace = jFaces.get(face.getName());
                        if (!jFace.isJsonObject()) {
                            throw new JsonSyntaxException("Expected an object, but got " + jFace);
                        }
                        JsonQuad q = new JsonQuad(null, null, null, null);// new JsonQuad(jFace.getAsJsonObject(), from,
                                                                          // to, face);
                        q.shade = shade;
                        quads.add(q);
                    }
                }
                if (quads.size() == 0) {
                    throw new JsonSyntaxException("Expected between 1 and 6 faces, got an empty object " + jFaces);
                }
                return quads.toArray(new JsonVariableQuad[quads.size()]);
            } else {
                throw new JsonSyntaxException("Expected between 1 and 6 faces, got " + faces);
            }
        } else {
            throw new JsonSyntaxException("Expected between 1 and 6 faces, got nothing");
        }
    }

    private static class TypeCuboid extends JsonVariableModelPart {
        private final INodeDouble[] from, to;
        private final INodeBoolean visible;
        private final boolean shade;

        private TypeCuboid(JsonObject obj, FunctionContext fnCtx) {
            from = readVariablePosition(obj, "from", fnCtx);
            to = readVariablePosition(obj, "to", fnCtx);
            shade = JsonUtils.getBoolean(obj, "shade", false);
            visible = obj.has("visible") ? readVariableBoolean(obj, "visible", fnCtx) : NodeValueBoolean.TRUE;

            if (obj.has("faces")) {
                JsonElement faces = obj.get("faces");
                if (faces.isJsonObject()) {
                    JsonObject jFaces = faces.getAsJsonObject();
                    List<JsonQuad> quads = new ArrayList<>();
                    for (EnumFacing face : EnumFacing.VALUES) {
                        if (jFaces.has(face.getName())) {
                            JsonElement jFace = jFaces.get(face.getName());
                            if (!jFace.isJsonObject()) {
                                throw new JsonSyntaxException("Expected an object, but got " + jFace);
                            }
                            JsonQuad q = new JsonQuad(null, null, null, null);// new JsonQuad(jFace.getAsJsonObject(),
                                                                              // from,
                                                                              // to, face);
                            q.shade = shade;
                            quads.add(q);
                        }
                    }
                    if (quads.size() == 0) {
                        throw new JsonSyntaxException("Expected between 1 and 6 faces, got an empty object " + jFaces);
                    }
                    return quads.toArray(new JsonVariableQuad[quads.size()]);
                } else {
                    throw new JsonSyntaxException("Expected between 1 and 6 faces, got " + faces);
                }
            } else {
                throw new JsonSyntaxException("Expected between 1 and 6 faces, got nothing");
            }
        }

        @Override
        public MutableQuad[] getQuads() {
            return null;
        }
    }
}
