package buildcraft.core.client;

import java.util.ArrayList;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.config.BCConfigElement;
import buildcraft.lib.config.EnumRestartRequirement;

public class ConfigManager implements IModGuiFactory {
    public static class GuiConfigManager extends GuiConfig {
        public GuiConfigManager(GuiScreen parentScreen) {
            super(parentScreen, new ArrayList<IConfigElement>(), "buildcraftcore", "config", false, false, I18n.format("config.buildcraft"));

            for (String s : BCCoreConfig.config.getCategoryNames()) {
                if (!s.contains(".")) {
                    configElements.add(new BCConfigElement(BCCoreConfig.config.getCategory(s)));
                }
            }

            for (String s : BCCoreConfig.objConfig.getCategoryNames()) {
                if (!s.contains(".")) {
                    configElements.add(new BCConfigElement(BCCoreConfig.objConfig.getCategory(s)));
                }
            }
        }
    }

    /** Needed for forge IModGuiFactory */
    public ConfigManager() {}

    public static ConfigCategory getCat(String name) {
        return BCCoreConfig.config.getCategory(name);
    }

    public static Property get(String iName) {
        int sep = iName.lastIndexOf(".");
        return get(iName.substring(0, sep), iName.substring(sep + 1));
    }

    public static Property get(String catName, String propName) {
        ConfigCategory c = BCCoreConfig.config.getCategory(catName);
        return c.get(propName);
    }

    private static Property create(String s, Object o) {
        Property p;
        if (o instanceof Integer) {
            p = new Property(s, o.toString(), Property.Type.INTEGER);
        } else if (o instanceof String) {
            p = new Property(s, (String) o, Property.Type.STRING);
        } else if (o instanceof Double || o instanceof Float) {
            p = new Property(s, o.toString(), Property.Type.DOUBLE);
        } else if (o instanceof Boolean) {
            p = new Property(s, o.toString(), Property.Type.BOOLEAN);
        } else if (o instanceof String[]) {
            p = new Property(s, (String[]) o, Property.Type.STRING);
        } else {
            return null;
        }
        return p;
    }

    public static Property register(String catName, String propName, Object property, String comment, EnumRestartRequirement restartRequirement) {
        ConfigCategory c = BCCoreConfig.config.getCategory(catName);
        ConfigCategory parent = c;
        while (parent != null) {
            parent.setLanguageKey("config." + parent.getQualifiedName());
            parent = parent.parent;
        }
        Property p;
        if (c.get(propName) != null) {
            p = c.get(propName);
        } else {
            p = create(propName, property);
            c.put(propName, p);
        }
        p.setComment(comment);
        p.setLanguageKey("config." + catName + "." + propName);
        restartRequirement.setTo(p);
        return p;
    }

    public static Property register(String name, Object property, String comment, EnumRestartRequirement restartRequirement) {
        String prefix = name.substring(0, name.lastIndexOf("."));
        String suffix = name.substring(name.lastIndexOf(".") + 1);

        return register(prefix, suffix, property, comment, restartRequirement);
    }

    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return GuiConfigManager.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
