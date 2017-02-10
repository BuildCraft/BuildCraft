package buildcraft.lib.client.model.json;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.EnumFacing;

import buildcraft.lib.client.model.json.JsonVariableModel.ITextureGetter;
import buildcraft.lib.expression.FunctionContext;

public class VariablePartCuboid extends VariablePartCuboidBase {
    public final Map<EnumFacing, JsonVariableFaceUV> faces = new HashMap<>();

    public VariablePartCuboid(JsonObject obj, FunctionContext fnCtx) {
        super(obj, fnCtx);
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
    protected VariableFaceData getFaceData(EnumFacing side, ITextureGetter spriteLookup) {
        JsonVariableFaceUV var = faces.get(side);
        if (var == null || !var.visible.evaluate()) {
            return null;
        }
        VariableFaceData data = new VariableFaceData();
        data.sprite = spriteLookup.get(var.texture.evaluate());
        data.rotations = (int) var.textureRotation.evaluate();
        data.uvs.uMin = (float) var.uv[0].evaluate();
        data.uvs.vMin = (float) var.uv[1].evaluate();
        data.uvs.uMax = (float) var.uv[2].evaluate();
        data.uvs.vMax = (float) var.uv[3].evaluate();
        return data;
    }
}
