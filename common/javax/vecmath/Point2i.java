// STUB(R.Chen): javax.vecmath.Point2i — compile shim.
package javax.vecmath;

public class Point2i {
    public int x, y;
    public Point2i() {}
    public Point2i(int x, int y) { this.x = x; this.y = y; }
    public void set(int x, int y) { this.x = x; this.y = y; }
    public int getX() { return x; }
    public int getY() { return y; }
    @Override public String toString() { return "(" + x + ", " + y + ")"; }
    @Override public boolean equals(Object o) {
        if (!(o instanceof Point2i)) return false;
        Point2i p = (Point2i)o;
        return x == p.x && y == p.y;
    }
    @Override public int hashCode() { return 31 * x + y; }
}
