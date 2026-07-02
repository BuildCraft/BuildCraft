package ct.buildcraft.robotics.statements;

import java.util.LinkedList;
import net.minecraft.world.item.ItemStack;
import ct.buildcraft.api.core.IStackFilter;

public class StateStationProvideItems {
    public final LinkedList<ItemStack> items = new LinkedList<>();

    public boolean matches(IStackFilter filter) {
        for (ItemStack s : items) {
            if (!s.isEmpty() && filter.matches(s)) return true;
        }
        return false;
    }
}
