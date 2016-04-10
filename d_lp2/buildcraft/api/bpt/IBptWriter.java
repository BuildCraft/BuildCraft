package buildcraft.api.bpt;

/** Provides some helper functions for compressing the size of a blueprint.
 * 
 * @date Created on 10 Apr 2016 by AlexIIL */
public interface IBptWriter {
    /** Creates a mapping for the specified string. Use this to save space when writing block registry ID's or entity
     * registry ID's.
     * 
     * @return A integer that can be used at read-time to get back the string. */
    int mapString(String s);
}
