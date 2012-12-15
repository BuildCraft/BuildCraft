package buildcraft.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import buildcraft.core.proxy.CoreProxy;

import cpw.mods.fml.common.FMLLog;

public class Version {
	
	public enum EnumUpdateState { CURRENT, OUTDATED, CONNECTION_ERROR }
	
	public static final String VERSION = "@VERSION@";
	public static final String BUILD_NUMBER = "@BUILD_NUMBER@";
	private static final String REMOTE_VERSION_FILE = "http://bit.ly/buildcraftver";

	public static EnumUpdateState currentVersion = EnumUpdateState.CURRENT;

	public static final int FORGE_VERSION_MAJOR = 4;
	public static final int FORGE_VERSION_MINOR = 0;
	public static final int FORGE_VERSION_PATCH = 0;

	private static String recommendedVersion;
	
	public static String getVersion() {
		return VERSION + " (:"+ BUILD_NUMBER +")";
	}

	public static String getRecommendedVersion() {
		return recommendedVersion;
	}
	
	public static void versionCheck() {
		try {
			
			String location = REMOTE_VERSION_FILE;
			HttpURLConnection conn = null;
			while(location != null && !location.isEmpty()) {
				URL url = new URL(location);
				conn = (HttpURLConnection)url.openConnection();
				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; ru; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11 (.NET CLR 3.5.30729)");
				conn.connect();
				location = conn.getHeaderField("Location");
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String line = null;
			String mcVersion = CoreProxy.proxy.getMinecraftVersion();
		    while ((line = reader.readLine()) != null) {
		    	if (line.startsWith(mcVersion)) {
		    		if (line.contains(DefaultProps.MOD)) {
		    			
		    			String[] tokens = line.split(":");
		    			recommendedVersion = tokens[2];
		    			
			    		if (line.endsWith(VERSION)) {
			    			FMLLog.finer(DefaultProps.MOD + ": Using the latest version ["+ getVersion() +"] for Minecraft " + mcVersion);
			    			currentVersion = EnumUpdateState.CURRENT;
			    			return;
			    		}
		    		}
		    	}
		    }

		    FMLLog.warning(DefaultProps.MOD + ": Using outdated version ["+ VERSION +" (build:"+ BUILD_NUMBER +")] for Minecraft " + mcVersion + ". Consider updating.");
			currentVersion = EnumUpdateState.OUTDATED;

		} catch (Exception e) {
			e.printStackTrace();
			FMLLog.warning(DefaultProps.MOD + ": Unable to read from remote version authority.");
			currentVersion = EnumUpdateState.CONNECTION_ERROR;
		}
	}

}
