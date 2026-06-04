// STUB(R.Chen): javax.vecmath.Point3f → org.joml.Vector3f shim.
package javax.vecmath;

public class Point3f extends org.joml.Vector3f {
    public float x, y, z;
    public Point3f() {}
    public Point3f(float x, float y, float z) { super(x, y, z); this.x = x; this.y = y; this.z = z; }
    public void set(float x, float y, float z) { this.x = x; this.y = y; this.z = z; super.set(x, y, z); }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
}
