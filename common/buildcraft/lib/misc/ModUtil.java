package buildcraft.lib.misc;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

public class ModUtil {

    @Nullable
    public static String getNameOfMod(String domain) {
        ModContainer mod = ModList.get().getModContainerById(domain).get();
        if (mod != null) {
            return mod.getModInfo().getDisplayName();
        }
        return null;
    }
}
