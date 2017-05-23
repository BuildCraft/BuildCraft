/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.BCLib;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RulesLoader {
    private static final List<JsonRule> RULES = new ArrayList<>();
    public static final Set<String> READ_DOMAINS = new HashSet<>();
    private static final LoadingCache<IBlockState, Set<JsonRule>> BLOCK_RULES_CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build(CacheLoader.from(RulesLoader::getBlockRulesInternal));

    public static void loadAll() {
        RULES.clear();
        READ_DOMAINS.clear();
        Block.REGISTRY.forEach(block -> {
            if (block == null || block.getRegistryName() == null) {
                return;
            }
            String domain = block.getRegistryName().getResourceDomain();
            if (!READ_DOMAINS.contains(domain)) {
                InputStream inputStream = block.getClass().getClassLoader().getResourceAsStream(
                    "assets/" + domain + "/buildcraft/builders/rules.json"
                );
                if (inputStream != null) {
                    RULES.addAll(
                        new GsonBuilder()
                            .registerTypeAdapter(
                                ItemStack.class,
                                (JsonDeserializer<ItemStack>) (json, typeOfT, context) -> {
                                    String itemName = json.getAsString();
                                    itemName = itemName.contains("@") ? itemName : itemName + "@0";
                                    return new ItemStack(
                                        Objects.requireNonNull(
                                            Item.getByNameOrId(
                                                itemName.substring(
                                                    0,
                                                    itemName.indexOf("@")
                                                )
                                            )
                                        ),
                                        1,
                                        Integer.parseInt(itemName.substring(itemName.indexOf("@") + 1))
                                    );
                                }
                            )
                            .registerTypeAdapter(
                                BlockPos.class,
                                (JsonDeserializer<BlockPos>) (json, typeOfT, context) ->
                                    new BlockPos(
                                        json.getAsJsonArray().get(0).getAsInt(),
                                        json.getAsJsonArray().get(1).getAsInt(),
                                        json.getAsJsonArray().get(2).getAsInt()
                                    )
                            )
                            .create()
                            .fromJson(
                                new InputStreamReader(inputStream),
                                new TypeToken<List<JsonRule>>() {
                                }.getType()
                            )
                    );
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

    private static Set<JsonRule> getBlockRulesInternal(IBlockState blockState) {
        return RulesLoader.RULES.stream()
            .filter(rule -> rule.selectors != null)
            .filter(rule ->
                rule.selectors.stream()
                    .anyMatch(selector -> {
                        boolean complex = selector.contains("[");
                        return Block.getBlockFromName(
                            complex
                                ? selector.substring(0, selector.indexOf("["))
                                : selector
                        ) == blockState.getBlock() &&
                            (!complex ||
                                Arrays.stream(
                                    selector.substring(
                                        selector.indexOf("[") + 1,
                                        selector.indexOf("]")
                                    )
                                        .split(",")
                                )
                                    .map(nameValue -> nameValue.split("="))
                                    .allMatch(nameValue ->
                                        blockState.getPropertyKeys().stream()
                                            .filter(property ->
                                                property.getName().equals(nameValue[0])
                                            )
                                            .findFirst()
                                            .map(blockState::getValue)
                                            .map(Object::toString)
                                            .map(nameValue[1]::equals)
                                            .orElse(false)
                                    )
                            );
                    })
            )
            .collect(Collectors.toCollection(HashSet::new));
    }

    public static Set<JsonRule> getRules(IBlockState blockState) {
        return BLOCK_RULES_CACHE.getUnchecked(blockState);
    }

    public static Set<JsonRule> getRules(Entity entity) {
        // noinspection ConstantConditions
        return RulesLoader.RULES.stream()
            .filter(rule -> rule.selectors != null)
            .filter(rule -> rule.selectors.stream().anyMatch(EntityList.getKey(entity).toString()::equals))
            .collect(Collectors.toCollection(HashSet::new));
    }
}
