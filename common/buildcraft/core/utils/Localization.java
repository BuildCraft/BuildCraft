
package buildcraft.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;

import buildcraft.core.ProxyCore;


/**
 * Simple mod localization class.
 *
 * @author Jimeo Wan
 * @license Public domain
 */
public class Localization {

	private static class modInfo {

		final String modName, defaultLanguage;

		public modInfo(String modName, String defaultLanguage) {
			this.modName = modName;
			this.defaultLanguage = defaultLanguage;
		}
	}
	private static String loadedLanguage = getCurrentLanguage();
	private static Properties defaultMappings = new Properties();
	private static Properties mappings = new Properties();
	private static LinkedList<modInfo> mods = new LinkedList<modInfo>();

	/**
	 * Adds localization from a given directory. The files must have the same
	 * name as the corresponding language file in minecraft and a ".properties"
	 * file extention e.g "en_US.properties"
	 *
	 * @param path The path to the localization files
	 * @param defaultLanguage The default localization to be used when there is
	 * no localization for the selected language or if a string is missing (e.g.
	 * "en_US")
	 */
	public static void addLocalization(String path, String defaultLanguage) {
		mods.add(new modInfo(path, defaultLanguage));
		load(path, defaultLanguage);
	}

	/**
	 * Get a string for the given key, in the currently active translation.
	 *
	 * @param key
	 * @return
	 */
	public static synchronized String get(String key) {
		if (!getCurrentLanguage().equals(loadedLanguage)) {
			defaultMappings.clear();
			mappings.clear();
			for (modInfo mInfo : mods) {
				load(mInfo.modName, mInfo.defaultLanguage);
			}
			loadedLanguage = getCurrentLanguage();
		}

		return mappings.getProperty(key, defaultMappings.getProperty(key, key));
	}

	private static void load(String path, String default_language) {
		InputStream langStream = null;
		Properties modMappings = new Properties();

		try {
			//Load the default language mappings
			langStream = Localization.class.getResourceAsStream(path + default_language + ".properties");
			modMappings.load(langStream);
			defaultMappings.putAll(modMappings);
			langStream.close();

			//Try to load the current language mappings. 
			//If the file doesn't exist use the default mappings.
			langStream = Localization.class.getResourceAsStream(path + getCurrentLanguage() + ".properties");
			if (langStream != null) {
				modMappings.clear();
				modMappings.load(langStream);
			}
			mappings.putAll(modMappings);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (langStream != null)
					langStream.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private static String getCurrentLanguage() {
		return ProxyCore.proxy.getCurrentLanguage();
	}
}
