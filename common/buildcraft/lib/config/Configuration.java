package buildcraft.lib.config;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCLog;
import com.google.common.collect.Lists;
import com.google.gson.*;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Configuration {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private static final Path BC_CONFIG_FOLDER_PATH = FMLPaths.CONFIGDIR.relative().resolve("buildcraft");

    private JsonObject configJson;
    private boolean changed;
    private final String name;
    private final Path configFilePath;
    private final List<ConfigCategory<?>> all = Lists.newArrayList();
    private final Lock lock = new ReentrantLock();

    public Configuration(BCModules module) {
        this(module.lowerCaseName);
    }

    public Configuration(String name) {
        this.name = name;
        this.configFilePath = BC_CONFIG_FOLDER_PATH.resolve(name + ".json");
        try {
            BC_CONFIG_FOLDER_PATH.toFile().mkdirs();
            if (configFilePath.toFile().exists()) {
                try (Reader reader = Files.newBufferedReader(configFilePath, StandardCharsets.UTF_8)) {
                    this.configJson = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                } catch (Exception e) {
                    throw e;
                }
            } else {
                this.configJson = new JsonObject();
            }
        } catch (Exception e) {
            BCLog.logger.warn("[lib.config] Failed to open config file [" + configFilePath + "]", e);
            this.configJson = new JsonObject();
        }
    }

    public synchronized ConfigCategory<Boolean> define(String category, String rawComment, EnumRestartRequirement worldRestart, String subPath, boolean defaultValue) {
        lock.lock();

        String comment = ensureCommentNotEmpty(rawComment, subPath);
        String fullPath = category + "." + subPath;
        String[] split = fullPath.split("\\.");
        ConfigCategory<Boolean> ret = new ConfigCategory<>(fullPath, () -> {
            JsonObject j = this.getConfigJson();
            for (int index = 0; index < split.length; index++) {
                try {
                    j = GsonHelper.getAsJsonObject(j, split[index]);
                } catch (JsonSyntaxException e) {
                    JsonObject newObj = new JsonObject();
                    j.add(split[index], newObj);
                    j = newObj;
                }
            }
            j.addProperty("#Comment", comment);
            if (!GsonHelper.isBooleanValue(j, "value")) {
                this.setChanged();
                j.addProperty("value", defaultValue);
            }
            return j.get("value").getAsBoolean();
        }, worldRestart);

        all.add(ret);
        lock.unlock();
        return ret;
    }

    public synchronized ConfigCategory<Integer> defineInRange(String category, String rawComment, EnumRestartRequirement worldRestart, String subPath, int defaultValue, int min, int max) {
        lock.lock();

        String comment = ensureCommentNotEmpty(rawComment, subPath);
        String fullPath = category + "." + subPath;
        String[] split = fullPath.split("\\.");
        ConfigCategory<Integer> ret = new ConfigCategory<>(fullPath, () -> {
            JsonObject j = this.getConfigJson();
            for (int index = 0; index < split.length; index++) {
                try {
                    j = GsonHelper.getAsJsonObject(j, split[index]);
                } catch (JsonSyntaxException e) {
                    JsonObject newObj = new JsonObject();
                    j.add(split[index], newObj);
                    j = newObj;
                }
            }
            j.addProperty("#Comment", comment);
            j.addProperty("#Range", min + " - " + max);
            int gotValue = defaultValue;
            if (!GsonHelper.isNumberValue(j, "value")) {
                this.setChanged();
                j.addProperty("value", defaultValue);
            } else {
                gotValue = j.get("value").getAsInt();
                if (gotValue < min || gotValue > max) {
                    this.setChanged();
                    j.addProperty("value", defaultValue);
                    gotValue = defaultValue;
                }
            }
            return gotValue;
        }, worldRestart);

        all.add(ret);
        lock.unlock();
        return ret;
    }

    public synchronized ConfigCategory<Integer> defineInRange(String category, String rawComment, EnumRestartRequirement worldRestart, String subPath, int defaultValue, int min) {
        return defineInRange(category, rawComment, worldRestart, subPath, defaultValue, min, Integer.MAX_VALUE);
    }

    public synchronized ConfigCategory<Long> defineInRange(String category, String rawComment, EnumRestartRequirement worldRestart, String subPath, long defaultValue, long min) {
        lock.lock();

        long max = Long.MAX_VALUE;
        String comment = ensureCommentNotEmpty(rawComment, subPath);
        String fullPath = category + "." + subPath;
        String[] split = fullPath.split("\\.");
        ConfigCategory<Long> ret = new ConfigCategory<>(fullPath, () -> {
            JsonObject j = this.getConfigJson();
            for (int index = 0; index < split.length; index++) {
                try {
                    j = GsonHelper.getAsJsonObject(j, split[index]);
                } catch (JsonSyntaxException e) {
                    JsonObject newObj = new JsonObject();
                    j.add(split[index], newObj);
                    j = newObj;
                }
            }
            j.addProperty("#Comment", comment);
            j.addProperty("#Range", min + " - " + max);
            long gotValue = defaultValue;
            if (!GsonHelper.isNumberValue(j, "value")) {
                this.setChanged();
                j.addProperty("value", defaultValue);
            } else {
                gotValue = j.get("value").getAsLong();
                if (gotValue < min || gotValue > max) {
                    this.setChanged();
                    j.addProperty("value", defaultValue);
                    gotValue = defaultValue;
                }
            }
            return gotValue;
        }, worldRestart);

        all.add(ret);
        lock.unlock();
        return ret;
    }

    public synchronized ConfigCategory<Integer> defineInRange(String category, String rawComment, EnumRestartRequirement worldRestart, String subPath, int defaultValue) {
        return defineInRange(category, rawComment, worldRestart, subPath, defaultValue, 0, Integer.MAX_VALUE);
    }

    public synchronized ConfigCategory<Double> defineInRange(String category, String rawComment, EnumRestartRequirement worldRestart, String subPath, double defaultValue) {
        return defineInRange(category, rawComment, worldRestart, subPath, defaultValue, 0D, Double.MAX_VALUE);
    }

    public synchronized ConfigCategory<Double> defineInRange(String category, String rawComment, EnumRestartRequirement worldRestart, String subPath, double defaultValue, double min, double max) {
        lock.lock();

        String comment = ensureCommentNotEmpty(rawComment, subPath);
        String fullPath = category + "." + subPath;
        String[] split = fullPath.split("\\.");
        ConfigCategory<Double> ret = new ConfigCategory<>(fullPath, () -> {
            JsonObject j = this.getConfigJson();
            for (int index = 0; index < split.length; index++) {
                try {
                    j = GsonHelper.getAsJsonObject(j, split[index]);
                } catch (JsonSyntaxException e) {
                    JsonObject newObj = new JsonObject();
                    j.add(split[index], newObj);
                    j = newObj;
                }
            }
            j.addProperty("#Comment", comment);
            j.addProperty("#Range", min + " - " + max);
            double gotValue = defaultValue;
            if (!GsonHelper.isNumberValue(j, "value")) {
                this.setChanged();
                j.addProperty("value", defaultValue);
            } else {
                gotValue = j.get("value").getAsDouble();
                if (gotValue < min || gotValue > max) {
                    this.setChanged();
                    j.addProperty("value", defaultValue);
                    gotValue = defaultValue;
                }
            }
            return gotValue;
        }, worldRestart);

        all.add(ret);
        lock.unlock();
        return ret;
    }

    public synchronized <V extends Enum<V>> ConfigCategory<V> defineEnum(String category, String rawComment, EnumRestartRequirement worldRestart, String subPath, V defaultValue) {
        lock.lock();

        String comment = ensureCommentNotEmpty(rawComment, subPath);
        String fullPath = category + "." + subPath;
        String[] split = fullPath.split("\\.");
        ConfigCategory<V> ret = new ConfigCategory<>(fullPath, () -> {
            JsonObject j = this.getConfigJson();
            for (int index = 0; index < split.length; index++) {
                try {
                    j = GsonHelper.getAsJsonObject(j, split[index]);
                } catch (JsonSyntaxException e) {
                    JsonObject newObj = new JsonObject();
                    j.add(split[index], newObj);
                    j = newObj;
                }
            }
            j.addProperty("#Comment", comment);
            j.addProperty("#Allowed Values", Arrays.stream(defaultValue.getDeclaringClass().getEnumConstants()).map(Enum::name).collect(Collectors.joining(", ")));
            V gotEnum = defaultValue;
            if (!GsonHelper.isStringValue(j, "value")) {
                this.setChanged();
                j.addProperty("value", defaultValue.name());
            } else {
                String gotString = j.get("value").getAsString();
                try {
                    gotEnum = Enum.valueOf(defaultValue.getDeclaringClass(), gotString);
                } catch (IllegalArgumentException e) {
                    this.setChanged();
                    j.addProperty("value", defaultValue.name());
                }
            }
            return gotEnum;
        }, worldRestart);

        all.add(ret);
        lock.unlock();
        return ret;
    }

    public synchronized ConfigCategory<List<String>> defineList(String category, String rawComment, EnumRestartRequirement worldRestart, String subPath, List<String> defaultValue) {
        lock.lock();

        String comment = ensureCommentNotEmpty(rawComment, subPath);
        String fullPath = category + "." + subPath;
        String[] split = fullPath.split("\\.");
        ConfigCategory<List<String>> ret = new ConfigCategory<>(fullPath, () -> {
            JsonObject j = this.getConfigJson();
            for (int index = 0; index < split.length; index++) {
                try {
                    j = GsonHelper.getAsJsonObject(j, split[index]);
                } catch (JsonSyntaxException e) {
                    JsonObject newObj = new JsonObject();
                    j.add(split[index], newObj);
                    j = newObj;
                }
            }
            j.addProperty("#Comment", comment);
            List<String> gotList = defaultValue;
            if (!GsonHelper.isArrayNode(j, "value")) {
                this.setChanged();
                JsonArray defaultValueJson = new JsonArray();
                defaultValue.forEach(defaultValueJson::add);
                j.add("value", defaultValueJson);
            } else {
                gotList = Lists.newArrayList();
                JsonArray gotString = j.get("value").getAsJsonArray();
                for (JsonElement jsonElement : gotString) {
                    gotList.add(jsonElement.getAsString());
                }
            }
            return gotList;
        }, worldRestart);

        all.add(ret);
        lock.unlock();
        return ret;
    }

    private static String ensureCommentNotEmpty(String comment, String subPath) {
        if (comment.equals("")) {
            comment = subPath;
        }
        return comment;
    }

    private JsonObject getConfigJson() {
        return configJson;
    }

    public Path getConfigFilePath() {
        return configFilePath;
    }

    public void save() {
        this.lock.lock();
        this.changed = false;
        if (configFilePath.toFile().exists()) {
            try {
                Files.copy(configFilePath, BC_CONFIG_FOLDER_PATH.resolve(name + ".bak.json"));
            } catch (IOException e) {
                BCLog.logger.error("[lib.config] Failed to backup old config file [" + this.configFilePath + "]", e);
            }
        }
        try (BufferedWriter bufferedwriter = Files.newBufferedWriter(configFilePath)) {
            String s = GSON.toJson(this.configJson);
            bufferedwriter.write(s);
        } catch (IOException e) {
            BCLog.logger.error("[lib.config] Failed to save config [" + this.configFilePath + "]", e);
        } finally {
            this.lock.unlock();
        }
    }

    public void setChanged() {
        this.changed = true;
    }

    public boolean hasChanged() {
        return changed;
    }

    public List<ConfigCategory<?>> getAll() {
        return all;
    }
}
