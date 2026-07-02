/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BCLog {
    public static final Logger logger = LogManager.getLogger("BuildCraft");
    									//LogUtils.getLogger();
    
    /** Deactivate constructor */
    private BCLog() {}

    @Deprecated
    public static void logErrorAPI(String mod, Throwable error, Class<?> classFile) {
        logErrorAPI(error, classFile);
        
     
    }

    public static void logErrorAPI(Throwable error, Class<?> classFile) {
        StringBuilder msg = new StringBuilder("API error! Please update your mods. Error: ");
        msg.append(error);
        StackTraceElement[] stackTrace = error.getStackTrace();
        if (stackTrace.length > 0) {
            msg.append(", ").append(stackTrace[0]);
        }
        
        logger.error(msg.toString());

        if (classFile != null) {
            msg.append("API error: ").append(classFile.getSimpleName()).append(" is loaded from ").append(classFile.getProtectionDomain()
                    .getCodeSource().getLocation());
            logger.error(msg.toString());
        }
    }

    @Deprecated
    public static String getVersion() {
        return BuildCraftAPI.getVersion();
    }
    
    public static void d(String s) {
    	logger.debug(new Exception().getStackTrace()[1].getClassName() + " : " + s);
    }
    
    public static void d(boolean flag) {
    	if(flag)
    		logger.debug(new Exception().getStackTrace()[1].getClassName() + " : Catched!");
    }
    
    public static void d(boolean flag, String msg) {
    	if(flag)
    		logger.debug(new Exception().getStackTrace()[1].getClassName() + " : "+msg);
    }
}
