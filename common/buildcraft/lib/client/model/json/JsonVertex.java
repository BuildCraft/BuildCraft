package buildcraft.lib.client.model.json;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import net.minecraft.client.renderer.block.model.ModelRotation;

import buildcraft.lib.client.model.MutableVertex;

public class JsonVertex {
    public Point3f pos;
    public Vector3f normal;
    public Point2f uv;

    public JsonVertex(MutableVertex vertex) {
        pos = vertex.positionvf();
        normal = vertex.normal();
        uv = vertex.tex();
    }

    private JsonVertex(JsonVertex from, ModelRotation rot) {
        pos = new Point3f(from.pos);
        normal = new Vector3f(from.normal);
        // TODO!
//        rot.getMatrix4d()
        uv = new Point2f(from.uv);
    }

    public void loadInto(MutableVertex vertex) {
        vertex.positionv(pos);
        vertex.normalv(normal);
        vertex.texv(uv);
    }

    public JsonVertex rotate(ModelRotation rot) {
        return new JsonVertex(this, rot);
    }
}
