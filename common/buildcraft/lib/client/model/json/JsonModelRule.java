/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
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
            float ox = (float) origin[0].evaluate();
            float oy = (float) origin[1].evaluate();
            float oz = (float) origin[2].evaluate();
            for (MutableQuad q : quads) {
                q.rotate(faceFrom, faceTo, ox, oy, oz);
            }
        }
    }
}
