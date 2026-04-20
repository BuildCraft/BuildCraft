package ct.buildcraft.energy.generation;

import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.energy.BCEnergyWorldGen;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import terrablender.api.ParameterUtils.Continentalness;
import terrablender.api.ParameterUtils.Erosion;
import terrablender.api.ParameterUtils.Weirdness;
import terrablender.api.Region;
import terrablender.api.RegionType;

public class BCOverWorldRegion extends Region {

	public static final ResourceLocation NAME = new ResourceLocation("buildcraft:overworld");

	public BCOverWorldRegion(int weight) {
		super(NAME, RegionType.OVERWORLD, weight);
	}

	@Override
	public void addBiomes(Registry<Biome> registry, Consumer<Pair<ParameterPoint, ResourceKey<Biome>>> mapper) {
		this.addModifiedVanillaOverworldBiomes(mapper, b ->
			BCEnergyWorldGen.OIL_BIOME_REPLACEMENT.forEach((a) -> b.replaceBiome(a.getFirst(), a.getSecond())));
	}

	private void addDesertOil(Registry<Biome> registry, Consumer<Pair<ParameterPoint, ResourceKey<Biome>>> mapper) {
	}

	private void addMidSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> map, Climate.Parameter w) {
		Climate.Parameter tempurature = terrablender.api.ParameterUtils.Temperature.HOT.parameter();
		for (int j = 0; j < terrablender.api.ParameterUtils.Humidity.values().length - 1; ++j) {
			Climate.Parameter climate$parameter1 = terrablender.api.ParameterUtils.Humidity.values()[j].parameter();
			ResourceKey<Biome> resourcekey = BCEnergyWorldGen.OIL_DESERT_KEY;// target
			// ResourceKey<Biome> resourcekey7 = this.pickShatteredCoastBiome(4, j,
			// w);//only j = 4
			this.addSurfaceBiome(map, tempurature, climate$parameter1,Climate.Parameter.span(Continentalness.COAST.parameter(), Continentalness.NEAR_INLAND.parameter()),Erosion.EROSION_3.parameter(), w, 0.0F, resourcekey);
			this.addSurfaceBiome(map, tempurature, climate$parameter1,Climate.Parameter.span(Continentalness.COAST.parameter(), Continentalness.FAR_INLAND.parameter()),Erosion.EROSION_4.parameter(), w, 0.0F, resourcekey);
			this.addSurfaceBiome(map, tempurature, climate$parameter1, Continentalness.COAST.parameter(),Erosion.EROSION_6.parameter(), w, 0.0F, resourcekey);
			if (j >= 4)
				this.addSurfaceBiome(map, tempurature, climate$parameter1, Continentalness.COAST.parameter(),Erosion.EROSION_5.parameter(), w, 0.0F, resourcekey);
		}
	}

	private void addSurfaceBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> p_187181_,
			Climate.Parameter p_187182_, Climate.Parameter p_187183_, Climate.Parameter p_187184_,
			Climate.Parameter p_187185_, Climate.Parameter p_187186_, float p_187187_, ResourceKey<Biome> p_187188_) {
		BCLog.logger.debug("BCOverWorldRegion:registrying for " + Climate.parameters(p_187182_, p_187183_, p_187184_,
				p_187185_, Climate.Parameter.point(0.0F), p_187186_, p_187187_));
		p_187181_.accept(Pair.of(Climate.parameters(p_187182_, p_187183_, p_187184_, p_187185_,
				Climate.Parameter.point(0.0F), p_187186_, p_187187_), p_187188_));
		p_187181_.accept(Pair.of(Climate.parameters(p_187182_, p_187183_, p_187184_, p_187185_,
				Climate.Parameter.point(1.0F), p_187186_, p_187187_), p_187188_));
	}
}
