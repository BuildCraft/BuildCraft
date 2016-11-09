package buildcraft.lib.config;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OverridableConfigOption extends DetailedConfigOption {
    private final String assetName;
    private final RoamingConfigManager manager;

    public OverridableConfigOption(String assetLoc, String assetName, String defultVal) {
        super(assetLoc + "|" + assetName, defultVal);
        this.assetName = assetName;
        ResourceLocation loc = new ResourceLocation("buildcraftconfig:", assetLoc.replace(".", "/") + ".properties");
        this.manager = RoamingConfigManager.getOrCreateDefault(loc);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected boolean refresh() {
        if (manager.exists()) {
            return manager.refresh(this, assetName);
        }
        return super.refresh();
    }
}
