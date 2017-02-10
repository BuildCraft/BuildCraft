package buildcraft.lib.client.model.json;

import com.google.gson.JsonObject;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.ModelLoader;

import buildcraft.lib.client.model.json.JsonVariableModel.ITextureGetter;
import buildcraft.lib.expression.FunctionContext;

public class VariablePartLed extends VariablePartCuboidBase {
    private static final VariableFaceData FACE_DATA = new VariableFaceData();

    static {
        FACE_DATA.sprite = ModelLoader.White.INSTANCE;
        FACE_DATA.uvs.uMin = 1;
        FACE_DATA.uvs.vMin = 2;
        FACE_DATA.uvs.uMax = 1;
        FACE_DATA.uvs.vMax = 2;
    }

    public VariablePartLed(JsonObject obj, FunctionContext fnCtx) {
        super(obj, fnCtx);
    }

    @Override
    protected VariableFaceData getFaceData(EnumFacing side, ITextureGetter spriteLookup) {
        FACE_DATA.uvs.uMin = 1;
        FACE_DATA.uvs.vMin = 2;
        FACE_DATA.uvs.uMax = 1;
        FACE_DATA.uvs.vMax = 2;
        return FACE_DATA;
    }
}
