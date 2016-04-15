package buildcraft.lib.guide;

import com.google.gson.Gson;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.guide.parts.GuideInWorldFactory;
import buildcraft.lib.guide.world.WorldInfo;

public class WorldLoader extends LocationLoader {
    public static GuideInWorldFactory loadWorld(ResourceLocation location) {
        String string = asString(location);
        if (string == null) {
            return null;
        }
        WorldInfo info = new Gson().fromJson(string, WorldInfo.class);
        return new GuideInWorldFactory(info);
    }
}
