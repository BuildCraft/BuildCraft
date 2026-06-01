// STUB(R.Chen): Forge BiomeDictionary — compile shim. TODO: replace with vanilla biome tags.
package net.minecraftforge.common;

import java.util.Collections;
import java.util.Set;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

public class BiomeDictionary {

    public static class Type {
        public static final Type OCEAN = new Type("OCEAN");
        public static final Type DESERT = new Type("DESERT");
        public static final Type FOREST = new Type("FOREST");
        public static final Type PLAINS = new Type("PLAINS");
        public static final Type MOUNTAIN = new Type("MOUNTAIN");
        public static final Type HOT = new Type("HOT");
        public static final Type COLD = new Type("COLD");
        public static final Type WET = new Type("WET");
        public static final Type DRY = new Type("DRY");
        public static final Type SPARSE = new Type("SPARSE");
        public static final Type DENSE = new Type("DENSE");
        public static final Type WEB = new Type("WEB");
        public static final Type MAGICAL = new Type("MAGICAL");
        public static final Type RARE = new Type("RARE");
        public static final Type MESA = new Type("MESA");
        public static final Type SAVANNA = new Type("SAVANNA");
        public static final Type CONIFEROUS = new Type("CONIFEROUS");
        public static final Type JUNGLE = new Type("JUNGLE");
        public static final Type SANDY = new Type("SANDY");
        public static final Type SNOWY = new Type("SNOWY");
        public static final Type WASTELAND = new Type("WASTELAND");
        public static final Type VOID = new Type("VOID");
        public static final Type MUSHROOM = new Type("MUSHROOM");

        private final String name;
        private Type(String name) { this.name = name; }

        public static Type getType(String name, boolean create) { return new Type(name); }
        public static Type getType(String name) { return new Type(name); }
        public String getName() { return name; }
    }

    public static void addTypes(RegistryEntry<Biome> biome, Type... types) {
        // no-op — replaced by biome tags
    }

    public static boolean hasType(RegistryEntry<Biome> biome, Type type) {
        return false;
    }

    public static Set<Type> getTypes(RegistryEntry<Biome> biome) {
        return Collections.emptySet();
    }
}
