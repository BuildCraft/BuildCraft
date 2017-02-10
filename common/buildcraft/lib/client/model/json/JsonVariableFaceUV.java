package buildcraft.lib.client.model.json;

import java.util.Arrays;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.misc.JsonUtil;

public class JsonVariableFaceUV {
    final INodeDouble[] uv;
    final INodeLong textureRotation;
    final INodeBoolean visible;
    final INodeString texture;

    public JsonVariableFaceUV(JsonObject json, FunctionContext fnCtx) {
        uv = readVariableUV(json, "uv", fnCtx);
        if (json.has("visible")) {
            visible = JsonVariableModelPart.readVariableBoolean(json, "visible", fnCtx);
        } else {
            visible = NodeConstantBoolean.TRUE;
        }
        texture = readVariableString(json, "texture", fnCtx);
        if (json.has("rotation")) {
            textureRotation = JsonVariableModelPart.readVariableLong(json, "rotation", fnCtx);
        } else {
            textureRotation = NodeConstantLong.ZERO;
        }
    }

    private static INodeString readVariableString(JsonObject json, String memeber, FunctionContext fnCtx) {
        if (!json.has(memeber)) {
            throw new JsonSyntaxException("Required member " + memeber + " in '" + json + "'");
        }
        JsonElement elem = json.get(memeber);
        if (!elem.isJsonPrimitive()) {
            throw new JsonSyntaxException("Expected a string, but got '" + json + "'");
        }
        return JsonVariableModelPart.convertStringToStringNode(elem.getAsString(), fnCtx);
    }

    public static INodeDouble[] readVariableUV(JsonObject obj, String member, FunctionContext fnCtx) {
        String[] got = JsonUtil.getSubAsStringArray(obj, member);
        INodeDouble[] to = new INodeDouble[4];
        if (got.length != 4) {
            throw new JsonSyntaxException("Expected exactly 4 doubles, but got " + Arrays.toString(got));
        } else {
            to[0] = JsonVariableModelPart.convertStringToDoubleNode(got[0], fnCtx);
            to[1] = JsonVariableModelPart.convertStringToDoubleNode(got[1], fnCtx);
            to[2] = JsonVariableModelPart.convertStringToDoubleNode(got[2], fnCtx);
            to[3] = JsonVariableModelPart.convertStringToDoubleNode(got[3], fnCtx);
        }
        return to;
    }
}
