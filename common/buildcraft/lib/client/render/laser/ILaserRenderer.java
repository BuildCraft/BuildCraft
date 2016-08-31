package buildcraft.lib.client.render.laser;

public interface ILaserRenderer {
    void vertex(double x, double y, double z, double u, double v, int lmap, float nx, float ny, float nz, float colour);
}
