package buildcraft.api.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;


/**
 * 
 * @author Aroma1997
 *
 */
public class WorldGen {
	public static List<Integer> biomeBlacklist = new ArrayList<Integer>();
	public static List<Integer> biomeWhitelist = new ArrayList<Integer>();
	/**
	 * Add a Biome to the Blacklist, where Buildcraft does not generate.
	 * @param biomeGenBase the Biome
	 */
	public static void addBiomeToBlacklist(BiomeGenBase biomeGenBase) {
		biomeBlacklist.add(biomeGenBase.biomeID);
	}
	/**
	 * Add a Biome to the Blacklist, where Buildcraft does not generate.
	 * @param biomeGenBase the BiomeID
	 */
	public static void addBiomeToBlacklist(int biomeGenBaseID) {
		biomeBlacklist.add(biomeGenBaseID);
	}
	
	/**
	 * Add a Biome to the Whitelist, where Buildcraft does extremely generate. (like Oil in deserts)
	 * @param biomeGenBase the Biome
	 */
	public static void addBiomeToWhitelist(BiomeGenBase biomeGenBase) {
		biomeWhitelist.add(biomeGenBase.biomeID);
	}
	/**
	 * Add a Biome to the Whitelist, where Buildcraft does extremely generate. (like Oil in deserts)
	 * @param biomeGenBase the BiomeID
	 */
	public static void addBiomeToWhitelist(int biomeGenBaseID) {
		biomeWhitelist.add(biomeGenBaseID);
	}
	
	/**
	 * Check, if the biome is on the blacklist, or not.
	 * @param biomegenbase the Biome
	 * @return if BC should generate in the world
	 */
	public static boolean generateInBiome(BiomeGenBase biomegenbase) {
		int biomeID = biomegenbase.biomeID;
		for (int biomeNumber = 0; biomeNumber < WorldGen.biomeBlacklist.size(); biomeNumber++) {
			if (WorldGen.biomeBlacklist.get(biomeNumber) == biomeID) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Check, if the biome is on the whitelist, or not.
	 * @param biomegenbase the Biome
	 * @return if BC should generate in the world
	 */
	public static boolean isBiomeWhitelisted(BiomeGenBase biomegenbase) {
		int biomeID = biomegenbase.biomeID;
		for (int biomeNumber = 0; biomeNumber < WorldGen.biomeWhitelist.size(); biomeNumber++) {
			if (WorldGen.biomeWhitelist.get(biomeNumber) == biomeID) {
				return true;
			}
		}
		
		return false;
	}

}
