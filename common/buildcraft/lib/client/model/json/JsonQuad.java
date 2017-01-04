package buildcraft.lib.client.model.json;

import java.util.Arrays;

import javax.vecmath.Vector3f;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.JsonUtil;

public class JsonQuad {
    public boolean shade = false;
    public int tint;
    public String texture;
    public final JsonVertex[] vertices = new JsonVertex[4];
    public EnumFacing face;

    public JsonQuad(JsonObject obj, float[] from, float[] to, EnumFacing face) {
        this.face = face;
        tint = JsonUtils.getInt(obj, "tintindex", 0);
        texture = JsonUtils.getString(obj, "texture");
        int rotation = JsonUtils.getInt(obj, "rotation", 0);
        float[] uv = JsonUtil.getSubAsFloatArray(obj, "uv");
        if (uv.length != 4) {
            throw new JsonSyntaxException("Expected exactly 4 floats, but got " + Arrays.toString(uv));
        }
        UvFaceData uvs = new UvFaceData();
        uvs.uMin = uv[0];
        uvs.vMin = uv[1];
        uvs.uMax = uv[2];
        uvs.vMax = uv[3];
        Vector3f radius = new Vector3f(to[0] - from[0], to[1] - from[1], to[2] - from[2]);
        radius.scale(0.5f);
        Vector3f center = new Vector3f(from);
        center.add(radius);
        MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
        quad.rotateTextureUp(rotation);
        vertices[0] = new JsonVertex(quad.getVertex(0));
        vertices[1] = new JsonVertex(quad.getVertex(1));
        vertices[2] = new JsonVertex(quad.getVertex(2));
        vertices[3] = new JsonVertex(quad.getVertex(3));
    }
    
    private JsonQuad(JsonQuad from, JsonVertex[] vs, EnumFacing face) {
        this.shade = from.shade;
        this.tint = from.tint;
        this.texture = from.texture;
        vertices[0] = vs[0];
        vertices[1] = vs[1];
        vertices[2] = vs[2];
        vertices[3] = vs[3];
        this.face = face;
    }

    public JsonQuad rotate(ModelRotation rot) {
        JsonVertex[] vs = new JsonVertex[4];
        for (int i = 0; i < 4; i++) {
            vs[i] = vertices[i].rotate(rot);
        }
        EnumFacing f = rot.rotate(face);
        return new JsonQuad(this, vs, f);
    }

    public MutableQuad toQuad(TextureAtlasSprite sprite) {
        MutableQuad quad = new MutableQuad(tint, face, shade);
        vertices[0].loadInto(quad.getVertex(0));
        vertices[1].loadInto(quad.getVertex(1));
        vertices[2].loadInto(quad.getVertex(2));
        vertices[3].loadInto(quad.getVertex(3));
        if (sprite != null) {
            quad.texFromSprite(sprite);
            quad.setSprite(sprite);
        }
        return quad;
    }
}
