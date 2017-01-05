package buildcraft.lib.client.model.json;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.JsonUtils;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
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
                default:
                case UP:
                    break;
                case DOWN:
                    faceTo = faceTo.getOpposite();
                    break;
                case WEST:
                case EAST:
                case NORTH:
                case SOUTH:

            }
            faceFrom = EnumFacing.UP;
            if (faceTo == EnumFacing.UP) return;
            float ox = (float) origin[0].evaluate();
            float oy = (float) origin[1].evaluate();
            float oz = (float) origin[2].evaluate();

            for (MutableQuad q : quads) {
                for (int i = 0; i < 4; i++) {
                    MutableVertex v = q.getVertex(i);
                    v.position_x -= ox;
                    v.position_y -= oy;
                    v.position_z -= oz;
                    Axis axis = faceTo.getAxis();
                    if (axis == Axis.X) {
                        float ym = faceTo.getFrontOffsetX();
                        float xm = -ym;

                        // rotate around the Z-axis
                        float t = v.position_y * xm;
                        v.position_y = v.position_x * ym;
                        v.position_x = t;

                        t = v.normal_y * xm;
                        v.normal_y = v.normal_x * ym;
                        v.normal_x = t;
                    } else if (axis == Axis.Z) {
                        float zm = faceTo.getFrontOffsetZ();
                        float ym = -zm;

                        // rotate around the X-axis
                        float t = v.position_y * zm;
                        v.position_y = v.position_z * ym;
                        v.position_z = t;

                        t = v.normal_y * zm;
                        v.normal_y = v.normal_z * ym;
                        v.normal_z = t;
                    } else {// axis == Axis.Y && faceTo == DOWN
                        // Invert the entire model
                        v.position_x = -v.position_x;
                        v.position_y = -v.position_y;
                        v.position_z = -v.position_z;

                        v.normal_x = -v.normal_x;
                        v.normal_y = -v.normal_y;
                        v.normal_z = -v.normal_z;
                    }
                    v.position_x += ox;
                    v.position_y += oy;
                    v.position_z += oz;
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
