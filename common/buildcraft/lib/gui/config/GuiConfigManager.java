package buildcraft.lib.gui.config;

import buildcraft.api.core.BCLog;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeBoolean;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.misc.MessageUtil;
import com.google.gson.*;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/** Stores configuration values about GUI elements. Primarily which ledger is open, however json based gui's may add
 * config options per-gui. */
public class GuiConfigManager {
    public static final Map<String, GuiPropertyConstructor> customGuiProperties = new HashMap<>();
    private static final Map<ResourceLocation, GuiConfigSet> properties;
    private static boolean isDirty = false;

    static {
        properties = new TreeMap<>((a, b) -> a.toString().compareTo(b.toString()));
        // TODO (AlexIIL, post 1.12 move): Flesh this system out more! Add settings that can be loaded from json GUI's
        // TODO (AlexIIL, post 1.12 move): Move config file loading from core -> lib
        customGuiProperties.put(NodeTypes.getName(boolean.class), GuiPropertyBoolean::new);
        // customGuiProperties.put(NodeTypes.getName(long.class), GuiPropertyLong::new);
        // customGuiProperties.put(NodeTypes.getName(double.class), GuiPropertyDouble::new);
        // customGuiProperties.put(NodeTypes.getName(String.class), GuiPropertyString::new);
    }

    public static IVariableNode getOrAddProperty(ResourceLocation gui, String name, IExpressionNode value) {
        GuiConfigSet props = properties.computeIfAbsent(gui, r -> new GuiConfigSet());
        return props.getOrAddProperty(name, value);
    }

    public static IVariableNodeBoolean getOrAddBoolean(ResourceLocation gui, String name, boolean defaultValue) {
        return (IVariableNodeBoolean) getOrAddProperty(gui, name, NodeConstantBoolean.of(defaultValue));
    }

    public static void markDirty() {
//        if (!isDirty && BCLibConfig.guiConfigFile != null)
        if (!isDirty && BCLibConfig.getGuiConfigFileAndEnsureCreated() != null) {
            // Minimise successive file writes -- add a little bit of a delay
            MessageUtil.doDelayedClient(10, () ->
            {
                if (!isDirty) {
                    return;
                }
//                try (FileWriter fw = new FileWriter(BCLibConfig.guiConfigFile))
                try (FileWriter fw = new FileWriter(BCLibConfig.getGuiConfigFileAndEnsureCreated())) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    try (BufferedWriter bw = new BufferedWriter(fw)) {
                        JsonObject json = writeToJson();
                        String str = gson.toJson(json);
                        bw.write(str);
                        bw.flush();
                        bw.close();
                    }
                } catch (IOException io) {
                    BCLog.logger.warn("[lib.gui.cfg] Failed to write the config file! " + io.getMessage());
                }
                isDirty = false;
            });
        }
        isDirty = true;
    }

    public static void loadFromConfigFile() {
//        if (BCLibConfig.guiConfigFile != null)
        if (BCLibConfig.getGuiConfigFileAndEnsureCreated() != null) {
            Gson gson = new Gson();
            List<String> lines;
            try {
//                lines = Files.readAllLines(BCLibConfig.guiConfigFile.toPath());
                lines = Files.readAllLines(BCLibConfig.getGuiConfigFileAndEnsureCreated().toPath());
            } catch (IOException io) {
                BCLog.logger.warn("[lib.gui.cfg] Failed to read the config file! " + io.getMessage());
                return;
            }
            StringBuilder allLines = new StringBuilder();
            for (String line : lines) {
                allLines.append(line);
                allLines.append('\n');
            }
            try {
                readFromJson(gson.fromJson(allLines.toString(), JsonObject.class));
                return;
            } catch (JsonSyntaxException jse) {
                BCLog.logger.warn("[lib.gui.cfg] There's a problem with the config file: try fixing it manually, "
                        + "or deleting it to let buildcraft overwrite it on save." + jse.getMessage());
            } catch (ClassCastException cce) {
                // This happens occasionally, and its a bit wierd
                BCLog.logger.warn("[lib.gui.cfg] There's a major problem with the config file: try fixing it manually, "
                        + "or deleting it to let buildcraft overwrite it on save." + cce.getMessage());

            }
            BCLog.logger.info("File contents:");
            for (String line : lines) {
                BCLog.logger.info(line.replace("\0", "\\0"));
            }
        }
    }

    private static JsonObject writeToJson() {
        JsonObject json = new JsonObject();
        for (Entry<ResourceLocation, GuiConfigSet> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            json.add(key, entry.getValue().writeToJson());
        }
        return json;
    }

    private static void readFromJson(JsonObject json) {
        if (json == null) {
            throw new JsonSyntaxException("No json element!");
        }
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            ResourceLocation location = new ResourceLocation(entry.getKey());
            GuiConfigSet set = properties.get(location);
            if (set == null) {
                set = new GuiConfigSet();
                properties.put(location, set);
            }
            JsonElement elem = entry.getValue();
            if (!elem.isJsonObject()) {
                BCLog.logger.warn("[lib.gui.config] Found a non-object element in '" + location + "'");
                continue;
            }
            set.readFromJson(elem.getAsJsonObject());
        }
    }
}
