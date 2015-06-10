package buildcraft.robotics.map;

public class MapChunkLoadRequest {
	public final MapWorld world;
	public final int x, z;

	public MapChunkLoadRequest(MapWorld world, int x, int z) {
		this.world = world;
		this.x = x;
		this.z = z;
	}
}
