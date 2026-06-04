// STUB(R.Chen): javax.vecmath.Point3d → org.joml.Vector3d shim.
package javax.vecmath;

public class Point3d extends org.joml.Vector3d {
    public double x, y, z;
    public Point3d() {}
    public Point3d(double x, double y, double z) { super(x, y, z); this.x = x; this.y = y; this.z = z; }
}
