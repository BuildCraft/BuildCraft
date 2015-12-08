package buildcraft.core.lib.world;

import net.minecraft.world.WorldProvider;

public class FakeWorldProvider extends WorldProvider {
    @Override
    public String getDimensionName() {
        return "fake";
    }

    @Override
    public String getInternalNameSuffix() {
        return "fake";
    }
}
