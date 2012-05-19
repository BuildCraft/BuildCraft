package net.minecraft.src.buildcraft.core.utils;

import java.io.InputStream;
import java.util.Properties;

import net.minecraft.src.buildcraft.core.CoreProxy;

/**
* Simple mod localization class.
*
* @author Jimeo Wan
* @license Public domain
*/
public class Localization {

	public static Localization instance = new Localization();
	
	private static final String DEFAULT_LANGUAGE = "en_US";

	private String loadedLanguage = null;
	private Properties defaultMappings = new Properties();
	private Properties mappings = new Properties();

	/**
	 * Loads the mod's localization files. All language files must be stored in
	 * "[modname]/lang/", in .properties files. (ex: for the mod 'invtweaks',
	 * the french translation is in: "invtweaks/lang/fr_FR.properties")
	 *
	 * @param modName The mod name
	 */
	public Localization() {
		load(getCurrentLanguage());
	}

	/**
	 * Get a string for the given key, in the currently active translation.
	 *
	 * @param key
	 * @return
	 */
	public synchronized String get(String key) {
		String currentLanguage = getCurrentLanguage();
		if (!currentLanguage.equals(loadedLanguage))
			load(currentLanguage);
		
		return mappings.getProperty(key, defaultMappings.getProperty(key, key));
	}

	private void load(String newLanguage) {
		defaultMappings.clear();
		mappings.clear();
		try {
			InputStream langStream = Localization.class.getResourceAsStream(
					"/lang/buildcraft/" + newLanguage + ".properties");
			InputStream defaultLangStream = Localization.class.getResourceAsStream(
					"/lang/buildcraft/" + DEFAULT_LANGUAGE + ".properties");
			mappings.load((langStream == null) ? defaultLangStream : langStream);
			defaultMappings.load(defaultLangStream);

			if (langStream != null) {
				langStream.close();
			}
			defaultLangStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		loadedLanguage = newLanguage;
	}

	private static String getCurrentLanguage() {
		return CoreProxy.getCurrentLanguage();
	}

}
