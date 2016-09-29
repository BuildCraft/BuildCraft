package buildcraft.lib.client.model.json;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import buildcraft.lib.client.model.MutableVertex;

public class JsonVertex {
    public Point3f pos;
    public Point2f uv;

    public JsonVertex(MutableVertex vertex) {
        pos = vertex.position();
        uv = vertex.tex();
    }

    public void loadInto(MutableVertex vertex) {
        vertex.positionf(pos.x, pos.y, pos.z);
        vertex.texf(uv.x, uv.y);
    }
}
