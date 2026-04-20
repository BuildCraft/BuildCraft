package ct.buildcraft.energy;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.energy.generation.features.OilFeatureConfiguration;
import ct.buildcraft.energy.generation.features.OilGenFeature;
import ct.buildcraft.lib.misc.JsonUtil;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BCEnergyWorldGen {
	public static final DeferredRegister<Biome> BIOME_REGISTER = DeferredRegister.create(ForgeRegistries.BIOMES, BCEnergy.MODID);
	public static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(ForgeRegistries.FEATURES, BCEnergy.MODID);
	
	public static final TagKey<Biome> IS_OIL_BIOME = TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(BCEnergy.MODID, "is_oil_biome"));
	
    public static final ResourceKey<Biome> OIL_DESERT_KEY = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(BCEnergy.MODID,"oil_desert"));
    public static final ResourceKey<Biome> OIL_DEEP_OCEAN_KEY = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(BCEnergy.MODID,"oil_deep_ocean"));
    
    public static final Biome OIL_DESERT_BIOME ; 
    public static final Biome OIL_DEEP_OCEAN_BIOME;
    
    public static final OilGenFeature OIL_FEATURE = new OilGenFeature(OilFeatureConfiguration.CODEC);
    
    public static final boolean isTerraBlenderLoaded = ModList.get().isLoaded("terrablender");
    
//    public static Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> OIL_FEN_CON = FeatureUtils.register("buildcraftenergy:desert_oil", OIL_FEATURE);
    
//    public static Holder<PlacedFeature> OIL_PLACED_FEATURE = PlacementUtils.register("buildcraftenergy:desert_oil", OIL_FEN_CON, BiomeFilter.biome(), CountPlacement.of(1), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT);
    
    public static List<Pair<ParameterPoint, ResourceKey<Biome>>> OIL_BIOME_REPLACEMENT;
    
    static{
//    	PlacementUtils.register(null, null, BiomeFilter.biome());
    	Biome desert = OverworldBiomes.desert();
        BiomeGenerationSettings.Builder desert_builder = new BiomeGenerationSettings.Builder();
        BiomeDefaultFeatures.addFossilDecoration(desert_builder);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(desert_builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(desert_builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(desert_builder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(desert_builder);
        BiomeDefaultFeatures.addDefaultSprings(desert_builder);
        BiomeDefaultFeatures.addSurfaceFreezing(desert_builder);
        BiomeDefaultFeatures.addDefaultOres(desert_builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(desert_builder);
        BiomeDefaultFeatures.addDefaultFlowers(desert_builder);
        BiomeDefaultFeatures.addDefaultGrass(desert_builder);
        BiomeDefaultFeatures.addDesertVegetation(desert_builder);
        BiomeDefaultFeatures.addDefaultMushrooms(desert_builder);
        BiomeDefaultFeatures.addDesertExtraVegetation(desert_builder);
        BiomeDefaultFeatures.addDesertExtraDecoration(desert_builder);
        MobSpawnSettings.Builder mobspawnsettings$builder0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.desertSpawns(mobspawnsettings$builder0);
 //       desert_builder.addFeature(Decoration.SURFACE_STRUCTURES, OIL_PLACED_FEATURE);
        OIL_DESERT_BIOME = new Biome.BiomeBuilder().precipitation(Precipitation.NONE).temperature(2.0f).downfall(0.0f).generationSettings(desert_builder.build())
        		.mobSpawnSettings(mobspawnsettings$builder0.build())
        		.specialEffects(desert.getModifiedSpecialEffects())
        		.build();
        
    	Biome deep_ocean = OverworldBiomes.ocean(true);
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.mooshroomSpawns(mobspawnsettings$builder);
        BiomeGenerationSettings.Builder deep_ocean_builder = new BiomeGenerationSettings.Builder();
        BiomeDefaultFeatures.addDefaultCarversAndLakes(deep_ocean_builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(deep_ocean_builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(deep_ocean_builder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(deep_ocean_builder);
        BiomeDefaultFeatures.addDefaultSprings(deep_ocean_builder);
        BiomeDefaultFeatures.addSurfaceFreezing(deep_ocean_builder);
        BiomeDefaultFeatures.addDefaultOres(deep_ocean_builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(deep_ocean_builder);
        BiomeDefaultFeatures.addMushroomFieldVegetation(deep_ocean_builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(deep_ocean_builder);
 //       deep_ocean_builder.addFeature(Decoration.SURFACE_STRUCTURES, OIL_PLACED_FEATURE);
        OIL_DEEP_OCEAN_BIOME = new Biome.BiomeBuilder().precipitation(Precipitation.RAIN).temperature(0.5f).downfall(0.5f).generationSettings(deep_ocean_builder.build())
        		.mobSpawnSettings(mobspawnsettings$builder.build())
        		.specialEffects(deep_ocean.getModifiedSpecialEffects())
        		.build();
    }
    
    
    
    public static void preInit(IEventBus modEventBus) {
    	BIOME_REGISTER.register("oil_desert", () -> OIL_DESERT_BIOME);
    	BIOME_REGISTER.register("oil_deep_ocean", () -> OIL_DEEP_OCEAN_BIOME);
    	FEATURE_REGISTER.register("worldgen.feature.oil", () -> OIL_FEATURE);
    	FEATURE_REGISTER.register(modEventBus);
    	BIOME_REGISTER.register(modEventBus);
    	getReplaceMentFromOverworldBiomeBuilder();
    }
    
    public static void init() {
//		BiomeManager.addAdditionalOverworldBiomes(OIL_DEEP_OCEAN_KEY);
//		BiomeManager.addAdditionalOverworldBiomes(OIL_DESERT_KEY);
		BiomeManager.addBiome(BiomeType.DESERT, new BiomeEntry(OIL_DESERT_KEY, 10));//USELESS
		BiomeManager.addBiome(BiomeType.DESERT_LEGACY, new BiomeEntry(OIL_DESERT_KEY, 10));//USELESS
		
	}
    
    public static void registryFeature() {
//    	 OIL_FEN_CON = FeatureUtils.register("buildcraftenergy:desert_oil", OIL_FEATURE);
//    	 OIL_PLACED_FEATURE = PlacementUtils.register("buildcraftenergy:desert_oil", OIL_FEN_CON, BiomeFilter.biome());
    	    
    }
    
    private static void getReplaceMentFromOverworldBiomeBuilder() {
        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> list = new ArrayList<Pair<Climate.ParameterPoint, ResourceKey<Biome>>>();
        OIL_BIOME_REPLACEMENT = new ArrayList<>();
		Constructor<?> constructor = OverworldBiomeBuilder.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> con = list::add;
		for(Method method : OverworldBiomeBuilder.class.getDeclaredMethods()) {
			method.setAccessible(true);
			if(method.getName().equals("addBiomes")) {//TODO
				try {
					OverworldBiomeBuilder builder = (OverworldBiomeBuilder) constructor.newInstance();
					method.invoke(builder, con);
					BCLog.logger.debug("BCEnergyBiomes:invoke:successed");
					for (var pair : list) {
						ResourceKey<Biome> replacement = null;
						if (pair.getSecond().equals(Biomes.DESERT) && pair.getFirst().weirdness().max() > 0) {
							replacement = OIL_DESERT_KEY;
						} else if (pair.getSecond().location().getPath().contains("ocean")) {
							replacement = OIL_DEEP_OCEAN_KEY;
						}
						if (replacement != null) {
							OIL_BIOME_REPLACEMENT.add(Pair.of(pair.getFirst(), replacement));
						}
					}
				} catch (Exception e) {
					BCLog.logger.debug("BCEnergyBiomes:invoke:fail to get BiomeReplaceMent");
					//e.printStackTrace();
				} 
			} 
		}
    }
    
    private static boolean saveBiomeReplaceMent() {
    	Map<String, ResourceKey<Biome>> map = new HashMap<>();
    	OIL_BIOME_REPLACEMENT.stream().forEach((a) -> map.put(a.getFirst().toString(), a.getSecond()));
    	try {
			FileWriter output = new FileWriter("BiomeReplaceMent.json");
			Gson g = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			g.toJson(JsonUtil.BIOME_MAP_CODEC.encodeStart(JsonOps.INSTANCE, map).getOrThrow(false, BCLog.logger::error), output);
			output.close();
		} catch (Exception e) {
			BCLog.logger.debug("BCEnergyBiomes:save:fail to open BiomeReplaceMent.json");
			e.printStackTrace();
			return false;
		}
    	return true;

    }
    
    private static boolean loadBiomeReplaceMent() {
    	Map<String, ResourceKey<Biome>> map = new HashMap<>();
    	try {
			FileReader input = new FileReader("BiomeReplaceMent.json");
			Gson g = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
//			BCLog.logger.debug(JsonUtil.BIOME_MAP_CODEC.encodeStart(JsonOps.INSTANCE, map).getOrThrow(false, BCLog.logger::error).toString());
//			g.toJson(JsonUtil.BIOME_MAP_CODEC.encodeStart(JsonOps.INSTANCE, map).getOrThrow(false, BCLog.logger::error).toString(), output);
			map = JsonUtil.BIOME_MAP_CODEC.parse(JsonOps.INSTANCE, g.fromJson(input, JsonElement.class)).getOrThrow(false, BCLog.logger::error);
//			g.(JsonUtil.BIOME_MAP_CODEC.encodeStart(JsonOps.INSTANCE, map).getOrThrow(false, BCLog.logger::error), output);
			input.close();
			loadMapIntoList(map);
		} catch (Exception e) {
			BCLog.logger.debug("BCEnergyBiomes:save:fail to open BiomeReplaceMent.json");
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    private static void loadMapIntoList(Map<String, ResourceKey<Biome>> map) {
    	//ParameterPoint[temperature=[5500-10000], humidity=[-10000--3500], continentalness=[-1900-10000], erosion=[500-4500], depth=0, weirdness=[9333-10000], offset=0]";
    	Pattern sp = Pattern.compile("(?<=\\d-)");
    	Pattern tem = Pattern.compile("temperature\\=\\[(.*?)\\]");
    	Pattern hum = Pattern.compile("humidity\\=\\[(.*?)\\]");
    	Pattern con = Pattern.compile("continentalness\\=\\[(.*?)\\]");
    	Pattern ero = Pattern.compile("erosion\\=\\[(.*?)\\]");
    	Pattern dep = Pattern.compile("depth\\=(\\d+)");
    	Pattern wei = Pattern.compile("weirdness\\=\\[(.*?)\\]");
    	Pattern off = Pattern.compile("offset\\=(\\d+)");
    	
    	OIL_BIOME_REPLACEMENT = (map.entrySet().stream().map((a) -> {
    		String s = a.getKey();
    		Matcher offm = off.matcher(s);
    		offm.find();
    		ParameterPoint p = new ParameterPoint(getPara(s, tem), getPara(s, hum), getPara(s, con), getPara(s, ero),
    				getPara(s, dep), getPara(s, wei), Long.valueOf(offm.group(1)));
    		return Pair.of(p, a.getValue());
    	}).toList());
    }
    
    private static Climate.Parameter getPara(String s, Pattern type){
    	Matcher v = type.matcher(s);
    	if(!v.find()) {
    		BCLog.logger.error("BCEnergyBiome:Cannot match BiomeReplaceMent.json element, please Check you File");
    	}
    	String targe = v.group(1);
    	int index = targe.indexOf("-", 1);
    	if(index == -1) 
    		return Climate.Parameter.point(Long.valueOf(targe)/10000f);
    	var k = Climate.Parameter.span(Long.valueOf(targe.substring(0, index))/10000F, Long.valueOf(targe.substring(index+1))/10000F);
    	return k;
    }
    

/*        super(new BiomeProperties("Desert Oil Field").setBaseHeight(0.125F).setHeightVariation(0.05F)
            .setTemperature(2.0F).setRainfall(0.0F).setRainDisabled());
        setRegistryName();
        SurfaceRules*/
    
}
