package buildcraft.api.transport;

import buildcraft.transport.ItemFacade;
import net.minecraft.src.ItemStack;

public class FacadeManager
{
    public static void addFacade(ItemStack is) {
        ItemFacade.addFacade(is);
    }
}
