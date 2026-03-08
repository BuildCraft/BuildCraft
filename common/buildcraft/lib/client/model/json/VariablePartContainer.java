package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.client.model.json.JsonVariableModel.ITextureGetter;
import buildcraft.lib.expression.FunctionContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.Collections;
import java.util.List;

public class VariablePartContainer extends JsonVariableModelPart {

    public final JsonVariableModel model;

    public VariablePartContainer(JsonObject obj, FunctionContext fnCtx, ResourceLoaderContext ctx) {
        if (obj.has("textures")) {
            throw new JsonSyntaxException("Contained variable parts must not have 'textures'");
        }
        if (obj.has("variables")) {
            throw new JsonSyntaxException("Contained variable parts must not have 'variables'");
        }
        if (obj.has("translucent")) {
            throw new JsonSyntaxException("Contained variable parts must not have 'translucent'");
        }
        model = new JsonVariableModel(obj, fnCtx, ctx);
    }

    @Override
    public void addQuads(List<MutableQuad> to, ITextureGetter spriteLookup) {
        Collections.addAll(to, model.bakePart(model.cutoutElements, spriteLookup));
    }
}
