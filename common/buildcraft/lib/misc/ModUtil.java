package buildcraft.lib.misc;

import javax.annotation.Nullable;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class ModUtil {

    @Nullable
    public static String getNameOfMod(String domain) {
        ModContainer mod = Loader.instance().getIndexedModList().get(domain);
        if (mod != null) {
            return mod.getName();
        }
        return null;
    }
}
