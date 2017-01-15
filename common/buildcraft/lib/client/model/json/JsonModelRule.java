package buildcraft.lib.client.model.json;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

/** A rule for changing a model's elements. The most basic example is rotating an entire model based of a single
 * property. */
public abstract class JsonModelRule {

    public final INodeBoolean when;

    public JsonModelRule(INodeBoolean when) {
        this.when = when;
    }

    public static JsonModelRule deserialize(JsonElement json, FunctionContext fnCtx, ResourceLoaderContext ctx) {
        if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected an object, got " + json);
        }
        JsonObject obj = json.getAsJsonObject();
        String when = JsonUtils.getString(obj, "when");
        INodeBoolean nodeWhen = JsonVariableModelPart.convertStringToBooleanNode(when, fnCtx);

        String type = JsonUtils.getString(obj, "type");
        if (type.startsWith("builtin:")) {
            String builtin = type.substring("builtin:".length());
            if ("rotate_facing".equals(builtin)) {
                String from = JsonUtils.getString(obj, "from");
                INodeString nodeFrom = JsonVariableModelPart.convertStringToStringNode(from, fnCtx);

                String to = JsonUtils.getString(obj, "to");
                INodeString nodeTo = JsonVariableModelPart.convertStringToStringNode(to, fnCtx);

                INodeDouble[] origin;
                if (obj.has("origin")) {
                    origin = JsonVariableModelPart.readVariablePosition(obj, "origin", fnCtx);
                } else {
                    origin = RuleRotateFacing.DEFAULT_ORIGIN;
                }

                return new RuleRotateFacing(nodeWhen, nodeFrom, nodeTo, origin);
            } else {
                throw new JsonSyntaxException("Unknown built in rule type '" + builtin + "'");
            }
        } else {
            throw new JsonSyntaxException("Unknown rule type '" + type + "'");
        }
    }

    public abstract void apply(List<MutableQuad> quads);

    public static class RuleRotateFacing extends JsonModelRule {

        private static final NodeConstantDouble CONST_ORIGIN = new NodeConstantDouble(0.5);
        public static final INodeDouble[] DEFAULT_ORIGIN = { CONST_ORIGIN, CONST_ORIGIN, CONST_ORIGIN };

        public final INodeString from, to;
        public final INodeDouble[] origin;
        private final Set<String> invalidFaceStrings = new HashSet<>();

        public RuleRotateFacing(INodeBoolean when, INodeString from, INodeString to, INodeDouble[] origin) {
            super(when);
            this.from = from;
            this.to = to;
            this.origin = origin;
        }

        @Override
        public void apply(List<MutableQuad> quads) {
            EnumFacing faceFrom = evaluateFace(from);
            EnumFacing faceTo = evaluateFace(to);
            if (faceFrom == faceTo) {
                // don't bother rotating: there is nothing to rotate!
                return;
            }
            // always rotate from "UP" - it simplifies the math a bit
            switch (faceFrom) {
                case UP:
                    break;
                case DOWN:
                    faceTo = faceTo.getOpposite();
                    break;
                case WEST:
                case EAST:
                case NORTH:
                case SOUTH:
                    throw new IllegalArgumentException("Not yet implemented - rotate from " + faceFrom);
                default:
                    throw new IllegalArgumentException("Unknown EnumFacing " + faceFrom);
            }
            faceFrom = EnumFacing.UP;
            if (faceTo == EnumFacing.UP) return;
            float ox = (float) origin[0].evaluate();
            float oy = (float) origin[1].evaluate();
            float oz = (float) origin[2].evaluate();

            switch (faceTo.getAxis()) {
                case X: {
                    for (MutableQuad q : quads) {
                        q.translatef(-ox, -oy, -oz);
                        q.rotateZ_90(faceTo.getFrontOffsetX());
                        q.translatef(ox, oy, oz);
                    }
                    break;
                }
                case Z: {
                    for (MutableQuad q : quads) {
                        q.translatef(-ox, -oy, -oz);
                        q.rotateX_90(faceTo.getFrontOffsetZ());
                        q.translatef(ox, oy, oz);
                    }
                    break;
                }
                case Y: {
                    for (MutableQuad q : quads) {
                        q.translatef(-ox, -oy, -oz);
                        q.rotateX_180();
                        q.translatef(ox, oy, oz);
                    }
                    break;
                }
                default: {
                    throw new IllegalStateException("Unknown axis " + faceTo.getAxis());
                }
            }
        }

        private EnumFacing evaluateFace(INodeString node) {
            String s = node.evaluate();
            EnumFacing face = EnumFacing.byName(s);
            if (face == null) {
                if (invalidFaceStrings.add(s)) {
                    BCLog.logger.warn("Invalid facing '" + s + "' from expression '" + node + "'");
                }
                return EnumFacing.UP;
            } else {
                return face;
            }
        }
    }
}
