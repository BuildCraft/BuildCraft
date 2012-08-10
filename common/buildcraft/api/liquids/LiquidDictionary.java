package buildcraft.api.liquids;

import java.util.HashMap;
import java.util.Map;

/**
 * When creating liquids you should register them with this class.
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public abstract class LiquidDictionary
{

    private static Map<String, LiquidStack> liquids = new HashMap<String, LiquidStack>();

    /**
     * When creating liquids you should call this function.
     *
     * Upon passing it a name and liquid item it will return either
     * a preexisting implementation of that liquid or the liquid passed in.
     *
     *
     * @param name the name of the liquid
     * @param liquid the liquid to use if one doesn't exist
     * @return
     */
    public static LiquidStack getOrCreateLiquid(String name, LiquidStack liquid)
    {
        LiquidStack existing = liquids.get(name);
        if(existing != null) {
            return existing;
        }
        liquids.put(name, liquid);
        return liquid;
    }
    
    /**
     * Returns the liquid matching the name,
     * if such a liquid exists.
     * 
     * Can return null.
     * 
     * @param name the name of the liquid
     * @return
     */
    public static LiquidStack getLiquid(String name)
    {
        return liquids.get(name);
    }
}
