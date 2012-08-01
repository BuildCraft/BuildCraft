package buildcraft.api.liquids;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public abstract class LiquidDictionary
{

    private static Map<String, LiquidStack> liquids = new HashMap<String, LiquidStack>();

    public static LiquidStack getOrCreateLiquid(String name, LiquidStack liquid)
    {
        LiquidStack existing = liquids.get(name);
        if(existing != null) {
            return existing;
        }
        liquids.put(name, liquid);
        return liquid;
    }
}
