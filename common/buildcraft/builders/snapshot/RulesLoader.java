package buildcraft.builders.snapshot;

import buildcraft.lib.BCLib;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public enum RulesLoader {
    INSTANCE;

    public final List<JsonRule> rules = new ArrayList<>();
    public final Set<String> readDomains = new HashSet<>();

    public void loadAll() {
        rules.clear();
        readDomains.clear();
        Block.REGISTRY.forEach(block -> {
            if (block == null || block.getRegistryName() == null) {
                return;
            }
            String domain = block.getRegistryName().getResourceDomain();
            if (!readDomains.contains(domain)) {
                InputStream inputStream = block.getClass().getClassLoader().getResourceAsStream(
                        "assets/" + domain + "/buildcraft/builders/rules.json"
                );
                if (inputStream != null) {
                    rules.addAll(
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
                    readDomains.add(domain);
                }
            }
        });
        readDomains.add("minecraft");
        readDomains.add("buildcraftcore");
        readDomains.add("buildcraftlib");
        readDomains.add("buildcraftbuilders");
        readDomains.add("buildcraftenergy");
        readDomains.add("buildcraftfactory");
        readDomains.add("buildcraftrobotics");
        readDomains.add("buildcraftsilicon");
        readDomains.add("buildcrafttransport");
        if (!BCLib.DEV) {
            readDomains.removeIf(domain -> domain.startsWith("buildcraft"));
        }
    }
}
