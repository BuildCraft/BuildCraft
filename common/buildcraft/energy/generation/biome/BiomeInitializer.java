/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation.biome;

public class BiomeInitializer {
//    @SubscribeEvent(priority = EventPriority.LOW)
////    public void initBiomeGens(WorldTypeEvent.InitBiomeGens event)
//    public void initBiomeGens(ServerAboutToStartEvent event) {
//
////        boolean oilOcean = BCEnergyConfig.enableOilOceanBiome && GenLayerAddOilOcean.getOilBiomeId() != null;
////        boolean oilDesert = BCEnergyConfig.enableOilDesertBiome && GenLayerAddOilDesert.getOilBiomeId() != null;
//        boolean oilOcean = BCEnergyConfig.enableOilOceanBiome && GenLayerAddOilOcean.getOilBiomeId() != null;
//        boolean oilDesert = BCEnergyConfig.enableOilDesertBiome && GenLayerAddOilDesert.getOilBiomeId() != null;
//
//        if (!oilOcean && !oilDesert) {
//            // The biomes aren't registered, so don't bother creating a new array.
//            return;
//        }
//
//        MinecraftServer server = event.getServer();
//        WorldGenSettings settings = server.getWorldData().worldGenSettings();
//
//        if (oilOcean) {
//            BCEnergyBiomeSource.reg(new GenLayerAddOilOcean(settings.seed(), 1500L));
//        }
//        if (oilDesert) {
//            BCEnergyBiomeSource.reg(new GenLayerAddOilDesert(settings.seed(), 1500L));
//        }
//
//        settings.dimensions().entrySet().stream().filter(entry -> entry.getKey() == LevelStem.OVERWORLD).forEach(entry ->
//        {
//            ChunkGenerator chunkGenerator = entry.getValue().generator();
//            BiomeSource source = chunkGenerator.getBiomeSource();
//            if (source instanceof MultiNoiseBiomeSource multiNoise) {
//                List<Pair<ParameterPoint, Holder<Biome>>> parameterList = new LinkedList<>(multiNoise.parameters.values());
//                BCEnergyBiomeSource.oilBiomes.forEach(replacer ->
//                        {
//                            multiNoise.possibleBiomes().add(replacer.getHolder());
//
//                            parameterList.add(
//                                    Pair.of(
//                                            replacer.getParameterPoint(),
//                                            replacer.getHolder()
//                                    )
//                            );
//                        }
//                );
//                multiNoise.parameters = new ParameterList<>(parameterList);
//                if (chunkGenerator.runtimeBiomeSource instanceof MultiNoiseBiomeSource multi1) {
//                    multi1.parameters = new ParameterList<>(parameterList);
//                }
////                if(chunkGenerator instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator){
////                    noiseBasedChunkGenerator.surfaceSystem.
////                }
//
////                BCEnergyBiomeSource bcBiomeSource = new BCEnergyBiomeSource(multiNoise);
////                bcBiomeSource.featuresPerStep = Suppliers.memoize(() -> bcBiomeSource.buildFeaturesPerStep(List.copyOf(bcBiomeSource.possibleBiomes()), true));
////                chunkGenerator.biomeSource = bcBiomeSource;
////                chunkGenerator.runtimeBiomeSource = bcBiomeSource;
//
//                if (BCLib.DEV) {
//                    // dump biome data
//                    Path output = FMLPaths.GAMEDIR.get().resolve("overworld_multinoise_biome_dump.csv");
//                    try {
//                        Files.createDirectories(output.getParent());
//                        BufferedWriter bufferedwriter = Files.newBufferedWriter(output);
//
//                        String line = "biome,";
//                        line += "temperature.min,";
//                        line += "temperature.max,";
//                        line += "humidity.min,";
//                        line += "humidity.max,";
//                        line += "continentalness.min,";
//                        line += "continentalness.max,";
//                        line += "erosion.min,";
//                        line += "erosion.max,";
//                        line += "depth.min,";
//                        line += "depth.max,";
//                        line += "weirdness.min,";
//                        line += "weirdness.max,";
//                        line += "offset";
//                        bufferedwriter.write(line);
//                        bufferedwriter.newLine();
//
//                        multiNoise.parameters.values().forEach(p ->
//                                {
//                                    String line_i = "";
//                                    line_i += p.getSecond().value().getRegistryName() + ",";
//                                    ParameterPoint point = p.getFirst();
//
//                                    line_i += point.temperature().min() + ",";
//                                    line_i += point.temperature().max() + ",";
//
//                                    line_i += point.humidity().min() + ",";
//                                    line_i += point.humidity().max() + ",";
//
//                                    line_i += point.continentalness().min() + ",";
//                                    line_i += point.continentalness().max() + ",";
//
//                                    line_i += point.erosion().min() + ",";
//                                    line_i += point.erosion().max() + ",";
//
//                                    line_i += point.depth().min() + ",";
//                                    line_i += point.depth().max() + ",";
//
//                                    line_i += point.weirdness().min() + ",";
//                                    line_i += point.weirdness().max() + ",";
//
//                                    line_i += point.offset();
//                                    try {
//                                        bufferedwriter.write(line_i);
//                                        bufferedwriter.newLine();
//                                    } catch (IOException e) {
//                                        BCLog.logger.error(e);
//                                    }
//                                }
//                        );
//                        bufferedwriter.flush();
//                        bufferedwriter.close();
//                    } catch (IOException e) {
//                        BCLog.logger.error(e);
//                    }
//                }
//            }
//
//        });
//
////        GenLayer[] newBiomeGens = event.getNewBiomeGens().clone();
////        for (int i = 0; i < newBiomeGens.length; i++) {
////            if (oilOcean) newBiomeGens[i] = new GenLayerAddOilOcean(event.getSeed(), 1500L, newBiomeGens[i]);
////            if (oilDesert) newBiomeGens[i] = new GenLayerAddOilDesert(event.getSeed(), 1500L, newBiomeGens[i]);
////        }
////        event.setNewBiomeGens(newBiomeGens);
//    }
}
