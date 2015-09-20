package buildcraft.core.config;

import java.util.ArrayList;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigManager implements IModGuiFactory {
	public static Configuration config;

	public static class GuiConfigManager extends GuiConfig {
		public GuiConfigManager(GuiScreen parentScreen) {
			super(parentScreen, new ArrayList<IConfigElement>(), "BuildCraft|Core", "config", false, false, I18n.format("config.buildcraft"));

			for (String s : config.getCategoryNames()) {
				if (!s.contains(".")) {
					configElements.add(new BCConfigElement<Object>(config.getCategory(s)));
				}
			}
		}
	}

	public enum RestartRequirement {
		NONE, WORLD, GAME
	}

	public ConfigManager() {

	}

	public ConfigManager(Configuration c) {
		config = c;
	}

	public ConfigCategory getCat(String name) {
		return config.getCategory(name);
	}

	public Property get(String iName) {
		int sep = iName.lastIndexOf(".");
		return get(iName.substring(0, sep), iName.substring(sep + 1));
	}

	public Property get(String catName, String propName) {
		ConfigCategory c = config.getCategory(catName);
		return c.get(propName);
	}

	private Property create(String s, Object o) {
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

	public Property register(String catName, String propName, Object property, String comment, RestartRequirement restartRequirement) {
		ConfigCategory c = config.getCategory(catName);
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
		p.comment = comment;
		RestartRequirement r = restartRequirement;
		p.setLanguageKey("config." + catName + "." + propName);
		p.setRequiresWorldRestart(r == RestartRequirement.WORLD);
		p.setRequiresMcRestart(r == RestartRequirement.GAME);
		return p;
	}

	public Property register(String name, Object property, String comment, RestartRequirement restartRequirement) {
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
