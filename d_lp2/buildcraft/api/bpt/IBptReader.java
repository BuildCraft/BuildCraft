package buildcraft.api.bpt;

public interface IBptReader {
    /** Gets a mapping for the specified string. Use this to get the values back after using
     * {@link IBptWriter#mapString(String)} in your write method.
     * 
     * @return The original string. */
    String unmapString(int id);
}
