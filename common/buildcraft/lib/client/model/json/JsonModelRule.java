/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.MathHelper;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.minecraft.ExpressionCompat;
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
            switch (builtin) {
                case "rotate_facing": {
                    fnCtx = new FunctionContext(fnCtx, ExpressionCompat.ENUM_FACING);
                    String from = JsonUtils.getString(obj, "from");
                    INodeObject<EnumFacing> nodeFrom = JsonVariableModelPart.convertStringToObjectNode(from, fnCtx, EnumFacing.class);

                    String to = JsonUtils.getString(obj, "to");
                    INodeObject<EnumFacing> nodeTo = JsonVariableModelPart.convertStringToObjectNode(to, fnCtx, EnumFacing.class);

                    INodeDouble[] origin;
                    if (obj.has("origin")) {
                        origin = JsonVariableModelPart.readVariablePosition(obj, "origin", fnCtx);
                    } else {
                        origin = RuleRotateFacing.DEFAULT_ORIGIN;
                    }

                    return new RuleRotateFacing(nodeWhen, nodeFrom, nodeTo, origin);
                }
                case "rotate": {
                    INodeDouble[] origin;
                    if (obj.has("origin")) {
                        origin = JsonVariableModelPart.readVariablePosition(obj, "origin", fnCtx);
                    } else {
                        origin = RuleRotate.DEFAULT_ORIGIN;
                    }
                    INodeDouble[] angles = JsonVariableModelPart.readVariablePosition(obj, "angle", fnCtx);
                    return new RuleRotate(nodeWhen, origin, angles);
                }
                case "scale": {
                    INodeDouble[] origin;
                    if (obj.has("origin")) {
                        origin = JsonVariableModelPart.readVariablePosition(obj, "origin", fnCtx);
                    } else {
                        origin = RuleRotate.DEFAULT_ORIGIN;
                    }
                    INodeDouble[] scales = JsonVariableModelPart.readVariablePosition(obj, "scale", fnCtx);
                    return new RuleScale(nodeWhen, origin, scales);
                }
                default:
                    throw new JsonSyntaxException("Unknown built in rule type '" + builtin + "'");
            }
        } else {
            throw new JsonSyntaxException("Unknown rule type '" + type + "'");
        }
    }

    public abstract void apply(List<MutableQuad> quads);

    public static class RuleRotateFacing extends JsonModelRule {

        private static final NodeConstantDouble CONST_ORIGIN = new NodeConstantDouble(8);
        public static final INodeDouble[] DEFAULT_ORIGIN = { CONST_ORIGIN, CONST_ORIGIN, CONST_ORIGIN };

        public final INodeObject<EnumFacing> from, to;
        public final INodeDouble[] origin;

        public RuleRotateFacing(INodeBoolean when, INodeObject<EnumFacing> from, INodeObject<EnumFacing> to,
            INodeDouble[] origin) {
            super(when);
            this.from = from;
            this.to = to;
            this.origin = origin;
        }

        @Override
        public void apply(List<MutableQuad> quads) {
            EnumFacing faceFrom = from.evaluate();
            EnumFacing faceTo = to.evaluate();
            if (faceFrom == faceTo) {
                // don't bother rotating: there is nothing to rotate!
                return;
            }
            float ox = (float) origin[0].evaluate() / 16f;
            float oy = (float) origin[1].evaluate() / 16f;
            float oz = (float) origin[2].evaluate() / 16f;
            for (MutableQuad q : quads) {
                q.rotate(faceFrom, faceTo, ox, oy, oz);
            }
        }
    }

    public static class RuleRotate extends JsonModelRule {
        private static final NodeConstantDouble CONST_ORIGIN = new NodeConstantDouble(0.5);
        public static final INodeDouble[] DEFAULT_ORIGIN = { CONST_ORIGIN, CONST_ORIGIN, CONST_ORIGIN };

        public final INodeDouble[] origin, angle;

        public RuleRotate(INodeBoolean when, INodeDouble[] origin, INodeDouble[] angle) {
            super(when);
            this.origin = origin;
            this.angle = angle;
        }

        @Override
        public void apply(List<MutableQuad> quads) {
            float ox = (float) origin[0].evaluate() / 16f;
            float oy = (float) origin[1].evaluate() / 16f;
            float oz = (float) origin[2].evaluate() / 16f;

            float ax = (float) Math.toRadians(angle[0].evaluate());
            float ay = (float) Math.toRadians(angle[1].evaluate());
            float az = (float) Math.toRadians(angle[2].evaluate());

            if (ax == 0 && ay == 0 && az == 0) {
                return;
            }

            float cx = MathHelper.cos(ax);
            float cy = MathHelper.cos(ay);
            float cz = MathHelper.cos(az);

            float sx = MathHelper.sin(ax);
            float sy = MathHelper.sin(ay);
            float sz = MathHelper.sin(az);

            for (MutableQuad q : quads) {
                q.translatef(-ox, -oy, -oz);
                if (cx != 1) q.rotateDirectlyX(cx, sx);
                if (cy != 1) q.rotateDirectlyY(cy, sy);
                if (cz != 1) q.rotateDirectlyZ(cz, sz);
                q.translatef(ox, oy, oz);
            }
        }
    }

    public static class RuleScale extends JsonModelRule {
        private static final NodeConstantDouble CONST_ORIGIN = new NodeConstantDouble(0.5);
        public static final INodeDouble[] DEFAULT_ORIGIN = { CONST_ORIGIN, CONST_ORIGIN, CONST_ORIGIN };

        public final INodeDouble[] origin, scale;

        public RuleScale(INodeBoolean when, INodeDouble[] origin, INodeDouble[] scale) {
            super(when);
            this.origin = origin;
            this.scale = scale;
        }

        @Override
        public void apply(List<MutableQuad> quads) {
            float ox = (float) origin[0].evaluate() / 16f;
            float oy = (float) origin[1].evaluate() / 16f;
            float oz = (float) origin[2].evaluate() / 16f;

            float sx = (float) scale[0].evaluate();
            float sy = (float) scale[1].evaluate();
            float sz = (float) scale[2].evaluate();

            if (sx == 1 && sy == 1 && sz == 1) {
                return;
            }

            for (MutableQuad q : quads) {
                q.translatef(-ox, -oy, -oz);
                q.scalef(sx, sy, sz);
                q.translatef(ox, oy, oz);
            }
        }
    }
}
