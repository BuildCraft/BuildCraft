// STUB(R.Chen): 1.12 WorldType — removed in 1.20.
package net.minecraft.world;

public class WorldType {
    public static final WorldType DEFAULT = new WorldType("default");
    public static final WorldType FLAT = new WorldType("flat");
    public static final WorldType LARGE_BIOMES = new WorldType("largeBiomes");
    public static final WorldType AMPLIFIED = new WorldType("amplified");

    private final String name;
    public WorldType(String name) { this.name = name; }
    public String getName() { return name; }
    public int getId() { return 0; }
}
