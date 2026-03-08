package buildcraft.lib.script;

import buildcraft.api.core.BCLog;
import buildcraft.api.registry.IReloadableRegistryManager;
import buildcraft.api.registry.IScriptableRegistry;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.misc.JsonUtil;
import buildcraft.lib.misc.TimeUtil;
import buildcraft.lib.script.SimpleScript.ScriptAction;
import buildcraft.lib.script.SimpleScript.ScriptActionAdd;
import buildcraft.lib.script.SimpleScript.ScriptActionRemove;
import buildcraft.lib.script.SimpleScript.ScriptActionReplace;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;

public class ScriptableRegistry<E> extends SimpleReloadableRegistry<E> implements IScriptableRegistry<E> {

    private final String entryPath;
    private final Map<String, Class<? extends E>> types = new HashMap<>();
    private final Map<String, IEntryDeserializer<? extends E>> deserializers = new HashMap<>();
    private final Set<String> sourceDomains = new HashSet<>();

    public ScriptableRegistry(IReloadableRegistryManager manager, String entryPath) {
        super(manager);
        this.entryPath = entryPath;
    }

    public ScriptableRegistry(PackType type, String entryPath) {
        this(type == PackType.DATA_PACK ? ReloadableRegistryManager.DATA_PACKS
                : ReloadableRegistryManager.RESOURCE_PACKS, entryPath);
    }

    @Override
    public String getEntryType() {
        return entryPath;
    }

    @Override
    public Map<String, Class<? extends E>> getScriptableTypes() {
        return types;
    }

    @Override
    public Map<String, IEntryDeserializer<? extends E>> getCustomDeserializers() {
        return deserializers;
    }

    @Override
    public Set<String> getSourceDomains() {
        return Collections.unmodifiableSet(sourceDomains);
    }

    void loadScripts(Gson gson) {
        try (AutoCloseable fle = SimpleScript.createLogFile(entryPath)) {
            long start = System.currentTimeMillis();
            SimpleScript.logForAll("Started at: " + TimeUtil.formatNow());

            loadScripts0(gson);

            long end = System.currentTimeMillis();
            SimpleScript.logForAll("Finished at: " + TimeUtil.formatNow() + ", took " + (end - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadScripts0(Gson gson) {

        SimpleScript.logForAll("#############");
        SimpleScript.logForAll("#");
        SimpleScript.logForAll("# Loading");
        SimpleScript.logForAll("#");
        SimpleScript.logForAll("#############");

        sourceDomains.clear();

        List<ScriptAction> actions = new ArrayList<>();

        List<FileSystem> openFileSystems = new ArrayList<>();
        Map<File, Path> loadedFiles = new HashMap<>();
        List<Path> jarRoots = new ArrayList<>();

        for (ModFileInfo container : LoadingModList.get().getModFiles()) {
            // Calen 1.18.2
            if ("forge".equals(container.moduleName())) {
                continue;
            }
            // 1.12.2
            File source = container.getFile().getFilePath().toFile();
            if (!source.exists()) {
                continue;
            }
            visitFile(openFileSystems, loadedFiles, jarRoots, source);
        }

        switch (manager.getType()) {
            case RESOURCE_PACK: {
                for (File rpFile : BCLibProxy.getProxy().getLoadedResourcePackFiles()) {
                    visitFile(openFileSystems, loadedFiles, null, rpFile);
                }
                break;
            }
            case DATA_PACK: {
                // TODO(1.13): Load from datapacks as well!
                break;
            }
        }

        File baseFile = new File(FMLPaths.CONFIGDIR.get().toFile(), "buildcraft/scripts");
        if (!baseFile.isDirectory()) {
            baseFile.mkdirs();
        }
        visitFile(openFileSystems, loadedFiles, null, baseFile);

        for (Entry<File, Path> entry : loadedFiles.entrySet()) {
            File file = entry.getKey();
            loadScripts(openFileSystems, actions, file, entry.getValue(), jarRoots, file == baseFile);
        }

        SimpleScript.logForAll("#############");
        SimpleScript.logForAll("#");
        SimpleScript.logForAll("# Executing");
        SimpleScript.logForAll("#");
        SimpleScript.logForAll("#############");
        SimpleScript.logForAll("");

        executeScripts(gson, actions);

        for (FileSystem system : openFileSystems) {
            IOUtils.closeQuietly(system);
        }
    }

    private void visitFile(List<FileSystem> openFileSystems, Map<File, Path> loadedFiles, List<Path> roots, File source) {
//        if (loadedFiles.containsKey(source))
        if (loadedFiles.containsKey(source) || !source.exists()) {
            return;
        }
        Path root = getRoot(openFileSystems, source);
        if (root != null) {
            loadedFiles.put(source, root);
            if (roots != null) {
                roots.add(root);
            }
        }
    }

    @Nullable
    private Path getRoot(List<FileSystem> openFileSystems, File file) {
        final PackType sourceType = manager.getType();
        Path scriptDirRoot = file.toPath();
        if (file.isDirectory()) {
            Path root = scriptDirRoot.resolve(sourceType.prefix);
            return Files.exists(root) ? root : null;
        }
        try {
            FileSystem fileSystem = FileSystems.newFileSystem(scriptDirRoot, /* since java 13 */ (ClassLoader) null);
            Path root = fileSystem.getPath("/" + sourceType.prefix);
            if (!Files.exists(root)) {
                return null;
            }
            openFileSystems.add(fileSystem);
            return root;
        } catch (IOException e) {
            BCLog.logger.error("Unable to load " + file + " as a separate file system!", e);
            return null;
        }
    }

    private void loadScripts(List<FileSystem> openFileSystems, List<ScriptAction> actions, File file, Path root,
                             List<Path> jarRoots, boolean genInfo) {
        try {
            boolean loggedInsn = false;
            String postPath = "compat/" + this.entryPath;
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(root)) {
                Iterator<Path> iter = dirStream.iterator();
                while (iter.hasNext()) {
                    Path subFolder = iter.next();
                    // subFolder will be "/data/[mod_id]"
                    String scriptDomain = subFolder.getFileName().toString().replace("/", "");
                    Path scriptDir = subFolder.resolve(postPath);
                    Path scriptFile = subFolder.resolve(postPath + ".txt");
                    if (!Files.exists(scriptFile)) {
                        // TODO: Load "as if" the script file wasn't missing!
                        continue;
                    }
                    if (!loggedInsn) {
                        loggedInsn = true;
                        SimpleScript.logForAll("");
                        SimpleScript.logForAll("# Found pack: " + file);
                        SimpleScript.logForAll("");
                    }
                    List<String> contents = Files.readAllLines(scriptFile);
                    if (contents.isEmpty()) {
                        SimpleScript.logForAll(root.relativize(scriptFile) + " was empty!");
                        continue;
                    }
                    if (!"~{buildcraft/json/insn}".equals(contents.set(0, "// Valid file declaration was here"))) {
                        SimpleScript.logForAll(
                                root.relativize(scriptFile) + " didn't start with '~{buildcraft/json/insn}', ignoring.");
                        continue;
                    }
                    SimpleScript script =
                            new SimpleScript(this, root, scriptDomain, scriptDir, scriptFile, jarRoots, contents);
                    actions.addAll(script.actions);
                    if (!script.actions.isEmpty()) {
                        sourceDomains.add(scriptDomain);
                    }
                }
            }
        } catch (IOException io) {
            BCLog.logger.warn("Unable to load from ...", io);
        }
    }

    private void executeScripts(Gson gson, List<ScriptAction> actions) {
        Multimap<ResourceLocation, ScriptAction> added = HashMultimap.create();
        Multimap<ResourceLocation, ScriptAction> removed = HashMultimap.create();

        for (ScriptAction action : actions) {
            if (action instanceof ScriptActionRemove) {
                removed.put(((ScriptActionRemove) action).name, action);
            } else if (action instanceof ScriptActionAdd) {
                ScriptActionAdd add = (ScriptActionAdd) action;
                added.put(add.name, add);
            } else if (action instanceof ScriptActionReplace) {
                ScriptActionReplace replace = (ScriptActionReplace) action;
                removed.put(replace.toReplace, replace);
                added.put(replace.name, replace);
            } else {
                throw new IllegalStateException("Unknown action " + action.getClass());
            }
        }

        // Order:
        // - add *only* happens if it hasn't been removed
        // - modify and replace *only* happens if
        // - - 1: the 'to_remove' *hasn't* been removed
        // - - 2: the 'to_add' hasn't been removed

        // Multiple things remove however

        for (ResourceLocation name : added.keySet()) {
            Collection<ScriptAction> adders = added.get(name);
            if (adders.size() > 1) {
                SimpleScript.logForAll("Multiple scripts attempting to add " + name
                        + "! This is likely caused by either a single script containing duplicate 'add' entries "
                        + "with the same id, or multiple datapacks with the same namespace!");
                continue;
            }
            ScriptAction adder = adders.iterator().next();
            Collection<ScriptAction> removers = removed.get(name);
            removers.remove(adder);
            if (!removers.isEmpty()) {
                SimpleScript.logForAll("Skipping " + name + " as it is marked as removed.");
                continue;
            }
            JsonObject json = null;
            while (!(adder instanceof ScriptActionAdd)) {
                if (adder instanceof ScriptActionReplace) {
                    ScriptActionReplace replace = (ScriptActionReplace) adder;
                    if (replace.inheritTags) {
                        // Long and complicated
                        ResourceLocation location = replace.toReplace;
                        adders = added.get(location);
                        if (adders.size() > 1) {
                            // This will be logged by the above code
                            adder = null;
                            break;
                        }
                        adder = adders.iterator().next();
                        if (json == null) {
                            json = replace.json;
                        }
                        json = JsonUtil.inheritTags(adder.getJson(), json);
                    } else {
                        // nice and simple
                        adder = replace.convertToAdder();
                        json = null;
                    }
                } else {
                    throw new IllegalStateException("Unknown action " + adder.getClass());
                }
            }
            if (adder == null) {
                // The error will have already been logged
                continue;
            } else if (adder instanceof ScriptActionAdd) {
                ScriptActionAdd action = (ScriptActionAdd) adder;
                if (action.json == null) {
                    SimpleScript.logForAll("Skipping " + name + " as it couldn't find a JSON to load from.");
                    continue;
                } else if (json != null) {
                    json = JsonUtil.inheritTags(json, action.json);
                } else {
                    json = action.json;
                }
                try {
                    loadReloadable(name, gson, json);
                } catch (JsonSyntaxException jse) {
                    SimpleScript.logForAll("Unable to load " + name + " from " + json + " because " + jse.getMessage());
                }
            } else {
                throw new IllegalStateException("Unknown action " + adder.getClass());
            }
        }
    }

    private void loadReloadable(ResourceLocation name, Gson gson, JsonObject json) throws JsonSyntaxException {
        String type = "";
        if (json.has("type")) {
//            type = JsonUtils.getString(json, "type");
            type = GsonHelper.getAsString(json, "type");
        }
        IEntryDeserializer<? extends E> deserializer = getCustomDeserializers().get(type);
        if (deserializer != null) {
            OptionallyDisabled<? extends E> optional = deserializer.deserialize(name, json, gson::fromJson);
            if (optional.isPresent()) {
                E instance = optional.get();
                SimpleScript.logForAll("Adding " + name + " as " + instance);
                getReloadableEntryMap().put(name, instance);
                return;
            }
            SimpleScript.logForAll("Skipping " + name + " because " + optional.getDisabledReason());
            return;
        }
        Class<? extends E> recipeClass = getScriptableTypes().get(type);
        if (recipeClass != null) {
            E recipe = gson.fromJson(json, recipeClass);
            SimpleScript.logForAll("Adding " + name + " as " + recipe);
            return;
        }
        SimpleScript.logForAll("Unable to add '" + name + "' as the type '" + type + "' is not defined!");
    }
}
