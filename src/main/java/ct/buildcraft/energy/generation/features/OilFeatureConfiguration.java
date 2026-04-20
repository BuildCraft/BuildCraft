package ct.buildcraft.energy.generation.features;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record OilFeatureConfiguration(List<ResourceLocation> excludedBiomes, List<ExcessiveBiome> excessiveBiomes,
		List<ResourceLocation> surfaceDepositBiomes, double oilWellGenerationRate,
		boolean genOilInEveryVanillaBiomes, boolean genOilInEveryModBiomes, GenSetting genSetting) implements FeatureConfiguration {
	public static final Codec<OilFeatureConfiguration> CODEC = RecordCodecBuilder.create((p_65912_) -> 
	      p_65912_.group(
	    		  Codec.list(ResourceLocation.CODEC).fieldOf("excludedBiomes").forGetter((cfg) -> cfg.excludedBiomes),
	    		  Codec.list(ExcessiveBiome.CODEC).fieldOf("excessiveBiomes").forGetter((cfg) -> cfg.excessiveBiomes),
	    		  Codec.list(ResourceLocation.CODEC).fieldOf("surfaceDepositBiomes").forGetter((cfg) -> cfg.surfaceDepositBiomes),
	    		  Codec.doubleRange(0, 100).fieldOf("oilWellGenerationRate").forGetter((cfg) ->  cfg.oilWellGenerationRate),
	    		  Codec.BOOL.fieldOf("genOilInEveryVanillaBiomes").forGetter((cfg) ->  cfg.genOilInEveryVanillaBiomes),
	    		  Codec.BOOL.fieldOf("genOilInEveryModBiomes").forGetter((cfg) ->  cfg.genOilInEveryModBiomes),
	    		  GenSetting.CODEC.fieldOf("oilStructureSetting").forGetter((cfg) -> cfg.genSetting)
	    		  ).apply(p_65912_, OilFeatureConfiguration::new)
	    );
	
	public record GenSetting(BlockState oilState, boolean enableOilSpouts,
			int smallSpoutMinHeight, int smallSpoutMaxHeight, int largeSpoutMinHeight, int largeSpoutMaxHeight,
			double smallOilGenProb, double mediumOilGenProb, double largeOilGenProb) {
		static final Codec<GenSetting> CODEC = RecordCodecBuilder.create((ins) -> 
			ins.group(
					BlockState.CODEC.fieldOf("genOilState").forGetter((cfg) ->  cfg.oilState),
					Codec.BOOL.fieldOf("enableOilSpouts").forGetter((cfg) ->  cfg.enableOilSpouts),
					Codec.intRange(0, 256).fieldOf("smallSpoutMinHeight").forGetter((cfg) ->  cfg.smallSpoutMinHeight),
					Codec.intRange(0, 256).fieldOf("smallSpoutMaxHeight").forGetter((cfg) ->  cfg.smallSpoutMaxHeight),
					Codec.intRange(0, 256).fieldOf("largeSpoutMinHeight").forGetter((cfg) ->  cfg.largeSpoutMinHeight),
					Codec.intRange(0, 256).fieldOf("largeSpoutMaxHeight").forGetter((cfg) ->  cfg.largeSpoutMaxHeight),
					Codec.doubleRange(0, 100).fieldOf("smallOilGenProb").forGetter((cfg) ->  cfg.smallOilGenProb),
					Codec.doubleRange(0, 100).fieldOf("mediumOilGenProb").forGetter((cfg) ->  cfg.mediumOilGenProb),
					Codec.doubleRange(0, 100).fieldOf("largeOilGenProb").forGetter((cfg) ->  cfg.largeOilGenProb)
					).apply(ins, GenSetting::new)
		);
	}
	
	public record ExcessiveBiome(ResourceLocation biome, double noiseScale, double noiseThreshold) {
		static final Codec<ExcessiveBiome> CODEC =  RecordCodecBuilder.create((ins) -> 
			ins.group(
					ResourceLocation.CODEC.fieldOf("biome").forGetter((cfg) ->  cfg.biome),
					Codec.doubleRange(0, 1).fieldOf("noiseScale").forGetter((cfg) ->  cfg.noiseScale),
					Codec.doubleRange(0, 1).fieldOf("noiseThreshold").forGetter((cfg) ->  cfg.noiseThreshold)
					).apply(ins, ExcessiveBiome::new)
		);
	}
}