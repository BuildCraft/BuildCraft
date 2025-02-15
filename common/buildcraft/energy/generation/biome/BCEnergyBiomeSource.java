//package buildcraft.energy.generation.biome;
//
//import com.google.common.collect.Maps;
//import com.mojang.datafixers.util.Either;
//import com.mojang.datafixers.util.Pair;
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.DataResult;
//import com.mojang.serialization.MapCodec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import net.minecraft.core.Holder;
//import net.minecraft.core.Registry;
//import net.minecraft.resources.RegistryOps;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.ExtraCodecs;
//import net.minecraft.world.level.biome.Biome;
//import net.minecraft.world.level.biome.BiomeSource;
//import net.minecraft.world.level.biome.Climate;
//import net.minecraft.world.level.biome.Climate.ParameterPoint;
//import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
//
//import java.util.*;
//import java.util.function.Function;
//
//public class BCEnergyBiomeSource extends MultiNoiseBiomeSource {
////    public static final MapCodec<BCEnergyBiomeSource> DIRECT_CODEC = RecordCodecBuilder.mapCodec((p_187070_) -> {
////        return p_187070_.group(ExtraCodecs.<Pair<ParameterPoint, Holder<Biome>>>nonEmptyList(RecordCodecBuilder.<Pair<Climate.ParameterPoint, Holder<Biome>>>create((p_187078_) -> {
////            return p_187078_.group(Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)).apply(p_187078_, Pair::of);
////        }).listOf()).xmap(Climate.ParameterList::new, (Function<Climate.ParameterList<Holder<Biome>>, List<Pair<Climate.ParameterPoint, Holder<Biome>>>>) Climate.ParameterList::values).fieldOf("biomes").forGetter((p_187080_) -> {
////            return p_187080_.parameters;
////        })).apply(p_187070_, BCEnergyBiomeSource::new);
////    });
////    public static final Codec<BCEnergyBiomeSource> CODEC = Codec.mapEither(BCEnergyBiomeSource.PresetInstance.CODEC, DIRECT_CODEC).xmap((p_187068_) ->
////    {
////        return p_187068_.map(BCEnergyBiomeSource.PresetInstance::biomeSource, Function.identity());
////    }, (p_187066_) ->
////    {
////        if (p_187066_.presetMultiNoise().isPresent()) {
////            return p_187066_.presetMultiNoise().map(Either::<MultiNoiseBiomeSource.PresetInstance, BCEnergyBiomeSource>left).orElseGet(() ->
////            {
////                return Either.right(p_187066_);
////            });
////        }else {
////            return p_187066_.presetBC().map(Either::<BCEnergyBiomeSource.PresetInstance, BCEnergyBiomeSource>left).orElseGet(() ->
////            {
////                return Either.right(p_187066_);
////            });
////        }
////    }).codec();
//    public static final List<GenLayerBiomeReplacer> oilBiomes = new LinkedList<>();
//    //    private final Climate.ParameterList<Holder<Biome>> climateParaList;
////    private final Optional<PresetInstance> preset;
//    private final Optional<BCEnergyBiomeSource.PresetInstance> preset;
//    private final Optional<MultiNoiseBiomeSource.PresetInstance> presetMultiNoise;
//
//    private MultiNoiseBiomeSource delegate;
//
//    private BCEnergyBiomeSource(Climate.ParameterList<Holder<Biome>> parameterList) {
//        super(parameterList, Optional.empty());
//        this.preset = Optional.empty();
//        this.presetMultiNoise = Optional.empty();
//    }
//
//    public BCEnergyBiomeSource(Climate.ParameterList<Holder<Biome>> parameterList, Optional<BCEnergyBiomeSource.PresetInstance> p_187060_) {
////        super(parameterList, p_187060_);
//        super(parameterList, Optional.empty());
//        this.preset = p_187060_;
//        this.presetMultiNoise = delegate.preset();
////        this.parameters = parameterList;
//    }
//
//    public BCEnergyBiomeSource(MultiNoiseBiomeSource delegate) {
////        this(delegate.parameters, delegate.preset());
//        super(delegate.parameters, Optional.empty());
////        this.climateParaList = climateParaList;
//        this.preset = Optional.empty();
//        this.presetMultiNoise = delegate.preset();
//        this.delegate = delegate;
//        super.possibleBiomes().addAll(oilBiomes.stream().map(GenLayerBiomeReplacer::getHolder).toList());
//    }
//
////    @Override
////    public void addDebugInfo(List<String> strings, BlockPos pos, Climate.Sampler sampler) {
////        BiomeSource original = this.originalSource;
////        original.addDebugInfo(strings, pos, sampler);
////        if (!(original instanceof ModdedBiomeSource))
////            strings.add("Modded Biome Slice: " + this.getSlice(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getZ())).name());
////    }
//
//    public static void reg(GenLayerBiomeReplacer replacer) {
//        oilBiomes.add(replacer);
//    }
//
//    @Override
//    protected Codec<? extends BiomeSource> codec() {
//        return CODEC;
//    }
//
//    @Override
//    public BiomeSource withSeed(long seed) {
//        return this;
//    }
//
//    @Override
//    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
//        Holder<Biome> old = super.getNoiseBiome(x, y, z, sampler);
//        for (GenLayerBiomeReplacer biome : oilBiomes) {
//            if (biome.doReplace(old, x, y, z)) {
//                return biome.getHolder();
//            }
//        }
//        return old;
//    }
//
//    public Optional<MultiNoiseBiomeSource.PresetInstance> presetMultiNoise() {
//        return this.presetMultiNoise;
//    }
//
//    public Optional<BCEnergyBiomeSource.PresetInstance> presetBC() {
//        return this.preset;
//    }
//
//    public boolean stable(MultiNoiseBiomeSource.Preset p_187064_) {
//        return (this.preset.isPresent() && Objects.equals(this.preset.get().preset(), p_187064_))||(this.presetMultiNoise.isPresent() && Objects.equals(this.presetMultiNoise.get().preset(), p_187064_));
//    }
//
//    public static class Preset extends MultiNoiseBiomeSource.Preset {
//        static final Map<ResourceLocation, BCEnergyBiomeSource.Preset> BY_NAME = Maps.newHashMap();
//        final ResourceLocation name;
//        private final Function<Registry<Biome>, Climate.ParameterList<Holder<Biome>>> parameterSource;
//
//        public Preset(ResourceLocation p_187090_, Function<Registry<Biome>, Climate.ParameterList<Holder<Biome>>> p_187091_) {
//            super(p_187090_, p_187091_);
//            this.name = p_187090_;
//            this.parameterSource = p_187091_;
//            BY_NAME.put(p_187090_, this);
//        }
//
//        BCEnergyBiomeSource biomeSource(BCEnergyBiomeSource.PresetInstance p_187093_, boolean p_187094_) {
//            Climate.ParameterList<Holder<Biome>> parameterlist = this.parameterSource.apply(p_187093_.biomes());
//            return new BCEnergyBiomeSource(parameterlist, p_187094_ ? Optional.of(p_187093_) : Optional.empty());
//        }
//
//        public BCEnergyBiomeSource biomeSource(Registry<Biome> p_187105_, boolean p_187106_) {
//            return this.biomeSource(new BCEnergyBiomeSource.PresetInstance(this, p_187105_), p_187106_);
//        }
//
//        public BCEnergyBiomeSource biomeSource(Registry<Biome> p_187100_) {
//            return this.biomeSource(p_187100_, true);
//        }
//    }
//
//    public static record PresetInstance(BCEnergyBiomeSource.Preset preset, Registry<Biome> biomes) {
//        public static final MapCodec<BCEnergyBiomeSource.PresetInstance> CODEC = RecordCodecBuilder.mapCodec((p_48558_) ->
//        {
//            return p_48558_.group(ResourceLocation.CODEC.flatXmap((p_151869_) ->
//            {
//                return Optional.ofNullable(BCEnergyBiomeSource.Preset.BY_NAME.get(p_151869_)).map(DataResult::success).orElseGet(() ->
//                {
//                    return DataResult.error("Unknown preset: " + p_151869_);
//                });
//            }, (p_151867_) ->
//            {
//                return DataResult.success(p_151867_.name);
//            }).fieldOf("preset").stable().forGetter(BCEnergyBiomeSource.PresetInstance::preset), RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(BCEnergyBiomeSource.PresetInstance::biomes)).apply(p_48558_, p_48558_.stable(BCEnergyBiomeSource.PresetInstance::new));
//        });
//
//        public BCEnergyBiomeSource biomeSource() {
//            return this.preset.biomeSource(this, true);
//        }
//    }
//}
