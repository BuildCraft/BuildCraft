package buildcraft.builders.snapshot;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                    rules.addAll(new GsonBuilder().create().fromJson(new InputStreamReader(inputStream), new TypeToken<List<JsonRule>>() {
                    }.getType()));
                    readDomains.add(domain);
                }
            }
        });
        readDomains.add("minecraft");
    }
}
