/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.BCLib;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.JsonUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RulesLoader {
    private static final Gson GSON = JsonUtil.registerNbtSerializersDeserializers(new GsonBuilder())
            .registerTypeAdapter(
                    BlockPos.class,
                    (JsonDeserializer<BlockPos>) (json, typeOfT, context) ->
                            new BlockPos(
                                    json.getAsJsonArray().get(0).getAsInt(),
                                    json.getAsJsonArray().get(1).getAsInt(),
                                    json.getAsJsonArray().get(2).getAsInt()
                            )
            )
            .registerTypeAdapter(RequiredExtractor.class, RequiredExtractor.DESERIALIZER)
            .registerTypeAdapter(EnumNbtCompareOperation.class, EnumNbtCompareOperation.DESERIALIZER)
            .registerTypeAdapter(NbtPath.class, NbtPath.DESERIALIZER)
            .registerTypeAdapterFactory(JsonSelector.TYPE_ADAPTER_FACTORY)
            .registerTypeAdapterFactory(NbtRef.TYPE_ADAPTER_FACTORY)
            .create();

    private static final List<JsonRule> RULES = new ArrayList<>();
    @SuppressWarnings("WeakerAccess")
    public static final Set<String> READ_DOMAINS = new HashSet<>();
    @SuppressWarnings("ConstantConditions")
    private static final LoadingCache<Pair<BlockState, CompoundNBT>, Set<JsonRule>>
            BLOCK_RULES_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(CacheLoader.from(pair -> getBlockRulesInternal(pair.getLeft(), pair.getRight())));

    public static void loadAll() {
        RULES.clear();
        READ_DOMAINS.clear();
//        for (ModContainer modContainer : Loader.instance().getModList())
        ModList.get().forEachModContainer((domain, modContainer) ->
        {
//            String domain = modContainer.getModId();
            if (!READ_DOMAINS.contains(domain)) {
                String base = "assets/" + domain + "/compat/buildcraft/builders/";
                if (modContainer.getMod() == null) {
//                    continue;
                    return;
                }
//                InputStream inputStream = modContainer.getMod().getClass().getClassLoader().getResourceAsStream(
                InputStream inputStream = modContainer.getMod().getClass().getResourceAsStream(
                        base + "index.json"
                );
                if (inputStream != null) {
                    GSON.<List<String>>fromJson(
                                    new InputStreamReader(inputStream, StandardCharsets.UTF_8),
                                    new TypeToken<List<String>>() {
                                    }.getType()
                            ).stream()
                            .map(name -> base + name + ".json")
                            .map(name ->
                            {
                                InputStream resourceAsStream = modContainer.getMod()
                                        .getClass()
                                        .getClassLoader()
                                        .getResourceAsStream(name);
                                if (resourceAsStream == null) {
                                    throw new RuntimeException(new IOException("Can't read " + name));
                                }
                                return resourceAsStream;
                            })
                            .flatMap(localInputStream ->
                                    GSON.<List<JsonRule>>fromJson(
                                            new InputStreamReader(localInputStream),
                                            new TypeToken<List<JsonRule>>() {
                                            }.getType()
                                    ).stream()
                            )
                            .forEach(RULES::add);
                    READ_DOMAINS.add(domain);
                }
            }
        });
        READ_DOMAINS.add("minecraft");
        READ_DOMAINS.add("buildcraftcore");
        READ_DOMAINS.add("buildcraftlib");
        READ_DOMAINS.add("buildcraftbuilders");
        READ_DOMAINS.add("buildcraftenergy");
        READ_DOMAINS.add("buildcraftfactory");
        READ_DOMAINS.add("buildcraftrobotics");
        READ_DOMAINS.add("buildcraftsilicon");
        READ_DOMAINS.add("buildcrafttransport");
        if (!BCLib.DEV) {
            READ_DOMAINS.removeIf(domain -> domain.startsWith("buildcraft"));
        }
    }

    private static Set<JsonRule> getBlockRulesInternal(BlockState blockState, CompoundNBT tileNbt) {
        return RulesLoader.RULES.stream()
                .filter(rule -> rule.selectors != null)
                .filter(rule ->
                                rule.selectors.stream()
                                        .anyMatch(selector ->
                                                        selector.matches(
                                                                base ->
                                                                {
                                                                    boolean complex = base.contains("[");
//                                                    return Block.getBlockFromName(
                                                                    return ForgeRegistries.BLOCKS.getValue(
                                                                            new ResourceLocation(
                                                                                    complex
                                                                                            ? base.substring(0, base.indexOf("["))
                                                                                            : base
                                                                            )
                                                                    ) == blockState.getBlock() &&
                                                                            (!complex ||
                                                                                    Arrays.stream(
                                                                                                    base.substring(
                                                                                                                    base.indexOf("[") + 1,
                                                                                                                    base.indexOf("]")
                                                                                                            )
                                                                                                            .split(", ")
                                                                                            )
                                                                                            .map(nameValue -> nameValue.split("="))
                                                                                            .allMatch(nameValue ->
//                                                                                                            blockState.getPropertyKeys().stream()
                                                                                                            blockState.getProperties().stream()
                                                                                                                    .filter(property -> property.getName().equals(nameValue[0]))
                                                                                                                    .findFirst()
                                                                                                                    .map(property ->
                                                                                                                            BlockUtil.getPropertyStringValue(
                                                                                                                                    blockState,
                                                                                                                                    property
                                                                                                                            )
                                                                                                                    )
                                                                                                                    .map(nameValue[1]::equals)
                                                                                                                    .orElse(false)
                                                                                            )
                                                                            );
                                                                },
                                                                tileNbt == null ? new CompoundNBT() : tileNbt
                                                        )
                                        )
                )
                .collect(Collectors.toCollection(HashSet::new));
    }

    @SuppressWarnings("WeakerAccess")
    public static Set<JsonRule> getRules(BlockState blockState, CompoundNBT tileNbt) {
        return BLOCK_RULES_CACHE.getUnchecked(Pair.of(blockState, tileNbt));
    }

    @SuppressWarnings("WeakerAccess")
    public static Set<JsonRule> getRules(ResourceLocation entityId, CompoundNBT tileNbt) {
        // noinspection ConstantConditions
        return RulesLoader.RULES.stream()
                .filter(rule -> rule.selectors != null)
                .filter(rule ->
                        rule.selectors.stream()
                                .anyMatch(selector -> selector.matches(entityId.toString()::equals, tileNbt))
                )
                .collect(Collectors.toCollection(HashSet::new));
    }
}
