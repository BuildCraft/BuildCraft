/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import buildcraft.lib.BCLib;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.JsonUtil;

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
        .registerTypeAdapter(
            ItemStack.class,
            (JsonDeserializer<ItemStack>) (json, typeOfT, context) ->
                FluidRegistry.isFluidRegistered(json.getAsJsonObject().get("item").getAsString())
                    ?
                    new ItemStack(
                        Objects.requireNonNull(Item.getByNameOrId(json.getAsJsonObject().get("item").getAsString())),
                        json.getAsJsonObject().get("amount").getAsInt(),
                        json.getAsJsonObject().get("meta").getAsInt()
                    )
                    : ItemStack.EMPTY
        )
        .registerTypeAdapter(
            FluidStack.class,
            (JsonDeserializer<FluidStack>) (json, typeOfT, context) ->
                FluidRegistry.isFluidRegistered(json.getAsJsonObject().get("fluid").getAsString())
                    ?
                    new FluidStack(
                        Objects.requireNonNull(FluidRegistry.getFluid(json.getAsJsonObject().get("fluid").getAsString())),
                        json.getAsJsonObject().get("amount").getAsInt()
                    )
                    : null
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
    private static final LoadingCache<Pair<IBlockState, NBTTagCompound>, Set<JsonRule>>
        BLOCK_RULES_CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build(CacheLoader.from(pair -> getBlockRulesInternal(pair.getLeft(), pair.getRight())));

    public static void loadAll() {
        RULES.clear();
        READ_DOMAINS.clear();
        for (ModContainer modContainer : Loader.instance().getModList()) {
            String domain = modContainer.getModId();
            if (!READ_DOMAINS.contains(domain)) {
                String base = "assets/" + domain + "/buildcraft/builders/";
                if (modContainer.getMod() == null) {
                    continue;
                }
                InputStream inputStream = modContainer.getMod().getClass().getClassLoader().getResourceAsStream(
                    base + "index.json"
                );
                if (inputStream != null) {
                    GSON.<List<String>>fromJson(
                        new InputStreamReader(inputStream),
                        new TypeToken<List<String>>() {
                        }.getType()
                    ).stream()
                        .map(name -> base + name + ".json")
                        .map(name -> {
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
        }
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

    private static Set<JsonRule> getBlockRulesInternal(IBlockState blockState, NBTTagCompound tileNbt) {
        return RulesLoader.RULES.stream()
            .filter(rule -> rule.selectors != null)
            .filter(rule ->
                rule.selectors.stream()
                    .anyMatch(selector ->
                        selector.matches(
                            base -> {
                                boolean complex = base.contains("[");
                                return Block.getBlockFromName(
                                    complex
                                        ? base.substring(0, base.indexOf("["))
                                        : base
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
                                                blockState.getPropertyKeys().stream()
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
                            tileNbt == null ? new NBTTagCompound() : tileNbt
                        )
                    )
            )
            .collect(Collectors.toCollection(HashSet::new));
    }

    @SuppressWarnings("WeakerAccess")
    public static Set<JsonRule> getRules(IBlockState blockState, NBTTagCompound tileNbt) {
        return BLOCK_RULES_CACHE.getUnchecked(Pair.of(blockState, tileNbt));
    }

    @SuppressWarnings("WeakerAccess")
    public static Set<JsonRule> getRules(ResourceLocation entityId, NBTTagCompound tileNbt) {
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
